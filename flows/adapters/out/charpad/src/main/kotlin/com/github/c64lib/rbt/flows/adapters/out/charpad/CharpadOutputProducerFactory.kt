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
import com.github.c64lib.rbt.flows.domain.config.FilterConfig
import com.github.c64lib.rbt.flows.domain.config.MetadataOutput
import com.github.c64lib.rbt.processors.charpad.domain.*
import com.github.c64lib.rbt.shared.gradle.fllter.BinaryInterleaver as BinaryInterleaverImpl
import com.github.c64lib.rbt.shared.gradle.fllter.Nybbler as NybblerImpl
import com.github.c64lib.rbt.shared.processor.Output
import com.github.c64lib.rbt.shared.processor.OutputProducer
import com.github.c64lib.rbt.shared.processor.TextOutput
import io.vavr.collection.List as VavrList
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

/**
 * Factory for creating OutputProducer instances based on CharpadCommand configuration.
 *
 * This factory converts flows domain CharpadOutputs and configuration into the complete collection
 * of OutputProducer instances that ProcessCharpadUseCase expects, supporting all existing producers
 * with their dedicated configurations (start/end ranges, map regions, metadata parameters).
 */
class CharpadOutputProducerFactory {

  fun createOutputProducers(command: CharpadCommand): Collection<OutputProducer<*>> {
    val producers = mutableListOf<OutputProducer<*>>()
    val outputs = command.charpadOutputs

    // Create charset producers from dedicated charset outputs
    outputs.charsets.forEach { charsetOutput ->
      val file = resolveOutputFile(charsetOutput.output, command.projectRootDir)
      producers.add(
          CharsetProducer(
              start = charsetOutput.start,
              end = charsetOutput.end,
              output = createBinaryOutput(file, charsetOutput.filter, command.projectRootDir)))
    }

    // Create charset attributes producers
    outputs.charAttributes.forEach { attrOutput ->
      val file = resolveOutputFile(attrOutput.output, command.projectRootDir)
      producers.add(
          CharAttributesProducer(
              start = attrOutput.start,
              end = attrOutput.end,
              output = createBinaryOutput(file, attrOutput.filter, command.projectRootDir)))
    }

    // Create charset colours producers
    outputs.charColours.forEach { colourOutput ->
      val file = resolveOutputFile(colourOutput.output, command.projectRootDir)
      producers.add(
          CharColoursProducer(
              start = colourOutput.start,
              end = colourOutput.end,
              output = createBinaryOutput(file, colourOutput.filter, command.projectRootDir)))
    }

    // Create charset materials producers
    outputs.charMaterials.forEach { materialOutput ->
      val file = resolveOutputFile(materialOutput.output, command.projectRootDir)
      producers.add(
          CharMaterialsProducer(
              start = materialOutput.start,
              end = materialOutput.end,
              output = createBinaryOutput(file, materialOutput.filter, command.projectRootDir)))
    }

    // Create charset screen colours producers
    outputs.charScreenColours.forEach { screenColourOutput ->
      val file = resolveOutputFile(screenColourOutput.output, command.projectRootDir)
      producers.add(
          CharScreenColoursProducer(
              start = screenColourOutput.start,
              end = screenColourOutput.end,
              output = createBinaryOutput(file, screenColourOutput.filter, command.projectRootDir)))
    }

    // Create tiles producers
    outputs.tiles.forEach { tileOutput ->
      val file = resolveOutputFile(tileOutput.output, command.projectRootDir)
      producers.add(
          TileProducer(
              start = tileOutput.start,
              end = tileOutput.end,
              output = createBinaryOutput(file, tileOutput.filter, command.projectRootDir)))
    }

    // Create tile tags producers
    outputs.tileTags.forEach { tileTagOutput ->
      val file = resolveOutputFile(tileTagOutput.output, command.projectRootDir)
      producers.add(
          TileTagsProducer(
              start = tileTagOutput.start,
              end = tileTagOutput.end,
              output = createBinaryOutput(file, tileTagOutput.filter, command.projectRootDir)))
    }

    // Create tile colours producers
    outputs.tileColours.forEach { tileColourOutput ->
      val file = resolveOutputFile(tileColourOutput.output, command.projectRootDir)
      producers.add(
          TileColoursProducer(
              start = tileColourOutput.start,
              end = tileColourOutput.end,
              output = createBinaryOutput(file, tileColourOutput.filter, command.projectRootDir)))
    }

    // Create tile screen colours producers
    outputs.tileScreenColours.forEach { tileScreenColourOutput ->
      val file = resolveOutputFile(tileScreenColourOutput.output, command.projectRootDir)
      producers.add(
          TileScreenColoursProducer(
              start = tileScreenColourOutput.start,
              end = tileScreenColourOutput.end,
              output =
                  createBinaryOutput(file, tileScreenColourOutput.filter, command.projectRootDir)))
    }

    // Create map producers
    outputs.maps.forEach { mapOutput ->
      val file = resolveOutputFile(mapOutput.output, command.projectRootDir)
      producers.add(
          MapProducer(
              leftTop = MapCoord(mapOutput.left, mapOutput.top),
              rightBottom = MapCoord(mapOutput.right, mapOutput.bottom),
              output = createBinaryOutput(file, mapOutput.filter, command.projectRootDir)))
    }

    // Create metadata/header producers
    outputs.metadata.forEach { metadataOutput ->
      val file = resolveOutputFile(metadataOutput.output, command.projectRootDir)
      producers.add(
          HeaderProducer(output = createHeaderOutput(file, command.config, metadataOutput)))
    }

    return producers
  }

