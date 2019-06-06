package com.github.c64lib.gradle.tasks

import com.github.c64lib.gradle.GROUP_BUILD
import com.github.c64lib.gradle.RetroAssemblerPluginExtension
import com.github.c64lib.gradle.asms.AssemblerFacadeFactory
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class Assemble : DefaultTask() {

    init {
        description = "Runs assembler over all source files"
        group = GROUP_BUILD
    }

    lateinit var extension: RetroAssemblerPluginExtension

    @TaskAction
    fun assemble() {
        val asm = AssemblerFacadeFactory.of(extension.dialect, project, extension)
        asm.sourceFiles().forEach { file ->
            asm.assemble(file)
        }
    }
}
