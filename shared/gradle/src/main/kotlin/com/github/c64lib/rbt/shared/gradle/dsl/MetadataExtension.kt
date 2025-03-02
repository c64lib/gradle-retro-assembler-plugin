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
package com.github.c64lib.rbt.shared.gradle.dsl

import com.github.c64lib.rbt.shared.processor.FileTextOutputBuffer
import com.github.c64lib.rbt.shared.processor.TextOutputBuffer
import com.github.c64lib.retroassembler.domain.AssemblerType
import java.io.File
import javax.inject.Inject
import org.gradle.api.file.ProjectLayout

abstract class MetadataExtension @Inject constructor(project: ProjectLayout) :
    OutputExtension<TextOutputBuffer>(CHARPAD_DIR, project) {

  var dialect = AssemblerType.None
  var prefix = ""
  var namespace: String? = null
  var includeVersion = false
  var includeBgColours = true
  var includeCharColours = true
  var includeMode = false

  override fun createBuffer(output: File): TextOutputBuffer = FileTextOutputBuffer(output)
}
