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
 * Domain models for dedicated Charpad output configurations.
 *
 * These data classes represent different types of outputs that can be generated from CharPad CTM
 * files, matching the functionality of the original processor DSL.
 */

/**
 * Filter configuration for binary outputs (nybbler or interleaver).
 *
 * Binary filters allow transforming extracted charset/tile data at different granularity levels:
 * - **Nybbler**: Splits bytes into 4-bit halves (nibbles) for independent processing
 * - **Interleaver**: Distributes bytes across multiple outputs in round-robin fashion
 *
 * Filters are **mutually exclusive per output** - only one filter type can be applied to any single
 * output. This design prevents filter composition complexity while supporting flexible data
 * transformation pipelines.
 *
 * ## Usage Examples:
 * ```kotlin
 * // Nybbler: Split charset into low and high nibbles
 * val nybblerFilter = FilterConfig.Nybbler(
 *     loOutput = "build/charset_lo.chr",
 *     hiOutput = "build/charset_hi.chr",
 *     normalizeHi = true
 * )
 *
 * // Interleaver: Split charset across 2 outputs (even/odd bytes)
 * val interleaverFilter = FilterConfig.Interleaver(
 *     outputs = listOf("build/charset_even.chr", "build/charset_odd.chr")
 * )
 *
 * // Apply to charset output
 * CharsetOutput(
 *     "build/charset.chr",
 *     filter = nybblerFilter
 * )
 * ```
 *
 * Uses a sealed class hierarchy to enforce mutual exclusivity:
 * - Only one filter type per output (nybbler, interleaver, or none)
 * - Type-safe configuration prevents invalid combinations at compile time
 */
sealed class FilterConfig {
  /**
   * Nybbler filter: Splits bytes into low and high nibbles (4-bit halves).
   *
   * The nybbler filter is useful for accessing individual 4-bit values independently. For example,
   * in Commodore 64 graphics, character codes often pack two nybbles per byte, and the nybbler
   * allows extracting and transforming them separately.
   *
   * ## How it works:
   * - Input byte `0xA5` (binary: 1010 0101)
   * - Low nibble output: `0x5` (binary: 0000 0101)
   * - High nibble output: `0xA` (binary: 0000 1010) when normalized, or `0xA0` when not
   *
   * ## Parameters:
   * @param loOutput Optional file path for low nibbles (lower 4 bits). If null, low nibbles are
   * ```
   *     discarded. Relative paths are resolved from project root.
   * @param hiOutput
   * ```
   * Optional file path for high nibbles (upper 4 bits). If null, high nibbles are
   * ```
   *     discarded. Relative paths are resolved from project root.
   * @param normalizeHi
   * ```
   * Whether to normalize high nibbles by shifting right 4 bits (default: true).
   * ```
   *     When true, high nibble 0xA becomes 0x0A. When false, it remains 0xA0. Set to false
   *     if you need the original bit positions preserved.
   * ```
   * ## Example:
   * ```kotlin
   * FilterConfig.Nybbler(
   *     loOutput = "build/lo.bin",
   *     hiOutput = "build/hi.bin",
   *     normalizeHi = true
   * )
   * ```
   */
  data class Nybbler(
      val loOutput: String? = null,
      val hiOutput: String? = null,
      val normalizeHi: Boolean = true
  ) : FilterConfig()

  /**
   * Interleaver filter: Distributes binary data across multiple output streams in round-robin.
   *
   * The interleaver filter is useful for accessing bytes within larger chunks independently. For
   * example, in word-aligned data (2 bytes per element), the interleaver can separate upper and
   * lower bytes for independent processing.
   *
   * ## How it works:
   * - Input bytes: [0x01, 0x02, 0x03, 0x04]
   * - With 2 outputs (even/odd distribution):
   * - Output 0: [0x01, 0x03]
   * - Output 1: [0x02, 0x04]
   * - With 3 outputs:
   * - Output 0: [0x01, 0x04]
   * - Output 1: [0x02]
   * - Output 2: [0x03]
   *
   * ## Parameters:
   * @param outputs List of file paths for interleaved outputs. Must have at least 1 entry.
   * ```
   *     Input data size must be evenly divisible by the number of outputs, otherwise an
   *     exception is thrown. Relative paths are resolved from project root.
   * ```
   * ## Example:
   * ```kotlin
   * FilterConfig.Interleaver(
   *     outputs = listOf(
   *         "build/charset_even.chr",
   *         "build/charset_odd.chr"
   *     )
   * )
   * ```
   *
   * @throws IllegalInputException if input data size is not evenly divisible by output count
   */
  data class Interleaver(val outputs: List<String>) : FilterConfig()

  /**
   * No filter applied to this output.
   *
   * This is the default filter configuration for all outputs. When applied, the binary data is
   * written directly to the output file without any transformation.
   */
  object None : FilterConfig()
}

/** Base interface for range-based outputs (start/end parameters). */
sealed interface RangeOutput {
  val output: String
  val start: Int
  val end: Int
  val filter: FilterConfig
}

/** Charset output configuration with start/end range. */
data class CharsetOutput(
    override val output: String,
    override val start: Int = 0,
    override val end: Int = 65536,
    override val filter: FilterConfig = FilterConfig.None
) : RangeOutput

