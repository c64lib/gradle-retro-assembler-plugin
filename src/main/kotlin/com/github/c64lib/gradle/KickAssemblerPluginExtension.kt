package com.github.c64lib.gradle

import org.gradle.api.Project
import java.io.File

class KickAssemblerPluginExtension(project: Project) {
    var kaJar = File(project.buildDir, "ka/KickAss.jar")
    var libDir = File("..")
}
