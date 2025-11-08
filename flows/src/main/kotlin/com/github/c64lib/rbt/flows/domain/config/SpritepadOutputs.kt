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
package com.github.c64lib.rbt.flows.domain.config

/**
 * Domain models for dedicated Spritepad output configurations.
 *
 * These data classes represent different types of outputs that can be generated from SpritePad SPD
 * files, matching the functionality of the original processor DSL.
 */

/** Base interface for range-based spritepad outputs (start/end parameters). */
sealed interface SpritepadRangeOutput {
  val output: String
  val start: Int
  val end: Int
}

/**
 * Sprite output configuration with start/end range for selective sprite export.
 *
 * Spritepad SPD files contain multiple sprites. The start and end parameters allow selecting a
 * subset of sprites to export to the binary output file.
 */
data class SpriteOutput(
    override val output: String,
    override val start: Int = 0,
    override val end: Int = 65536
) : SpritepadRangeOutput

/**
 * Container for all Spritepad output configurations.
 *
 * This class holds lists of all possible output types, allowing multiple outputs of the same type
 * (e.g., multiple sprite outputs with different ranges).
 */
data class SpritepadOutputs(val sprites: List<SpriteOutput> = emptyList()) {
  /** Returns all output file paths for dependency tracking. */
  fun getAllOutputPaths(): List<String> {
    return sprites.map { it.output }
  }

  /** Checks if any outputs are configured. */
  fun hasOutputs(): Boolean = getAllOutputPaths().isNotEmpty()
}