  private fun resolveOutputFile(outputPath: String, projectRootDir: File): File {
    return if (File(outputPath).isAbsolute) {
      File(outputPath)
    } else {
      File(projectRootDir, outputPath)
    }
  }

  private fun createBinaryOutput(
      outputFile: File,
      filter: FilterConfig,
      projectRootDir: File
  ): Output<ByteArray> {
    val baseOutput =
        object : Output<ByteArray> {
          override fun write(data: ByteArray) {
            outputFile.parentFile?.mkdirs()
            FileOutputStream(outputFile).use { it.write(data) }
          }
        }

    return when (filter) {
      is FilterConfig.Nybbler -> {
        val lo =
            filter.loOutput?.let { path ->
              object : Output<ByteArray> {
                override fun write(data: ByteArray) {
                  val file = resolveOutputFile(path, projectRootDir)
                  file.parentFile?.mkdirs()
                  FileOutputStream(file).use { it.write(data) }
                }
              }
            }
        val hi =
            filter.hiOutput?.let { path ->
              object : Output<ByteArray> {
                override fun write(data: ByteArray) {
                  val file = resolveOutputFile(path, projectRootDir)
                  file.parentFile?.mkdirs()
                  FileOutputStream(file).use { it.write(data) }
                }
              }
            }
        NybblerImpl(lo, hi, filter.normalizeHi)
      }
      is FilterConfig.Interleaver -> {
        val outputs =
            filter.outputs.map { path ->
              object : Output<ByteArray> {
                override fun write(data: ByteArray) {
                  val file = resolveOutputFile(path, projectRootDir)
                  file.parentFile?.mkdirs()
                  FileOutputStream(file).use { it.write(data) }
                }
              }
            }
        BinaryInterleaverImpl(VavrList.ofAll(outputs))
      }
      FilterConfig.None -> baseOutput
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

  private fun createHeaderOutput(
      outputFile: File,
      config: com.github.c64lib.rbt.flows.domain.config.CharpadConfig,
      metadataOutput: MetadataOutput
  ): Output<CTMHeader> {
    return object : Output<CTMHeader> {
      override fun write(data: CTMHeader) {
        val textOutput = createTextOutput(outputFile)

        // Use metadata-specific configuration (overrides global config)
        val namespace = metadataOutput.namespace.ifEmpty { config.namespace }
        val prefix = metadataOutput.prefix.ifEmpty { config.prefix }

        if (metadataOutput.includeVersion) {
          textOutput.writeLn("// CTM Version: ${data.version}")
        }

        if (metadataOutput.includeBgColours) {
          textOutput.writeLn(
              "// Background Colors: ${data.backgroundColour0}, ${data.backgroundColour1}, ${data.backgroundColour2}, ${data.backgroundColour3}")
        }

        if (metadataOutput.includeCharColours) {
          textOutput.writeLn("// Character Color: ${data.charColour}")
        }

        if (metadataOutput.includeMode) {
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
