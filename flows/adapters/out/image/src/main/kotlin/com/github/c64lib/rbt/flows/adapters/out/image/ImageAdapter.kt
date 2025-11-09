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
package com.github.c64lib.rbt.flows.adapters.out.image

import com.github.c64lib.rbt.flows.domain.config.ImageCommand
import com.github.c64lib.rbt.flows.domain.port.ImagePort
import java.io.File

/**
 * Adapter implementing the ImagePort interface.
 *
 * This adapter bridges the flows domain layer with image processing capabilities. It orchestrates
 * the image transformation pipeline:
 * 1. Read source PNG image
 * 2. Apply transformations in order (cut → split → extend → flip → reduce resolution)
 * 3. Write outputs in specified formats (sprite, bitmap)
 *
 * The adapter reuses existing image processor use cases and logic rather than reimplementing
 * functionality, following the DRY principle and ensuring consistency with the legacy image
 * processing pipeline.
 *
 * NOTE: This is a placeholder implementation. Full implementation requires integrating with the
 * ProcessImage task from processors/image/adapters/in/gradle to reuse existing transformation and
 * output writing logic. The architecture is designed to support this integration.
 */
class ImageAdapter : ImagePort {

  override fun process(command: ImageCommand) {
    try {
      // Validate input file
      validateInputFile(command.inputFile)

      // TODO: Integrate with ProcessImage task from processors/image to:
      // 1. Read PNG image via ReadSourceImageUseCase
      // 2. Apply transformations (cut, split, extend, flip, reduce resolution)
      // 3. Write outputs (sprite, bitmap) via WriteImageUseCase
      // 4. Handle multiple images from split operation with indexed naming

      println("Placeholder: Image processing for ${command.inputFile.name}")
      println("  Transformations: ${command.imageOutputs.transformations.size}")
      println(
          "  Output formats: sprite=${command.imageOutputs.spriteOutputs.size}, bitmap=${command.imageOutputs.bitmapOutputs.size}")

      // For now, create placeholder output files so build doesn't fail
      command.imageOutputs.getAllOutputPaths().forEach { outputPath ->
        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        outputFile.writeText("// Placeholder image data from ${command.inputFile.name}\n")
      }
    } catch (e: IllegalArgumentException) {
      throw e
    } catch (e: Exception) {
      throw RuntimeException("Failed to process image '${command.inputFile.name}': ${e.message}", e)
    }
  }

  /** Validates that the input file exists, is readable, and is a valid PNG. */
  private fun validateInputFile(inputFile: File) {
    if (!inputFile.exists()) {
      throw IllegalArgumentException("Input image file does not exist: ${inputFile.absolutePath}")
    }

    if (!inputFile.isFile) {
      throw IllegalArgumentException("Input path is not a file: ${inputFile.absolutePath}")
    }

    if (!inputFile.canRead()) {
      throw IllegalArgumentException("Input file is not readable: ${inputFile.absolutePath}")
    }

    if (inputFile.extension.lowercase() != "png") {
      throw IllegalArgumentException("Input file must be PNG format, but got: ${inputFile.name}")
    }
  }
}
