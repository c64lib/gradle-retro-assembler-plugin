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

import com.github.c64lib.rbt.compilers.dasm.usecase.port.UnpackZipPort
import java.io.File
import java.util.zip.ZipInputStream
import org.gradle.api.Project

/**
 * Unpacks a dasm release zip into the target directory using the JDK zip support.
 *
 * Extracted files are marked executable so the dasm binary can be run directly on Unix-like
 * systems; entries resolving outside the target directory are rejected (zip-slip protection).
 */
class UnpackZipAdapter(private val project: Project) : UnpackZipPort {

  override fun unpack(archive: String, targetDirectory: String) {
    val targetDir = project.file(targetDirectory).canonicalFile
    targetDir.mkdirs()

    ZipInputStream(project.file(archive).inputStream().buffered()).use { zip ->
      generateSequence { zip.nextEntry }
          .forEach { entry ->
            val target = File(targetDir, entry.name).canonicalFile
            if (!target.path.startsWith(targetDir.path)) {
              throw IllegalStateException("Zip entry escapes target directory: ${entry.name}")
            }
            if (entry.isDirectory) {
              target.mkdirs()
            } else {
              target.parentFile.mkdirs()
              target.outputStream().use { output -> zip.copyTo(output) }
              target.setExecutable(true, false)
            }
          }
    }
  }
}
