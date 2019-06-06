package com.github.c64lib.gradle.tasks

import com.github.c64lib.gradle.GROUP_BUILD
import com.github.c64lib.gradle.RetroAssemblerPluginExtension
import com.github.c64lib.gradle.asms.AssemblerFacadeFactory
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.tasks.TaskAction

open class ResolveDevDeps : Download() {

    init {
        description = "Downloads KickAssembler dependency"
        group = GROUP_BUILD
    }

    lateinit var extension: RetroAssemblerPluginExtension

    @TaskAction
    override fun download() =
            AssemblerFacadeFactory.of(extension.dialect, project, extension).resolveDependencies();
}
