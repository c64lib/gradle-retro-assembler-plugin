package com.github.c64lib.gradle

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.tasks.TaskAction

open class DownloadKickAss : Download() {

    init {
        description = "Downloads KickAssembler dependency"
        group = DEPS
    }

    @TaskAction
    override fun download() {
        val kaFile = project.file("ka/KickAss.jar")
        if (!kaFile.exists()) {
            src("https://github.com/c64lib/asm-ka/releases/download/5.6/KickAss.jar");
            dest(kaFile);
            super.download()
        }
    }
}
