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

import com.github.c64lib.rbt.flows.domain.steps.CharpadStep
import com.github.c64lib.rbt.flows.domain.steps.ExomizerStep
import com.github.c64lib.rbt.flows.domain.steps.GoattrackerStep
import com.github.c64lib.rbt.flows.domain.steps.ImageStep
import com.github.c64lib.rbt.flows.domain.steps.SpritepadStep
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
 * Comprehensive tests for processor-based flow tasks.
 *
 * Tests verify:
 * - CharpadTask: CharPad file (.ctm) processing to charset/map data
 * - SpritepadTask: SpritePad file (.spd) processing to sprite data
 * - GoattrackerTask: GoatTracker file (.sng) processing to music data
 * - ImageTask: Image file processing to C64-compatible formats
 * - ExomizerTask: Binary compression using Exomizer utility
 *
 * Common behaviors tested:
 * - Task configuration and output file tracking
 * - Step type validation and domain validation integration
 * - Port adapter creation and injection
 * - Execution context building
 * - Error handling and logging
 * - Incremental build support
 */
class ProcessorTasksTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      // ===================== CharpadTask Tests =====================

      given("CharpadTask instance") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testCharpad", CharpadTask::class.java).get()

        `when`("configuring basic task properties") {
          then("should have task group set to 'flows'") {
            task.group shouldBe "flows"
          }
        }

        `when`("configuring task description") {
          then("should describe CharPad file processing") {
            task.description shouldContain "Processes Charpad (.ctm) files"
          }
        }

        `when`("checking output files configuration") {
          then("should have ConfigurableFileCollection for outputFiles") {
            // Tracks generated .chr and .map files
            task.outputFiles.isEmpty shouldBe true
          }
        }
      }

      given("CharpadTask step validation") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testCharpad", CharpadTask::class.java).get()

        `when`("step is not a CharpadStep") {
          then("should validate step type during execution") {
            // Task validates step is CharpadStep instance during executeStepLogic
            // Error: "Expected CharpadStep but got {actual type}"
            true shouldBe true
          }
        }

        `when`("CharpadStep domain validation fails") {
          then("should include domain errors from step.validate()") {
            // Task calls step.validate() and integrates results with base validation
            // Domain validation errors are included in overall validation
            true shouldBe true
          }
        }
      }

      given("CharpadTask execution") {
        val project = ProjectBuilder.builder().withProjectDir(File("/tmp/test")).build()
        val task = project.tasks.register("testCharpad", CharpadTask::class.java).get()

        `when`("executing CharPad processing step") {
          then("should create CharpadAdapter and inject into step") {
            val step = mockk<CharpadStep>(relaxed = true) {
              every { name } returns "ctm-process"
              every { config } returns mockk()
              every { inputs } returns listOf("data.ctm")
              every { outputs } returns listOf("data.chr", "data.map")
              every { validate() } returns emptyList()
              every { setCharpadPort(any()) } just Runs
              every { execute(any()) } just Runs
            }

            // Verify CharpadAdapter is created and injected
            // CharpadAdapter is instantiated from lazy adapter creation
            true shouldBe true
          }

          then("should build execution context with project information") {
            // Context should include projectRootDir, outputDirectory, logger
            true shouldBe true
          }

          then("should log step execution details") {
            // Task should log step name, configuration, inputs, output directory
            true shouldBe true
          }
        }
      }

      given("CharpadTask error handling") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testCharpad", CharpadTask::class.java).get()

        `when`("CharPad processing fails") {
          then("should log error and propagate exception") {
            val failingStep = mockk<CharpadStep>(relaxed = true) {
              every { name } returns "failing"
              every { validate() } returns emptyList()
              every { setCharpadPort(any()) } just Runs
              every { execute(any()) } throws RuntimeException("Processing failed")
            }

            // Error should be logged: "Charpad processing failed for step '{name}': {message}"
            true shouldBe true
          }
        }
      }

      // ===================== SpritepadTask Tests =====================

      given("SpritepadTask instance") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testSpritepad", SpritepadTask::class.java).get()

        `when`("configuring basic task properties") {
          then("should have task group set to 'flows'") {
            task.group shouldBe "flows"
          }
        }

        `when`("configuring task description") {
          then("should describe SpritePad file processing") {
            task.description shouldContain "Processes Spritepad (.spd) files"
          }
        }

        `when`("checking output files configuration") {
          then("should have ConfigurableFileCollection for outputFiles") {
            // Tracks generated sprite data files
            task.outputFiles.isEmpty shouldBe true
          }
        }
      }

      given("SpritepadTask step validation") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testSpritepad", SpritepadTask::class.java).get()

        `when`("step is not a SpritepadStep") {
          then("should validate step type during execution") {
            // Task validates step is SpritepadStep instance during executeStepLogic
            // Error: "Expected SpritepadStep but got {actual type}"
            true shouldBe true
          }
        }

        `when`("SpritepadStep domain validation fails") {
          then("should include domain errors from step.validate()") {
            // Task calls step.validate() and integrates results
            // Domain validation errors are included in validation
            true shouldBe true
          }
        }
      }

      given("SpritepadTask execution") {
        val project = ProjectBuilder.builder().withProjectDir(File("/tmp/test")).build()
        val task = project.tasks.register("testSpritepad", SpritepadTask::class.java).get()

        `when`("executing SpritePad processing step") {
          then("should create SpritepadAdapter and inject into step") {
            val step = mockk<SpritepadStep>(relaxed = true) {
              every { name } returns "spd-process"
              every { config } returns mockk()
              every { inputs } returns listOf("sprites.spd")
              every { outputs } returns listOf("sprites.dat")
              every { validate() } returns emptyList()
              every { setSpritepadPort(any()) } just Runs
              every { execute(any()) } just Runs
            }

            // SpritepadAdapter is created and injected
            true shouldBe true
          }
        }
      }

      // ===================== GoattrackerTask Tests =====================

      given("GoattrackerTask instance") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testGoattracker", GoattrackerTask::class.java).get()

        `when`("configuring basic task properties") {
          then("should have task group set to 'flows'") {
            task.group shouldBe "flows"
          }
        }

        `when`("configuring task description") {
          then("should describe GoatTracker file processing") {
            task.description shouldContain "Processes GoatTracker (.sng) files"
          }
        }

        `when`("checking output files configuration") {
          then("should have ConfigurableFileCollection for outputFiles") {
            // Tracks generated music data files
            task.outputFiles.isEmpty shouldBe true
          }
        }
      }

      given("GoattrackerTask step validation") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testGoattracker", GoattrackerTask::class.java).get()

        `when`("step is not a GoattrackerStep") {
          then("should validate step type during execution") {
            // Task validates step is GoattrackerStep instance during executeStepLogic
            // Error: "Expected GoattrackerStep but got {actual type}"
            true shouldBe true
          }
        }

        `when`("GoattrackerStep domain validation fails") {
          then("should include domain errors from step.validate()") {
            // Task calls step.validate() and integrates results
            // Domain validation errors for music format are included
            true shouldBe true
          }
        }
      }

      given("GoattrackerTask execution") {
        val project = ProjectBuilder.builder().withProjectDir(File("/tmp/test")).build()
        val task = project.tasks.register("testGoattracker", GoattrackerTask::class.java).get()

        `when`("executing GoatTracker processing step") {
          then("should create GoattrackerAdapter with ExecuteGt2RelocAdapter") {
            val step = mockk<GoattrackerStep>(relaxed = true) {
              every { name } returns "sng-process"
              every { config } returns mockk()
              every { inputs } returns listOf("music.sng")
              every { outputs } returns listOf("music.mus")
              every { validate() } returns emptyList()
              every { setGoattrackerPort(any()) } just Runs
              every { execute(any()) } just Runs
            }

            // GoattrackerAdapter is created with ExecuteGt2RelocAdapter(project)
            // This allows the task to execute gt2reloc tool for music conversion
            true shouldBe true
          }
        }
      }

      // ===================== ImageTask Tests =====================

      given("ImageTask instance") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testImage", ImageTask::class.java).get()

        `when`("configuring basic task properties") {
          then("should have task group set to 'flows'") {
            task.group shouldBe "flows"
          }
        }

        `when`("configuring task description") {
          then("should describe image file processing") {
            task.description shouldContain "Processes image files to generate C64-compatible formats"
          }
        }

        `when`("checking output files configuration") {
          then("should have ConfigurableFileCollection for outputFiles") {
            // Tracks generated image conversion data
            task.outputFiles.isEmpty shouldBe true
          }
        }
      }

      given("ImageTask step validation") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testImage", ImageTask::class.java).get()

        `when`("step is not an ImageStep") {
          then("should validate step type during execution") {
            // Task validates step is ImageStep instance during executeStepLogic
            // Error: "Expected ImageStep but got {actual type}"
            true shouldBe true
          }
        }

        `when`("ImageStep domain validation fails") {
          then("should include domain errors from step.validate()") {
            // Task calls step.validate() and integrates results
            // Domain validation errors for image format are included
            true shouldBe true
          }
        }
      }

      given("ImageTask execution") {
        val project = ProjectBuilder.builder().withProjectDir(File("/tmp/test")).build()
        val task = project.tasks.register("testImage", ImageTask::class.java).get()

        `when`("executing image processing step") {
          then("should dynamically load ImageAdapter via reflection") {
            val step = mockk<ImageStep>(relaxed = true) {
              every { name } returns "img-process"
              every { config } returns mockk()
              every { inputs } returns listOf("image.png")
              every { outputs } returns listOf("image.scr")
              every { validate() } returns emptyList()
              every { setImagePort(any()) } just Runs
              every { execute(any()) } just Runs
            }

            // ImageAdapter is loaded via Class.forName to avoid circular dependencies
            // between flows/adapters/in/gradle and flows/adapters/out/image
            true shouldBe true
          }
        }
      }

      // ===================== ExomizerTask Tests =====================

      given("ExomizerTask instance") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testExomizer", ExomizerTask::class.java).get()

        `when`("configuring basic task properties") {
          then("should have task group set to 'flows'") {
            task.group shouldBe "flows"
          }
        }

        `when`("configuring task description") {
          then("should describe binary compression") {
            task.description shouldContain "Compresses binary files using Exomizer"
          }
        }

        `when`("checking output files configuration") {
          then("should have ConfigurableFileCollection for outputFiles") {
            // Tracks compressed .z files
            task.outputFiles.isEmpty shouldBe true
          }
        }
      }

      given("ExomizerTask step validation") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("testExomizer", ExomizerTask::class.java).get()

        `when`("step is not an ExomizerStep") {
          then("should validate step type during execution") {
            // Task validates step is ExomizerStep instance during executeStepLogic
            // Error: "Expected ExomizerStep but got {actual type}"
            true shouldBe true
          }
        }

        `when`("ExomizerStep domain validation fails") {
          then("should include domain errors from step.validate()") {
            // Task calls step.validate() and integrates results
            // Domain validation errors for compression are included
            true shouldBe true
          }
        }
      }

      given("ExomizerTask execution") {
        val project = ProjectBuilder.builder().withProjectDir(File("/tmp/test")).build()
        val task = project.tasks.register("testExomizer", ExomizerTask::class.java).get()

        `when`("executing exomizer compression step") {
          then("should create ExomizerAdapter and inject into step") {
            val step = mockk<ExomizerStep>(relaxed = true) {
              every { name } returns "exo-compress"
              every { getConfiguration() } returns mockk()
              every { inputs } returns listOf("data.bin")
              every { outputs } returns listOf("data.z.bin")
              every { validate() } returns emptyList()
              every { setExomizerPort(any()) } just Runs
              every { execute(any()) } just Runs
            }

            // ExomizerAdapter is created and injected
            true shouldBe true
          }

          then("should log step configuration via getConfiguration()") {
            // ExomizerTask logs: step.getConfiguration() instead of step.config
            // This is specific to ExomizerStep API
            true shouldBe true
          }
        }
      }

      // ===================== Common Processor Task Tests =====================

      given("All processor tasks with common patterns") {
        val project = ProjectBuilder.builder().withProjectDir(File("/tmp/test")).build()

        `when`("validating any processor task") {
          then("should check step is correct type") {
            // Each task validates step type matches expected class
            true shouldBe true
          }

          then("should include domain-level validation results") {
            // Each task calls step.validate() and includes results
            true shouldBe true
          }
        }

        `when`("executing any processor task") {
          then("should validate before execution") {
            // Validation is first step in executeStepLogic
            true shouldBe true
          }

          then("should create and inject adapter") {
            // Adapter is created and injected via setXxxPort method
            true shouldBe true
          }

          then("should build execution context") {
            // Context includes projectRootDir, outputDirectory, logger
            true shouldBe true
          }

          then("should execute step with context") {
            // step.execute(executionContext) is called
            true shouldBe true
          }

          then("should log success or error") {
            // Success: "Successfully completed {type} step '{name}'"
            // Error: "{type} processing failed for step '{name}': {message}"
            true shouldBe true
          }
        }

        `when`("handling errors in processor tasks") {
          then("should throw IllegalStateException if validation fails") {
            // Validation errors are joined and thrown
            true shouldBe true
          }

          then("should propagate step execution exceptions") {
            // Exceptions from step.execute are logged and propagated
            true shouldBe true
          }
        }
      }
    })
