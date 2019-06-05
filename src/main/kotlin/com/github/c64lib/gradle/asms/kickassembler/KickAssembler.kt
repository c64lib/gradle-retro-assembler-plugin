package com.github.c64lib.gradle.asms.kickassembler

import com.github.c64lib.gradle.asms.AssemblerFacade
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import java.io.File

class KickAssembler(private val project: Project) : AssemblerFacade {
    override fun sourceFiles(): FileCollection =
            project.fileTree(".").filter { element ->
                element.isFile && element.name.endsWith(".asm")
            }

    override fun targetFiles(): FileCollection =
            project.fileTree(".").matching { pattern ->
                pattern.include("**/*.prg", "**/*.sym")
            }

    override fun assemble(sourceFile: File, kaJar: File, libDir: File) =
            project.javaexec { spec ->
                spec.main = "-jar"
                spec.args = listOf(kaJar.absolutePath, "-libdir", libDir.absolutePath, sourceFile.path)
            }
}
