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
import com.github.c64lib.rbt.flows.domain.config.SpritepadCommand
import com.github.c64lib.rbt.flows.domain.config.SpritepadConfig
import com.github.c64lib.rbt.flows.domain.config.SpritepadOutputs
import com.github.c64lib.rbt.flows.domain.port.SpritepadPort
import java.io.File

/**
 * Processes SpritePad (.spd) files and generates sprite outputs with configurable format,
 * optimization, and animation support. Uses SpritepadPort for processing.
 */
data class SpritepadStep(
    override val name: String,
    override val inputs: List<String> = emptyList(),
    val spritepadOutputs: SpritepadOutputs,
    val config: SpritepadConfig = SpritepadConfig()
) : FlowStep(name, "spritepad", inputs, spritepadOutputs.getAllOutputPaths()) {

  // Port injection - set by task adapter before execution
  private var spritepadPort: SpritepadPort? = null

  /**
   * Injects the spritepad port dependency. This is called by the adapter layer when the step is
   * prepared for execution.
   */
  fun setSpritepadPort(port: SpritepadPort) {
    this.spritepadPort = port
  }

  override fun execute(context: Map<String, Any>) {
    val port =
        spritepadPort
            ?: throw IllegalStateException(
                "SpritepadPort not injected for step '$name'. Call setSpritepadPort() before execution.")

    // Extract project root directory from context
    val projectRootDir =
        context["projectRootDir"] as? File
            ?: throw IllegalStateException("Project root directory not found in execution context")

    // Convert input paths to SPD files using base class helper
    val inputFiles = resolveInputFiles(inputs, projectRootDir)

    // Create SpritepadCommand instances for each input file
    val spritepadCommands: List<SpritepadCommand> =
        inputFiles.map { inputFile ->
          SpritepadCommand(
              inputFile = inputFile,
              spritepadOutputs = spritepadOutputs,
              config = config,
              projectRootDir = projectRootDir)
        }

    // Execute spritepad processing through the port
    try {
      port.process(spritepadCommands as List<SpritepadCommand>)
    } catch (e: Exception) {
      throw RuntimeException("Spritepad processing failed for step '$name': ${e.message}", e)
    }

    outputs.forEach { outputPath -> println("  Generated output: $outputPath") }
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    if (inputs.isEmpty()) {
      errors.add("Spritepad step '$name' requires at least one input .spd file")
    }

    if (!spritepadOutputs.hasOutputs()) {
      errors.add("Spritepad step '$name' requires at least one output configuration")
    }

    // Validate input file extensions
    inputs.forEach { inputPath ->
      if (!inputPath.endsWith(".spd", ignoreCase = true)) {
        errors.add("Spritepad step '$name' expects .spd files, but got: $inputPath")
      }
    }

    // Validate output configurations
    spritepadOutputs.sprites.forEach { sprite ->
      // Allow empty output path only if no output is configured, which is caught above
      if (sprite.output.isEmpty()) {
        errors.add("Spritepad step '$name': sprite output path cannot be empty")
      }
      if (sprite.start < 0 || sprite.end < 0 || sprite.start >= sprite.end) {
        errors.add(
            "Spritepad step '$name': sprite start/end range invalid: start=${sprite.start}, end=${sprite.end}")
      }
    }

    return errors
  }

  override fun getConfiguration(): Map<String, Any> {
    return mapOf(
        "format" to config.format.name,
        "optimization" to config.optimization.name,
        "exportRaw" to config.exportRaw,
        "exportOptimized" to config.exportOptimized,
        "animationSupport" to config.animationSupport,
        "spriteOutputs" to spritepadOutputs.sprites.size)
  }
}
