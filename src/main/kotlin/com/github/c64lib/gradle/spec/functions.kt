/*
 * MIT License
 *
 * Copyright (c) 2018-2019 c64lib: The Ultimate Commodore 64 Library
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.c64lib.gradle.spec

import java.io.File

fun prgFile(file: File) = fileWithoutExtension(file) + ".prg"
fun resultFileName(file: File) = fileNameWithoutExtension(file) + ".specOut"
fun resultFile(file: File) = fileWithoutExtension(file) + ".specOut"
fun viceSymbolFile(file: File) = fileWithoutExtension(file) + ".vs"

private fun fileWithoutExtension(file: File) = file.canonicalPath.substring(0, file.canonicalPath.lastIndexOf('.'))
private fun fileNameWithoutExtension(file: File) = file.name.substring(0, file.name.lastIndexOf('.'))
