package com.github.c64lib.gradle

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class Assemble : DefaultTask() {

    @Input
    lateinit var kaJar: File

    @Input
    lateinit var libDir: File

    @TaskAction
    fun assemble() {
        println("libDir=" + libDir)
        println("kaJar=" + kaJar)

        project.fileTree(".")
                .filter { element ->
                    element.isFile && element.name.endsWith(".asm")
                }
                .forEach { file ->
                    println(file.path)
                    project.javaexec { spec ->
                        spec.main = "-jar"
                        spec.args = listOf(kaJar.absolutePath, "-libdir", libDir.absolutePath, file.path)
                    }
                }

//        project.fileTree(mapOf(/*"dir" to '.',*/"include" to "**//*.asm"/*, "exclude" to "cpm_modules"*/))
//                .forEach { file ->
//                    println(file.path)
//                    project.javaexec { spec ->
//                        spec.main = "-jar"
//                        spec.args = listOf(kaJar.absolutePath, "-libdir", libDir.absolutePath, file.path)
//                    }
//                }
    }
}
