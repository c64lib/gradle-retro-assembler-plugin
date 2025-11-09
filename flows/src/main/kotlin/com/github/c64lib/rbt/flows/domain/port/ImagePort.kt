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
package com.github.c64lib.rbt.flows.domain.port

import com.github.c64lib.rbt.flows.domain.config.ImageCommand

/**
 * Port interface for image processing operations.
 *
 * This port abstracts the image processing capabilities from the domain layer, allowing different
 * implementations (e.g., legacy ProcessImage adapter, direct use case orchestration) to provide the
 * actual image processing functionality.
 *
 * The port is responsible for:
 * - Reading PNG image files
 * - Applying transformations (cut, split, extend, flip, reduce resolution)
 * - Writing outputs in sprite and bitmap formats
 * - Handling multiple images from split operations
 * - Managing output file generation and naming
 */
interface ImagePort {

  /**
   * Process a single image according to the provided command.
   *
   * Implementations must:
   * 1. Read the input PNG image file
   * 2. Apply transformations in order (cut → split → extend → flip → reduce)
   * 3. Generate output files in the specified format(s)
   * 4. Handle split-generated multiple images with indexed naming (e.g., tile_0.prg, tile_1.prg)
   *
   * @param command The image processing command containing input file, transformations, config
   * @throws IllegalArgumentException if input file doesn't exist or is not a valid PNG
   * @throws RuntimeException if processing fails
   */
  fun process(command: ImageCommand)

  /**
   * Process multiple image commands (convenience method for batch processing).
   *
   * Default implementation calls process() for each command sequentially.
   *
   * @param commands List of image processing commands to execute
   */
  fun process(commands: List<ImageCommand>) {
    commands.forEach { command -> process(command) }
  }
}
