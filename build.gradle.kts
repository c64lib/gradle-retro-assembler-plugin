val kotlinVersion: String by project
val gradleDownloadTaskVersion: String by project
val tagPropertyName = "tag"

plugins {
    kotlin("jvm") version "1.4.32"
    id("com.diffplug.gradle.spotless") version "4.5.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

allprojects {

    group = "com.github.c64lib"
    version = "1.1.0-SNAPSHOT"

    if (project.hasProperty(tagPropertyName)) {
        version = project.property(tagPropertyName) ?: version
    }

    repositories {
        jcenter()
    }
}
