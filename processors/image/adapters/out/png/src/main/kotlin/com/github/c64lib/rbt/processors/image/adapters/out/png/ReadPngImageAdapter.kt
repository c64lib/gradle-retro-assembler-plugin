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
package com.github.c64lib.rbt.processors.image.adapters.out.png

import ar.com.hjg.pngj.ImageInfo
import ar.com.hjg.pngj.ImageLineByte
import ar.com.hjg.pngj.ImageLineHelper
import ar.com.hjg.pngj.ImageLineInt
import ar.com.hjg.pngj.PngReader
import ar.com.hjg.pngj.chunks.PngChunkPLTE
import com.github.c64lib.rbt.processors.image.domain.Image
import com.github.c64lib.rbt.processors.image.usecase.port.ReadImagePort
import com.github.c64lib.rbt.shared.domain.Color
import java.io.File

class ReadPngImageAdapter : ReadImagePort {
  override fun read(file: File): Image {
    val pngReader = PngReader(file)
    val imageInfo: ImageInfo = pngReader.imgInfo
    val image = handlePNG(pngReader, imageInfo)
    pngReader.end()

    return image
  }

  private fun handlePNG(pngReader: PngReader, imageInfo: ImageInfo): Image =
      if (imageInfo.channels < 3) {
        handlePalettePNG(pngReader, imageInfo)
      } else {
        handleRgbaPNG(pngReader, imageInfo)
      }

  private fun handleRgbaPNG(pngReader: PngReader, imageInfo: ImageInfo): Image {
    val width = imageInfo.cols
    val height = imageInfo.rows
    val image = Image(width, height)

    for (y in 0 until height) {
      val row = pngReader.readRow(y)
      for (x in 0 until width) {
        val color =
            when (row) {
              is ImageLineInt -> {
                val scanline = row.scanline
                val triples = toTuples(scanline, 4)
                val r = triples[x][0]
                val g = triples[x][1]
                val b = triples[x][2]
                val a = 255
                Color(r, g, b, a)
              }
              is ImageLineByte ->
                  throw IllegalStateException("Unsupported row type: ${row.javaClass}")
              else -> throw IllegalStateException("Unsupported row type: ${row.javaClass}")
            }
        image[x, y] = color
      }
    }
    return image
  }

  private fun handlePalettePNG(pngReader: PngReader, imageInfo: ImageInfo): Image {
    val paletteChunks = pngReader.chunksList.getById(PngChunkPLTE.ID)
    val paletteChunk = paletteChunks.first() as PngChunkPLTE
    val width = imageInfo.cols
    val height = imageInfo.rows
    val image = Image(width, height)

    for (y in 0 until height) {
      val row = pngReader.readRow(y)
      for (x in 0 until width) {
        val color =
            when (row) {
              is ImageLineInt -> {
                val triples = toTuples(ImageLineHelper.palette2rgb(row, paletteChunk, null))
                val r = triples[x][0]
                val g = triples[x][1]
                val b = triples[x][2]
                val a = 255
                Color(r, g, b, a)
              }
              is ImageLineByte ->
                  throw IllegalStateException("Unsupported row type: ${row.javaClass}")
              else -> throw IllegalStateException("Unsupported row type: ${row.javaClass}")
            }
        image[x, y] = color
      }
    }
    return image
  }

  private fun toTuples(array: IntArray, size: Int = 3): Array<IntArray> {
    if (array.size % size != 0) {
      throw IllegalArgumentException("Input array size must be a multiple of $size")
    }

    val tripleArraySize = array.size / size
    val result = Array(tripleArraySize) { IntArray(size) }

    for (i in 0 until tripleArraySize) {
      result[i] = array.sliceArray(i * size until (i + 1) * size)
    }

    return result
  }
}
