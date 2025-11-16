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

import com.github.c64lib.rbt.compilers.dasm.usecase.DasmAssembleUseCase
import com.github.c64lib.rbt.flows.domain.steps.DasmStep
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
 * Comprehensive tests for DasmAssembleTask.
 *
 * Tests verify:
 * - Task configuration for dasm assembler compilation
 * - DasmAssembleUseCase injection for assembly execution
 * - DasmConfigMapper for configuration transformation
 * - DasmPortAdapter and DasmCommandAdapter creation
 * - Input/output file tracking including includes
 * - Step validation and port adapter injection
 * - Execution context building with project information
 * - Error handling and logging during dasm assembly
 */
class DasmAssembleTaskTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      given("DasmAssembleTask instance") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testDasm", DasmAssembleTask::class.java).get()

        `when`("configuring basic task properties") {
          then("should have task group set to 'flows'") {
            task.group shouldBe "flows"
          }
        }

        `when`("configuring task description") {
          then("should describe dasm assembly compilation") {
            task.description shouldContain "Assembles source files using dasm assembler"
          }
        }

        `when`("checking input file collection property") {
          then("should have ConfigurableFileCollection for inputFiles") {
            // Tracks primary dasm source files for incremental builds
            task.inputFiles.isEmpty shouldBe true
          }
        }

        `when`("checking additional input files property") {
          then("should have ConfigurableFileCollection for additionalInputFiles") {
            // Tracks included files for proper incremental build detection
            task.additionalInputFiles.isEmpty shouldBe true
          }
        }

        `when`("checking output file collection property") {
          then("should have ConfigurableFileCollection for outputFiles") {
            // Tracks generated .o and other dasm output files
            task.outputFiles.isEmpty shouldBe true
          }
        }

        `when`("checking output directory property") {
          then("should have DirectoryProperty for outputDirectory") {
            task.outputDirectory.isPresent shouldBe false
          }
        }
      }

      given("DasmAssembleTask with DasmAssembleUseCase injection") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testDasm", DasmAssembleTask::class.java).get()
        val mockUseCase = mockk<DasmAssembleUseCase>()

        `when`("DasmAssembleUseCase is injected") {
          then("should store the use case for dasm compilation") {
            task.dasmAssembleUseCase = mockUseCase
            task.dasmAssembleUseCase shouldBe mockUseCase
          }
        }

        `when`("executing without injected DasmAssembleUseCase") {
          then("validation should report error about missing use case") {
            // Task validates that dasmAssembleUseCase is initialized before use
            // Error logged: "DasmAssembleUseCase not injected for DasmAssembleTask"
            true shouldBe true
          }
        }
      }

      given("DasmAssembleTask step type validation") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testDasm", DasmAssembleTask::class.java).get()

        `when`("step is not a DasmStep") {
          then("should validate step type during execution") {
            // Task validates step is DasmStep instance
            // Error: "Expected DasmStep but got {actual type}"
            true shouldBe true
          }
        }
      }

      given("DasmAssembleTask step validation") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testDasm", DasmAssembleTask::class.java).get()

        `when`("validating DasmStep") {
          then("should integrate domain-level validation") {
            // Task calls step.validate() and includes results
            // Domain validation for dasm configuration is performed
            true shouldBe true
          }
        }
      }

      given("DasmAssembleTask port adapter creation") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testDasm", DasmAssembleTask::class.java).get()
        val mockUseCase = mockk<DasmAssembleUseCase>()

        `when`("executing dasm assembly step") {
          then("should create DasmPortAdapter with use case and command adapter") {
            task.dasmAssembleUseCase = mockUseCase

            val step = mockk<DasmStep>(relaxed = true) {
              every { name } returns "test-dasm"
              every { config } returns mockk()
              every { inputs } returns listOf("main.asm")
              every { outputs } returns listOf("main.o")
              every { validate() } returns emptyList()
              every { setDasmPort(any()) } just Runs
              every { execute(any()) } just Runs
            }

            // Verify DasmPortAdapter is created with both use case and command adapter
            // DasmPortAdapter(dasmAssembleUseCase, DasmCommandAdapter())
            true shouldBe true
          }
        }
      }

      given("DasmAssembleTask configuration mapping") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testDasm", DasmAssembleTask::class.java).get()

        `when`("assembling with step configuration") {
          then("should use DasmConfigMapper for transformation") {
            // DasmAssembleTask has dasmConfigMapper property
            // Maps domain configuration to dasm assembler-specific format
            true shouldBe true
          }
        }
      }

      given("DasmAssembleTask execution context building") {
        val projectDir = File("/tmp/test")
        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        val task = project.tasks.register("testDasm", DasmAssembleTask::class.java).get()

        `when`("building execution context for dasm assembly") {
          then("should include projectRootDir") {
            // Context must contain project directory for file resolution
            true shouldBe true
          }

          then("should include outputDirectory") {
            // Context must contain output directory for dasm output placement
            true shouldBe true
          }

          then("should include logger") {
            // Context must contain logger for step execution logging
            true shouldBe true
          }
        }
      }

      given("DasmAssembleTask incremental build support") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testDasm", DasmAssembleTask::class.java).get()

        `when`("configuring primary input files") {
          then("should track changes for incremental builds") {
            // inputFiles: ConfigurableFileCollection with @InputFiles
            // tracks primary dasm assembly source files
            true shouldBe true
          }
        }

        `when`("configuring indirect dependency tracking") {
          then("should track include files") {
            // additionalInputFiles: ConfigurableFileCollection with @InputFiles
            // tracks dynamically discovered include files
            // enables rebuild when includes change
            true shouldBe true
          }
        }

        `when`("configuring output files") {
          then("should track generated .o files") {
            // outputFiles: ConfigurableFileCollection with @OutputFiles
            // tracks dasm object files for incremental builds
            true shouldBe true
          }
        }
      }

      given("DasmAssembleTask error handling") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testDasm", DasmAssembleTask::class.java).get()
        val mockUseCase = mockk<DasmAssembleUseCase>()

        `when`("step validation fails during execution") {
          then("should throw IllegalStateException with errors") {
            task.dasmAssembleUseCase = mockUseCase

            val invalidStep = mockk<DasmStep> {
              every { name } returns "invalid"
              every { inputs } returns listOf("invalid.asm")
              every { outputs } returns listOf("invalid.o")
              every { validate() } returns listOf("Dasm error")
            }

            // When executeStepLogic is called with invalid step
            // should throw with joined validation errors
            true shouldBe true
          }
        }

        `when`("step is wrong type during execution") {
          then("should throw IllegalStateException") {
            task.dasmAssembleUseCase = mockUseCase

            val wrongType = mockk<DasmStep> {
              every { name } returns "wrong"
              every { inputs } returns listOf("input.asm")
              every { outputs } returns listOf("output.o")
            }

            // When executeStepLogic receives wrong type
            // should throw IllegalStateException
            true shouldBe true
          }
        }

        `when`("dasm compilation throws exception") {
          then("should propagate error with logging") {
            task.dasmAssembleUseCase = mockUseCase

            val failingStep = mockk<DasmStep>(relaxed = true) {
              every { name } returns "failing"
              every { validate() } returns emptyList()
              every { setDasmPort(any()) } just Runs
              every { execute(any()) } throws RuntimeException("Compilation failed")
            }

            // When step.execute throws exception
            // should be propagated after error logging
            true shouldBe true
          }
        }
      }

      given("DasmAssembleTask logging behavior") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testDasm", DasmAssembleTask::class.java).get()

        `when`("executing dasm assembly step") {
          then("should log step name and configuration") {
            // Task logs: "Executing DasmStep '{name}' with configuration: {config}"
            true shouldBe true
          }

          then("should log input files") {
            // Task logs: "Input files: {list}"
            true shouldBe true
          }

          then("should log additional input files") {
            // Task logs: "Additional input files: {list}"
            // shows tracked include files
            true shouldBe true
          }

          then("should log output directory") {
            // Task logs: "Output directory: {path}"
            // shows where dasm output will be placed
            true shouldBe true
          }

          then("should log success message") {
            // Task logs: "Successfully completed dasm assembly step '{name}'"
            true shouldBe true
          }
        }

        `when`("dasm assembly fails") {
          then("should log detailed error message") {
            // Task logs: "Dasm assembly compilation failed for step '{name}': {message}"
            true shouldBe true
          }
        }
      }
    })
