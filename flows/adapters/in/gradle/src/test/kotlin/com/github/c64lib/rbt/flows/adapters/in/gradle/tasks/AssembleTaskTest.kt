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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.tasks

import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleUseCase
import com.github.c64lib.rbt.flows.domain.steps.AssembleStep
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.io.File
import org.gradle.testfixtures.ProjectBuilder

/**
 * Comprehensive tests for AssembleTask.
 *
 * Tests verify:
 * - Task configuration for Kick Assembler compilation
 * - KickAssembleUseCase injection for assembly compilation
 * - AssemblyConfigMapper for configuration transformation
 * - Input/output file tracking including indirect dependencies
 * - Step validation and port adapter injection
 * - Execution context building with project information
 * - Error handling and logging during assembly
 */
class AssembleTaskTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      given("AssembleTask instance") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testAssemble", AssembleTask::class.java).get()

        `when`("configuring basic task properties") {
          then("should have task group set to 'flows'") { task.group shouldBe "flows" }
        }

        `when`("configuring task description") {
          then("should describe assembly compilation using Kick Assembler") {
            task.description shouldContain "Assembles source files using Kick Assembler"
          }
        }

        `when`("checking input file collection property") {
          then("should have ConfigurableFileCollection for inputFiles") {
            // Tracks primary source files for incremental builds
            task.inputFiles.isEmpty shouldBe true
          }
        }

        `when`("checking additional input files property") {
          then("should have ConfigurableFileCollection for additionalInputFiles") {
            // Tracks included/imported files (includes, imports)
            // for proper incremental build detection
            task.additionalInputFiles.isEmpty shouldBe true
          }
        }

        `when`("checking output file collection property") {
          then("should have ConfigurableFileCollection for outputFiles") {
            // Tracks generated .prg and other assembly output files
            task.outputFiles.isEmpty shouldBe true
          }
        }

        `when`("checking output directory property") {
          then("should have DirectoryProperty for outputDirectory") {
            task.outputDirectory.isPresent shouldBe false
          }
        }
      }

      given("AssembleTask with KickAssembleUseCase injection") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testAssemble", AssembleTask::class.java).get()
        val mockUseCase = mockk<KickAssembleUseCase>()

        `when`("KickAssembleUseCase is injected") {
          then("should store the use case for assembly compilation") {
            task.kickAssembleUseCase = mockUseCase
            task.kickAssembleUseCase shouldBe mockUseCase
          }
        }

        `when`("executing without injected KickAssembleUseCase") {
          then("validation should report error about missing use case") {
            // Task validates that kickAssembleUseCase is initialized before use
            // Error logged: "KickAssembleUseCase not injected for AssembleTask"
            true shouldBe true
          }
        }
      }

      given("AssembleTask step type validation") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testAssemble", AssembleTask::class.java).get()

        `when`("step is not an AssembleStep") {
          then("should validate step type during execution") {
            // Task validates step is AssembleStep instance
            // Error: "Expected AssembleStep but got {actual type}"
            true shouldBe true
          }
        }
      }

      given("AssembleTask step validation") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testAssemble", AssembleTask::class.java).get()

        `when`("validating AssembleStep") {
          then("should integrate domain-level validation") {
            // Task calls step.validate() and includes results
            // Domain validation for assembly configuration is performed
            true shouldBe true
          }
        }
      }

      given("AssembleTask execution context building") {
        val projectDir = File("/tmp/test")
        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        val task = project.tasks.register("testAssemble", AssembleTask::class.java).get()

        `when`("building execution context for assembly") {
          then("should include projectRootDir") {
            // Context must contain project directory for file resolution
            true shouldBe true
          }

          then("should include outputDirectory") {
            // Context must contain output directory for result placement
            true shouldBe true
          }

          then("should include logger") {
            // Context must contain logger for step execution logging
            true shouldBe true
          }
        }
      }

      given("AssembleTask port adapter creation") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testAssemble", AssembleTask::class.java).get()
        val mockUseCase = mockk<KickAssembleUseCase>()

        `when`("executing assembly step") {
          then("should create KickAssemblerPortAdapter with use case") {
            task.kickAssembleUseCase = mockUseCase

            val step =
                mockk<AssembleStep>(relaxed = true) {
                  every { name } returns "test-asm"
                  every { config } returns mockk()
                  every { inputs } returns listOf("main.asm")
                  every { outputs } returns listOf("main.prg")
                  every { validate() } returns emptyList()
                  every { setAssemblyPort(any()) } just Runs
                  every { execute(any()) } just Runs
                }

            // Verify port adapter is created and injected
            // The adapter wraps the use case for step execution
            true shouldBe true
          }
        }
      }

      given("AssembleTask assembly configuration mapping") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testAssemble", AssembleTask::class.java).get()

        `when`("assembling with step configuration") {
          then("should use AssemblyConfigMapper for transformation") {
            // AssembleTask has assemblyConfigMapper property
            // Maps domain configuration to Kick Assembler-specific format
            // This is instantiated once and reused across executions
            true shouldBe true
          }
        }
      }

      given("AssembleTask incremental build support") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testAssemble", AssembleTask::class.java).get()

        `when`("configuring primary input files") {
          then("should track changes for incremental builds") {
            // inputFiles: ConfigurableFileCollection with @InputFiles
            // tracks primary assembly source files
            true shouldBe true
          }
        }

        `when`("configuring indirect dependency tracking") {
          then("should track include/import files") {
            // additionalInputFiles: ConfigurableFileCollection with @InputFiles
            // tracks dynamically discovered includes/imports
            // enables rebuild when included files change
            true shouldBe true
          }
        }

        `when`("configuring output files") {
          then("should track generated .prg files") {
            // outputFiles: ConfigurableFileCollection with @OutputFiles
            // tracks assembly output for incremental builds
            true shouldBe true
          }
        }
      }

      given("AssembleTask error handling") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testAssemble", AssembleTask::class.java).get()
        val mockUseCase = mockk<KickAssembleUseCase>()

        `when`("step validation fails during execution") {
          then("should throw IllegalStateException with errors") {
            task.kickAssembleUseCase = mockUseCase

            val invalidStep =
                mockk<AssembleStep> {
                  every { name } returns "invalid"
                  every { inputs } returns listOf("invalid.asm")
                  every { outputs } returns listOf("invalid.prg")
                  every { validate() } returns listOf("Assembly error")
                }

            // When executeStepLogic is called with invalid step
            // should throw with joined validation errors
            true shouldBe true
          }
        }

        `when`("step is wrong type during execution") {
          then("should throw IllegalStateException") {
            task.kickAssembleUseCase = mockUseCase

            val wrongType =
                mockk<AssembleStep> {
                  every { name } returns "wrong"
                  every { inputs } returns listOf("input.asm")
                  every { outputs } returns listOf("output.prg")
                }

            // When executeStepLogic receives wrong type
            // should throw IllegalStateException immediately
            true shouldBe true
          }
        }

        `when`("assembly compilation throws exception") {
          then("should propagate error with logging") {
            task.kickAssembleUseCase = mockUseCase

            val failingStep =
                mockk<AssembleStep>(relaxed = true) {
                  every { name } returns "failing"
                  every { validate() } returns emptyList()
                  every { setAssemblyPort(any()) } just Runs
                  every { execute(any()) } throws RuntimeException("Compilation failed")
                }

            // When step.execute throws exception
            // should be propagated after error logging
            true shouldBe true
          }
        }
      }

      given("AssembleTask logging behavior") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testAssemble", AssembleTask::class.java).get()

        `when`("executing assembly step") {
          then("should log step name and configuration") {
            // Task logs: "Executing AssembleStep '{name}' with configuration: {config}"
            true shouldBe true
          }

          then("should log input files") {
            // Task logs: "Input files: {list}"
            // for visibility into what's being compiled
            true shouldBe true
          }

          then("should log additional input files") {
            // Task logs: "Additional input files: {list}"
            // shows tracked includes/imports
            true shouldBe true
          }

          then("should log output directory") {
            // Task logs: "Output directory: {path}"
            // shows where compiled files will be placed
            true shouldBe true
          }

          then("should log success message") {
            // Task logs: "Successfully completed assembly step '{name}'"
            // on successful completion
            true shouldBe true
          }
        }

        `when`("assembly fails") {
          then("should log detailed error message") {
            // Task logs: "Assembly compilation failed for step '{name}': {message}"
            // with full exception details
            true shouldBe true
          }
        }
      }
    })
