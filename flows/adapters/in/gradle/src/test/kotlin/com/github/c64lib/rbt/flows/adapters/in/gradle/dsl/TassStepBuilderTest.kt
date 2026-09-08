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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TassStepBuilderTest {

  @Test
  fun `builds a step with defaults`() {
    val step =
        TassStepBuilder("t")
            .apply {
              from("main.asm")
              to("main.prg")
            }
            .build()

    assertEquals("t", step.name)
    assertEquals(listOf("main.asm"), step.inputs)
    assertEquals(listOf("main.prg"), step.outputs)
    assertEquals("prg", step.config.outputFormat)
    assertNull(step.config.cpu)
    assertEquals(listOf("."), step.config.srcDirs)
    assertEquals(listOf("**/*.asm"), step.config.includes)
    assertEquals(listOf(".ra/**/*.asm"), step.config.excludes)
    assertEquals(".ra", step.config.workDir)
  }

  @Test
  fun `builds a step with every option set`() {
    val step =
        TassStepBuilder("t")
            .apply {
              from("a.asm", "b.asm")
              to("a.bin", "b.bin")
              includePaths("lib1", "lib2")
              includePath("lib3")
              define("DEBUG", "1")
              defines("A" to "1", "B" to "2")
              srcDirs("src")
              srcDir("asm")
              includes("**/*.s")
              include("**/*.asm")
              excludes("build/**")
              exclude("dist/**")
              additionalInputs("**/*.inc")
              additionalInput("**/*.mac")
              outputFormat("bin")
              cpu("65816")
              caseSensitive(true)
              asciiConversion(true)
              quiet(true)
              longBranch(true)
              viceLabels(true)
              listFile("out.lst")
              labelsFile("out.lbl")
            }
            .build()

    assertEquals(listOf("a.asm", "b.asm"), step.inputs)
    assertEquals(listOf("a.bin", "b.bin"), step.outputs)
    assertEquals(listOf("lib1", "lib2", "lib3"), step.config.includePaths)
    assertEquals(mapOf("DEBUG" to "1", "A" to "1", "B" to "2"), step.config.defines)
    assertEquals(listOf("src", "asm"), step.config.srcDirs)
    assertEquals(listOf("**/*.s", "**/*.asm"), step.config.includes)
    assertEquals(listOf("build/**", "dist/**"), step.config.excludes)
    assertEquals(listOf("**/*.inc", "**/*.mac"), step.config.additionalInputs)
    assertEquals("bin", step.config.outputFormat)
    assertEquals("65816", step.config.cpu)
    assertEquals(true, step.config.caseSensitive)
    assertEquals(true, step.config.asciiConversion)
    assertEquals(true, step.config.quiet)
    assertEquals(true, step.config.longBranch)
    assertEquals(true, step.config.viceLabels)
    assertEquals("out.lst", step.config.listFile)
    assertEquals("out.lbl", step.config.labelsFile)
  }

  @Test
  fun `property-style assignment works alongside function-style setters`() {
    val step =
        TassStepBuilder("t")
            .apply {
              from("main.asm")
              to("main.bin")
              outputFormat = "bin"
              cpu = "6502"
              quiet = false
            }
            .build()

    assertEquals("bin", step.config.outputFormat)
    assertEquals("6502", step.config.cpu)
    assertEquals(false, step.config.quiet)
  }
}
