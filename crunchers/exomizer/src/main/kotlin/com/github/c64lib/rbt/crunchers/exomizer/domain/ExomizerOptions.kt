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
package com.github.c64lib.rbt.crunchers.exomizer.domain

/**
 * Raw mode compression options for Exomizer.
 *
 * All properties are optional with sensible defaults matching exomizer behavior.
 */
data class RawOptions(
    val backwards: Boolean = false,
    val reverse: Boolean = false,
    val decrunch: Boolean = false,
    val compatibility: Boolean = false,
    val speedOverRatio: Boolean = false,
    val encoding: String? = null,
    val skipEncoding: Boolean = false,
    val maxOffset: Int = 65535,
    val maxLength: Int = 65535,
    val passes: Int = 100,
    val bitStreamTraits: Int? = null,
    val bitStreamFormat: Int? = null,
    val controlAddresses: String? = null,
    val quiet: Boolean = false,
    val brief: Boolean = false
)

/**
 * Memory mode compression options for Exomizer.
 *
 * Extends RawOptions with memory-specific settings.
 */
data class MemOptions(
    val rawOptions: RawOptions = RawOptions(),
    val loadAddress: String = "auto",
    val forward: Boolean = false
) {
  // Convenience properties to access raw options
  val backwards: Boolean
    get() = rawOptions.backwards
  val reverse: Boolean
    get() = rawOptions.reverse
  val decrunch: Boolean
    get() = rawOptions.decrunch
  val compatibility: Boolean
    get() = rawOptions.compatibility
  val speedOverRatio: Boolean
    get() = rawOptions.speedOverRatio
  val encoding: String?
    get() = rawOptions.encoding
  val skipEncoding: Boolean
    get() = rawOptions.skipEncoding
  val maxOffset: Int
    get() = rawOptions.maxOffset
  val maxLength: Int
    get() = rawOptions.maxLength
  val passes: Int
    get() = rawOptions.passes
  val bitStreamTraits: Int?
    get() = rawOptions.bitStreamTraits
  val bitStreamFormat: Int?
    get() = rawOptions.bitStreamFormat
  val controlAddresses: String?
    get() = rawOptions.controlAddresses
  val quiet: Boolean
    get() = rawOptions.quiet
  val brief: Boolean
    get() = rawOptions.brief
}
