package com.github.c64lib.gradle

import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskAction

open class CleanKickFiles() : Delete() {

    @TaskAction
    override fun clean() {
        delete(project.buildDir)
        delete(project.fileTree(".").matching { pattern ->
            pattern.include("**/*.prg", "**/*.sym")
        })
        super.clean()
    }
}
