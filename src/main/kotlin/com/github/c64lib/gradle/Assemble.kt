package com.github.c64lib.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class Assemble : DefaultTask() {

    @Input
    var kaJar = ""

    @Input
    var libDir = ""

    @TaskAction
    fun assemble() {
        project.fileTree(mapOf("dir" to '.', "include" to "**/*.asm", "exclude" to "cpm_modules"))
                .forEach { file ->
                    println(file.path)
                    project.javaexec { spec ->
                        spec.main = "-jar"
                        spec.args = listOf(kaJar, "-libdir", libDir, file.path)
                    }
                }
    }
}
