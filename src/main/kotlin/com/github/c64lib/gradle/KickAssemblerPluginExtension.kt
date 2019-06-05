package com.github.c64lib.gradle

import org.gradle.api.Project
import java.io.File

open class KickAssemblerPluginExtension(project: Project) {
    var kaJar = File( "ka/KickAss.jar")
    var libDir = File("..")
}
