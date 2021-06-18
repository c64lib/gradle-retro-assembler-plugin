/*
MIT License

Copyright (c) 2018-2021 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.retroassembler.binutils

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class WordTest :
    ShouldSpec({
      should("wordOf(0) be 0x0000") { wordOf(0).value shouldBe 0x0000 }
      should("wordOf(65535) be 0xFFFF") { wordOf(65535).value shouldBe 0xFFFF }
      should("wordOf(255) be 0x00FF") { wordOf(255).value shouldBe 0x00FF }
      should("wordOf(65280) be 0xFF00") { wordOf(65280).value shouldBe 0xFF00 }
      should("wordOf(0,0) be 0x0000") { wordOf(0, 0).value shouldBe 0x0000 }
      should("wordOf(255,255) be 0xFFFF") { wordOf(255, 255).value shouldBe 0xFFFF }
      should("wordOf(0,255) be 0xFF00") { wordOf(0, 255).value shouldBe 0xFF00 }
      should("wordOf(255,0) be 0x00FF") { wordOf(255, 0).value shouldBe 0x00FF }
    })
