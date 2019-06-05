package com.github.c64lib.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class KickAssemblerPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create<KickAssemblerPluginExtension>("cbmProject", KickAssemblerPluginExtension::class.java, project)

        project.afterEvaluate {
            val download = project.tasks.create("downloadKickAss", DownloadKickAss::class.java)
            val assemble = project.tasks.create("assemble", Assemble::class.java) { task ->
                System.out.println(extension.libDir.absolutePath);
                task.kaJar = extension.kaJar
                task.libDir = extension.libDir
            }
            assemble.dependsOn(download);

            project.tasks.create("clean", CleanKickFiles::class.java)
        }
    }
}

