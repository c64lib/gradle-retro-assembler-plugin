package com.github.c64lib.gradle

import com.github.c64lib.gradle.asms.AssemblerFacadeFactory
import com.github.c64lib.gradle.asms.Assemblers
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class Assemble : DefaultTask() {

    init {
        description = "Runs assembler over all source files"
        group = ASM
    }

    lateinit var kaJar: File

    lateinit var libDir: File

    @TaskAction
    fun assemble() {
        val asm = AssemblerFacadeFactory.of(Assemblers.KICK_ASSEMBLER, project)
        asm.sourceFiles().forEach { file ->
            asm.assemble(file, kaJar, libDir)
        }
    }
}
