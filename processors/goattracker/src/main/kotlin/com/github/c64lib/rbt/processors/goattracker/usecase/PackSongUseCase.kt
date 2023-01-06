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
package com.github.c64lib.rbt.processors.goattracker.usecase

import com.github.c64lib.rbt.processors.goattracker.usecase.port.ExecuteGt2RelocPort
import java.io.File

class PackSongUseCase(private val executeGt2RelocPort: ExecuteGt2RelocPort) {

  fun apply(packSongCommand: PackSongCommand) = executeGt2RelocPort.execute(packSongCommand)
}

data class PackSongCommand(
    val source: File,
    val output: File,
    val executable: String,
    val useBuildDir: Boolean,
    val bufferedSidWrites: Boolean? = null,
    val disableOptimization: Boolean? = null,
    val playerMemoryLocation: Int? = null,
    val sfxSupport: Boolean? = null,
    val sidMemoryLocation: Int? = null,
    val storeAuthorInfo: Boolean? = null,
    val volumeChangeSupport: Boolean? = null,
    val zeroPageLocation: Int? = null,
    val zeropageGhostRegisters: Boolean? = null
)
