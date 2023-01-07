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
package com.github.c64lib.rbt.compilers.kickass.usecase

import com.github.c64lib.rbt.compilers.kickass.usecase.port.DownloadKickAssemblerPort
import com.github.c64lib.rbt.compilers.kickass.usecase.port.ReadVersionPort
import com.github.c64lib.rbt.compilers.kickass.usecase.port.SaveVersionPort
import com.github.c64lib.rbt.shared.domain.SemVer
import java.net.URL

class DownloadKickAssemblerUseCase(
    private val downloadKickAssemblerPort: DownloadKickAssemblerPort,
    private val readVersionPort: ReadVersionPort,
    private val saveVersionPort: SaveVersionPort
) {

  fun apply(command: DownloadKickAssemblerCommand) {
    val url =
        URL("https://github.com/c64lib/asm-ka/releases/download/${command.version}/KickAss.jar")
    val target = "${command.workDir}/asms/ka/${command.version}/KickAss.jar"
    val versionFile = "${command.workDir}/asms/ka/version"
    if (command.force || command.version != readVersionPort.readVersion(versionFile)) {
      downloadKickAssemblerPort.download(url, target)
      saveVersionPort.saveVersion(versionFile, command.version)
    }
  }
}

data class DownloadKickAssemblerCommand(
    val version: SemVer,
    val workDir: String,
    val force: Boolean = false
)
