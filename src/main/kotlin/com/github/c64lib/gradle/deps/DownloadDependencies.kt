package com.github.c64lib.gradle.deps

import com.github.c64lib.gradle.GROUP_BUILD
import com.github.c64lib.gradle.RetroAssemblerPluginExtension
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DownloadDependencies : DefaultTask() {

    init {
        description = "Downloads and unzips library dependency"
        group = GROUP_BUILD
    }

    lateinit var extension: RetroAssemblerPluginExtension

    @TaskAction
    fun downloadAndUnzip() {
        extension.dependencies.forEach { dependency ->
            val downloadTask = project.tasks.create("_c64lib_download_${dependency.type}:${dependency.name}@${dependency.version}", Download::class.java)
            val archive = when (dependency.type) {
                DependencyType.GitHub -> configureGitHub(dependency, downloadTask)
            }
            downloadTask.download();
            unzip(archive, archive.parentFile)
            archive.delete()
        }
    }

    private fun configureGitHub(dependency: Dependency, downloadTask: Download): File {
        val url = "https://github.com/${dependency.name}/archive/${dependency.version}.tar.gz"
        downloadTask.src(url)
        val dest = project.file(".ra/deps/${dependency.name}/${dependency.version}.tar.gz")
        downloadTask.dest(dest)
        return dest
    }

    private fun unzip(what: File, where: File) {
        val files = project.tarTree(what)
        project.copy { spec ->
            spec.from(files)
            spec.into(where)
            spec.eachFile { it ->
                val index = it.path.indexOf('/')
                if (index >= 0) {
                    val path = it.path.substring(index + 1)
                    it.path = path
                } else {
                    it.exclude()
                }
            }
        }
    }
}