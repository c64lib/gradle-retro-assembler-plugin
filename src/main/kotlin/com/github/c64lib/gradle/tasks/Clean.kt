package com.github.c64lib.gradle.tasks

import com.github.c64lib.gradle.GROUP_BUILD
import com.github.c64lib.gradle.RetroAssemblerPluginExtension
import com.github.c64lib.gradle.asms.AssemblerFacadeFactory
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskAction

open class Clean : Delete() {

    init {
        description = "Cleans KickAssembler target files"
        group = GROUP_BUILD
    }

    lateinit var extension: RetroAssemblerPluginExtension

    @TaskAction
    override fun clean() {
        delete(project.buildDir)
        val asm = AssemblerFacadeFactory.of(extension.dialect, project)
        delete(asm.targetFiles())
        super.clean()
    }
}
