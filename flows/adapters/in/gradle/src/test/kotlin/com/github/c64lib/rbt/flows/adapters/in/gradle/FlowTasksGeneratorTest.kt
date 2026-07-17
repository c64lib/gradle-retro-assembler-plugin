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

import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleSpecUseCase
import com.github.c64lib.rbt.compilers.kickass.usecase.port.KickAssembleSpecPort
import com.github.c64lib.rbt.emulators.vice.usecase.RunTestOnViceUseCase
import com.github.c64lib.rbt.emulators.vice.usecase.port.RunTestOnVicePort
import com.github.c64lib.rbt.emulators.vice.usecase.port.ViceParameters
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.tasks.TestTask
import com.github.c64lib.rbt.flows.domain.Flow
import com.github.c64lib.rbt.shared.gradle.dsl.RetroAssemblerPluginExtension
import com.github.c64lib.rbt.testing.a64spec.usecase.Run64SpecTestUseCase
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import java.io.File
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
            val exception = shouldThrow<GradleException> { registerTasksInto(project, flows) }
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

      fun dependenciesOf(project: org.gradle.api.Project, taskName: String): Set<String> {
        val task = project.tasks.getByName(taskName)
        return task.taskDependencies.getDependencies(task).map { it.name }.toSet()
      }

      given("a flow with two independent steps (no shared files)") {
        val flows =
            FlowDslBuilder()
                .flow("assets") {
                  commandStep("charpad", "charpad") {
                    from("src/a.ctm")
                    to("build/a.bin")
                  }
                  commandStep("spritepad", "spritepad") {
                    from("src/b.spd")
                    to("build/b.bin")
                  }
                }
                .build()

        `when`("registering tasks") {
          val project = registerTasks(flows)

          then("no dependency exists between the two step tasks") {
            dependenciesOf(project, "flowAssetsStepCharpad") shouldBe emptySet()
            dependenciesOf(project, "flowAssetsStepSpritepad") shouldBe emptySet()
          }

          then("the flow aggregation task depends on both step tasks") {
            dependenciesOf(project, "flowAssets") shouldBe
                setOf("flowAssetsStepCharpad", "flowAssetsStepSpritepad")
          }
        }
      }

      given("a flow whose second step consumes the first step's output, plus an independent step") {
        val flows =
            FlowDslBuilder()
                .flow("pipeline") {
                  commandStep("produce", "tool") {
                    from("src/a.txt")
                    to("build/a.bin")
                  }
                  commandStep("consume", "tool") {
                    from("build/a.bin")
                    to("build/a.z.bin")
                  }
                  commandStep("independent", "tool") {
                    from("src/c.txt")
                    to("build/c.bin")
                  }
                }
                .build()

        `when`("registering tasks") {
          val project = registerTasks(flows)

          then("the consuming step task depends on the producing step task") {
            dependenciesOf(project, "flowPipelineStepConsume") shouldBe
                setOf("flowPipelineStepProduce")
          }

          then("the independent step task has no dependencies") {
            dependenciesOf(project, "flowPipelineStepIndependent") shouldBe emptySet()
          }

          then("the flow aggregation task depends on all three step tasks") {
            dependenciesOf(project, "flowPipeline") shouldBe
                setOf(
                    "flowPipelineStepProduce",
                    "flowPipelineStepConsume",
                    "flowPipelineStepIndependent")
          }
        }
      }

      given("two flows linked only by artifact consumption (no explicit dependsOn)") {
        val flows =
            FlowDslBuilder()
                .flow("assets") {
                  commandStep("font", "charpad") {
                    from("src/font.ctm")
                    to("build/font.bin")
                  }
                }
                .flow("compilation") {
                  commandStep("compile", "kickass") {
                    from("src/main.asm", "build/font.bin")
                    to("build/main.prg")
                  }
                }
                .build()

        `when`("registering tasks") {
          val project = registerTasks(flows)

          then("the consuming flow's aggregation task depends on the producing flow's task") {
            dependenciesOf(project, "flowCompilation") shouldBe
                setOf("flowCompilationStepCompile", "flowAssets")
          }

          then("the consuming step task depends on the producing step task across flows") {
            dependenciesOf(project, "flowCompilationStepCompile") shouldBe
                setOf("flowAssetsStepFont")
          }
        }
      }

      given("two flows with an explicit dependsOn and no shared files") {
        val flows =
            FlowDslBuilder()
                .flow("first") {
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
          val project = registerTasks(flows)

          then("the dependent flow's aggregation task depends on the dependency flow's task") {
            dependenciesOf(project, "flowSecond") shouldBe setOf("flowSecondStepStepB", "flowFirst")
          }
        }
      }

      given("two fully independent flows") {
        val flows =
            FlowDslBuilder()
                .flow("left") {
                  commandStep("stepL", "tool") {
                    from("src/l.txt")
                    to("build/l.bin")
                  }
                }
                .flow("right") {
                  commandStep("stepR", "tool") {
                    from("src/r.txt")
                    to("build/r.bin")
                  }
                }
                .build()

        `when`("registering tasks") {
          val project = registerTasks(flows)

          then("no dependency exists between the two flow aggregation tasks") {
            dependenciesOf(project, "flowLeft") shouldBe setOf("flowLeftStepStepL")
            dependenciesOf(project, "flowRight") shouldBe setOf("flowRightStepStepR")
          }

          then("the top-level flows task depends on both flow tasks") {
            dependenciesOf(project, "flows") shouldBe setOf("flowLeft", "flowRight")
          }
        }
      }

      val noopSpecUseCase =
          KickAssembleSpecUseCase(
              object : KickAssembleSpecPort {
                override fun assemble(
                    libDirs: List<File>,
                    defines: List<String>,
                    resultFileName: String,
                    source: File
                ) {}
              })
      val noopRunUseCase =
          Run64SpecTestUseCase(
              RunTestOnViceUseCase(
                  object : RunTestOnVicePort {
                    override fun run(parameters: ViceParameters) {}
                  }))
      val extension = RetroAssemblerPluginExtension()

      fun registerTestFlow(project: org.gradle.api.Project, flows: List<Flow>) =
          FlowTasksGenerator(
                  project,
                  flows,
                  kickAssembleSpecUseCase = noopSpecUseCase,
                  run64SpecTestUseCase = noopRunUseCase,
                  extension = extension)
              .registerTasks()

      given("a flow with a test step") {
        val flows =
            FlowDslBuilder()
                .flow("verification") {
                  testStep("specs") {
                    specs("spec/math.spec.asm")
                    from("lib/math.asm")
                  }
                }
                .build()

        `when`("registering tasks with the 64spec use cases provided") {
          val project = newProject()
          registerTestFlow(project, flows)

          then("a TestTask is created with the flow{Flow}Step{Name} name") {
            val task = project.tasks.findByName("flowVerificationStepSpecs")
            task.shouldNotBeNull()
            task.shouldBeInstanceOf<TestTask>()
          }

          then("the 64spec use cases and extension are injected") {
            val task = project.tasks.getByName("flowVerificationStepSpecs") as TestTask
            task.kickAssembleSpecUseCase shouldBe noopSpecUseCase
            task.run64SpecTestUseCase shouldBe noopRunUseCase
            task.extension shouldBe extension
          }
        }
      }

      given("a flow with a test step but no 64spec use cases provided") {
        val flows =
            FlowDslBuilder()
                .flow("verification") { testStep("specs") { specs("spec/math.spec.asm") } }
                .build()

        `when`("registering tasks without the use cases") {
          then("the build fails with a clear message") {
            val project = newProject()
            val exception =
                shouldThrow<IllegalStateException> {
                  FlowTasksGenerator(project, flows).registerTasks()
                }
            exception.message.shouldNotBeNull() shouldContain "TestStep 'specs'"
          }
        }
      }
    })
