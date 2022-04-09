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
package com.github.c64lib.retroassembler.binary_interleaver

import com.github.c64lib.processor.commons.BinaryOutput
import com.github.c64lib.rbt.domain.shared.IllegalInputException
import io.vavr.collection.Seq

class BinaryInterleaver(private val outputs: Seq<BinaryOutput>) : BinaryOutput {

  override fun write(data: ByteArray) =
      checkInput(data) {
        val outputsLength = outputs.length()
        outputs.forEachWithIndex { output, index ->
          val filtered = data.filterIndexed { subIndex, _ -> subIndex % outputsLength == index }
          output.write(filtered.toByteArray())
        }
      }

  private fun checkInput(data: ByteArray, perform: () -> Unit) =
      if (data.size % outputs.length() != 0) {
        throw IllegalInputException(
            "Input data of size ${data.size} cannot be evenly distributed amongst ${outputs.length()} outputs")
      } else {
        perform.invoke()
      }
}
