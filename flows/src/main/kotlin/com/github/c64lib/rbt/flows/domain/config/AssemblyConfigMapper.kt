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
package com.github.c64lib.rbt.flows.domain.config

import com.github.c64lib.rbt.shared.domain.OutputFormat
import java.io.File

/**
 * Domain abstraction for assembly compilation commands.
 *
 * This interface abstracts away the specific compiler implementation details and provides a clean
 * domain boundary for assembly operations.
 */
data class AssemblyCommand(
    val libDirs: List<File>,
    val defines: List<String>,
    val values: Map<String, String>,
    val source: File,
    val outputFormat: OutputFormat,
    val outputFile: File? = null,
    val outputDirectory: File? = null
)

/**
 * Maps AssemblyConfig from the flows domain to AssemblyCommand.
 *
 * This mapper handles the structural differences between the domain configuration and the assembly
 * command format, while maintaining architectural boundaries.
 */
class AssemblyConfigMapper {

  /**
   * Creates an AssemblyCommand from AssemblyConfig and execution context.
   *
   * @param config The domain assembly configuration
   * @param sourceFile The specific source file to compile
   * @param projectRootDir The project root directory for resolving relative paths
   * @return AssemblyCommand ready for execution
   */
  fun toAssemblyCommand(
      config: AssemblyConfig,
      sourceFile: File,
      projectRootDir: File
  ): AssemblyCommand {
    return AssemblyCommand(
        libDirs = mapLibraryDirectories(config.includePaths, projectRootDir),
        defines = extractDefineNames(config.defines),
        values = extractDefineValues(config.defines),
        source = sourceFile,
        outputFormat = config.outputFormat)
  }

  /** Converts string include paths to File objects resolved against project root. */
  private fun mapLibraryDirectories(includePaths: List<String>, projectRoot: File): List<File> {
    return includePaths
        .map { path ->
          if (File(path).isAbsolute) {
            File(path)
          } else {
            File(projectRoot, path)
          }
        }
        .filter { it.exists() && it.isDirectory }
  }

  /**
   * Extracts define names (keys) from the defines map for preprocessor definitions. These become
   * command-line defines like -define NAME
   */
  private fun extractDefineNames(defines: Map<String, String>): List<String> {
    return defines.keys.toList()
  }

  /**
   * Extracts define values from the defines map for variable assignments. These become command-line
   * values like -symbolfile VALUE
   */
  private fun extractDefineValues(defines: Map<String, String>): Map<String, String> {
    // For now, we pass the same map as values
    // In the future, we might want to separate pure defines (no value)
    // from variable assignments (with values)
    return defines.filterValues { it.isNotEmpty() }
  }

  /**
   * Creates multiple AssemblyCommands for a list of source files. This is useful when an
   * AssemblyStep processes multiple input files.
   */
  fun toAssemblyCommands(
      config: AssemblyConfig,
      sourceFiles: List<File>,
      projectRootDir: File
  ): List<AssemblyCommand> {
    return sourceFiles.map { sourceFile -> toAssemblyCommand(config, sourceFile, projectRootDir) }
  }

  /**
   * Discovers source files based on AssemblyConfig file patterns. This method replicates the file
   * discovery logic from the existing Assemble task.
   *
   * @param config The assembly configuration containing srcDirs, includes, and excludes
   * @param projectRootDir The project root directory for resolving relative paths
   * @return List of discovered source files ready for compilation
   */
  fun discoverSourceFiles(config: AssemblyConfig, projectRootDir: File): List<File> {
    return config.srcDirs
        .map { srcDir ->
          val srcDirectory =
              if (File(srcDir).isAbsolute) {
                File(srcDir)
              } else {
                File(projectRootDir, srcDir)
              }

          if (!srcDirectory.exists() || !srcDirectory.isDirectory) {
            emptyList<File>()
          } else {
            findMatchingFiles(srcDirectory, config.includes, config.excludes)
          }
        }
        .flatten()
        .distinct()
  }

