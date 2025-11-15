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
package com.github.c64lib.rbt.crunchers.exomizer.adapters.`in`.gradle

import com.github.c64lib.rbt.crunchers.exomizer.domain.CrunchMemCommand
import com.github.c64lib.rbt.crunchers.exomizer.domain.MemOptions
import com.github.c64lib.rbt.crunchers.exomizer.domain.RawOptions
import com.github.c64lib.rbt.crunchers.exomizer.usecase.CrunchMemUseCase
import com.github.c64lib.rbt.crunchers.exomizer.usecase.port.ExecuteExomizerPort
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task for Exomizer memory mode compression.
 *
 * Exposes all raw mode options plus memory-specific options as Gradle task properties.
 */
abstract class CrunchMem @Inject constructor(private val port: ExecuteExomizerPort) :
    DefaultTask() {

  @get:InputFile abstract val input: RegularFileProperty

  @get:OutputFile abstract val output: RegularFileProperty

  // Memory-specific options
  @get:Input @get:Optional abstract val loadAddress: Property<String>

  @get:Input @get:Optional abstract val forward: Property<Boolean>

  // Raw mode options
  @get:Input @get:Optional abstract val backwards: Property<Boolean>

  @get:Input @get:Optional abstract val reverse: Property<Boolean>

  @get:Input @get:Optional abstract val decrunch: Property<Boolean>

  @get:Input @get:Optional abstract val compatibility: Property<Boolean>

  @get:Input @get:Optional abstract val speedOverRatio: Property<Boolean>

  @get:Input @get:Optional abstract val encoding: Property<String>

  @get:Input @get:Optional abstract val skipEncoding: Property<Boolean>

  @get:Input @get:Optional abstract val maxOffset: Property<Int>

  @get:Input @get:Optional abstract val maxLength: Property<Int>

  @get:Input @get:Optional abstract val passes: Property<Int>

  @get:Input @get:Optional abstract val bitStreamTraits: Property<Int>

  @get:Input @get:Optional abstract val bitStreamFormat: Property<Int>

  @get:Input @get:Optional abstract val controlAddresses: Property<String>

  @get:Input @get:Optional abstract val quiet: Property<Boolean>

  @get:Input @get:Optional abstract val brief: Property<Boolean>

  @TaskAction
  fun crunch() {
    val useCase = CrunchMemUseCase(port)

    val rawOptions =
        RawOptions(
            backwards = backwards.getOrElse(false),
            reverse = reverse.getOrElse(false),
            decrunch = decrunch.getOrElse(false),
            compatibility = compatibility.getOrElse(false),
            speedOverRatio = speedOverRatio.getOrElse(false),
            encoding = encoding.orNull,
            skipEncoding = skipEncoding.getOrElse(false),
            maxOffset = maxOffset.getOrElse(65535),
            maxLength = maxLength.getOrElse(65535),
            passes = passes.getOrElse(100),
            bitStreamTraits = bitStreamTraits.orNull,
            bitStreamFormat = bitStreamFormat.orNull,
            controlAddresses = controlAddresses.orNull,
            quiet = quiet.getOrElse(false),
            brief = brief.getOrElse(false))

    val memOptions =
        MemOptions(
            rawOptions = rawOptions,
            loadAddress = loadAddress.getOrElse("auto"),
            forward = forward.getOrElse(false))

    val command =
        CrunchMemCommand(
            source = input.asFile.get(), output = output.asFile.get(), options = memOptions)

    useCase.apply(command)
  }
}
