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
package com.github.c64lib.rbt.shared.binutils

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class ToWordTest :
    ShouldSpec({
      should("[0,0].toWord == 0") {
        byteArrayOf(0.toByte(), 0.toByte()).toWord() shouldBe wordOf(0)
      }
      should("[1,0].toWord == 1") {
        byteArrayOf(1.toByte(), 0.toByte()).toWord() shouldBe wordOf(1)
      }
      should("(0,1).toWord == 256") {
        byteArrayOf(0.toByte(), 1.toByte()).toWord() shouldBe wordOf(256)
      }
      should("[255,0].toWord == 255") {
        byteArrayOf((-1).toByte(), 0.toByte()).toWord() shouldBe wordOf(255)
      }
      should("[0,255].toWord == 65280") {
        byteArrayOf(0.toByte(), (-1).toByte()).toWord() shouldBe wordOf(65280)
      }
    })
