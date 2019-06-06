package com.github.c64lib.gradle.asms.kickassembler

import com.github.c64lib.gradle.RetroAssemblerPluginExtension
import com.github.c64lib.gradle.TASK_DOWNLOAD
import com.github.c64lib.gradle.asms.AssemblerFacade
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import java.io.File

class KickAssembler(private val project: Project) : AssemblerFacade {

    private val kaFile = project.file(".ra/asms/ka/5.6/KickAss.jar")

    override fun resolveDependencies() {
        if (!kaFile.exists()) {
            val downloadTask = project.tasks.getByPath(TASK_DOWNLOAD) as Download
            downloadTask.src("https://github.com/c64lib/asm-ka/releases/download/5.6/KickAss.jar");
            downloadTask.dest(kaFile);
            downloadTask.download();
        }
    }

    override fun sourceFiles(): FileCollection =
            project.fileTree(".").filter { element ->
                element.isFile && element.name.endsWith(".asm")
            }

    override fun targetFiles(): FileCollection =
            project.fileTree(".").matching { pattern ->
                pattern.include("**/*.prg", "**/*.sym")
            }

    override fun assemble(sourceFile: File, extension: RetroAssemblerPluginExtension) =
            project.javaexec { spec ->
                spec.main = "-jar"
                spec.args = listOf(
                        listOf(kaFile.absolutePath),
                        asLibDirList(extension.libDirs),
                        listOf(sourceFile.path)).flatten()
                println(spec.args)
            }

    private fun asLibDirList(libDirs: Array<String>) =
            libDirs.flatMap { file -> listOf("-libdir", project.file(file).absolutePath) }
}
