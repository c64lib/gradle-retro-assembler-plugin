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
import com.github.c64lib.rbt.processors.image.usecase.ReadSourceImageCommand
import com.github.c64lib.rbt.processors.image.usecase.ReadSourceImageUseCase
import com.github.c64lib.rbt.processors.image.usecase.WriteImageCommand
import com.github.c64lib.rbt.processors.image.usecase.WriteImageUseCase
import com.github.c64lib.rbt.processors.image.usecase.WriteMethod
import com.github.c64lib.rbt.shared.gradle.GROUP_BUILD
import com.github.c64lib.rbt.shared.gradle.dsl.ImageTransformationExtension
import com.github.c64lib.rbt.shared.gradle.dsl.PreprocessingExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class ProcessImage : DefaultTask() {

  init {
    description = "Processes PNG Image and convert it into hardware or software sprite shape(s)."
    group = GROUP_BUILD
  }

  @Input lateinit var preprocessingExtension: PreprocessingExtension

  @Internal lateinit var readSourceImageUseCase: ReadSourceImageUseCase

  @Internal lateinit var writeImageUseCase: WriteImageUseCase

  @TaskAction
  fun process() =
      preprocessingExtension.imagePipelines.forEach { pipeline ->
        val inputFile = requireNotNull(pipeline.getInput().get())
        val image = readSourceImageUseCase.apply(ReadSourceImageCommand(inputFile))
        process(image, pipeline)
      }

  private fun process(image: Image, extension: ImageTransformationExtension) {
    extension.spriteWriter?.let {
      writeImageUseCase.apply(
          WriteImageCommand(
              image,
              WriteMethod.SPRITE,
              it.getOutput().get(),
          ),
      )
    }

    extension.bitmapWriter?.let {
      writeImageUseCase.apply(
          WriteImageCommand(
              image,
              WriteMethod.BITMAP,
              it.getOutput().get(),
          ),
      )
    }
  }
}