/** Charset attributes output configuration with start/end range. */
data class CharAttributesOutput(
    override val output: String,
    override val start: Int = 0,
    override val end: Int = 65536,
    override val filter: FilterConfig = FilterConfig.None
) : RangeOutput

/** Charset colours output configuration with start/end range. */
data class CharColoursOutput(
    override val output: String,
    override val start: Int = 0,
    override val end: Int = 65536,
    override val filter: FilterConfig = FilterConfig.None
) : RangeOutput

/** Charset materials output configuration with start/end range. */
data class CharMaterialsOutput(
    override val output: String,
    override val start: Int = 0,
    override val end: Int = 65536,
    override val filter: FilterConfig = FilterConfig.None
) : RangeOutput

/** Charset screen colours output configuration with start/end range. */
data class CharScreenColoursOutput(
    override val output: String,
    override val start: Int = 0,
    override val end: Int = 65536,
    override val filter: FilterConfig = FilterConfig.None
) : RangeOutput

/** Tiles output configuration with start/end range. */
data class TileOutput(
    override val output: String,
    override val start: Int = 0,
    override val end: Int = 65536,
    override val filter: FilterConfig = FilterConfig.None
) : RangeOutput

/** Tile tags output configuration with start/end range. */
data class TileTagsOutput(
    override val output: String,
    override val start: Int = 0,
    override val end: Int = 65536,
    override val filter: FilterConfig = FilterConfig.None
) : RangeOutput

/** Tile colours output configuration with start/end range. */
data class TileColoursOutput(
    override val output: String,
    override val start: Int = 0,
    override val end: Int = 65536,
    override val filter: FilterConfig = FilterConfig.None
) : RangeOutput

/** Tile screen colours output configuration with start/end range. */
data class TileScreenColoursOutput(
    override val output: String,
    override val start: Int = 0,
    override val end: Int = 65536,
    override val filter: FilterConfig = FilterConfig.None
) : RangeOutput

/**
 * Map output configuration with rectangular region (left/top/right/bottom) and optional binary
 * filter.
 */
data class MapOutput(
    val output: String,
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 65536,
    val bottom: Int = 65536,
    val filter: FilterConfig = FilterConfig.None
)

/** Metadata output configuration with namespace, prefix, and inclusion flags. */
data class MetadataOutput(
    val output: String,
    val namespace: String = "",
    val prefix: String = "",
    val includeVersion: Boolean = false,
    val includeBgColours: Boolean = true,
    val includeCharColours: Boolean = true,
    val includeMode: Boolean = false
)

/**
 * Container for all Charpad output configurations.
 *
 * This class holds lists of all possible output types, allowing multiple outputs of the same type
 * (e.g., multiple charset outputs with different ranges).
 */
data class CharpadOutputs(
    val charsets: List<CharsetOutput> = emptyList(),
    val charAttributes: List<CharAttributesOutput> = emptyList(),
    val charColours: List<CharColoursOutput> = emptyList(),
    val charMaterials: List<CharMaterialsOutput> = emptyList(),
    val charScreenColours: List<CharScreenColoursOutput> = emptyList(),
    val tiles: List<TileOutput> = emptyList(),
    val tileTags: List<TileTagsOutput> = emptyList(),
    val tileColours: List<TileColoursOutput> = emptyList(),
    val tileScreenColours: List<TileScreenColoursOutput> = emptyList(),
    val maps: List<MapOutput> = emptyList(),
    val metadata: List<MetadataOutput> = emptyList()
) {
  /** Returns all output file paths for dependency tracking. */
  fun getAllOutputPaths(): List<String> {
    val primaryOutputs =
        charsets.map { it.output } +
            charAttributes.map { it.output } +
            charColours.map { it.output } +
            charMaterials.map { it.output } +
            charScreenColours.map { it.output } +
            tiles.map { it.output } +
            tileTags.map { it.output } +
            tileColours.map { it.output } +
            tileScreenColours.map { it.output } +
            maps.map { it.output } +
            metadata.map { it.output }

    val rangeFilterOutputs =
        (charsets +
                charAttributes +
                charColours +
                charMaterials +
                charScreenColours +
                tiles +
                tileTags +
                tileColours +
                tileScreenColours)
            .flatMap { output ->
              when (val filter = output.filter) {
                is FilterConfig.Nybbler -> listOfNotNull(filter.loOutput, filter.hiOutput)
                is FilterConfig.Interleaver -> filter.outputs
                FilterConfig.None -> emptyList()
              }
            }

    val mapFilterOutputs =
        maps.flatMap { output ->
          when (val filter = output.filter) {
            is FilterConfig.Nybbler -> listOfNotNull(filter.loOutput, filter.hiOutput)
            is FilterConfig.Interleaver -> filter.outputs
            FilterConfig.None -> emptyList()
          }
        }

    return primaryOutputs + rangeFilterOutputs + mapFilterOutputs
  }

  /** Checks if any outputs are configured. */
  fun hasOutputs(): Boolean = getAllOutputPaths().isNotEmpty()
}
