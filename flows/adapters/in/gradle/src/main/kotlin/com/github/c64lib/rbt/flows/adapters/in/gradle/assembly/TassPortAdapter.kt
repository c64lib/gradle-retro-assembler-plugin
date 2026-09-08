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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.assembly

import com.github.c64lib.rbt.compilers.tass.usecase.TassAssembleUseCase
import com.github.c64lib.rbt.flows.domain.config.TassCommand
import com.github.c64lib.rbt.flows.usecase.port.TassAssemblyPort

/**
 * Adapter implementation of TassAssemblyPort that bridges to TassAssembleUseCase.
 *
 * This adapter is responsible for translating domain-level 64tass commands to compiler-specific
 * operations while maintaining hexagonal architecture boundaries.
 */
class TassPortAdapter(
    private val tassAssembleUseCase: TassAssembleUseCase,
    private val commandAdapter: TassCommandAdapter = TassCommandAdapter()
) : TassAssemblyPort {

  override fun assemble(command: TassCommand) {
    val tassAssembleCommand = commandAdapter.toTassAssembleCommand(command)
    tassAssembleUseCase.apply(tassAssembleCommand)
  }

  override fun assemble(commands: List<TassCommand>) {
    commands.forEach { command -> assemble(command) }
  }
}
