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
package com.github.c64lib.rbt.processors.charpad.adapters.`in`.gradle

import com.github.c64lib.rbt.compilers.kickass.usecase.GenerateKickAssSourceUseCase
import com.github.c64lib.rbt.processors.charpad.domain.CTMHeader
import com.github.c64lib.rbt.shared.domain.source.SourceModelBuilder
import com.github.c64lib.rbt.shared.gradle.dsl.MetadataExtension
import com.github.c64lib.rbt.shared.processor.Output

internal class CharpadMetaOutput(
    private val srcWriter: GenerateKickAssSourceUseCase,
    private val metadataExtension: MetadataExtension
) : Output<CTMHeader> {
  override fun write(data: CTMHeader) {

    val modelBuilder =
        SourceModelBuilder()
            .comment(
                arrayOf(
                    "This file is generated by Charpad processor of Retro Build Tool.",
                    "",
                    "DO NOT MODIFY THIS FILE!"))
            .separator()

    metadataExtension.namespace?.let { value -> modelBuilder.namespace(value).separator() }

    if (metadataExtension.includeVersion) {
      modelBuilder.label(name("version"), data.version.toInt()).separator()
    }
    if (metadataExtension.includeBgColours) {
      modelBuilder
          .label(name("backgroundColour0"), data.backgroundColour0.toInt())
          .label(name("backgroundColour1"), data.backgroundColour1.toInt())
          .label(name("backgroundColour2"), data.backgroundColour2.toInt())
          .label(name("backgroundColour3"), data.backgroundColour3.toInt())
          .separator()
    }
    if (metadataExtension.includeCharColours) {
      modelBuilder.label(name("charColour"), data.charColour.toInt()).separator()
    }
    if (metadataExtension.includeMode) {
      modelBuilder
          .label(name("colouringMethod"), data.colouringMethod.value.toInt())
          .label(name("screenMode"), data.screenMode.value.toInt())
          .separator()
    }
    srcWriter.apply(modelBuilder.build())
  }

  private fun name(value: String): String = metadataExtension.prefix + value
}
