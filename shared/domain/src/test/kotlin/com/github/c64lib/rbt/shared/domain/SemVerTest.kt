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
package com.github.c64lib.rbt.shared.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SemVerTest :
    DescribeSpec({
      describe("SemVer") {
        describe("can be parsed from") {
          val testCases: List<Pair<String, SemVer>> =
              listOf(
                  Pair("2.1", SemVer(2, 1)),
                  Pair("2.1.3", SemVer(2, 1, 3)),
                  Pair("2.1.3-rc01", SemVer(2, 1, 3, "rc01")))

          testCases.forEach { testCase ->
            it(testCase.first) {
              val semVer = SemVer.fromString(testCase.first)
              val should = testCase.second
              semVer.major shouldBe should.major
              semVer.minor shouldBe should.minor
              semVer.patch shouldBe should.patch
              semVer.suffix shouldBe should.suffix
              semVer.toString() shouldBe should.toString()
            }
          }
        }

        describe("converts to string from") {
          val testCases =
              listOf(
                  Pair(SemVer(2, 1), "2.1"),
                  Pair(SemVer(2, 1, 3), "2.1.3"),
                  Pair(SemVer(2, 1, 3, "rc01"), "2.1.3-rc01"))

          testCases.forEach { testCase ->
            it(testCase.second) {
              val semVer = testCase.first
              val textual = testCase.second
              semVer.toString() shouldBe textual
            }
          }
        }

        describe("can be compared") {
          val testCases =
              listOf(
                  Pair(SemVer(2, 1), SemVer(2, 2)),
                  Pair(SemVer(2, 1, 2), SemVer(2, 1, 3)),
                  Pair(SemVer(2, 1, 2, "rc01"), SemVer(2, 1, 3, "rc01")),
                  Pair(SemVer(2, 1, 3, "rc01"), SemVer(2, 1, 3, "rc02")),
              )

          testCases.forEach { testCase ->
            it("${testCase.first} < ${testCase.second}") {
              (testCase.first < testCase.second) shouldBe true
            }
          }
        }
      }
    })
