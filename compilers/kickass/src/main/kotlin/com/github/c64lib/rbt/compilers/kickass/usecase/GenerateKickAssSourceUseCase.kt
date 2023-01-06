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
package com.github.c64lib.rbt.compilers.kickass.usecase

import com.github.c64lib.rbt.shared.domain.source.Comment
import com.github.c64lib.rbt.shared.domain.source.Label
import com.github.c64lib.rbt.shared.domain.source.Namespace
import com.github.c64lib.rbt.shared.domain.source.Separator
import com.github.c64lib.rbt.shared.domain.source.SourceModel
import com.github.c64lib.rbt.shared.processor.TextOutputBuffer

class GenerateKickAssSourceUseCase(private val writer: TextOutputBuffer) {
  fun apply(sourceModel: SourceModel) {
    sourceModel.stream { sourceItem ->
      when (sourceItem) {
        is Comment ->
            when (sourceItem.texts.size) {
              0 -> {}
              1 -> writer.writeLn("// ${sourceItem.texts[0]}")
              else -> {
                writer.writeLn("/*")
                sourceItem.texts.forEach { writer.writeLn("  $it") }
                writer.writeLn("*/")
              }
            }
        is Label -> writer.writeLn(".label ${sourceItem.name} = ${sourceItem.value}")
        is Namespace -> writer.writeLn(".filenamespace ${sourceItem.name}")
        is Separator -> writer.writeLn()
      }
    }
  }
}