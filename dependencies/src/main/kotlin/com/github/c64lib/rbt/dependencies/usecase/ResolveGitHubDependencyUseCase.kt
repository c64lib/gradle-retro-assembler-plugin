/*
MIT License

Copyright (c) 2018-2023 c64lib: The Ultimate Commodore 64 Library
Copyright (c) 2018-2023 Maciej Małecki

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
package com.github.c64lib.rbt.dependencies.usecase

import com.github.c64lib.rbt.dependencies.usecase.port.DownloadDependencyPort
import com.github.c64lib.rbt.dependencies.usecase.port.ReadDependencyVersionPort
import com.github.c64lib.rbt.dependencies.usecase.port.SaveDependencyVersionPort
import com.github.c64lib.rbt.dependencies.usecase.port.UntarDependencyPort
import com.github.c64lib.rbt.shared.domain.DependencyVersion
import java.net.URL

class ResolveGitHubDependencyUseCase(
    private val downloadDependencyPort: DownloadDependencyPort,
    private val untarDependencyPort: UntarDependencyPort,
    private val readDependencyVersionPort: ReadDependencyVersionPort,
    private val saveDependencyVersionPort: SaveDependencyVersionPort
) {
  fun apply(command: ResolveGitHubDependencyCommand) {
    val versionFile = "${command.workDir}/depvers/${command.dependencyName}/version"
    if (command.force ||
        command.dependencyVersion.version != readDependencyVersionPort.readVersion(versionFile)) {
      val libFile =
          downloadDependencyPort.download(
              URL(
                  "https://github.com/${command.dependencyName}/archive/${command.dependencyVersion.version}.tar.gz"),
              command.dependencyName)
      try {
        untarDependencyPort.untar(libFile)
        saveDependencyVersionPort.saveVersion(versionFile, command.dependencyVersion.version)
      } finally {
        libFile.delete()
      }
    }
  }
}

data class ResolveGitHubDependencyCommand(
    val dependencyName: String,
    val dependencyVersion: DependencyVersion,
    val force: Boolean,
    val workDir: String
)
