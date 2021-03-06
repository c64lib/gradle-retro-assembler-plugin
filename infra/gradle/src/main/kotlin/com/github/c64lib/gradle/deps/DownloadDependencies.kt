/*
MIT License

Copyright (c) 2018-2021 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.gradle.deps

import com.github.c64lib.gradle.GROUP_BUILD
import com.github.c64lib.gradle.RetroAssemblerPluginExtension
import com.github.c64lib.retroassembler.domain.Dependency
import com.github.c64lib.retroassembler.domain.DependencyType
import de.undercouch.gradle.tasks.download.Download
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class DownloadDependencies : DefaultTask() {

  init {
    description = "Downloads and unzips library dependency"
    group = GROUP_BUILD
  }

  @Input lateinit var extension: RetroAssemblerPluginExtension

  @TaskAction
  fun downloadAndUnzip() {
    extension.dependencies.forEach { dependency ->
      val dependencyName = dependency.name.replace('/', '-')
      val downloadTask =
          project.tasks
              .create(
                  "_c64lib_download_${dependency.type}-$dependencyName@${dependency.version.version}",
                  Download::class.java)
      val archive =
          when (dependency.type) {
            DependencyType.GitHub -> configureGitHub(dependency, downloadTask)
          }
      downloadTask.download()
      unzip(archive, archive.parentFile)
      archive.delete()
    }
  }

  private fun configureGitHub(dependency: Dependency, downloadTask: Download): File {
    val url = "https://github.com/${dependency.name}/archive/${dependency.version.version}.tar.gz"
    downloadTask.src(url)
    val dest = project.file(".ra/deps/${dependency.name}/${dependency.version.version}.tar.gz")
    downloadTask.dest(dest)
    return dest
  }

  private fun unzip(what: File, where: File) {
    val files = project.tarTree(what)
    project.copy { spec ->
      spec.from(files)
      spec.into(where)
      spec.eachFile {
        val index = it.path.indexOf('/')
        if (index >= 0) {
          val path = it.path.substring(index + 1)
          it.path = path
        } else {
          it.exclude()
        }
      }
    }
  }
}
