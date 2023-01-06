/*
MIT License

Copyright (c) 2018-2023 c64lib: The Ultimate Commodore 64 Library

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.github.c64lib.gradle

import com.github.c64lib.gradle.tasks.Build
import com.github.c64lib.gradle.tasks.Preprocess
import com.github.c64lib.rbt.compilers.kickass.adapters.`in`.gradle.Assemble
import com.github.c64lib.rbt.compilers.kickass.adapters.`in`.gradle.AssembleSpec
import com.github.c64lib.rbt.compilers.kickass.adapters.`in`.gradle.Clean
import com.github.c64lib.rbt.compilers.kickass.adapters.`in`.gradle.ResolveDevDeps
import com.github.c64lib.rbt.compilers.kickass.adapters.out.filedownload.DownloadKickAssemblerAdapter
import com.github.c64lib.rbt.compilers.kickass.adapters.out.gradle.DeleteFilesAdapter
import com.github.c64lib.rbt.compilers.kickass.adapters.out.gradle.KickAssembleAdapter
import com.github.c64lib.rbt.compilers.kickass.adapters.out.gradle.KickAssembleSpecAdapter
import com.github.c64lib.rbt.compilers.kickass.adapters.out.gradle.ReadVersionAdapter
import com.github.c64lib.rbt.compilers.kickass.adapters.out.gradle.SaveVersionAdapter
import com.github.c64lib.rbt.compilers.kickass.domain.KickAssemblerSettings
import com.github.c64lib.rbt.compilers.kickass.usecase.CleanBuildArtefactsUseCase
import com.github.c64lib.rbt.compilers.kickass.usecase.DownloadKickAssemblerUseCase
import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleSpecUseCase
import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleUseCase
import com.github.c64lib.rbt.dependencies.adapters.`in`.gradle.DownloadDependencies
import com.github.c64lib.rbt.dependencies.adapters.out.gradle.DownloadDependencyAdapter
import com.github.c64lib.rbt.dependencies.adapters.out.gradle.UntarDependencyAdapter
import com.github.c64lib.rbt.dependencies.usecase.ResolveGitHubDependencyUseCase
import com.github.c64lib.rbt.emulators.vice.adapters.out.gradle.RunTestOnViceAdapter
import com.github.c64lib.rbt.emulators.vice.usecase.RunTestOnViceUseCase
import com.github.c64lib.rbt.processors.charpad.adapters.`in`.gradle.Charpad
import com.github.c64lib.rbt.processors.goattracker.adapters.`in`.gradle.Goattracker
import com.github.c64lib.rbt.processors.goattracker.adapters.out.gradle.ExecuteGt2RelocAdapter
import com.github.c64lib.rbt.processors.goattracker.usecase.PackSongUseCase
import com.github.c64lib.rbt.processors.spritepad.adapters.`in`.gradle.Spritepad
import com.github.c64lib.rbt.shared.domain.SemVer
import com.github.c64lib.rbt.shared.filedownload.FileDownloader
import com.github.c64lib.rbt.shared.gradle.TASK_ASM
import com.github.c64lib.rbt.shared.gradle.TASK_ASM_SPEC
import com.github.c64lib.rbt.shared.gradle.TASK_BUILD
import com.github.c64lib.rbt.shared.gradle.TASK_CHARPAD
import com.github.c64lib.rbt.shared.gradle.TASK_CLEAN
import com.github.c64lib.rbt.shared.gradle.TASK_DEPENDENCIES
import com.github.c64lib.rbt.shared.gradle.TASK_GOATTRACKER
import com.github.c64lib.rbt.shared.gradle.TASK_PREPROCESS
import com.github.c64lib.rbt.shared.gradle.TASK_RESOLVE_DEV_DEPENDENCIES
import com.github.c64lib.rbt.shared.gradle.TASK_SPRITEPAD
import com.github.c64lib.rbt.shared.gradle.TASK_TEST
import com.github.c64lib.rbt.shared.gradle.dsl.EXTENSION_DSL_NAME
import com.github.c64lib.rbt.shared.gradle.dsl.PREPROCESSING_EXTENSION_DSL_NAME
import com.github.c64lib.rbt.shared.gradle.dsl.PreprocessingExtension
import com.github.c64lib.rbt.shared.gradle.dsl.RetroAssemblerPluginExtension
import com.github.c64lib.rbt.testing.a64spec.adapters.`in`.gradle.Test
import com.github.c64lib.rbt.testing.a64spec.usecase.Run64SpecTestUseCase
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project

class RetroAssemblerPlugin : Plugin<Project> {

  override fun apply(project: Project) {

    val extension =
        project.extensions.create(EXTENSION_DSL_NAME, RetroAssemblerPluginExtension::class.java)

    val preprocessExtension =
        project.extensions.create(
            PREPROCESSING_EXTENSION_DSL_NAME, PreprocessingExtension::class.java)

    project.afterEvaluate {
      // deps
      val resolveDevDeps =
          project.tasks.create(TASK_RESOLVE_DEV_DEPENDENCIES, ResolveDevDeps::class.java) { task ->
            task.extension = extension
            task.downloadKickAssemblerUseCase =
                DownloadKickAssemblerUseCase(
                    DownloadKickAssemblerAdapter(project, FileDownloader()),
                    ReadVersionAdapter(project),
                    SaveVersionAdapter(project))
          }
      val downloadDependencies =
          project.tasks.create(TASK_DEPENDENCIES, DownloadDependencies::class.java) { task ->
            task.extension = extension
            task.resolveGitHubDependencyUseCase =
                ResolveGitHubDependencyUseCase(
                    DownloadDependencyAdapter(project, extension, FileDownloader()),
                    UntarDependencyAdapter(project))
          }
      // preprocess
      val charpad =
          project.tasks.create(TASK_CHARPAD, Charpad::class.java) { task ->
            task.preprocessingExtension = preprocessExtension
          }
      val spritepad =
          project.tasks.create(TASK_SPRITEPAD, Spritepad::class.java) { task ->
            task.preprocessingExtension = preprocessExtension
          }
      val goattracker =
          project.tasks.create(TASK_GOATTRACKER, Goattracker::class.java) { task ->
            task.preprocessingExtension = preprocessExtension
            task.packSongUseCase = PackSongUseCase(ExecuteGt2RelocAdapter(project))
          }
      val preprocess = project.tasks.create(TASK_PREPROCESS, Preprocess::class.java)

      preprocess.dependsOn(charpad, spritepad, goattracker)

      // TODO Somehow, the ResolveDevDeps should give the settings. How!?
      val settings =
          KickAssemblerSettings(
              File("${extension.workDir}/asms/ka/${extension.dialectVersion}/KickAss.jar"),
              SemVer.fromString(extension.dialectVersion))

      // sources
      val assemble =
          project.tasks.create(TASK_ASM, Assemble::class.java) { task ->
            task.extension = extension
            task.kickAssembleUseCase = KickAssembleUseCase(KickAssembleAdapter(project, settings))
          }
      assemble.dependsOn(resolveDevDeps, downloadDependencies, preprocess)

      project.tasks.create(TASK_CLEAN, Clean::class.java) { task ->
        task.extension = extension
        task.cleanBuildArtefactsUseCase = CleanBuildArtefactsUseCase(DeleteFilesAdapter(project))
      }

      // spec
      val assembleSpec =
          project.tasks.create(TASK_ASM_SPEC, AssembleSpec::class.java) { task ->
            task.extension = extension
            task.kickAssembleSpecUseCase =
                KickAssembleSpecUseCase(KickAssembleSpecAdapter(project, settings))
          }
      assembleSpec.dependsOn(resolveDevDeps, downloadDependencies)
      val runSpec =
          project.tasks.create(TASK_TEST, Test::class.java) { task ->
            task.extension = extension
            task.run64SpecTestUseCase =
                Run64SpecTestUseCase(RunTestOnViceUseCase(RunTestOnViceAdapter(project, extension)))
          }
      runSpec.dependsOn(assembleSpec)

      // build
      val build = project.tasks.create(TASK_BUILD, Build::class.java)
      build.dependsOn(assemble, runSpec)

      if (project.defaultTasks.isEmpty()) {
        project.defaultTasks.add(TASK_BUILD)
      }
    }
  }
}
