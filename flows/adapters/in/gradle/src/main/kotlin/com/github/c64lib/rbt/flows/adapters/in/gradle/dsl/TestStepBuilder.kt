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

import com.github.c64lib.rbt.flows.domain.steps.TestStep

/**
 * Type-safe DSL builder for 64spec test steps.
 *
 * [specs] declare the `*.spec.asm` sources to assemble and run; [from] declares additional watched
 * inputs (the library sources the specs `#import`) so editing them re-runs the tests. The step's
 * inputs are specs ∪ watched files; outputs are the derived `.prg`/`.vs`/`.specOut` files.
 */
class TestStepBuilder(private val name: String) {
  private val specs = mutableListOf<String>()
  private val watched = mutableListOf<String>()

  /** Adds a single spec (`*.spec.asm`) to assemble and run. */
  fun spec(path: String) {
    specs.add(path)
  }

  /** Adds one or more specs (`*.spec.asm`) to assemble and run. */
  fun specs(vararg paths: String) {
    specs.addAll(paths)
  }

  /** Adds a single watched input (e.g. a library source the specs import). */
  fun from(path: String) {
    watched.add(path)
  }

  /** Adds multiple watched inputs (e.g. library sources the specs import). */
  fun from(vararg paths: String) {
    watched.addAll(paths)
  }

  internal fun build(): TestStep {
    val inputs = specs + watched
    val outputs = specs.flatMap { spec -> derivedOutputs(spec) }
    return TestStep(name = name, specs = specs.toList(), inputs = inputs, outputs = outputs)
  }

  /** Derives the `.prg`/`.vs`/`.specOut` outputs a spec produces, mirroring 64spec conventions. */
  private fun derivedOutputs(spec: String): List<String> {
    val base = spec.substring(0, spec.lastIndexOf('.'))
    return listOf("$base.prg", "$base.vs", "$base.specOut")
  }
}
