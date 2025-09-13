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
package com.github.c64lib.rbt.flows.adapters.out.charpad

import com.github.c64lib.rbt.flows.domain.config.CharpadCommand
import com.github.c64lib.rbt.processors.charpad.domain.*
import com.github.c64lib.rbt.shared.processor.Output
import com.github.c64lib.rbt.shared.processor.OutputProducer
import com.github.c64lib.rbt.shared.processor.TextOutput
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

/**
 * Factory for creating OutputProducer instances based on CharpadCommand configuration.
 *
 * This factory converts flows domain CharpadConfig and output file specifications into the complete
 * collection of OutputProducer instances that ProcessCharpadUseCase expects, supporting all
 * existing producers: charset, map, tiles, attributes, colors, materials, and metadata outputs.
 */
class CharpadOutputProducerFactory {

  fun createOutputProducers(command: CharpadCommand): Collection<OutputProducer<*>> {
    val producers = mutableListOf<OutputProducer<*>>()

    command.outputFiles.forEach { (outputKey, outputFile) ->
      when (outputKey.lowercase()) {
        "charset" -> {
          if (command.config.generateCharset) {
            producers.add(
                CharsetProducer(start = 0, end = 65536, output = createBinaryOutput(outputFile)))
          }
        }
        "map" -> {
          if (command.config.generateMap) {
            producers.add(
                MapProducer(
                    leftTop = MapCoord(0, 0),
                    rightBottom = MapCoord(39, 24), // Default C64 screen size
                    output = createBinaryOutput(outputFile)))
          }
        }
        "tiles" -> {
          producers.add(
              TileProducer(start = 0, end = 65536, output = createBinaryOutput(outputFile)))
        }
        "charattributes",
        "char-attributes" -> {
          producers.add(
              CharAttributesProducer(
                  start = 0, end = 65536, output = createBinaryOutput(outputFile)))
        }
        "charcolours",
        "char-colours" -> {
          producers.add(
              CharColoursProducer(start = 0, end = 65536, output = createBinaryOutput(outputFile)))
        }
        "charmaterials",
        "char-materials" -> {
          producers.add(
              CharMaterialsProducer(
                  start = 0, end = 65536, output = createBinaryOutput(outputFile)))
        }
        "charscreencolours",
        "char-screen-colours" -> {
          producers.add(
              CharScreenColoursProducer(
                  start = 0, end = 65536, output = createBinaryOutput(outputFile)))
        }
        "tiletags",
        "tile-tags" -> {
          producers.add(
              TileTagsProducer(start = 0, end = 65536, output = createBinaryOutput(outputFile)))
        }
        "tilecolours",
        "tile-colours" -> {
          producers.add(
              TileColoursProducer(start = 0, end = 65536, output = createBinaryOutput(outputFile)))
        }
        "tilescreencolours",
        "tile-screen-colours" -> {
          producers.add(
              TileScreenColoursProducer(
                  start = 0, end = 65536, output = createBinaryOutput(outputFile)))
        }
        "header",
        "metadata" -> {
          producers.add(HeaderProducer(output = createHeaderOutput(outputFile, command)))
        }
      }
    }

    return producers
  }

  private fun createBinaryOutput(outputFile: File): Output<ByteArray> {
    return object : Output<ByteArray> {
      override fun write(data: ByteArray) {
        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { it.write(data) }
      }
    }
  }

  private fun createTextOutput(outputFile: File): TextOutput {
    return object : TextOutput {
      override fun write(data: String) {
        outputFile.parentFile?.mkdirs()
        FileWriter(outputFile, true).use { it.write(data) }
      }

      override fun writeLn(data: String) {
        write(data + System.lineSeparator())
      }
    }
  }

  private fun createHeaderOutput(outputFile: File, command: CharpadCommand): Output<CTMHeader> {
    return object : Output<CTMHeader> {
      override fun write(data: CTMHeader) {
        val textOutput = createTextOutput(outputFile)

        // Generate metadata header based on configuration
        val config = command.config
        val namespace = if (config.namespace.isNotEmpty()) config.namespace else ""
        val prefix = if (config.prefix.isNotEmpty()) config.prefix else ""

        if (config.includeVersion) {
          textOutput.writeLn("// CTM Version: ${data.version}")
        }

        if (config.includeBgColours) {
          textOutput.writeLn(
              "// Background Colors: ${data.backgroundColour0}, ${data.backgroundColour1}, ${data.backgroundColour2}, ${data.backgroundColour3}")
        }

        if (config.includeCharColours) {
          textOutput.writeLn("// Character Color: ${data.charColour}")
        }

        if (config.includeMode) {
          textOutput.writeLn("// Screen Mode: ${data.screenMode}")
          textOutput.writeLn("// Coloring Method: ${data.colouringMethod}")
        }

        // Write header constants with namespace/prefix
        val fullPrefix = if (namespace.isNotEmpty()) "${namespace}_${prefix}" else prefix
        textOutput.writeLn("${fullPrefix}MAP_WIDTH = ${data.mapDimensions.width}")
        textOutput.writeLn("${fullPrefix}MAP_HEIGHT = ${data.mapDimensions.height}")
        data.tileDimensions?.let { tileDims ->
          textOutput.writeLn("${fullPrefix}TILE_WIDTH = ${tileDims.width}")
          textOutput.writeLn("${fullPrefix}TILE_HEIGHT = ${tileDims.height}")
        }
      }
    }
  }
}
