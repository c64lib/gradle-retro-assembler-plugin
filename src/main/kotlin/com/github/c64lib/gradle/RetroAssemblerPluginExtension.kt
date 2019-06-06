package com.github.c64lib.gradle

import com.github.c64lib.gradle.asms.Assemblers

const val EXTENSION_DSL_NAME = "retroProject"

open class RetroAssemblerPluginExtension {
    var dialect = Assemblers.None
    var libDirs: Array<String> = emptyArray();
    var srcDirs = arrayOf(".");
}
