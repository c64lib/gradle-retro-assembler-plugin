package com.github.c64lib.gradle

import com.github.c64lib.gradle.deps.DownloadDependencies
import com.github.c64lib.gradle.tasks.Assemble
import com.github.c64lib.gradle.tasks.Clean
import com.github.c64lib.gradle.tasks.ResolveDevDeps
import org.gradle.api.Plugin
import org.gradle.api.Project

class RetroAssemblerPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create<RetroAssemblerPluginExtension>(EXTENSION_DSL_NAME, RetroAssemblerPluginExtension::class.java)

        project.afterEvaluate {
            val resolveDevDeps = project.tasks.create(TASK_RESOLVE_DEV_DEPENDENCIES, ResolveDevDeps::class.java) { task ->
                task.extension = extension
            }
            val downloadDependencies = project.tasks.create(TASK_DEPENDENCIES, DownloadDependencies::class.java) { task ->
                task.extension = extension
            }
            val assemble = project.tasks.create(TASK_BUILD, Assemble::class.java) { task ->
                task.extension = extension
            }
            assemble.dependsOn(resolveDevDeps, downloadDependencies);

            val clean = project.tasks.create(TASK_CLEAN, Clean::class.java) { task ->
                task.extension = extension
            }
        }
    }
}
