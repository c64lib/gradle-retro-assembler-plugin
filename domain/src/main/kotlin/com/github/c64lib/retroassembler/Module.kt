package com.github.c64lib.retroassembler

import io.vavr.collection.List.empty
import io.vavr.collection.Seq
import java.nio.file.Path

data class Module(
    val name: String,
    val children: Seq<Module> = empty(),
    val dependencies: Seq<Dependency> = empty(),
    val sourceFiles: Seq<Path> = empty(),
    val testFiles: Seq<Path> = empty()
)
