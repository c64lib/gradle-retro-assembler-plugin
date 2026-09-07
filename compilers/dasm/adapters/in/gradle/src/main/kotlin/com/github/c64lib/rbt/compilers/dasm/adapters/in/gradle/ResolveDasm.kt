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
package com.github.c64lib.rbt.compilers.dasm.adapters.`in`.gradle

import com.github.c64lib.rbt.compilers.dasm.domain.DasmOperatingSystem
import com.github.c64lib.rbt.compilers.dasm.usecase.DownloadDasmCommand
import com.github.c64lib.rbt.compilers.dasm.usecase.DownloadDasmUseCase
import com.github.c64lib.rbt.shared.gradle.GROUP_BUILD
import com.github.c64lib.rbt.shared.gradle.dsl.RetroAssemblerPluginExtension
import com.github.c64lib.rbt.shared.gradle.getBooleanProperty
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/** Downloads the dasm binary for the host OS when `retroProject.dasmVersion` is configured. */
open class ResolveDasm : DefaultTask() {

  init {
    description = "Downloads dasm assembler binary"
    group = GROUP_BUILD
  }

  @Input lateinit var extension: RetroAssemblerPluginExtension

  @Internal lateinit var downloadDasmUseCase: DownloadDasmUseCase

  @TaskAction
  fun download() {
    val version =
        extension.dasmVersion
            ?: throw IllegalStateException("retroProject.dasmVersion is not configured")
    downloadDasmUseCase.apply(
        DownloadDasmCommand(
            version = version,
            workDir = extension.workDir,
            os = DasmOperatingSystem.detect(),
            force = project.getBooleanProperty("forceDownload") ?: false))
  }
}
