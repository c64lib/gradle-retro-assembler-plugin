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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl

import com.github.c64lib.rbt.flows.domain.config.*
import com.github.c64lib.rbt.flows.domain.steps.CharpadStep

/**
 * Type-safe DSL builder for Charpad processing steps with dedicated output methods.
 *
 * This builder provides dedicated DSL methods for each output type (charset, map, metadata, etc.),
 * matching the functionality of the original processor DSL.
 */
class CharpadStepBuilder(private val name: String) {
  private val inputs = mutableListOf<String>()

  // Output configuration lists
  private val charsetOutputs = mutableListOf<CharsetOutput>()
  private val charAttributesOutputs = mutableListOf<CharAttributesOutput>()
  private val charColoursOutputs = mutableListOf<CharColoursOutput>()
  private val charMaterialsOutputs = mutableListOf<CharMaterialsOutput>()
  private val charScreenColoursOutputs = mutableListOf<CharScreenColoursOutput>()
  private val tileOutputs = mutableListOf<TileOutput>()
  private val tileTagsOutputs = mutableListOf<TileTagsOutput>()
  private val tileColoursOutputs = mutableListOf<TileColoursOutput>()
  private val tileScreenColoursOutputs = mutableListOf<TileScreenColoursOutput>()
  private val mapOutputs = mutableListOf<MapOutput>()
  private val metadataOutputs = mutableListOf<MetadataOutput>()

  // Core charpad processing options
  var compression: CharpadCompression = CharpadCompression.NONE
  var exportFormat: CharpadFormat = CharpadFormat.STANDARD
  var tileSize: Int = 8
  var charsetOptimization: Boolean = true
  var generateMap: Boolean = true
  var generateCharset: Boolean = true
  var ctm8PrototypeCompatibility: Boolean = false

  // Global metadata defaults (can be overridden per metadata output)
  var namespace: String = ""
  var prefix: String = ""
  var includeVersion: Boolean = false
  var includeBgColours: Boolean = true // Align with original processor default
  var includeCharColours: Boolean = true // Align with original processor default
  var includeMode: Boolean = false

  /** Specifies input sources for this Charpad step. */
  fun from(path: String) {
    inputs.add(path)
  }

  /** Specifies multiple input sources for this Charpad step. */
  fun from(vararg paths: String) {
    inputs.addAll(paths)
  }

  /** Configures charset output with optional start/end range and filter. */
  fun charset(block: RangeOutputBuilder.() -> Unit) {
    val builder = RangeOutputBuilder()
    builder.block()
    charsetOutputs.add(CharsetOutput(builder.output, builder.start, builder.end, builder.filter))
  }

  /** Configures charset attributes output with optional start/end range and filter. */
  fun charsetAttributes(block: RangeOutputBuilder.() -> Unit) {
    val builder = RangeOutputBuilder()
    builder.block()
    charAttributesOutputs.add(
        CharAttributesOutput(builder.output, builder.start, builder.end, builder.filter))
  }

  /** Configures charset colours output with optional start/end range and filter. */
  fun charsetColours(block: RangeOutputBuilder.() -> Unit) {
    val builder = RangeOutputBuilder()
    builder.block()
    charColoursOutputs.add(
        CharColoursOutput(builder.output, builder.start, builder.end, builder.filter))
  }

  /** Configures charset materials output with optional start/end range and filter. */
  fun charsetMaterials(block: RangeOutputBuilder.() -> Unit) {
    val builder = RangeOutputBuilder()
    builder.block()
    charMaterialsOutputs.add(
        CharMaterialsOutput(builder.output, builder.start, builder.end, builder.filter))
  }

  /** Configures charset screen colours output with optional start/end range and filter. */
  fun charsetScreenColours(block: RangeOutputBuilder.() -> Unit) {
    val builder = RangeOutputBuilder()
    builder.block()
    charScreenColoursOutputs.add(
        CharScreenColoursOutput(builder.output, builder.start, builder.end, builder.filter))
  }

  /** Configures tiles output with optional start/end range and filter. */
  fun tiles(block: RangeOutputBuilder.() -> Unit) {
    val builder = RangeOutputBuilder()
    builder.block()
    tileOutputs.add(TileOutput(builder.output, builder.start, builder.end, builder.filter))
  }

  /** Configures tile tags output with optional start/end range and filter. */
  fun tileTags(block: RangeOutputBuilder.() -> Unit) {
    val builder = RangeOutputBuilder()
    builder.block()
    tileTagsOutputs.add(TileTagsOutput(builder.output, builder.start, builder.end, builder.filter))
  }

