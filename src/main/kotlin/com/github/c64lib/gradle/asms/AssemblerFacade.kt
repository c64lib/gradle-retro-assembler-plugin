package com.github.c64lib.gradle.asms

import com.github.c64lib.gradle.RetroAssemblerPluginExtension
import com.github.c64lib.gradle.asms.kickassembler.KickAssembler
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.process.ExecResult
import java.io.File

enum class Assemblers {
    KickAssembler,
    None
}

interface AssemblerFacade {
    fun resolveDependencies()
    fun targetFiles(): FileCollection
    fun sourceFiles(): FileCollection
    fun assemble(sourceFile: File): ExecResult
}

object AssemblerFacadeFactory {
    fun of(assembler: Assemblers, project: Project, extension: RetroAssemblerPluginExtension): AssemblerFacade =
            when (assembler) {
                Assemblers.KickAssembler -> KickAssembler(project, extension)
                Assemblers.None -> throw GradleException("Assembler dialect is not selected")
            }
}
