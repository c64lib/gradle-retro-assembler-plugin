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
import com.github.c64lib.rbt.flows.domain.steps.ImageStep
import com.github.c64lib.rbt.shared.domain.Axis
import com.github.c64lib.rbt.shared.domain.Color

/**
 * Type-safe DSL builder for Image processing steps.
 *
 * Provides a fluent API for configuring image transformations, output formats, and processing
 * options. Follows the builder pattern used in other flow steps (CharpadStep, SpritepadStep).
 *
 * Example usage:
 * ```kotlin
 * val step = ImageStepBuilder("process-sprites").apply {
 *   from("assets/sprite.png")
 *
 *   // Apply transformations (all optional, applied in fixed order)
 *   cut {
 *     left = 10
 *     top = 20
 *     width = 320
 *     height = 240
 *   }
 *
 *   split {
 *     width = 24
 *     height = 21
 *   }
 *
 *   flip {
 *     axis = Axis.X
 *   }
 *
 *   // Configure outputs
 *   sprite {
 *     output = "build/sprites.spr"
 *   }
 *
 *   bitmap {
 *     output = "build/charset.chr"
 *   }
 *
 *   // Configure processing
 *   targetFormat = ImageFormat.KOALA
 *   paletteOptimization = PaletteOptimization.QUANTIZE
 * }.build()
 * ```
 */
class ImageStepBuilder(private val name: String) {
  private val inputs = mutableListOf<String>()

  // Transformation configurations (at most one of each type)
  private var cutTransformation: CutTransformation? = null
  private var splitTransformation: SplitTransformation? = null
  private var extendTransformation: ExtendTransformation? = null
  private var flipTransformation: FlipTransformation? = null
  private var reduceResolutionTransformation: ReduceResolutionTransformation? = null

  // Output configurations
  private val spriteOutputs = mutableListOf<ImageSpriteOutput>()
  private val bitmapOutputs = mutableListOf<ImageBitmapOutput>()

  // Processing configuration
  var targetFormat: ImageFormat = ImageFormat.KOALA
  var paletteOptimization: PaletteOptimization = PaletteOptimization.REDUCE_COLORS
  var dithering: DitheringAlgorithm = DitheringAlgorithm.FLOYD_STEINBERG
  var backgroundColor: Int = 0
  var transparencySupport: Boolean = false

  /** Specifies a single input source for this Image step. */
  fun from(path: String) {
    inputs.add(path)
  }

  /** Specifies multiple input sources for this Image step. */
  fun from(vararg paths: String) {
    inputs.addAll(paths)
  }

  /**
   * Configures a cut (crop) transformation to extract a region from the image.
   *
   * At most one cut transformation can be applied per step.
   */
  fun cut(block: CutTransformationBuilder.() -> Unit) {
    val builder = CutTransformationBuilder()
    builder.block()
    cutTransformation = builder.build()
  }

  /**
   * Configures a split transformation to divide the image into sub-images (tiles).
   *
   * At most one split transformation can be applied per step.
   */
  fun split(block: SplitTransformationBuilder.() -> Unit) {
    val builder = SplitTransformationBuilder()
    builder.block()
    splitTransformation = builder.build()
  }

  /**
   * Configures an extend transformation to expand the image canvas.
   *
   * At most one extend transformation can be applied per step.
   */
  fun extend(block: ExtendTransformationBuilder.() -> Unit) {
    val builder = ExtendTransformationBuilder()
    builder.block()
    extendTransformation = builder.build()
  }

  /**
   * Configures a flip transformation to reflect the image.
   *
   * At most one flip transformation can be applied per step.
   */
  fun flip(block: FlipTransformationBuilder.() -> Unit) {
    val builder = FlipTransformationBuilder()
    builder.block()
    flipTransformation = builder.build()
  }

  /**
   * Configures a reduce resolution transformation to scale down the image.
   *
   * At most one reduce resolution transformation can be applied per step.
   */
  fun reduceResolution(block: ReduceResolutionTransformationBuilder.() -> Unit) {
    val builder = ReduceResolutionTransformationBuilder()
    builder.block()
    reduceResolutionTransformation = builder.build()
  }

  /**
   * Configures sprite format output.
   *
   * Multiple sprite outputs can be configured if needed (though typically only one is used).
   */
  fun sprite(block: SpriteOutputBuilder.() -> Unit) {
    val builder = SpriteOutputBuilder()
    builder.block()
    if (builder.output.isNotEmpty()) {
      spriteOutputs.add(ImageSpriteOutput(builder.output))
    }
  }

  /**
   * Configures bitmap format output.
   *
   * Multiple bitmap outputs can be configured if needed (though typically only one is used).
   */
  fun bitmap(block: BitmapOutputBuilder.() -> Unit) {
    val builder = BitmapOutputBuilder()
    builder.block()
    if (builder.output.isNotEmpty()) {
      bitmapOutputs.add(ImageBitmapOutput(builder.output))
    }
  }

  internal fun build(): ImageStep {
    // Collect all transformations in order
    val transformations =
        listOfNotNull(
            cutTransformation,
            splitTransformation,
            extendTransformation,
            flipTransformation,
            reduceResolutionTransformation)

    val imageOutputs =
        ImageOutputs(
            transformations = transformations,
            spriteOutputs = spriteOutputs.toList(),
            bitmapOutputs = bitmapOutputs.toList())

    val config =
        ImageConfig(
            targetFormat = targetFormat,
            paletteOptimization = paletteOptimization,
            dithering = dithering,
            backgroundColor = backgroundColor,
            transparencySupport = transparencySupport)

    return ImageStep(name, inputs.toList(), imageOutputs, config)
  }
}

/** Nested builder for cut (crop) transformation configuration. */
class CutTransformationBuilder {
  var left: Int = 0
  var top: Int = 0
  var width: Int? = null
  var height: Int? = null

  internal fun build(): CutTransformation {
    return CutTransformation(left, top, width, height)
  }
}

/** Nested builder for split transformation configuration. */
class SplitTransformationBuilder {
  var width: Int? = null
  var height: Int? = null

  internal fun build(): SplitTransformation {
    return SplitTransformation(width, height)
  }
}

/** Nested builder for extend transformation configuration. */
class ExtendTransformationBuilder {
  var newWidth: Int? = null
  var newHeight: Int? = null
  var fillColor: Color = Color(0, 0, 0, 255)

  internal fun build(): ExtendTransformation {
    return ExtendTransformation(newWidth, newHeight, fillColor)
  }
}

/** Nested builder for flip transformation configuration. */
class FlipTransformationBuilder {
  var axis: Axis = Axis.Y

  internal fun build(): FlipTransformation {
    return FlipTransformation(axis)
  }
}

/** Nested builder for reduce resolution transformation configuration. */
class ReduceResolutionTransformationBuilder {
  var reduceX: Int = 1
  var reduceY: Int = 1

  internal fun build(): ReduceResolutionTransformation {
    return ReduceResolutionTransformation(reduceX, reduceY)
  }
}

/** Nested builder for sprite output configuration. */
class SpriteOutputBuilder {
  var output: String = ""
}

/** Nested builder for bitmap output configuration. */
class BitmapOutputBuilder {
  var output: String = ""
}
