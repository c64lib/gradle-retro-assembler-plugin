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
package com.github.c64lib.rbt.processors.charpad.domain

enum class ScreenMode(val value: Byte) {
  TextHires(0),
  TextMulticolor(1),
  TextExtendedBackground(2),
  BitmapHires(3),
  BitmapMulticolor(4)
}

internal fun screenModeFrom(value: Byte): ScreenMode =
    when (value) {
      ScreenMode.TextHires.value -> ScreenMode.TextHires
      ScreenMode.TextMulticolor.value -> ScreenMode.TextMulticolor
      ScreenMode.TextExtendedBackground.value -> ScreenMode.TextExtendedBackground
      ScreenMode.BitmapHires.value -> ScreenMode.BitmapHires
      ScreenMode.BitmapMulticolor.value -> ScreenMode.BitmapMulticolor
      else -> throw InvalidCTMFormatException("Unsupported screen mode: $value.")
    }
