package com.github.c64lib.gradle

import com.github.c64lib.gradle.asms.AssemblerFacadeFactory
import com.github.c64lib.gradle.asms.Assemblers
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskAction

open class CleanKickFiles() : Delete() {

    init {
        description = "Cleans KickAssembler target files"
        group = GROUP_BUILD
    }

    @TaskAction
    override fun clean() {
        delete(project.buildDir)
        val asm = AssemblerFacadeFactory.of(Assemblers.KICK_ASSEMBLER, project)
        delete(asm.targetFiles())
        super.clean()
    }
}
