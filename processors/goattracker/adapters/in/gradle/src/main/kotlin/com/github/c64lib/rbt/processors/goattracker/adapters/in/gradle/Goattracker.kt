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
package com.github.c64lib.rbt.processors.goattracker.adapters.`in`.gradle

import com.github.c64lib.rbt.processors.goattracker.usecase.PackSongCommand
import com.github.c64lib.rbt.processors.goattracker.usecase.PackSongUseCase
import com.github.c64lib.rbt.shared.gradle.GROUP_BUILD
import com.github.c64lib.rbt.shared.gradle.PreprocessingExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

open class Goattracker : DefaultTask() {

  init {
    description = "Converts Goattracker song files (SNG) into SID files."
    group = GROUP_BUILD
  }

  @Input lateinit var preprocessingExtension: PreprocessingExtension

  @Internal lateinit var packSongUseCase: PackSongUseCase

  @TaskAction
  fun process() {
    preprocessingExtension.goattrackerPipelines.forEach { pipeline ->
      pipeline.outputs.forEach { output ->
        packSongUseCase.apply(
            PackSongCommand(
                source = pipeline.getInput().get(),
                output = output.getOutput().get(),
                executable = output.executable,
                useBuildDir = pipeline.getUseBuildDir().getOrElse(false),
                bufferedSidWrites = output.bufferedSidWrites,
                disableOptimization = output.disableOptimization,
                playerMemoryLocation = output.playerMemoryLocation,
                sfxSupport = output.sfxSupport,
                sidMemoryLocation = output.sidMemoryLocation,
                storeAuthorInfo = output.storeAuthorInfo,
                volumeChangeSupport = output.volumeChangeSupport,
                zeroPageLocation = output.zeroPageLocation,
                zeropageGhostRegisters = output.zeropageGhostRegisters))
      }
    }
  }
}
