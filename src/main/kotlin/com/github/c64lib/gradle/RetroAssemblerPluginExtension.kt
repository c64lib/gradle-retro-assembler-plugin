package com.github.c64lib.gradle

import com.github.c64lib.gradle.asms.Assemblers
import com.github.c64lib.gradle.deps.Dependency
import com.github.c64lib.gradle.deps.DependencyType

const val EXTENSION_DSL_NAME = "retroProject"

open class RetroAssemblerPluginExtension {
    var dialect = Assemblers.None
    var libDirs: Array<String> = emptyArray();
    var srcDirs = arrayOf(".");
    val dependencies: List<Dependency>
        get() = _dependencies

    private var _dependencies: MutableList<Dependency> = ArrayList()

    fun libFromGitHub(name: String, version: String) {
        _dependencies.add(Dependency(DependencyType.GitHub, name, version));
    }
}
