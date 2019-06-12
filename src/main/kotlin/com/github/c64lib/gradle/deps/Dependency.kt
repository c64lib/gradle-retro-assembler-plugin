package com.github.c64lib.gradle.deps

data class Dependency(
        val type: DependencyType,
        val name: String,
        val version: String,
        val prefix: String = "") {
}