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

import java.io.File

/**
 * Maps DasmConfig from the flows domain to DasmCommand.
 *
 * This mapper handles the structural differences between the domain configuration and the dasm
 * command format, while maintaining architectural boundaries.
 */
class DasmConfigMapper {

  /**
   * Creates a DasmCommand from DasmConfig and execution context.
   *
   * @param config The domain dasm configuration
   * @param sourceFile The specific source file to compile
   * @param projectRootDir The project root directory for resolving relative paths
   * @param outputPath Optional output path from step configuration (from DSL 'to' method)
   * @return DasmCommand ready for execution
   */
  fun toDasmCommand(
      config: DasmConfig,
      sourceFile: File,
      projectRootDir: File,
      outputPath: String? = null
  ): DasmCommand {
    val outputFile = resolveOutputFile(sourceFile, outputPath, projectRootDir)
    val listFile = config.listFile?.let { resolveFilePath(it, projectRootDir) }
    val symbolFile = config.symbolFile?.let { resolveFilePath(it, projectRootDir) }

    return DasmCommand(
        libDirs = mapLibraryDirectories(config.includePaths, projectRootDir),
        defines = config.defines,
        source = sourceFile,
        outputFormat = config.outputFormat,
        outputFile = outputFile,
        listFile = listFile,
        symbolFile = symbolFile,
        verboseness = config.verboseness,
        errorFormat = config.errorFormat,
        strictSyntax = config.strictSyntax,
        removeOnError = config.removeOnError,
        symbolTableSort = config.symbolTableSort)
  }

  /**
   * Resolves the output file path based on step configuration.
   *
   * @param sourceFile Source file being compiled
   * @param outputPath Optional output path from DSL 'to' method
   * @param projectRootDir Project root for resolving relative paths
   * @return The resolved output file
   */
  private fun resolveOutputFile(
      sourceFile: File,
      outputPath: String?,
      projectRootDir: File
  ): File? {
    return if (outputPath != null) {
      // Use explicit output path if provided
      if (File(outputPath).isAbsolute) File(outputPath) else File(projectRootDir, outputPath)
    } else {
      // Derive output from input file (same basename, no extension)
      val baseName = sourceFile.nameWithoutExtension
      File(sourceFile.parentFile, baseName)
    }
  }

  /** Resolves a file path against the project root if it's relative. */
  private fun resolveFilePath(path: String, projectRoot: File): File {
    val file = File(path)
    return if (file.isAbsolute) file else File(projectRoot, path)
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
   * Creates multiple DasmCommands for a list of source files. This is useful when a DasmStep
   * processes multiple input files.
   */
  fun toDasmCommands(
      config: DasmConfig,
      sourceFiles: List<File>,
      projectRootDir: File
  ): List<DasmCommand> {
    return sourceFiles.map { sourceFile -> toDasmCommand(config, sourceFile, projectRootDir) }
  }

  /**
   * Discovers source files based on DasmConfig file patterns.
   *
   * @param config The dasm configuration containing srcDirs, includes, and excludes
   * @param projectRootDir The project root directory for resolving relative paths
   * @return List of discovered source files ready for compilation
   */
  fun discoverSourceFiles(config: DasmConfig, projectRootDir: File): List<File> {
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
   * Discovers additional input files based on glob patterns. This method is used to track indirect
   * dependencies like included/imported files.
   *
   * @param config The dasm configuration containing additionalInputs patterns and srcDirs
   * @param projectRootDir The project root directory for resolving relative paths
   * @return List of discovered additional input files for dependency tracking
   */
  fun discoverAdditionalInputFiles(config: DasmConfig, projectRootDir: File): List<File> {
    if (config.additionalInputs.isEmpty()) {
      return emptyList()
    }

    return config.srcDirs
        .flatMap { srcDir ->
          val srcDirectory =
              if (File(srcDir).isAbsolute) {
                File(srcDir)
              } else {
                File(projectRootDir, srcDir)
              }

          if (!srcDirectory.exists() || !srcDirectory.isDirectory) {
            emptyList<File>()
          } else {
            // Find files matching additional input patterns in this source directory
            config.additionalInputs.flatMap { pattern ->
              findMatchingFilesForPattern(srcDirectory, pattern)
            }
          }
        }
        .distinct()
  }

  /** Finds files matching a single glob pattern. */
  private fun findMatchingFilesForPattern(searchRoot: File, pattern: String): List<File> {
    if (!searchRoot.exists() || !searchRoot.isDirectory) {
      return emptyList()
    }

    val allFiles = searchRoot.walkTopDown().filter { it.isFile }.toList()

    return allFiles.filter { file ->
      val relativePath = file.relativeTo(searchRoot).path.replace(File.separator, "/")
      matchesGlobPattern(relativePath, pattern)
    }
  }
}