  /** Configures tile colours output with optional start/end range and filter. */
  fun tileColours(block: RangeOutputBuilder.() -> Unit) {
    val builder = RangeOutputBuilder()
    builder.block()
    tileColoursOutputs.add(
        TileColoursOutput(builder.output, builder.start, builder.end, builder.filter))
  }

  /** Configures tile screen colours output with optional start/end range and filter. */
  fun tileScreenColours(block: RangeOutputBuilder.() -> Unit) {
    val builder = RangeOutputBuilder()
    builder.block()
    tileScreenColoursOutputs.add(
        TileScreenColoursOutput(builder.output, builder.start, builder.end, builder.filter))
  }

  /** Configures map output with optional rectangular region and filter. */
  fun map(block: MapOutputBuilder.() -> Unit) {
    val builder = MapOutputBuilder()
    builder.block()
    mapOutputs.add(
        MapOutput(
            builder.output,
            builder.left,
            builder.top,
            builder.right,
            builder.bottom,
            builder.filter))
  }

  /** Configures metadata output with optional parameters. */
  fun meta(block: MetadataOutputBuilder.() -> Unit) {
    val builder =
        MetadataOutputBuilder(
            namespace, prefix, includeVersion, includeBgColours, includeCharColours, includeMode)
    builder.block()
    metadataOutputs.add(
        MetadataOutput(
            builder.output,
            builder.namespace,
            builder.prefix,
            builder.includeVersion,
            builder.includeBgColours,
            builder.includeCharColours,
            builder.includeMode))
  }

  /**
   * DEPRECATED: Legacy method for backward compatibility. Use dedicated output methods instead.
   *
   * @deprecated Use dedicated output methods (charset, map, meta, etc.) instead of generic to()
   */
  @Deprecated(
      "Use dedicated output methods (charset, map, meta, etc.) instead of generic to()",
      ReplaceWith("charset { output = path }"))
  fun to(path: String) {
    // For backward compatibility, treat generic outputs as charset outputs
    charsetOutputs.add(CharsetOutput(path))
  }

  /**
   * DEPRECATED: Legacy method for backward compatibility. Use dedicated output methods instead.
   *
   * @deprecated Use dedicated output methods (charset, map, meta, etc.) instead of generic to()
   */
  @Deprecated(
      "Use dedicated output methods (charset, map, meta, etc.) instead of generic to()",
      ReplaceWith("paths.forEach { charset { output = it } }"))
  fun to(vararg paths: String) {
    // For backward compatibility, treat generic outputs as charset outputs
    paths.forEach { charsetOutputs.add(CharsetOutput(it)) }
  }

  /**
   * DEPRECATED: Legacy method for backward compatibility. Use meta() instead.
   *
   * @deprecated Use meta { ... } instead of metadata { ... }
   */
  @Deprecated("Use meta { ... } instead of metadata { ... }", ReplaceWith("meta(block)"))
  fun metadata(block: MetadataConfigBuilder.() -> Unit) {
    val builder = MetadataConfigBuilder()
    builder.block()
    namespace = builder.namespace
    prefix = builder.prefix
    includeVersion = builder.includeVersion
    includeBgColours = builder.includeBgColours
    includeCharColours = builder.includeCharColours
    includeMode = builder.includeMode
  }

  internal fun build(): CharpadStep {
    val config =
        CharpadConfig(
            compression = compression,
            exportFormat = exportFormat,
            tileSize = tileSize,
            charsetOptimization = charsetOptimization,
            generateMap = generateMap,
            generateCharset = generateCharset,
            ctm8PrototypeCompatibility = ctm8PrototypeCompatibility,
            namespace = namespace,
            prefix = prefix,
            includeVersion = includeVersion,
            includeBgColours = includeBgColours,
            includeCharColours = includeCharColours,
            includeMode = includeMode)

    val outputs =
        CharpadOutputs(
            charsets = charsetOutputs.toList(),
            charAttributes = charAttributesOutputs.toList(),
            charColours = charColoursOutputs.toList(),
            charMaterials = charMaterialsOutputs.toList(),
            charScreenColours = charScreenColoursOutputs.toList(),
            tiles = tileOutputs.toList(),
            tileTags = tileTagsOutputs.toList(),
            tileColours = tileColoursOutputs.toList(),
            tileScreenColours = tileScreenColoursOutputs.toList(),
            maps = mapOutputs.toList(),
            metadata = metadataOutputs.toList())

    return CharpadStep(name, inputs, outputs, config)
  }

  /**
   * DSL builder for range-based outputs (start/end parameters) with optional filter support.
   *
   * Supports nybbler and interleaver filters for binary data transformation.
   */
  class RangeOutputBuilder {
    var output: String = ""
    var start: Int = 0
    var end: Int = 65536
    internal var filter: FilterConfig = FilterConfig.None

