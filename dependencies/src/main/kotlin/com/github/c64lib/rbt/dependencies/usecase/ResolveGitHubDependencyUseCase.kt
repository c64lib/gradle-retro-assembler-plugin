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
package com.github.c64lib.rbt.dependencies.usecase

import com.github.c64lib.rbt.dependencies.usecase.port.DownloadDependencyPort
import com.github.c64lib.rbt.dependencies.usecase.port.ReadDependencyVersionPort
import com.github.c64lib.rbt.dependencies.usecase.port.SaveDependencyVersionPort
import com.github.c64lib.rbt.dependencies.usecase.port.UntarDependencyPort
import com.github.c64lib.rbt.shared.domain.DependencyVersion
import java.net.URL

/**
 * Use case for resolving a GitHub dependency.
 *
 * This class handles the process of downloading, extracting, and saving the version of a dependency
 * from GitHub. It uses various ports to perform these operations.
 *
 * @property downloadDependencyPort Port for downloading the dependency.
 * @property untarDependencyPort Port for extracting the downloaded dependency.
 * @property readDependencyVersionPort Port for reading the current version of the dependency.
 * @property saveDependencyVersionPort Port for saving the version of the dependency.
 */
class ResolveGitHubDependencyUseCase(
    private val downloadDependencyPort: DownloadDependencyPort,
    private val untarDependencyPort: UntarDependencyPort,
    private val readDependencyVersionPort: ReadDependencyVersionPort,
    private val saveDependencyVersionPort: SaveDependencyVersionPort
) {
  /**
   * Applies the command to resolve the GitHub dependency.
   *
   * This method checks if the dependency needs to be updated or forced to update. If so, it
   * downloads the dependency, extracts it, and saves the version information.
   *
   * @param command The command containing the details of the dependency to resolve.
   */
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

/**
 * Data class representing the command to resolve a GitHub dependency.
 *
 * @property dependencyName The name of the dependency to resolve.
 * @property dependencyVersion The version of the dependency to resolve.
 * @property force A flag indicating whether to force the update of the dependency.
 * @property workDir The working directory where the dependency will be resolved.
 */
data class ResolveGitHubDependencyCommand(
    val dependencyName: String,
    val dependencyVersion: DependencyVersion,
    val force: Boolean,
    val workDir: String
)
