/*
MIT License

Copyright (c) 2018-2022 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.gradle.preprocess

import com.github.c64lib.processor.commons.BinaryOutput
import com.github.c64lib.processor.commons.TextOutput
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter

interface Flushable {
  fun flush()
}

interface BinaryOutputBuffer : BinaryOutput, Flushable

interface TextOutputBuffer : TextOutput, Flushable

class DevNull : BinaryOutputBuffer {
  override fun flush() {
    // nothing to do
  }

  override fun write(data: ByteArray) {
    // ignore data
  }
}

class FileBinaryOutputBuffer(private val outputFile: File) : BinaryOutputBuffer {

  private var buffer: MutableList<ByteArray> = ArrayList()

  override fun write(data: ByteArray) {
    buffer.add(data)
  }

  override fun flush() =
      FileOutputStream(outputFile).use { fos -> buffer.forEach { data -> fos.write(data) } }
}

class FileTextOutputBuffer(private val outputFile: File) : TextOutputBuffer {
  private val buffer = StringWriter()
  private val writer = PrintWriter(buffer)

  override fun writeLn(data: String) = writer.println(data)
  override fun write(data: String) = writer.print(data)
  override fun flush() = FileWriter(outputFile).use { fw -> fw.write(buffer.toString()) }
}
