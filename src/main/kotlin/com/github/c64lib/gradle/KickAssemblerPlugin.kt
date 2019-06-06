package com.github.c64lib.gradle

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Plugin
import org.gradle.api.Project

class KickAssemblerPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create<KickAssemblerPluginExtension>("cbmProject", KickAssemblerPluginExtension::class.java, project)

        project.afterEvaluate {
            val download = project.tasks.create(TASK_RESOLVE_DEV_DEPENDENCIES, ResolveDevDeps::class.java)
            val assemble = project.tasks.create(TASK_BUILD, Assemble::class.java) { task ->
                task.kaJar = extension.kaJar
                task.libDir = extension.libDir
            }
            assemble.dependsOn(download);

            val clean = project.tasks.create(TASK_CLEAN, CleanKickFiles::class.java)

            project.tasks.create(TASK_DOWNLOAD, Download::class.java)
        }
    }
}
