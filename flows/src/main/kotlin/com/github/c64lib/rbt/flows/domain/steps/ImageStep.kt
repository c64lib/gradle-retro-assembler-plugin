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
package com.github.c64lib.rbt.flows.domain.steps

import com.github.c64lib.rbt.flows.domain.FlowStep
import com.github.c64lib.rbt.flows.domain.StepExecutionException
import com.github.c64lib.rbt.flows.domain.config.ImageCommand
import com.github.c64lib.rbt.flows.domain.config.ImageConfig
import com.github.c64lib.rbt.flows.domain.config.ImageOutputs
import com.github.c64lib.rbt.flows.domain.port.ImagePort

/**
 * Image file processor step.
 *
 * Processes PNG files with transformations and outputs. Validates input file extensions (.png) and
 * output configurations. Requires ImagePort injection via Gradle task.
 */
data class ImageStep(
    override val name: String,
    override val inputs: List<String> = emptyList(),
    val imageOutputs: ImageOutputs = ImageOutputs(),
    val config: ImageConfig = ImageConfig()
) : FlowStep(name, "image", inputs, imageOutputs.getAllOutputPaths()) {

  // Port injection - set by task adapter before execution
  private var imagePort: ImagePort? = null

  /** Injects the ImagePort dependency for processing. Called by the adapter layer. */
  fun setImagePort(port: ImagePort) {
    this.imagePort = port
  }

  override fun execute(context: Map<String, Any>) {
    // Get port or throw - ensures proper initialization by adapter layer
    val port = imagePort ?: throw StepExecutionException("ImagePort not injected", name)

    // Extract required context values
    val projectRootDir = getProjectRootDir(context)

    // Convert input paths to absolute File objects using base class helper
    val inputFiles = resolveInputFiles(inputs, projectRootDir)

    // Create command objects for each input image
    val imageCommands: List<ImageCommand> =
        inputFiles.map { inputFile ->
          ImageCommand(
              inputFile = inputFile,
              imageOutputs = imageOutputs,
              config = config,
              projectRootDir = projectRootDir)
        }

    // Execute image processing through port
    try {
      port.process(imageCommands as List<ImageCommand>)
    } catch (e: Exception) {
      throw StepExecutionException("Image processing failed: ${e.message}", name, e)
    }

    // Log generated outputs
    imageOutputs.getAllOutputPaths().forEach { outputPath ->
      println("  Generated output: $outputPath")
    }
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    // Validate inputs
    if (inputs.isEmpty()) {
      errors.add("Image step '$name' requires at least one input image file")
    }

    // Validate input file extensions
    inputs.forEach { inputPath ->
      val extension = inputPath.substringAfterLast('.', "").lowercase()
      if (extension != "png") {
        errors.add(
            "Image step '$name' only supports PNG input files, but got: $inputPath (use .png extension)")
      }
    }

    // Validate outputs are configured
    if (!imageOutputs.hasOutputs()) {
      errors.add("Image step '$name' requires at least one output (sprite or bitmap format)")
    }

    return errors
  }

  override fun getConfiguration(): Map<String, Any> {
    return mapOf(
        "targetFormat" to config.targetFormat.name,
        "paletteOptimization" to config.paletteOptimization.name,
        "dithering" to config.dithering.name,
        "backgroundColor" to config.backgroundColor,
        "transparencySupport" to config.transparencySupport,
        "transformations" to imageOutputs.transformations.size,
        "spriteOutputs" to imageOutputs.spriteOutputs.size,
        "bitmapOutputs" to imageOutputs.bitmapOutputs.size)
  }
}
