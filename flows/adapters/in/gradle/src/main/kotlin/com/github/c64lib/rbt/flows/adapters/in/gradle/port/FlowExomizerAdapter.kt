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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.port

import com.github.c64lib.rbt.crunchers.exomizer.domain.CrunchMemCommand
import com.github.c64lib.rbt.crunchers.exomizer.domain.CrunchRawCommand
import com.github.c64lib.rbt.crunchers.exomizer.domain.MemOptions
import com.github.c64lib.rbt.crunchers.exomizer.domain.RawOptions
import com.github.c64lib.rbt.crunchers.exomizer.usecase.CrunchMemUseCase
import com.github.c64lib.rbt.crunchers.exomizer.usecase.CrunchRawUseCase
import com.github.c64lib.rbt.crunchers.exomizer.usecase.port.ExecuteExomizerPort
import com.github.c64lib.rbt.flows.domain.port.ExomizerPort
import java.io.File

/**
 * Flows adapter for Exomizer compression.
 *
 * Bridges the flows domain and crunchers domain, exposing Exomizer functionality through the flows
 * ExomizerPort interface.
 */
class FlowExomizerAdapter(private val executeExomizerPort: ExecuteExomizerPort) : ExomizerPort {

  override fun crunchRaw(source: File, output: File) {
    val useCase = CrunchRawUseCase(executeExomizerPort)
    val command = CrunchRawCommand(source, output, RawOptions())
    useCase.apply(command)
  }

  override fun crunchMem(source: File, output: File, loadAddress: String, forward: Boolean) {
    val useCase = CrunchMemUseCase(executeExomizerPort)
    val memOptions =
        MemOptions(rawOptions = RawOptions(), loadAddress = loadAddress, forward = forward)
    val command = CrunchMemCommand(source, output, memOptions)
    useCase.apply(command)
  }
}
