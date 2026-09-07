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
package com.github.c64lib.rbt.compilers.dasm.adapters.out.filedownload

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.gradle.testfixtures.ProjectBuilder

private fun writeZip(zipFile: File, entries: Map<String, ByteArray>) {
  ZipOutputStream(zipFile.outputStream()).use { zip ->
    entries.forEach { (name, content) ->
      zip.putNextEntry(ZipEntry(name))
      zip.write(content)
      zip.closeEntry()
    }
  }
}

class UnpackZipAdapterTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      val project = ProjectBuilder.builder().build()
      val adapter = UnpackZipAdapter(project)

      Given("a dasm-style zip with a single binary at its root") {
        val zipFile = File(project.projectDir, "dasm-v2.20.17-windows.zip")
        writeZip(zipFile, mapOf("dasm.exe" to "binary-content".toByteArray()))

        When("unpacking into a target directory") {
          adapter.unpack(zipFile.path, File(project.projectDir, "unpacked").path)

          Then("the binary is extracted with its content") {
            val extracted = File(project.projectDir, "unpacked/dasm.exe")
            extracted.exists() shouldBe true
            extracted.readText() shouldBe "binary-content"
          }
        }
      }

      Given("a zip with nested directories") {
        val zipFile = File(project.projectDir, "nested.zip")
        writeZip(
            zipFile,
            mapOf(
                "bin/dasm" to "unix-binary".toByteArray(), "docs/readme.txt" to "hi".toByteArray()))

        When("unpacking") {
          adapter.unpack(zipFile.path, File(project.projectDir, "out").path)

          Then("the directory structure is recreated") {
            File(project.projectDir, "out/bin/dasm").readText() shouldBe "unix-binary"
            File(project.projectDir, "out/docs/readme.txt").readText() shouldBe "hi"
          }
        }
      }

      Given("a malicious zip with an entry escaping the target directory") {
        val zipFile = File(project.projectDir, "evil.zip")
        writeZip(zipFile, mapOf("../evil.txt" to "escape".toByteArray()))

        When("unpacking") {
          Then("the unpack is rejected") {
            shouldThrow<IllegalStateException> {
              adapter.unpack(zipFile.path, File(project.projectDir, "safe").path)
            }
          }
        }
      }
    })
