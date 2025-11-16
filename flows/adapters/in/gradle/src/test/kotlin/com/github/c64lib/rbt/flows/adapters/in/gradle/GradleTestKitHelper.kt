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

import java.io.File
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

/**
 * Helper class for functional testing Gradle tasks and extensions using Gradle TestKit.
 *
 * Provides utilities for creating test projects, executing tasks, and validating results.
 */
class GradleTestKitHelper(private val projectDir: File) {

  /** Create a minimal build.gradle.kts in the test project with the given content. */
  fun createBuildGradle(content: String) {
    projectDir.mkdirs()
    val buildFile = File(projectDir, "build.gradle.kts")
    buildFile.writeText(content)
  }

  /** Create a settings.gradle.kts in the test project with the given content. */
  fun createSettingsGradle(content: String) {
    projectDir.mkdirs()
    val settingsFile = File(projectDir, "settings.gradle.kts")
    settingsFile.writeText(content)
  }

  /** Create a file in the test project with the given relative path and content. */
  fun createFile(relativePath: String, content: String) {
    projectDir.mkdirs()
    val file = File(projectDir, relativePath)
    file.parentFile?.mkdirs()
    file.writeText(content)
  }

  /** Execute a Gradle task and return the result. */
  fun runTask(vararg taskNames: String): BuildResult {
    val args = taskNames.toMutableList()
    return GradleRunner.create()
        .withProjectDir(projectDir)
        .withArguments(args)
        .withPluginClasspath()
        .build()
  }

  /** Execute a Gradle task with custom configuration. */
  fun runTask(
      taskNames: List<String>,
      configure: GradleRunner.() -> GradleRunner = { this }
  ): BuildResult {
    return GradleRunner.create()
        .withProjectDir(projectDir)
        .withArguments(taskNames)
        .withPluginClasspath()
        .configure()
        .build()
  }

  /** Execute a Gradle task and expect it to fail. */
  fun runTaskExpectingFailure(vararg taskNames: String): BuildResult {
    val args = taskNames.toMutableList()
    return GradleRunner.create()
        .withProjectDir(projectDir)
        .withArguments(args)
        .withPluginClasspath()
        .buildAndFail()
  }

  /** Get the output directory for test artifacts. */
  fun getBuildDir(): File = File(projectDir, "build")

  /** Get a file relative to the project directory. */
  fun getFile(relativePath: String): File = File(projectDir, relativePath)

  /** Check if a file exists in the test project. */
  fun fileExists(relativePath: String): Boolean = File(projectDir, relativePath).exists()

  companion object {
    /** Create a new test helper with a temporary project directory. */
    fun createWithTempDir(): GradleTestKitHelper {
      val tempDir = createTempDir()
      return GradleTestKitHelper(tempDir)
    }

    /** Create a new test helper with a specific project directory. */
    fun create(projectDir: File): GradleTestKitHelper {
      return GradleTestKitHelper(projectDir)
    }
  }
}
