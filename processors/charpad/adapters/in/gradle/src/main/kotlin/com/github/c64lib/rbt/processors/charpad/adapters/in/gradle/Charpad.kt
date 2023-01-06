/*
MIT License

Copyright (c) 2018-2023 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.rbt.processors.charpad.adapters.`in`.gradle

import com.github.c64lib.rbt.compilers.kickass.usecase.GenerateKickAssSourceUseCase
import com.github.c64lib.rbt.processors.charpad.domain.CharAttributesProducer
import com.github.c64lib.rbt.processors.charpad.domain.CharColoursProducer
import com.github.c64lib.rbt.processors.charpad.domain.CharMaterialsProducer
import com.github.c64lib.rbt.processors.charpad.domain.CharScreenColoursProducer
import com.github.c64lib.rbt.processors.charpad.domain.CharsetProducer
import com.github.c64lib.rbt.processors.charpad.domain.HeaderProducer
import com.github.c64lib.rbt.processors.charpad.domain.MapCoord
import com.github.c64lib.rbt.processors.charpad.domain.MapProducer
import com.github.c64lib.rbt.processors.charpad.domain.TileColoursProducer
import com.github.c64lib.rbt.processors.charpad.domain.TileProducer
import com.github.c64lib.rbt.processors.charpad.domain.TileScreenColoursProducer
import com.github.c64lib.rbt.processors.charpad.domain.TileTagsProducer
import com.github.c64lib.rbt.processors.charpad.usecase.ProcessCharpadUseCase
import com.github.c64lib.rbt.shared.gradle.GROUP_BUILD
import com.github.c64lib.rbt.shared.gradle.dsl.CharpadPipelineExtension
import com.github.c64lib.rbt.shared.gradle.dsl.OutputsExtension
import com.github.c64lib.rbt.shared.gradle.dsl.PreprocessingExtension
import com.github.c64lib.rbt.shared.processor.BinaryOutputBuffer
import com.github.c64lib.rbt.shared.processor.FisInput
import com.github.c64lib.rbt.shared.processor.TextOutputBuffer
import java.io.FileInputStream
import java.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class Charpad : DefaultTask() {

  init {
    description = "Process Charpad data files (CTM) into a bunch of binary output files."
    group = GROUP_BUILD
  }

  @Input lateinit var preprocessingExtension: PreprocessingExtension

  @TaskAction
  fun process() {
    preprocessingExtension.charpadPipelines.forEach { pipeline ->
      val inputFile = pipeline.getInput().get()
      logger.debug("Found pipeline: input=${pipeline.getInput().get()}.")
      val fis = FileInputStream(inputFile)
      fis.use { pipeline.outputs.forEach { output -> processInput(fis, output, pipeline) } }
    }
  }

  private fun processInput(
      fis: FileInputStream,
      output: OutputsExtension,
      pipeline: CharpadPipelineExtension
  ) {
    val buffers: MutableList<BinaryOutputBuffer> = LinkedList()
    val textBuffers: MutableList<TextOutputBuffer> = LinkedList()
    val processor =
        ProcessCharpadUseCase(
            producers(output, buffers, textBuffers, pipeline),
            pipeline.getCtm8PrototypeCompatibility().getOrElse(false))
    processor.apply(FisInput(fis))
    buffers.forEach { it.flush() }
    textBuffers.forEach { it.flush() }
  }

  private fun producers(
      output: OutputsExtension,
      buffers: MutableList<BinaryOutputBuffer>,
      textBuffers: MutableList<TextOutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      charsetProducers(output, buffers, pipeline) +
          charsetAttributesProducers(output, buffers, pipeline) +
          charsetMaterialsProducers(output, buffers, pipeline) +
          charsetColoursProducer(output, buffers, pipeline) +
          charsetScreenColoursProducer(output, buffers, pipeline) +
          tileProducers(output, buffers, pipeline) +
          tileTagsProducers(output, buffers, pipeline) +
          tileColoursProducers(output, buffers, pipeline) +
          tileScreenColoursProducers(output, buffers, pipeline) +
          mapProducers(output, buffers, pipeline) +
          metaProducers(output, textBuffers, pipeline)

  private fun metaProducers(
      output: OutputsExtension,
      textBuffers: MutableList<TextOutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.meta.map { meta ->
        val textOutput = meta.resolveOutput(textBuffers, useBuildDir(pipeline))
        val srcWriter = GenerateKickAssSourceUseCase(textOutput)
        HeaderProducer(output = CharpadMetaOutput(srcWriter, meta))
      }

  private fun charsetProducers(
      output: OutputsExtension,
      buffers: MutableList<BinaryOutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.charsets.map { charset ->
        CharsetProducer(
            start = charset.start,
            end = charset.end,
            output = charset.resolveOutput(buffers, useBuildDir(pipeline)))
      }

  private fun charsetAttributesProducers(
      output: OutputsExtension,
      buffers: MutableList<BinaryOutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.charsetAttributes.map { charsetAttributes ->
        CharAttributesProducer(
            start = charsetAttributes.start,
            end = charsetAttributes.end,
            output = charsetAttributes.resolveOutput(buffers, useBuildDir(pipeline)))
      }

  private fun charsetColoursProducer(
      output: OutputsExtension,
      buffers: MutableList<BinaryOutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.charsetColours.map { charsetColours ->
        CharColoursProducer(
            start = charsetColours.start,
            end = charsetColours.end,
            output = charsetColours.resolveOutput(buffers, useBuildDir(pipeline)))
      }

  private fun charsetScreenColoursProducer(
      output: OutputsExtension,
      buffers: MutableList<BinaryOutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.charsetScreenColours.map { charsetScreenColours ->
        CharScreenColoursProducer(
            start = charsetScreenColours.start,
            end = charsetScreenColours.end,
            output = charsetScreenColours.resolveOutput(buffers, useBuildDir(pipeline)))
      }

  private fun charsetMaterialsProducers(
      output: OutputsExtension,
      buffers: MutableList<BinaryOutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.charsetMaterials.map { charsetMaterials ->
        CharMaterialsProducer(
            start = charsetMaterials.start,
            end = charsetMaterials.end,
            output = charsetMaterials.resolveOutput(buffers, useBuildDir(pipeline)))
      }

  private fun tileProducers(
      output: OutputsExtension,
      buffers: MutableList<BinaryOutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.tiles.map { tile ->
        TileProducer(
            start = tile.start,
            end = tile.end,
            output = tile.resolveOutput(buffers, useBuildDir(pipeline)))
      }

  private fun tileTagsProducers(
      output: OutputsExtension,
      buffers: MutableList<BinaryOutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.tileTags.map { tileTags ->
        TileTagsProducer(
            start = tileTags.start,
            end = tileTags.end,
            output = tileTags.resolveOutput(buffers, useBuildDir(pipeline)))
      }

  private fun tileColoursProducers(
      output: OutputsExtension,
      buffers: MutableList<BinaryOutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.tileColours.map { tileAttr ->
        TileColoursProducer(
            start = tileAttr.start,
            end = tileAttr.end,
            output = tileAttr.resolveOutput(buffers, useBuildDir(pipeline)))
      }

  private fun tileScreenColoursProducers(
      output: OutputsExtension,
      buffers: MutableList<BinaryOutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.tileScreenColours.map { tileScreenColours ->
        TileScreenColoursProducer(
            start = tileScreenColours.start,
            end = tileScreenColours.end,
            output = tileScreenColours.resolveOutput(buffers, useBuildDir(pipeline)))
      }

  private fun mapProducers(
      output: OutputsExtension,
      buffers: MutableList<BinaryOutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.maps.map { map ->
        MapProducer(
            leftTop = MapCoord(map.left, map.top),
            rightBottom = MapCoord(map.right, map.bottom),
            output = map.resolveOutput(buffers, useBuildDir(pipeline)))
      }

  private fun useBuildDir(pipeline: CharpadPipelineExtension): Boolean =
      pipeline.getUseBuildDir().getOrElse(true)
}
