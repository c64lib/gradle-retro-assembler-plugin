/*
MIT License

Copyright (c) 2018-2025 c64lib: The Ultimate Commodore 64 Library
Copyright (c) 2018-2025 Maciej Małecki

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

import com.github.c64lib.rbt.shared.gradle.TASK_ASM
import com.github.c64lib.rbt.shared.gradle.TASK_ASM_SPEC
import com.github.c64lib.rbt.shared.gradle.TASK_BUILD
import com.github.c64lib.rbt.shared.gradle.TASK_CHARPAD
import com.github.c64lib.rbt.shared.gradle.TASK_CLEAN
import com.github.c64lib.rbt.shared.gradle.TASK_DEPENDENCIES
import com.github.c64lib.rbt.shared.gradle.TASK_FLOWS
import com.github.c64lib.rbt.shared.gradle.TASK_GOATTRACKER
import com.github.c64lib.rbt.shared.gradle.TASK_IMAGE
import com.github.c64lib.rbt.shared.gradle.TASK_PREPROCESS
import com.github.c64lib.rbt.shared.gradle.TASK_RESOLVE_DEV_DEPENDENCIES
import com.github.c64lib.rbt.shared.gradle.TASK_SPRITEPAD
import com.github.c64lib.rbt.shared.gradle.TASK_TEST
import com.github.c64lib.rbt.shared.gradle.dsl.RetroAssemblerPluginExtension
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import org.gradle.testfixtures.ProjectBuilder

/**
 * Verifies that [RetroAssemblerPlugin] registers all expected tasks and preserves the `dependsOn`
 * graph across the per-domain wiring helpers extracted from `afterEvaluate`.
 */
class RetroAssemblerPluginTest :
    BehaviorSpec({
      fun newEvaluatedProject(): org.gradle.api.Project {
        val project =
            ProjectBuilder.builder()
                .withProjectDir(Files.createTempDirectory("retro-assembler-plugin-test").toFile())
                .build()
        project.pluginManager.apply(RetroAssemblerPlugin::class.java)
        project.extensions.getByType(RetroAssemblerPluginExtension::class.java).dialectVersion =
            "5.25"
        (project as org.gradle.api.internal.project.ProjectInternal).evaluate()
        return project
      }

      fun taskNamesDependedOnBy(project: org.gradle.api.Project, taskName: String): Set<String> =
          project.tasks
              .getByName(taskName)
              .taskDependencies
              .getDependencies(project.tasks.getByName(taskName))
              .map { it.name }
              .toSet()

      given("the plugin is applied and the project is evaluated") {
        val project = newEvaluatedProject()

        `when`("checking task registration") {
          then("all expected tasks exist") {
            val expectedTasks =
                listOf(
                    TASK_RESOLVE_DEV_DEPENDENCIES,
                    TASK_DEPENDENCIES,
                    TASK_CHARPAD,
                    TASK_SPRITEPAD,
                    TASK_GOATTRACKER,
                    TASK_IMAGE,
                    TASK_PREPROCESS,
                    TASK_ASM,
                    TASK_CLEAN,
                    TASK_ASM_SPEC,
                    TASK_TEST,
                    TASK_BUILD,
                    TASK_FLOWS)
            expectedTasks.forEach { taskName ->
              project.tasks.findByName(taskName) shouldBe project.tasks.getByName(taskName)
            }
          }
        }

        `when`("checking the dependsOn graph") {
          then("preprocess depends on the four processor tasks") {
            val deps = taskNamesDependedOnBy(project, TASK_PREPROCESS)
            deps shouldContain TASK_CHARPAD
            deps shouldContain TASK_SPRITEPAD
            deps shouldContain TASK_GOATTRACKER
            deps shouldContain TASK_IMAGE
          }

          then("asm depends on dev deps, dependencies, preprocess, and flows") {
            val deps = taskNamesDependedOnBy(project, TASK_ASM)
            deps shouldContain TASK_RESOLVE_DEV_DEPENDENCIES
            deps shouldContain TASK_DEPENDENCIES
            deps shouldContain TASK_PREPROCESS
            deps shouldContain TASK_FLOWS
          }

          then("asmSpec depends on dev deps and dependencies") {
            val deps = taskNamesDependedOnBy(project, TASK_ASM_SPEC)
            deps shouldContain TASK_RESOLVE_DEV_DEPENDENCIES
            deps shouldContain TASK_DEPENDENCIES
          }

          then("test depends on asmSpec") {
            taskNamesDependedOnBy(project, TASK_TEST) shouldContain TASK_ASM_SPEC
          }

          then("build depends on asm and test") {
            val deps = taskNamesDependedOnBy(project, TASK_BUILD)
            deps shouldContain TASK_ASM
            deps shouldContain TASK_TEST
          }
        }

        `when`("checking default tasks") {
          then("build is registered as the default task") {
            project.defaultTasks shouldContain TASK_BUILD
          }
        }
      }
    })
