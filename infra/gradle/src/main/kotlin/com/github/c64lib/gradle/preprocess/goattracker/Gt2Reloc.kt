/*
MIT License

Copyright (c) 2018-2021 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.gradle.preprocess.goattracker

import java.io.File
import org.gradle.api.Project

class Gt2Reloc(private val project: Project) {

  fun run(inputExt: GoattrackerPipelineExtension, musicExt: GoattrackerMusicExtension) {
    val result = project.exec { it.commandLine = buildCommandLine(inputExt, musicExt) }
    result.assertNormalExitValue()
  }

  private fun buildCommandLine(
      inputExt: GoattrackerPipelineExtension,
      musicExt: GoattrackerMusicExtension
  ): List<String> {
    val cli = mutableListOf(musicExt.executable)
    cli += inputExt.getInput().get().absolutePath
    cli +=
        normalize(musicExt.getOutput().get(), inputExt.getUseBuildDir().getOrElse(false))
            .absolutePath
    musicExt.bufferedSidWrites?.let { cli += toBooleanFlag("B", it) }
    musicExt.zeroPageLocation?.let { cli += toHexFlag("Z", it) }
    musicExt.zeropageGhostRegisters?.let { cli += toBooleanFlag("C", it) }
    musicExt.sfxSupport?.let { cli += toBooleanFlag("D", it) }
    musicExt.volumeChangeSupport?.let { cli += toBooleanFlag("E", it) }
    musicExt.storeAuthorInfo?.let { cli += toBooleanFlag("H", it) }
    musicExt.disableOptimization?.let { cli += toBooleanFlag("I", it) }
    musicExt.playerMemoryLocation?.let { cli += toHexFlag("W", it) }
    musicExt.sidMemoryLocation?.let { cli += toHexFlag("L", it) }
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
            project
                .layout
                .buildDirectory
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
}
