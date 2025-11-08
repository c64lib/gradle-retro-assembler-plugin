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

import com.github.c64lib.rbt.shared.domain.Axis
import com.github.c64lib.rbt.shared.domain.Color

/**
 * Represents image transformation configurations for the image processing pipeline.
 *
 * Image transformations are composable operations that modify image data. They follow a fixed
 * execution order when chained together:
 * 1. Cut (crop from image)
 * 2. Split (divide into sub-images)
 * 3. Extend (expand canvas)
 * 4. Flip (reflect image)
 * 5. ReduceResolution (scale down)
 *
 * At most one instance of each transformation type can be applied. Transformations are applied
 * sequentially to the image, and split operations expand the image count for all subsequent
 * transformations.
 */

/** Base class for all image transformation configurations */
sealed class ImageTransformation

/**
 * Crop (cut) a region from the image.
 *
 * @param left X offset (pixels from left edge) where cut starts
 * @param top Y offset (pixels from top edge) where cut starts
 * @param width Width of the cut region (null means from left to image right edge)
 * @param height Height of the cut region (null means from top to image bottom edge)
 */
data class CutTransformation(
    val left: Int = 0,
    val top: Int = 0,
    val width: Int? = null,
    val height: Int? = null
) : ImageTransformation()

/**
 * Split image into sub-images (tiles).
 *
 * When split is applied, the single input image becomes multiple output images. All subsequent
 * transformations apply to each split image independently.
 *
 * @param width Width of each split tile (null means use image width - produces 1x1 grid)
 * @param height Height of each split tile (null means use image height - produces 1x1 grid)
 */
data class SplitTransformation(val width: Int? = null, val height: Int? = null) :
    ImageTransformation()

/**
 * Extend (expand) the image canvas.
 *
 * @param newWidth Target width of the extended image (null means keep current width)
 * @param newHeight Target height of the extended image (null means keep current height)
 * @param fillColor Color to fill the extended area (RGBA format)
 */
data class ExtendTransformation(
    val newWidth: Int? = null,
    val newHeight: Int? = null,
    val fillColor: Color = Color(0, 0, 0, 255)
) : ImageTransformation()

/**
 * Flip (reflect) the image.
 *
 * @param axis Direction to flip: X (horizontal), Y (vertical), or BOTH
 */
data class FlipTransformation(val axis: Axis = Axis.Y) : ImageTransformation()

/**
 * Reduce resolution (scale down) the image.
 *
 * @param reduceX Horizontal scale factor (divide width by this value)
 * @param reduceY Vertical scale factor (divide height by this value)
 */
data class ReduceResolutionTransformation(val reduceX: Int = 1, val reduceY: Int = 1) :
    ImageTransformation()

/**
 * Output writer configuration for sprite format.
 *
 * @param output Output file path where the sprite data will be written
 */
data class ImageSpriteOutput(val output: String)

/**
 * Output writer configuration for bitmap format.
 *
 * @param output Output file path where the bitmap data will be written
 */
data class ImageBitmapOutput(val output: String)

/**
 * Container for all image output configurations.
 *
 * Holds lists of transformations to apply and output writer configurations. Transformations are
 * applied in a fixed order regardless of how they appear in the lists.
 */
data class ImageOutputs(
    val transformations: List<ImageTransformation> = emptyList(),
    val spriteOutputs: List<ImageSpriteOutput> = emptyList(),
    val bitmapOutputs: List<ImageBitmapOutput> = emptyList()
) {

  /** Returns all output file paths for dependency tracking and Gradle output configuration. */
  fun getAllOutputPaths(): List<String> {
    return spriteOutputs.map { it.output } + bitmapOutputs.map { it.output }
  }

  /** Checks if any outputs are configured. */
  fun hasOutputs(): Boolean = getAllOutputPaths().isNotEmpty()

  /**
   * Gets transformations in execution order.
   *
   * Transformations are applied in a fixed sequence regardless of their order in the list:
   * 1. Cut
   * 2. Split
   * 3. Extend
   * 4. Flip
   * 5. ReduceResolution
   */
  fun getTransformationsInOrder(): List<ImageTransformation> {
    val ordered = mutableListOf<ImageTransformation>()

    transformations.filterIsInstance<CutTransformation>().firstOrNull()?.let { ordered.add(it) }
    transformations.filterIsInstance<SplitTransformation>().firstOrNull()?.let { ordered.add(it) }
    transformations.filterIsInstance<ExtendTransformation>().firstOrNull()?.let { ordered.add(it) }
    transformations.filterIsInstance<FlipTransformation>().firstOrNull()?.let { ordered.add(it) }
    transformations.filterIsInstance<ReduceResolutionTransformation>().firstOrNull()?.let {
      ordered.add(it)
    }

    return ordered
  }

  /** Returns a human-readable representation of the output configuration. */
  override fun toString(): String {
    return "ImageOutputs(transformations=${transformations.size}, spriteOutputs=${spriteOutputs.size}, bitmapOutputs=${bitmapOutputs.size})"
  }
}
