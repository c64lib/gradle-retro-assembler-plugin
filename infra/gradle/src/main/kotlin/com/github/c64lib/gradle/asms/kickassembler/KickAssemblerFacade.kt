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
package com.github.c64lib.gradle.asms.kickassembler

import com.github.c64lib.gradle.asms.AssemblerFacade
import com.github.c64lib.rbt.shared.gradle.DIALECT_VERSION_LATEST
import com.github.c64lib.rbt.shared.gradle.RetroAssemblerPluginExtension
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

class KickAssemblerFacade(
    private val project: Project,
    private val extension: RetroAssemblerPluginExtension
) : AssemblerFacade {

  private val kaFile =
      project.file("${extension.workDir}/asms/ka/${extension.dialectVersion}/KickAss.jar")

  override fun installDevKit() {
    if (!kaFile.exists()) {
      if (extension.dialectVersion == DIALECT_VERSION_LATEST) {
        throw GradleException(
            "Dialect version ${extension.dialectVersion} is not supported for KickAssemblerFacade")
      }
      val downloadTask = project.tasks.create("_c64lib_download_ka", Download::class.java)
      downloadTask.src(
          "https://github.com/c64lib/asm-ka/releases/download/${extension.dialectVersion}/KickAss.jar")
      downloadTask.dest(kaFile)
      downloadTask.download()
    }
  }

  override fun targetFiles(): FileCollection =
      project.fileTree(".").matching { pattern ->
        pattern.include("**/*.prg", "**/*.sym", "**/*.vs", "**/*.specOut")
      }

  private fun asParamList(paramName: String, values: Array<String>) =
      values.flatMap { value -> listOf(paramName, value) }
}
