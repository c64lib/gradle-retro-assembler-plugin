/*
MIT License

Copyright (c) 2018 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.gradle.preprocess.charpad

import com.github.c64lib.gradle.GROUP_BUILD
import com.github.c64lib.gradle.preprocess.FisInput
import com.github.c64lib.gradle.preprocess.OutputBuffer
import com.github.c64lib.gradle.preprocess.PreprocessingExtension
import com.github.c64lib.retroassembler.charpad_processor.CharAttributesProducer
import com.github.c64lib.retroassembler.charpad_processor.CharpadProcessor
import com.github.c64lib.retroassembler.charpad_processor.CharsetProducer
import com.github.c64lib.retroassembler.charpad_processor.MapCoord
import com.github.c64lib.retroassembler.charpad_processor.MapProducer
import com.github.c64lib.retroassembler.charpad_processor.TileColoursProducer
import com.github.c64lib.retroassembler.charpad_processor.TileProducer
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
      fis: FileInputStream, output: OutputsExtension, pipeline: CharpadPipelineExtension
  ) {
    val buffers: MutableList<OutputBuffer> = LinkedList()
    val processor = CharpadProcessor(producers(output, buffers, pipeline))
    processor.process(FisInput(fis))
    buffers.forEach { it.flush() }
  }

  private fun producers(
      output: OutputsExtension,
      buffers: MutableList<OutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      charsetProducers(output, buffers, pipeline) +
          charsetAttributesProducers(output, buffers, pipeline) +
          tileProducers(output, buffers, pipeline) +
          tileColoursProducers(output, buffers, pipeline) +
          mapProducers(output, buffers, pipeline)

  private fun charsetProducers(
      output: OutputsExtension,
      buffers: MutableList<OutputBuffer>,
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
      buffers: MutableList<OutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.charsetAttributes.map { charsetAttributes ->
        CharAttributesProducer(
            start = charsetAttributes.start,
            end = charsetAttributes.end,
            output = charsetAttributes.resolveOutput(buffers, useBuildDir(pipeline)))
      }

  private fun tileProducers(
      output: OutputsExtension,
      buffers: MutableList<OutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.tiles.map { tile ->
        TileProducer(
            start = tile.start,
            end = tile.end,
            output = tile.resolveOutput(buffers, useBuildDir(pipeline)))
      }

  private fun tileColoursProducers(
      output: OutputsExtension,
      buffers: MutableList<OutputBuffer>,
      pipeline: CharpadPipelineExtension
  ) =
      output.tileColours.map { tileAttr ->
        TileColoursProducer(
            start = tileAttr.start,
            end = tileAttr.end,
            output = tileAttr.resolveOutput(buffers, useBuildDir(pipeline)))
      }

  private fun mapProducers(
      output: OutputsExtension,
      buffers: MutableList<OutputBuffer>,
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