    /**
     * Configures a nybbler filter to split bytes into low and high nibbles.
     *
     * Example:
     * ```
     * nybbler {
     *   loOutput = "charset_lo.chr"
     *   hiOutput = "charset_hi.chr"
     *   normalizeHi = true
     * }
     * ```
     */
    fun nybbler(block: NybblerFilterBuilder.() -> Unit) {
      val builder = NybblerFilterBuilder()
      builder.block()
      filter = FilterConfig.Nybbler(builder.loOutput, builder.hiOutput, builder.normalizeHi)
    }

    /**
     * Configures an interleaver filter to distribute bytes across multiple outputs.
     *
     * Example:
     * ```
     * interleaver {
     *   outputs = listOf("charset_0.chr", "charset_1.chr")
     * }
     * ```
     */
    fun interleaver(block: InterleaverFilterBuilder.() -> Unit) {
      val builder = InterleaverFilterBuilder()
      builder.block()
      filter = FilterConfig.Interleaver(builder.outputs)
    }
  }

  /**
   * DSL builder for map outputs (left/top/right/bottom rectangular region) with optional filter
   * support.
   *
   * Supports nybbler and interleaver filters for binary data transformation.
   */
  class MapOutputBuilder {
    var output: String = ""
    var left: Int = 0
    var top: Int = 0
    var right: Int = 65536
    var bottom: Int = 65536
    internal var filter: FilterConfig = FilterConfig.None

    /**
     * Configures a nybbler filter to split bytes into low and high nibbles.
     *
     * Example:
     * ```
     * nybbler {
     *   loOutput = "map_lo.bin"
     *   hiOutput = "map_hi.bin"
     *   normalizeHi = true
     * }
     * ```
     */
    fun nybbler(block: NybblerFilterBuilder.() -> Unit) {
      val builder = NybblerFilterBuilder()
      builder.block()
      filter = FilterConfig.Nybbler(builder.loOutput, builder.hiOutput, builder.normalizeHi)
    }

    /**
     * Configures an interleaver filter to distribute bytes across multiple outputs.
     *
     * Example:
     * ```
     * interleaver {
     *   outputs = listOf("map_0.bin", "map_1.bin")
     * }
     * ```
     */
    fun interleaver(block: InterleaverFilterBuilder.() -> Unit) {
      val builder = InterleaverFilterBuilder()
      builder.block()
      filter = FilterConfig.Interleaver(builder.outputs)
    }
  }

  /** DSL builder for metadata outputs with all configuration parameters. */
  class MetadataOutputBuilder(
      defaultNamespace: String = "",
      defaultPrefix: String = "",
      defaultIncludeVersion: Boolean = false,
      defaultIncludeBgColours: Boolean = true,
      defaultIncludeCharColours: Boolean = true,
      defaultIncludeMode: Boolean = false
  ) {
    var output: String = ""
    var namespace: String = defaultNamespace
    var prefix: String = defaultPrefix
    var includeVersion: Boolean = defaultIncludeVersion
    var includeBgColours: Boolean = defaultIncludeBgColours
    var includeCharColours: Boolean = defaultIncludeCharColours
    var includeMode: Boolean = defaultIncludeMode
  }

  /**
   * DEPRECATED: Legacy DSL builder for backward compatibility.
   *
   * @deprecated Use MetadataOutputBuilder with meta { ... } instead
   */
  @Deprecated("Use MetadataOutputBuilder with meta { ... } instead")
  class MetadataConfigBuilder {
    var namespace: String = ""
    var prefix: String = ""
    var includeVersion: Boolean = false
    var includeBgColours: Boolean = true
    var includeCharColours: Boolean = true
    var includeMode: Boolean = false
  }

  /**
   * DSL builder for nybbler filter configuration.
   *
   * Nybbler splits bytes into low and high nibbles (4-bit halves) for independent processing.
   */
  class NybblerFilterBuilder {
    /** Output file path for low nibbles (lower 4 bits). Null means discard low nibbles. */
    var loOutput: String? = null

    /** Output file path for high nibbles (upper 4 bits). Null means discard high nibbles. */
    var hiOutput: String? = null

    /**
     * Whether to normalize high nibbles by shifting right 4 bits (default: true).
     *
     * When true, high nibble 0xA0 becomes 0x0A. When false, it remains 0xA0.
     */
    var normalizeHi: Boolean = true
  }

  /**
   * DSL builder for interleaver filter configuration.
   *
   * Interleaver distributes bytes across multiple outputs in round-robin fashion.
   */
  class InterleaverFilterBuilder {
    /**
     * List of output file paths for interleaved data.
     *
     * Input data is distributed evenly across these outputs. Input size must be evenly divisible by
     * the number of outputs.
     */
    var outputs: List<String> = emptyList()
  }
}
