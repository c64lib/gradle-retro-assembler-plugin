package com.github.c64lib.gradle.asms

import com.github.c64lib.gradle.asms.kickassembler.KickAssembler
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.process.ExecResult
import java.io.File

enum class Assemblers {
    KICK_ASSEMBLER
}

interface AssemblerFacade {
    fun targetFiles(): FileCollection
    fun sourceFiles(): FileCollection
    fun assemble(sourceFile: File, kaJar: File, libDir: File): ExecResult
}

object AssemblerFacadeFactory {
    fun of(assembler: Assemblers, project: Project): AssemblerFacade =
            when (assembler) {
                Assemblers.KICK_ASSEMBLER -> KickAssembler(project)
            }
}