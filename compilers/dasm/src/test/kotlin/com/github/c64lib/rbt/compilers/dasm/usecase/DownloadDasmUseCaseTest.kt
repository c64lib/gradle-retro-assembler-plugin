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
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.net.URL

private class Fixture(installedVersion: String? = null) {
  val downloadedUrls = mutableListOf<URL>()
  val downloadedTargets = mutableListOf<String>()
  val unpackedArchives = mutableListOf<Pair<String, String>>()
  val savedVersions = mutableListOf<Pair<String, String>>()

  val downloadPort =
      object : DownloadDasmPort {
        override fun download(url: URL, target: String): File {
          downloadedUrls.add(url)
          downloadedTargets.add(target)
          return File(target)
        }
      }
  val unpackPort =
      object : UnpackZipPort {
        override fun unpack(archive: String, targetDirectory: String) {
          unpackedArchives.add(archive to targetDirectory)
        }
      }
  val readVersionPort =
      object : ReadVersionPort {
        override fun readVersion(file: String): String? = installedVersion
      }
  val saveVersionPort =
      object : SaveVersionPort {
        override fun saveVersion(file: String, version: String) {
          savedVersions.add(file to version)
        }
      }

  val useCase = DownloadDasmUseCase(downloadPort, unpackPort, readVersionPort, saveVersionPort)
}

class DownloadDasmUseCaseTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      Given("no dasm version installed yet") {
        val fixture = Fixture(installedVersion = null)

        When("downloading dasm 2.20.17 for Windows") {
          fixture.useCase.apply(
              DownloadDasmCommand(
                  version = "2.20.17", workDir = ".ra", os = DasmOperatingSystem.WINDOWS))

          Then("the windows release asset is downloaded from the official releases") {
            fixture.downloadedUrls.map { it.toString() } shouldBe
                listOf(
                    "https://github.com/dasm-assembler/dasm/releases/download/v2.20.17/dasm-v2.20.17-windows.zip")
            fixture.downloadedTargets shouldBe
                listOf(".ra/asms/dasm/2.20.17/dasm-v2.20.17-windows.zip")
          }

          Then("the archive is unpacked into the versioned cache directory") {
            fixture.unpackedArchives shouldBe
                listOf(".ra/asms/dasm/2.20.17/dasm-v2.20.17-windows.zip" to ".ra/asms/dasm/2.20.17")
          }

          Then("the version stamp is saved") {
            fixture.savedVersions shouldBe listOf(".ra/asms/dasm/version" to "2.20.17")
          }
        }

        When("downloading for Linux and macOS") {
          fixture.useCase.apply(
              DownloadDasmCommand(
                  version = "2.20.17", workDir = ".ra", os = DasmOperatingSystem.LINUX))
          fixture.useCase.apply(
              DownloadDasmCommand(
                  version = "2.20.17", workDir = ".ra", os = DasmOperatingSystem.MACOS))

          Then("the per-OS assets are selected") {
            fixture.downloadedUrls.map { it.toString() } shouldBe
                listOf(
                    "https://github.com/dasm-assembler/dasm/releases/download/v2.20.17/dasm-v2.20.17-linux.zip",
                    "https://github.com/dasm-assembler/dasm/releases/download/v2.20.17/dasm-v2.20.17-macos.zip")
          }
        }
      }

      Given("the requested dasm version is already installed") {
        val fixture = Fixture(installedVersion = "2.20.17")

        When("downloading the same version") {
          fixture.useCase.apply(
              DownloadDasmCommand(
                  version = "2.20.17", workDir = ".ra", os = DasmOperatingSystem.LINUX))

          Then("nothing is downloaded, unpacked, or stamped") {
            fixture.downloadedUrls shouldBe emptyList()
            fixture.unpackedArchives shouldBe emptyList()
            fixture.savedVersions shouldBe emptyList()
          }
        }

        When("downloading the same version with force") {
          fixture.useCase.apply(
              DownloadDasmCommand(
                  version = "2.20.17",
                  workDir = ".ra",
                  os = DasmOperatingSystem.LINUX,
                  force = true))

          Then("the download happens anyway") {
            fixture.downloadedUrls.size shouldBe 1
            fixture.savedVersions shouldBe listOf(".ra/asms/dasm/version" to "2.20.17")
          }
        }

        When("downloading a different version") {
          fixture.useCase.apply(
              DownloadDasmCommand(
                  version = "2.20.16", workDir = ".ra", os = DasmOperatingSystem.LINUX))

          Then("the new version is downloaded and stamped") {
            fixture.downloadedUrls.map { it.toString() } shouldBe
                listOf(
                    "https://github.com/dasm-assembler/dasm/releases/download/v2.20.16/dasm-v2.20.16-linux.zip")
            fixture.savedVersions shouldBe listOf(".ra/asms/dasm/version" to "2.20.16")
          }
        }
      }
    })
