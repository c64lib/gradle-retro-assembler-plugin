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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle

import com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl.AssembleStepBuilder
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl.CharpadStepBuilder
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl.CommandStepBuilder
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl.DasmStepBuilder
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl.ExomizerStepBuilder
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl.GoattrackerStepBuilder
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl.ImageStepBuilder
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl.SpritepadStepBuilder
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl.TestStepBuilder
import groovy.lang.Closure
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Covers both DSL call shapes for every `FlowBuilder` step method: the Groovy [Closure] overload
 * (delegate binding, as a real Groovy `build.gradle` would invoke it) and the Kotlin
 * receiver-lambda overload (to confirm trailing-lambda call sites still resolve to the lambda
 * overload, not the new `Closure` one, now that both exist side by side).
 */
class FlowDslGroovyOverloadTest {

  /** A minimal no-arg Groovy closure that runs [body] against whatever delegate it is bound to. */
  private class TestClosure(private val body: Any.() -> Unit) : Closure<Unit>(null) {
    @Suppress("unused")
    fun doCall(delegate: Any?) {
      (delegate as Any).body()
    }
  }

  private fun closureOf(body: Any.() -> Unit): Closure<*> = TestClosure(body)

  @Test
  fun `flow Closure overload binds delegate and registers the flow`() {
    val dsl = FlowDslBuilder()
    dsl.flow(
        "my-flow",
        closureOf {
          this as FlowBuilder
          description = "via closure"
          testStep("t") { from("a.spec") }
        })
    val flows = dsl.build()
    assertEquals(1, flows.size)
    assertEquals("my-flow", flows[0].name)
    assertEquals("via closure", flows[0].description)
    assertEquals(1, flows[0].steps.size)
  }

  @Test
  fun `testStep Closure overload binds delegate and registers artifacts`() {
    var registered: FlowBuilder? = null
    val flow =
        FlowBuilder("f").apply {
          registered = this
          testStep("spec-step", closureOf { (this as TestStepBuilder).from("x.spec") })
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "spec-step_input_0" })
    assertTrue(registered === flow)
  }

  @Test
  fun `assembleStep Closure overload binds delegate and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          assembleStep(
              "asm",
              closureOf {
                (this as AssembleStepBuilder).from("a.asm")
                to("a.prg")
              })
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "asm_input_0" })
    assertTrue(built.produces.any { it.name == "asm_output_0" })
  }

  @Test
  fun `charpadStep Closure overload binds delegate and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          charpadStep(
              "cp",
              closureOf {
                (this as CharpadStepBuilder).from("a.ctm")
                to("a.bin")
              })
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "cp_input_0" })
    assertTrue(built.produces.any { it.name == "cp_output_0" })
  }

  @Test
  fun `spritepadStep Closure overload binds delegate and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          spritepadStep(
              "sp",
              closureOf {
                (this as SpritepadStepBuilder).from("a.spd")
                to("a.bin")
              })
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "sp_input_0" })
    assertTrue(built.produces.any { it.name == "sp_output_0" })
  }

  @Test
  fun `goattrackerStep Closure overload binds delegate and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          goattrackerStep(
              "gt",
              closureOf {
                (this as GoattrackerStepBuilder).from("a.sng")
                to("a.bin")
              })
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "gt_input_0" })
    assertTrue(built.produces.any { it.name == "gt_output_0" })
  }

  @Test
  fun `dasmStep Closure overload binds delegate and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          dasmStep(
              "da",
              closureOf {
                (this as DasmStepBuilder).from("a.asm")
                to("a.prg")
              })
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "da_input_0" })
    assertTrue(built.produces.any { it.name == "da_output_0" })
  }

  @Test
  fun `imageStep Closure overload binds delegate and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          imageStep("im", closureOf { (this as ImageStepBuilder).from("a.png") })
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "im_input_0" })
  }

  @Test
  fun `exomizerStep Closure overload binds delegate and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          exomizerStep(
              "ex",
              closureOf {
                (this as ExomizerStepBuilder).from("a.prg")
                to("a.z.prg")
              })
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "ex_input_0" })
    assertTrue(built.produces.any { it.name == "ex_output_0" })
  }

  @Test
  fun `commandStep Closure overload binds delegate and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          commandStep(
              "cmd",
              "exomizer",
              closureOf {
                (this as CommandStepBuilder).from("a.bin")
                to("a.out")
              })
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "cmd_input_0" })
    assertTrue(built.produces.any { it.name == "cmd_output_0" })
  }

  // --- Kotlin trailing-lambda call sites still resolve to the lambda overload, not Closure ---

  @Test
  fun `assembleStep Kotlin lambda overload still resolves and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          assembleStep("asm") {
            from("a.asm")
            to("a.prg")
          }
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "asm_input_0" })
    assertTrue(built.produces.any { it.name == "asm_output_0" })
  }

  @Test
  fun `charpadStep Kotlin lambda overload still resolves and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          charpadStep("cp") {
            from("a.ctm")
            to("a.bin")
          }
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "cp_input_0" })
    assertTrue(built.produces.any { it.name == "cp_output_0" })
  }

  @Test
  fun `spritepadStep Kotlin lambda overload still resolves and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          spritepadStep("sp") {
            from("a.spd")
            to("a.bin")
          }
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "sp_input_0" })
    assertTrue(built.produces.any { it.name == "sp_output_0" })
  }

  @Test
  fun `goattrackerStep Kotlin lambda overload still resolves and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          goattrackerStep("gt") {
            from("a.sng")
            to("a.bin")
          }
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "gt_input_0" })
    assertTrue(built.produces.any { it.name == "gt_output_0" })
  }

  @Test
  fun `dasmStep Kotlin lambda overload still resolves and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          dasmStep("da") {
            from("a.asm")
            to("a.prg")
          }
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "da_input_0" })
    assertTrue(built.produces.any { it.name == "da_output_0" })
  }

  @Test
  fun `imageStep Kotlin lambda overload still resolves and registers artifacts`() {
    val flow = FlowBuilder("f").apply { imageStep("im") { from("a.png") } }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "im_input_0" })
  }

  @Test
  fun `exomizerStep Kotlin lambda overload still resolves and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          exomizerStep("ex") {
            from("a.prg")
            to("a.z.prg")
          }
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "ex_input_0" })
    assertTrue(built.produces.any { it.name == "ex_output_0" })
  }

  @Test
  fun `commandStep Kotlin lambda overload still resolves and registers artifacts`() {
    val flow =
        FlowBuilder("f").apply {
          commandStep("cmd", "exomizer") {
            from("a.bin")
            to("a.out")
          }
        }
    val built = flow.build()
    assertEquals(1, built.steps.size)
    assertTrue(built.consumes.any { it.name == "cmd_input_0" })
    assertTrue(built.produces.any { it.name == "cmd_output_0" })
  }
}
