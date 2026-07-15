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
package com.github.c64lib.rbt.flows.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class FlowServiceTest :
    BehaviorSpec({
      val service = FlowService()

      given("flows with explicit and artifact-based dependencies") {
        val fontArtifact = FlowArtifact("font_output_0", "build/font.bin")
        val assets = Flow(name = "assets", produces = listOf(fontArtifact))
        val compilation =
            Flow(
                name = "compilation",
                consumes = listOf(FlowArtifact("compile_input_0", "build/font.bin")),
                produces = listOf(FlowArtifact("compile_output_0", "build/main.prg")))
        val packaging = Flow(name = "packaging", dependsOn = listOf("compilation"))
        val flows = listOf(assets, compilation, packaging)

        `when`("getting dependencies of a flow with an artifact-based dependency") {
          val deps = service.getDependenciesOf(flows, "compilation")

          then("the producing flow is reported") { deps shouldBe setOf("assets") }
        }

        `when`("getting dependencies of a flow with an explicit dependency") {
          val deps = service.getDependenciesOf(flows, "packaging")

          then("the declared flow is reported") { deps shouldBe setOf("compilation") }
        }

        `when`("getting dependencies of an independent flow") {
          val deps = service.getDependenciesOf(flows, "assets")

          then("no dependencies are reported") { deps shouldBe emptySet() }
        }

        `when`("getting dependencies of an unknown flow") {
          val deps = service.getDependenciesOf(flows, "doesNotExist")

          then("no dependencies are reported") { deps shouldBe emptySet() }
        }
      }
    })
