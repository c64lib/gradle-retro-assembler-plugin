/*
MIT License

Copyright (c) 2018-2023 c64lib: The Ultimate Commodore 64 Library
Copyright (c) 2018-2023 Maciej Małecki

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
package com.github.c64lib.rbt.processors.image.adapters.`in`.gradle

import com.github.c64lib.rbt.processors.image.domain.Image
import com.github.c64lib.rbt.processors.image.usecase.CutImageCommand
import com.github.c64lib.rbt.processors.image.usecase.CutImageUseCase
import com.github.c64lib.rbt.processors.image.usecase.ExtendImageCommand
import com.github.c64lib.rbt.processors.image.usecase.ExtendImageUseCase
import com.github.c64lib.rbt.processors.image.usecase.FlipImageCommand
import com.github.c64lib.rbt.processors.image.usecase.FlipImageUseCase
import com.github.c64lib.rbt.processors.image.usecase.ReadSourceImageCommand
import com.github.c64lib.rbt.processors.image.usecase.ReadSourceImageUseCase
import com.github.c64lib.rbt.processors.image.usecase.ReduceResolutionCommand
import com.github.c64lib.rbt.processors.image.usecase.ReduceResolutionUseCase
import com.github.c64lib.rbt.processors.image.usecase.SplitImageCommand
import com.github.c64lib.rbt.processors.image.usecase.SplitImageUseCase
import com.github.c64lib.rbt.processors.image.usecase.WriteImageCommand
import com.github.c64lib.rbt.processors.image.usecase.WriteImageUseCase
import com.github.c64lib.rbt.processors.image.usecase.WriteMethod
import com.github.c64lib.rbt.shared.gradle.GROUP_BUILD
import com.github.c64lib.rbt.shared.gradle.dsl.ImageCutExtension
import com.github.c64lib.rbt.shared.gradle.dsl.ImageExtendExtension
import com.github.c64lib.rbt.shared.gradle.dsl.ImageFlipExtension
import com.github.c64lib.rbt.shared.gradle.dsl.ImageReduceResolutionExtension
import com.github.c64lib.rbt.shared.gradle.dsl.ImageSplitExtension
import com.github.c64lib.rbt.shared.gradle.dsl.ImageTransformationExtension
import com.github.c64lib.rbt.shared.gradle.dsl.PreprocessingExtension
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

open class ProcessImage : DefaultTask() {

  init {
    description = "Processes PNG Image and convert it into hardware or software sprite shape(s)."
    group = GROUP_BUILD
  }

  @Input lateinit var preprocessingExtension: PreprocessingExtension

  @Internal lateinit var readSourceImageUseCase: ReadSourceImageUseCase

  @Internal lateinit var writeImageUseCase: WriteImageUseCase

  @Internal lateinit var cutImageUseCase: CutImageUseCase

  @Internal lateinit var extendImageUseCase: ExtendImageUseCase

  @Internal lateinit var splitImageUseCase: SplitImageUseCase

  @Internal lateinit var flipImageUseCase: FlipImageUseCase

  @Internal lateinit var reduceResolutionUseCase: ReduceResolutionUseCase

  @TaskAction
  fun process() =
      preprocessingExtension.imagePipelines.forEach { pipeline ->
        val inputFile = requireNotNull(pipeline.getInput().get())
        val image = readSourceImageUseCase.apply(ReadSourceImageCommand(inputFile))
        process(arrayOf(image), pipeline, pipeline.getUseBuildDir().get() ?: true)
      }

  private fun process(
      images: Array<Image>,
      extension: ImageTransformationExtension,
      useBuildDir: Boolean
  ) {

    val postImages: Array<Image> =
        images
            .flatMap {
              when (extension) {
                is ImageFlipExtension ->
                    listOf(
                        flipImageUseCase.apply(FlipImageCommand(image = it, axis = extension.axis)),
                    )
                is ImageCutExtension ->
                    listOf(
                        cutImageUseCase.apply(
                            CutImageCommand(
                                image = it,
                                left = extension.left,
                                top = extension.top,
                                width = extension.width ?: (it.width - extension.left),
                                height = extension.height ?: (it.height - extension.top),
                            ),
                        ),
                    )
                is ImageExtendExtension ->
                    listOf(
                        extendImageUseCase.apply(
                            ExtendImageCommand(
                                image = it,
                                newWidth = extension.newWidth ?: it.width,
                                newHeight = extension.newHeight ?: it.height,
                                fillColor = extension.fillColor,
                            ),
                        ),
                    )
                is ImageSplitExtension ->
                    splitImageUseCase
                        .apply(
                            SplitImageCommand(
                                image = it,
                                subImageWidth = extension.width ?: it.width,
                                subImageHeight = extension.height ?: it.height,
                            ),
                        )
                        .toList()
                is ImageReduceResolutionExtension ->
                    listOf(
                        reduceResolutionUseCase.apply(
                            ReduceResolutionCommand(
                                image = it,
                                reduceX = extension.reduceX,
                                reduceY = extension.reduceY,
                            ),
                        ),
                    )
                else -> listOf(it)
              }
            }
            .toTypedArray()

    extension.cut?.let { process(postImages, it, useBuildDir) }
    extension.split?.let { process(postImages, it, useBuildDir) }
    extension.extend?.let { process(postImages, it, useBuildDir) }
    extension.flip?.let { process(postImages, it, useBuildDir) }
    extension.reduceResolution?.let { process(postImages, it, useBuildDir) }

    extension.spriteWriter?.let {
      postImages.forEachIndexed { i, image ->
        writeImageUseCase.apply(
            WriteImageCommand(
                image,
                WriteMethod.SPRITE,
                toIndexedName(it.getOutput().get(), i, images),
                useBuildDir,
            ),
        )
      }
    }

    extension.bitmapWriter?.let {
      postImages.forEachIndexed { i, image ->
        writeImageUseCase.apply(
            WriteImageCommand(
                image,
                WriteMethod.BITMAP,
                toIndexedName(it.getOutput().get(), i, images),
                useBuildDir,
            ),
        )
      }
    }
  }

  private fun toIndexedName(file: File, index: Int, array: Array<Image>): File =
      when (array.size) {
        1 -> file
        0 -> throw GradleException("No images to process")
        else -> File(file.parent, "${file.nameWithoutExtension}_${index}.${file.extension}")
      }
}
