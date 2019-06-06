package com.github.c64lib.gradle

import com.github.c64lib.gradle.asms.Assemblers
import org.gradle.api.Project

const val EXTENSION_DSL_NAME = "retroProject"

open class RetroAssemblerPluginExtension {
    var dialect = Assemblers.None
    var libDirs: Array<String> = emptyArray();
}