  /**
   * Finds files in a directory that match include patterns and don't match exclude patterns. Uses
   * Gradle-style glob patterns for matching.
   */
  private fun findMatchingFiles(
      directory: File,
      includes: List<String>,
      excludes: List<String>
  ): List<File> {
    val allFiles = directory.walkTopDown().filter { it.isFile }.toList()

    return allFiles.filter { file ->
      val relativePath = file.relativeTo(directory).path.replace(File.separator, "/")

      // Must match at least one include pattern
      val matchesInclude = includes.any { pattern -> matchesGlobPattern(relativePath, pattern) }

      // Must not match any exclude pattern
      val matchesExclude = excludes.any { pattern -> matchesGlobPattern(relativePath, pattern) }

      matchesInclude && !matchesExclude
    }
  }

  /**
   * Simple glob pattern matching for file paths. Supports ** for recursive directory matching and *
   * for single-level matching.
   */
  private fun matchesGlobPattern(path: String, pattern: String): Boolean {
    // For patterns like "lib/**/*.asm", we need special handling
    if (pattern.contains("**")) {
      val parts = pattern.split("**")
      if (parts.size == 2) {
        val prefix = parts[0]
        val suffix = parts[1].removePrefix("/")

        // Path must start with prefix (if any)
        if (prefix.isNotEmpty() && !path.startsWith(prefix)) {
          return false
        }

        // Get the part after prefix
        val pathAfterPrefix =
            if (prefix.isNotEmpty()) {
              path.substring(prefix.length).removePrefix("/")
            } else {
              path
            }

        // Check if any part of the remaining path matches the suffix pattern
        if (suffix.isEmpty()) return true

        // For suffix like "*.asm", check filename directly or any subdirectory
        return pathAfterPrefix.split("/").any { segment -> matchesSimpleGlob(segment, suffix) }
      }
    }

    // Simple pattern without **
    return matchesSimpleGlob(path, pattern)
  }

  private fun matchesSimpleGlob(text: String, pattern: String): Boolean {
    val regex = pattern.replace(".", "\\.").replace("*", ".*").let { "^$it$" }
    return text.matches(Regex(regex))
  }

  /**
   * Creates AssemblyCommands by discovering source files from configuration patterns. This is
   * useful when input files are not explicitly specified but should be discovered based on the
   * configuration's file patterns.
   *
   * @param config The assembly configuration
   * @param projectRootDir The project root directory
   * @return List of AssemblyCommands for all discovered source files
   */
  fun toAssemblyCommandsFromPatterns(
      config: AssemblyConfig,
      projectRootDir: File
  ): List<AssemblyCommand> {
    val sourceFiles = discoverSourceFiles(config, projectRootDir)
    return toAssemblyCommands(config, sourceFiles, projectRootDir)
  }

  /**
   * Discovers additional input files based on additionalInputs patterns. These are indirect
   * dependencies like include/import files that should be watched for changes.
   *
   * @param config The assembly configuration containing additionalInputs patterns
   * @param projectRootDir The project root directory for resolving relative paths
   * @return List of additional input files for dependency tracking
   */
  fun discoverAdditionalInputFiles(config: AssemblyConfig, projectRootDir: File): List<File> {
    if (config.additionalInputs.isEmpty()) {
      return emptyList()
    }

    return config.srcDirs
        .map { srcDir ->
          val srcDirectory =
              if (File(srcDir).isAbsolute) {
                File(srcDir)
              } else {
                File(projectRootDir, srcDir)
              }

          if (!srcDirectory.exists() || !srcDirectory.isDirectory) {
            emptyList()
          } else {
            findMatchingFiles(srcDirectory, config.additionalInputs, emptyList())
          }
        }
        .flatten()
        .distinct()
        .filter { it.exists() && it.isFile }
  }
}
