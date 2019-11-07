/*
 * MIT License
 *
 * Copyright (c) 2018-2019 c64lib: The Ultimate Commodore 64 Library
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.c64lib.gradle

import com.github.c64lib.gradle.asms.Assemblers
import com.github.c64lib.gradle.deps.Dependency
import com.github.c64lib.gradle.deps.DependencyType

const val EXTENSION_DSL_NAME = "retroProject"
const val DIALECT_VERSION_LATEST = "latest";

open class RetroAssemblerPluginExtension {

    var workDir = ".ra"
    var dialect = Assemblers.None
    var dialectVersion = DIALECT_VERSION_LATEST
    var libDirs: Array<String> = emptyArray();
    var srcDirs = arrayOf(".");
    var includes: Array<String> = arrayOf("**/*.asm");
    var excludes: Array<String> = arrayOf("$workDir/**/*.asm")

    var viceExecutable = "x64"
    var specDirs = arrayOf("spec")
    var specIncludes: Array<String> = arrayOf("**/*.spec.asm")

    val dependencies: List<Dependency>
        get() = _dependencies

    private var _dependencies: MutableList<Dependency> = ArrayList()

    fun libFromGitHub(name: String, version: String) {
        _dependencies.add(Dependency(DependencyType.GitHub, name, version));
    }
}