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

import com.github.c64lib.rbt.flows.domain.Flow
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder

/** Verifies task registration and configuration-time flow graph validation. */
class FlowTasksGeneratorTest :
    BehaviorSpec({
      fun newProject() =
          ProjectBuilder.builder()
              .withProjectDir(java.nio.file.Files.createTempDirectory("flows-test").toFile())
              .build()

      fun registerTasks(flows: List<Flow>) =
          newProject().also { project -> FlowTasksGenerator(project, flows).registerTasks() }

      fun registerTasksInto(project: org.gradle.api.Project, flows: List<Flow>) =
          FlowTasksGenerator(project, flows).registerTasks()

      given("two flows with a circular explicit dependency") {
        val flows =
            FlowDslBuilder()
                .flow("first") {
                  dependsOn("second")
                  commandStep("stepA", "tool") {
                    from("src/a.txt")
                    to("build/a.bin")
                  }
                }
                .flow("second") {
                  dependsOn("first")
                  commandStep("stepB", "tool") {
                    from("src/b.txt")
                    to("build/b.bin")
                  }
                }
                .build()

        `when`("registering tasks") {
          then("the build fails at configuration time with a clear message") {
            val project = newProject()
            val exception =
                shouldThrow<GradleException> { registerTasksInto(project, flows) }
            exception.message.shouldNotBeNull() shouldContain "Circular dependency"
          }
        }
      }

      given("tony-shaped flows (source inputs and within-flow intermediates)") {
        val flows =
            FlowDslBuilder()
                .flow("intro") {
                  commandStep("charpad", "charpad") {
                    from("src/charpad/intro.ctm")
                    to("build/charpad/intro.charset.bin")
                  }
                  commandStep("exomize", "exomizer") {
                    from("build/charpad/intro.charset.bin")
                    to("build/charpad/intro.charset.z.bin")
                  }
                }
                .flow("title") {
                  commandStep("compile", "kickass") {
                    from("src/kickass/title.asm")
                    to("build/kickass/title.bin")
                  }
                }
                .build()

        `when`("registering tasks") {
          then("validation passes and all tasks are created") {
            shouldNotThrowAny {
              val project = registerTasks(flows)
              (project.tasks.findByName("flowIntro") != null) shouldBe true
              (project.tasks.findByName("flowTitle") != null) shouldBe true
              (project.tasks.findByName("flows") != null) shouldBe true
            }
          }
        }
      }
    })
