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
package com.github.c64lib.gradle

import com.github.c64lib.rbt.flows.adapters.`in`.gradle.FlowsExtension
import com.github.c64lib.rbt.flows.domain.steps.CommandStep
import groovy.lang.Binding
import groovy.lang.GroovyShell
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import java.nio.file.Files
import org.gradle.testfixtures.ProjectBuilder

/**
 * Regression coverage for issue #182: `useFrom()`/`useTo()` used inside a Groovy `commandStep`
 * closure nested in a `flow` closure.
 *
 * These evaluate a real Groovy script (via [GroovyShell]) against a real [FlowsExtension] created
 * by ProjectBuilder, so Groovy compiles and dispatches the nested closures exactly as a
 * `build.gradle` would — the only faithful way to reproduce the Groovy method-resolution behaviour.
 * `useFrom()` and `useTo()` are zero-arg calls in Groovy; the Kotlin `index` default parameter does
 * not produce a callable no-arg JVM overload, so without explicit zero-arg overloads Groovy fails
 * to resolve them.
 */
class FlowsGroovyUseFromUseToTest :
    BehaviorSpec({
      fun evalFlows(script: String): List<CommandStep> {
        val project =
            ProjectBuilder.builder()
                .withProjectDir(Files.createTempDirectory("flows-groovy-182").toFile())
                .build()
        project.pluginManager.apply(RetroAssemblerPlugin::class.java)
        val flows = project.extensions.getByType(FlowsExtension::class.java)
        val binding = Binding()
        binding.setVariable("flowsExt", flows)
        // `flowsExt.with { ... }` binds the closure delegate to the extension, reproducing the
        // `flows { ... }` block; the nested flow/commandStep closures compile as in a real build.
        GroovyShell(FlowsGroovyUseFromUseToTest::class.java.classLoader, binding).evaluate(script)
        return flows.getFlows().flatMap { it.steps }.filterIsInstance<CommandStep>()
      }

      given("a Groovy flow with a nested commandStep") {
        `when`("useFrom() is used as a bare argument in param()") {
          then("it resolves to the first input path") {
            val steps =
                evalFlows(
                    """
                    flowsExt.with {
                      flow("f") {
                        commandStep("c", "tool") {
                          from("i.bin")
                          to("o.bin")
                          param(useFrom())
                        }
                      }
                    }
                    """
                        .trimIndent())
            steps.single().parameters shouldContain "i.bin"
          }
        }

        `when`("useTo() is used as a bare argument in option()") {
          then("it resolves to the first output path") {
            val steps =
                evalFlows(
                    """
                    flowsExt.with {
                      flow("f") {
                        commandStep("c", "tool") {
                          from("i.bin")
                          to("o.bin")
                          option("-o", useTo())
                        }
                      }
                    }
                    """
                        .trimIndent())
            steps.single().parameters shouldContain "o.bin"
          }
        }

        `when`("indexed useFrom(i) / useTo(i) are used with multiple inputs and outputs") {
          then("each resolves to the path at that index") {
            val steps =
                evalFlows(
                    """
                    flowsExt.with {
                      flow("f") {
                        commandStep("c", "tool") {
                          from("in0.bin", "in1.bin")
                          to("out0.bin", "out1.bin")
                          param(useFrom(1))
                          option("-o", useTo(1))
                        }
                      }
                    }
                    """
                        .trimIndent())
            val params = steps.single().parameters
            params shouldContain "in1.bin"
            params shouldContain "out1.bin"
          }
        }

        `when`("the two-statement workaround is used (assign to a local first)") {
          then("it still resolves the input path") {
            // Regression guard for the rollback path documented in PLAN-0015: assigning useFrom()
            // to
            // a Groovy local before calling param() must keep working after the fix.
            val steps =
                evalFlows(
                    """
                    flowsExt.with {
                      flow("f") {
                        commandStep("c", "tool") {
                          from("i.bin")
                          to("o.bin")
                          def input = useFrom()
                          param(input)
                        }
                      }
                    }
                    """
                        .trimIndent())
            steps.single().parameters shouldContain "i.bin"
          }
        }
      }
    })
