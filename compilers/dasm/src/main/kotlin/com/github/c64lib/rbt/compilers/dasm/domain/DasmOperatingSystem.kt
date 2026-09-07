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
package com.github.c64lib.rbt.compilers.dasm.domain

/**
 * Operating systems for which dasm release binaries are published.
 *
 * [assetName] matches the OS part of the release asset name (`dasm-v{version}-{assetName}.zip`,
 * dasm 2.20.16 and newer); [executableName] is the binary inside the archive.
 */
enum class DasmOperatingSystem(val assetName: String, val executableName: String) {
  WINDOWS("windows", "dasm.exe"),
  MACOS("macos", "dasm"),
  LINUX("linux", "dasm");

  companion object {
    fun detect(osName: String = System.getProperty("os.name")): DasmOperatingSystem {
      val normalized = osName.lowercase()
      // "darwin" contains "win", so macOS must be matched before Windows
      return when {
        normalized.contains("mac") || normalized.contains("darwin") -> MACOS
        normalized.contains("win") -> WINDOWS
        else -> LINUX
      }
    }
  }
}
