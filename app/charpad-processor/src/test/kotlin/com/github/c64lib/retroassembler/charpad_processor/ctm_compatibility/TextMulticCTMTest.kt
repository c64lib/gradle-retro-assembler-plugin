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
package com.github.c64lib.retroassembler.charpad_processor.ctm_compatibility

import com.github.c64lib.rbt.shared.binutils.BinaryOutputMock
import com.github.c64lib.rbt.shared.binutils.InputByteStreamAdapter
import com.github.c64lib.rbt.shared.binutils.OutputMock
import com.github.c64lib.retroassembler.charpad_processor.CharpadProcessor
import com.github.c64lib.retroassembler.charpad_processor.model.CTMHeader
import com.github.c64lib.retroassembler.charpad_processor.model.MapCoord
import com.github.c64lib.retroassembler.charpad_processor.producer.CharAttributesProducer
import com.github.c64lib.retroassembler.charpad_processor.producer.CharColoursProducer
import com.github.c64lib.retroassembler.charpad_processor.producer.CharMaterialsProducer
import com.github.c64lib.retroassembler.charpad_processor.producer.CharsetProducer
import com.github.c64lib.retroassembler.charpad_processor.producer.HeaderProducer
import com.github.c64lib.retroassembler.charpad_processor.producer.MapProducer
import com.github.c64lib.retroassembler.charpad_processor.producer.TileColoursProducer
import com.github.c64lib.retroassembler.charpad_processor.producer.TileProducer
import com.github.c64lib.retroassembler.charpad_processor.producer.TileTagsProducer
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class TextMulticCTMTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      val supportedVersions = listOf(5, 6, 7, 8)
      val tileTagsSupportingVersions = listOf(6, 7, 8)

      val expectedCharsetData =
          byteArrayOf(0x00, 0x7E, 0x42, 0x00, 0x00, 0x42, 0x7E, 0x00) +
              byteArrayOf(0x00, 0x28, 0x7E, 0x7E, 0x7E, 0x7E, 0x14, 0x00) +
              byteArrayOf(0x00, 0x7E, 0x7E, 0x00, 0x00, 0x7E, 0x7E, 0x00) +
              byteArrayOf(0x00, 0x14, 0x7E, 0x7E, 0x7E, 0x7E, 0x28, 0x00)

      val expectedCharsetColoursData = byteArrayOf(6, 11, 10, 13)

      val expectedMaterialData = byteArrayOf(0x0, 0x4, 0x8, 0xC)

      val expectedCharsetAttributesData = byteArrayOf(0x06, 0x4B, 0x8A.toByte(), 0xCD.toByte())

      val expectedTilesetData =
          byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00) +
              byteArrayOf(0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00) +
              byteArrayOf(0x03, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00)

      val expectedTileTagsData = byteArrayOf(0, 2, 4)

      val charsetOutput = BinaryOutputMock()
      val charsetColourOutput = BinaryOutputMock()
      val charsetAttributesOutput = BinaryOutputMock()
      val charsetMaterialOutput = BinaryOutputMock()
      val tilesetOutput = BinaryOutputMock()
      val tileTagsOutput = BinaryOutputMock()
      val tileColoursOutput = BinaryOutputMock()
      val mapOutput = BinaryOutputMock()
      val headerOutput = OutputMock<CTMHeader>()

      val processor =
          CharpadProcessor(
              listOf(
                  HeaderProducer(output = headerOutput),
                  CharsetProducer(output = charsetOutput),
                  CharColoursProducer(output = charsetColourOutput),
                  CharAttributesProducer(output = charsetAttributesOutput),
                  CharMaterialsProducer(output = charsetMaterialOutput),
                  TileProducer(output = tilesetOutput),
                  TileColoursProducer(output = tileColoursOutput),
                  TileTagsProducer(output = tileTagsOutput),
                  MapProducer(
                      leftTop = MapCoord(0, 0),
                      rightBottom = MapCoord(40, 25),
                      output = mapOutput)),
              true)

      supportedVersions.forEach { ctmVersion ->
        val input =
            InputByteStreamAdapter(
                this.javaClass.getResourceAsStream(
                    "/text-multic/text-mc-per-char-notiles-ctm$ctmVersion.ctm")!!)

        Given("[CTM v$ctmVersion] with per char colouring method and no tile set") {
          When("process is called") {
            processor.process(input)

            Then("CTM version matches") { headerOutput.data?.version?.toInt() shouldBe ctmVersion }
            Then("charset content is read") { charsetOutput.bytes shouldBe expectedCharsetData }
            Then("char colours are read") {
              charsetColourOutput.bytes shouldBe expectedCharsetColoursData
            }
            Then("char attributes are read") {
              charsetAttributesOutput.bytes shouldBe expectedCharsetAttributesData
            }
            Then("char materials are read") {
              charsetMaterialOutput.bytes shouldBe expectedMaterialData
            }
            Then("no tiles are produced") { tilesetOutput.bytes.size shouldBe 0 }
            Then("no tile colours are produced") { tileColoursOutput.bytes.size shouldBe 0 }
            Then("no tile tags are produced") { tileTagsOutput.bytes.size shouldBe 0 }
            Then("map is read") {
              mapOutput.bytes.copyOfRange(0, 8) shouldBe
                  byteArrayOf(0x00, 0x00, 0x01, 0x00, 0x03, 0x00, 0x02, 0x00)
              mapOutput.bytes.copyOfRange(2000 - 6, 2000) shouldBe
                  byteArrayOf(0x03, 0x00, 0x02, 0x00, 0x01, 0x00)
              mapOutput.bytes.copyOfRange(8, 2000 - 6).filter { it != 0x00.toByte() }.size shouldBe
                  0
            }
          }
        }
      }

      supportedVersions.forEach { ctmVersion ->
        val input =
            InputByteStreamAdapter(
                this.javaClass.getResourceAsStream(
                    "/text-multic/text-mc-per-char-tiles-ctm$ctmVersion.ctm")!!)

        Given("[CTM v$ctmVersion] with per char colouring method and with tile set") {
          When("process is called") {
            processor.process(input)

            Then("CTM version matches") { headerOutput.data?.version?.toInt() shouldBe ctmVersion }
            Then("charset content is read") { charsetOutput.bytes shouldBe expectedCharsetData }
            Then("char colours are read") {
              charsetColourOutput.bytes shouldBe expectedCharsetColoursData
            }
            Then("char attributes are read") {
              charsetAttributesOutput.bytes shouldBe expectedCharsetAttributesData
            }
            Then("char materials are read") {
              charsetMaterialOutput.bytes shouldBe expectedMaterialData
            }
            Then("tiles are read") { tilesetOutput.bytes shouldBe expectedTilesetData }
            Then("tile colours are empty") { tileColoursOutput.bytes.size shouldBe 0 }
            Then("map is read") {
              mapOutput.bytes.copyOfRange(0, 4) shouldBe byteArrayOf(0x01, 0x00, 0x02, 0x00)
              mapOutput.bytes.copyOfRange(480 - 4, 480) shouldBe byteArrayOf(0x02, 0x00, 0x01, 0x00)
              mapOutput.bytes.copyOfRange(4, 480 - 4).filter { it != 0.toByte() }.size shouldBe 0
            }
          }
        }
      }

      tileTagsSupportingVersions.forEach { ctmVersion ->
        val input =
            InputByteStreamAdapter(
                this.javaClass.getResourceAsStream(
                    "/text-multic/text-mc-per-char-tiles-ctm$ctmVersion.ctm")!!)

        Given("[CTM v$ctmVersion] with per char colouring method and with tile set") {
          When("process is called") {
            processor.process(input)

            Then("tile tags are read") { tileTagsOutput.bytes shouldBe expectedTileTagsData }
          }
        }
      }

      supportedVersions.forEach { ctmVersion ->
        val input =
            InputByteStreamAdapter(
                this.javaClass.getResourceAsStream(
                    "/text-multic/text-mc-per-tile-ctm$ctmVersion.ctm")!!)

        Given("[CTM v$ctmVersion] with per tile colouring method and with tile set") {
          When("process is called") {
            processor.process(input)

            Then("CTM version matches") { headerOutput.data?.version?.toInt() shouldBe ctmVersion }
            Then("charset content is read") { charsetOutput.bytes shouldBe expectedCharsetData }
            Then("char colours are empty") { charsetColourOutput.bytes.size shouldBe 0 }
            Then("char attributes are read") {
              charsetAttributesOutput.bytes shouldBe
                  expectedCharsetAttributesData.map { (it.toInt() and 0xF0).toByte() }.toByteArray()
            }
            Then("char materials are read") {
              charsetMaterialOutput.bytes shouldBe expectedMaterialData
            }
            Then("tiles are read") { tilesetOutput.bytes shouldBe expectedTilesetData }
            Then("tiles colours are read") {
              tileColoursOutput.bytes shouldBe byteArrayOf(0x06, 0x0A, 0x0D)
            }
            Then("map is read") {
              mapOutput.bytes.copyOfRange(0, 4) shouldBe byteArrayOf(0x01, 0x00, 0x02, 0x00)
              mapOutput.bytes.copyOfRange(480 - 4, 480) shouldBe byteArrayOf(0x02, 0x00, 0x01, 0x00)
              mapOutput.bytes.copyOfRange(4, 480 - 4).filter { it != 0.toByte() }.size shouldBe 0
            }
          }
        }
      }
    })
