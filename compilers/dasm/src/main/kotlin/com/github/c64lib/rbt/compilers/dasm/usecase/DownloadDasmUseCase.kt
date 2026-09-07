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
package com.github.c64lib.rbt.compilers.dasm.usecase

import com.github.c64lib.rbt.compilers.dasm.domain.DasmOperatingSystem
import com.github.c64lib.rbt.compilers.dasm.usecase.port.DownloadDasmPort
import com.github.c64lib.rbt.compilers.dasm.usecase.port.ReadVersionPort
import com.github.c64lib.rbt.compilers.dasm.usecase.port.SaveVersionPort
import com.github.c64lib.rbt.compilers.dasm.usecase.port.UnpackZipPort
import java.net.URL

/**
 * Downloads and caches the dasm binary for the host operating system from the official
 * dasm-assembler GitHub releases.
 *
 * Supports dasm 2.20.16 and newer (uniform `dasm-v{version}-{os}.zip` release assets). The binary
 * is cached under `{workDir}/asms/dasm/{version}/` with a version stamp; the download is skipped
 * when the stamp already matches the requested version.
 */
class DownloadDasmUseCase(
    private val downloadDasmPort: DownloadDasmPort,
    private val unpackZipPort: UnpackZipPort,
    private val readVersionPort: ReadVersionPort,
    private val saveVersionPort: SaveVersionPort
) {

  fun apply(command: DownloadDasmCommand) {
    val assetName = "dasm-v${command.version}-${command.os.assetName}.zip"
    val targetDirectory = "${command.workDir}/asms/dasm/${command.version}"
    val versionFile = "${command.workDir}/asms/dasm/version"
    if (command.force || command.version != readVersionPort.readVersion(versionFile)) {
      val url =
          URL(
              "https://github.com/dasm-assembler/dasm/releases/download/v${command.version}/$assetName")
      val archivePath = "$targetDirectory/$assetName"
      downloadDasmPort.download(url, archivePath)
      unpackZipPort.unpack(archivePath, targetDirectory)
      saveVersionPort.saveVersion(versionFile, command.version)
    }
  }
}

data class DownloadDasmCommand(
    val version: String,
    val workDir: String,
    val os: DasmOperatingSystem,
    val force: Boolean = false
)
