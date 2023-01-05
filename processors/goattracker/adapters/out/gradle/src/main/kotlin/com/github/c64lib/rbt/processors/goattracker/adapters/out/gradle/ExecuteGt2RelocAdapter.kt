/*
MIT License

Copyright (c) 2018-2023 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.rbt.processors.goattracker.adapters.out.gradle

import com.github.c64lib.rbt.processors.goattracker.usecase.PackSongCommand
import com.github.c64lib.rbt.processors.goattracker.usecase.port.ExecuteGt2RelocPort
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.stream.*
import org.gradle.api.Project

class ExecuteGt2RelocAdapter(private val project: Project) : ExecuteGt2RelocPort {
  override fun execute(packSongCommand: PackSongCommand) {
    val output = ByteArrayOutputStream()
    val result =
        project.exec {
          it.commandLine = buildCommandLine(packSongCommand)
          it.standardOutput = output

          val commandLine = it.commandLine.stream().collect(Collectors.joining(" "))
          println("Executing Gt2Reloc: $commandLine")
        }
    if (result.exitValue != 0) {
      println("Gt2Reloc returned with error:\n $output")
    }
    result.assertNormalExitValue()
  }

  private fun buildCommandLine(packSongCommand: PackSongCommand): List<String> {
    val cli = mutableListOf(packSongCommand.executable)
    cli += relativize(packSongCommand.source)
    cli += relativize(normalize(packSongCommand.output, packSongCommand.useBuildDir))

    packSongCommand.bufferedSidWrites?.let { cli += toBooleanFlag("B", it) }
    packSongCommand.zeroPageLocation?.let { cli += toHexFlag("Z", it) }
    packSongCommand.zeropageGhostRegisters?.let { cli += toBooleanFlag("C", it) }
    packSongCommand.sfxSupport?.let { cli += toBooleanFlag("D", it) }
    packSongCommand.volumeChangeSupport?.let { cli += toBooleanFlag("E", it) }
    packSongCommand.storeAuthorInfo?.let { cli += toBooleanFlag("H", it) }
    packSongCommand.disableOptimization?.let { cli += toBooleanFlag("I", it) }
    packSongCommand.playerMemoryLocation?.let { cli += toHexFlag("W", it) }
    packSongCommand.sidMemoryLocation?.let { cli += toHexFlag("L", it) }
    return cli.toList()
  }

  private fun toBooleanValue(flagValue: Boolean) =
      when (flagValue) {
        true -> "1"
        false -> "0"
      }

  private fun toHexValue(value: Int) = value.toString(16)

  private fun toBooleanFlag(flagName: String, flagValue: Boolean): String =
      "-$flagName" + toBooleanValue(flagValue)

  private fun toHexFlag(flagName: String, value: Int): String = "-$flagName" + toHexValue(value)

  private fun normalize(output: File, outputToBuildDir: Boolean): File =
      if (outputToBuildDir) {
        val outputRelativePath =
            project.layout.projectDirectory.asFile.toPath().relativize(output.toPath())
        val resultPath =
            project.layout.buildDirectory
                .dir("goattracker")
                .get()
                .asFile
                .toPath()
                .resolve(outputRelativePath)
        resultPath.parent?.toFile()?.mkdirs()
        resultPath.toFile()
      } else {
        output
      }

  private fun relativize(file: File): String =
      project.projectDir.toURI().relativize(file.toURI()).path
}
