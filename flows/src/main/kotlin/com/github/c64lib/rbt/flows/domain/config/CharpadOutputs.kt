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
 * Filter configuration for binary outputs (nybbler or interleaver).
 *
 * Filters are mutually exclusive per output: only one filter type can be applied to any output.
 */
sealed class FilterConfig {
  /**
   * Nybbler filter: Splits bytes into low and high nibbles (4-bit halves).
   *
   * @param loOutput File path for low nibbles (optional, relative to project root)
   * @param hiOutput File path for high nibbles (optional, relative to project root)
   * @param normalizeHi Whether to normalize high nibbles by shifting right 4 bits
   */
  data class Nybbler(
      val loOutput: String? = null,
      val hiOutput: String? = null,
      val normalizeHi: Boolean = true
  ) : FilterConfig()

  /**
   * Interleaver filter: Distributes binary data across multiple output streams in round-robin
   * fashion.
   *
   * @param outputs List of file paths for interleaved outputs (relative to project root)
   */
  data class Interleaver(val outputs: List<String>) : FilterConfig()

  /** No filter applied to this output (default). */
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
