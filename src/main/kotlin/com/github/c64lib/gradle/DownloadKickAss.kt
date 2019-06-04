package com.github.c64lib.gradle

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.tasks.TaskAction

open class DownloadKickAss : Download() {

    @TaskAction
    override fun download() {
        src("https://github.com/c64lib/asm-ka/releases/download/5.6/KickAss.jar");
        dest("ka/KickAss.jar");
        super.download()
    }
}
