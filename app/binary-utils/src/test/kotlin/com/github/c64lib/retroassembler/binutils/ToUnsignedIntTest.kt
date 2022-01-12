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
package com.github.c64lib.retroassembler.binutils

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class ToUnsignedIntTest :
    ShouldSpec({
      should("0.toUnsignedInt == 0") {
        val value: Byte = 0x00
        value.toUnsignedInt() shouldBe 0
      }
      should("1.toUnsignedInt == 1") {
        val value: Byte = 0x01
        value.toUnsignedInt() shouldBe 1
      }
      should("127.toUnsignedInt == 127") {
        val value: Byte = 127
        value.toUnsignedInt() shouldBe 127
      }
      should("-128.toUnsignedInt = 128") {
        val value: Byte = -128
        value.toUnsignedInt() shouldBe 128
      }
      should("-1.toUnsignedInt == 255") {
        val value: Byte = -1
        value.toUnsignedInt() shouldBe 255
      }
    })
