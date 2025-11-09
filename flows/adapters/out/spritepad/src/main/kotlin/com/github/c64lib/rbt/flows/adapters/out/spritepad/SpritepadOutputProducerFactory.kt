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
package com.github.c64lib.rbt.flows.adapters.out.spritepad

import com.c64lib.rbt.processors.spritepad.domain.SpriteProducer
import com.github.c64lib.rbt.flows.domain.config.SpritepadCommand
import com.github.c64lib.rbt.shared.processor.Output
import com.github.c64lib.rbt.shared.processor.OutputProducer
import java.io.File
import java.io.FileOutputStream

/**
 * Factory for creating output producers for spritepad sprite processing.
 *
 * This factory translates SpritepadCommand output configurations into SpriteProducer instances that
 * can be consumed by the ProcessSpritepadUseCase. It handles file path resolution and range
 * configuration.
 */
class SpritepadOutputProducerFactory {

  /**
   * Creates output producers for the given spritepad command.
   *
   * @param command The spritepad command with output configurations
   * @return List of OutputProducer instances (primarily SpriteProducer for sprite outputs)
   */
  fun createOutputProducers(command: SpritepadCommand): List<OutputProducer<*>> {
    val producers = mutableListOf<OutputProducer<*>>()

    // Create sprite output producers based on configured sprite outputs
    command.spritepadOutputs.sprites.forEach { spriteOutput ->
      val outputFile = resolveOutputFile(spriteOutput.output, command.projectRootDir)

      // Create binary output that writes to the file
      val binaryOutput = createBinaryOutput(outputFile)

      // Create SpriteProducer with range parameters
      val spriteProducer =
          SpriteProducer(start = spriteOutput.start, end = spriteOutput.end, output = binaryOutput)

      producers.add(spriteProducer)
    }

    return producers
  }

  /**
   * Resolves an output file path to an absolute File reference.
   *
   * @param outputPath The output file path (absolute or relative)
   * @param projectRootDir The project root directory to use for relative path resolution
   * @return Resolved File instance
   */
  private fun resolveOutputFile(outputPath: String, projectRootDir: File): File {
    return if (File(outputPath).isAbsolute) {
      File(outputPath)
    } else {
      File(projectRootDir, outputPath)
    }
  }

  /**
   * Creates a binary output that writes bytes to a file.
   *
   * @param outputFile The file to write to
   * @return Output instance that writes to the file
   */
  private fun createBinaryOutput(outputFile: File): Output<ByteArray> {
    return object : Output<ByteArray> {
      override fun write(data: ByteArray) {
        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { it.write(data) }
      }
    }
  }
}
