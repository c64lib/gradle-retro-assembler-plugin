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

import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleSpecCommand
import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleSpecUseCase
import com.github.c64lib.rbt.flows.usecase.port.TestPort
import com.github.c64lib.rbt.testing.a64spec.usecase.Run64SpecTestUseCase
import com.github.c64lib.rbt.testing.a64spec.usecase.TestResult
import java.io.File

/**
 * Adapter implementation of [TestPort] bridging to the 64spec use cases.
 *
 * [assembleSpec] compiles a spec via [KickAssembleSpecUseCase] using the plugin-wide `libDirs` and
 * `defines` (mirroring the legacy `AssembleSpec` task); [runSpec] runs it on VICE via
 * [Run64SpecTestUseCase]. The `.specOut` result-file name follows the 64spec naming convention.
 */
class Spec64TestPortAdapter(
    private val kickAssembleSpecUseCase: KickAssembleSpecUseCase,
    private val run64SpecTestUseCase: Run64SpecTestUseCase,
    private val libDirs: List<File>,
    private val defines: List<String>
) : TestPort {

  override fun assembleSpec(source: File) {
    kickAssembleSpecUseCase.apply(
        KickAssembleSpecCommand(
            libDirs = libDirs,
            defines = defines,
            resultFileName = resultFileName(source),
            source = source))
  }

  override fun runSpec(source: File): TestResult = run64SpecTestUseCase.apply(source)

  private fun resultFileName(source: File): String = source.nameWithoutExtension + ".specOut"
}
