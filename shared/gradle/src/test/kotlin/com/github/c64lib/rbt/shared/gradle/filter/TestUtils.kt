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
package com.github.c64lib.rbt.shared.gradle.filter

import com.github.c64lib.rbt.shared.gradle.processor.BinaryOutput
import com.github.c64lib.rbt.shared.gradle.processor.InputByteStream
import com.github.c64lib.rbt.shared.gradle.processor.Output
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*

class BinaryOutputMock : BinaryOutput {

  private var storedData: MutableList<ByteArray> = LinkedList()

  val bytes: ByteArray
    get() = concatByteArray(storedData)

  override fun write(data: ByteArray) {
    storedData.add(data)
  }
}

class OutputMock<T> : Output<T> {

  private var storedData: T? = null

  val data: T?
    get() = storedData

  override fun write(data: T) {
    storedData = data
  }
}

class BinaryInputMock(data: ByteArray) : InputByteStream {

  private var readCounter = 0
  private val stream = ByteArrayInputStream(data)

  override fun read(amount: Int): ByteArray {
    val result = stream.readNBytes(amount)
    readCounter += result.size
    return result
  }

  override fun readCounter(): Int = readCounter
}

class InputByteStreamAdapter(private val inputStream: InputStream) : InputByteStream {

  private var readCounter = 0

  override fun read(amount: Int): ByteArray {
    val buffer = ByteArray(amount)
    val size = inputStream.read(buffer)
    readCounter += size
    return buffer.copyOfRange(0, size)
  }

  override fun readCounter(): Int = readCounter
}
