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
package com.github.c64lib.rbt.crunchers.exomizer.adapters.`in`.gradle

import com.github.c64lib.rbt.crunchers.exomizer.domain.ExomizerExecutionException
import com.github.c64lib.rbt.crunchers.exomizer.domain.MemOptions
import com.github.c64lib.rbt.crunchers.exomizer.domain.RawOptions
import com.github.c64lib.rbt.crunchers.exomizer.usecase.port.ExecuteExomizerPort
import java.io.File

/**
 * Gradle adapter implementation of ExecuteExomizerPort.
 *
 * Executes the exomizer binary with command-line arguments built from options.
 */
class GradleExomizerAdapter : ExecuteExomizerPort {

  override fun executeRaw(source: File, output: File, options: RawOptions) {
    val args = buildRawArgs(output, options, source)
    execute(args)
  }

  override fun executeMem(source: File, output: File, options: MemOptions) {
    val args = buildMemArgs(output, options, source)
    execute(args)
  }

  private fun buildRawArgs(output: File, options: RawOptions, source: File): List<String> {
    val args = mutableListOf("exomizer", "raw", "-o", output.absolutePath)

    if (options.backwards) args.add("-b")
    if (options.reverse) args.add("-r")
    if (options.compatibility) args.add("-c")
    if (options.speedOverRatio) args.add("-C")

    if (options.encoding != null) {
      args.add("-e")
      args.add(options.encoding)
    }

    if (options.skipEncoding) args.add("-E")

    args.add("-m")
    args.add(options.maxOffset.toString())

    args.add("-M")
    args.add(options.maxLength.toString())

    args.add("-p")
    args.add(options.passes.toString())

    if (options.bitStreamTraits != null) {
      args.add("-T")
      args.add(options.bitStreamTraits.toString())
    }

    if (options.bitStreamFormat != null) {
      args.add("-P")
      args.add(options.bitStreamFormat.toString())
    }

    if (options.controlAddresses != null) {
      args.add("-N")
      args.add(options.controlAddresses)
    }

    if (options.quiet) args.add("-q")
    if (options.brief) args.add("-B")

    args.add(source.absolutePath)

    return args
  }

  private fun buildMemArgs(output: File, options: MemOptions, source: File): List<String> {
    val args = mutableListOf("exomizer", "mem", "-o", output.absolutePath)

    args.add("-l")
    args.add(options.loadAddress)

    if (options.forward) args.add("-f")

    if (options.backwards) args.add("-b")
    if (options.reverse) args.add("-r")
    if (options.compatibility) args.add("-c")
    if (options.speedOverRatio) args.add("-C")

    if (options.encoding != null) {
      args.add("-e")
      args.add(options.encoding)
    }

    if (options.skipEncoding) args.add("-E")

    args.add("-m")
    args.add(options.maxOffset.toString())

    args.add("-M")
    args.add(options.maxLength.toString())

    args.add("-p")
    args.add(options.passes.toString())

    if (options.bitStreamTraits != null) {
      args.add("-T")
      args.add(options.bitStreamTraits.toString())
    }

    if (options.bitStreamFormat != null) {
      args.add("-P")
      args.add(options.bitStreamFormat.toString())
    }

    if (options.controlAddresses != null) {
      args.add("-N")
      args.add(options.controlAddresses)
    }

    if (options.quiet) args.add("-q")
    if (options.brief) args.add("-B")

    args.add(source.absolutePath)

    return args
  }

  private fun execute(args: List<String>) {
    val processBuilder = ProcessBuilder(args)
    processBuilder.inheritIO()

    val process = processBuilder.start()
    val exitCode = process.waitFor()

    if (exitCode != 0) {
      throw ExomizerExecutionException(
          "Exomizer execution failed with exit code $exitCode. Command: ${args.joinToString(" ")}")
    }
  }
}
