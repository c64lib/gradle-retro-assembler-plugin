package com.github.c64lib.gradle

import com.github.c64lib.gradle.asms.AssemblerFacadeFactory
import com.github.c64lib.gradle.asms.Assemblers
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.tasks.TaskAction

open class ResolveDevDeps : Download() {

    init {
        description = "Downloads KickAssembler dependency"
        group = GROUP_BUILD
    }

    @TaskAction
    override fun download() =
            AssemblerFacadeFactory.of(Assemblers.KICK_ASSEMBLER, project).resolveDependencies();
}
