val kotlinVersion: String by project
val gradleDownloadTaskVersion: String by project
val tagPropertyName = "tag"


plugins {
    kotlin("jvm") version "1.3.70"
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.10.1"
    id("com.diffplug.gradle.spotless") version "3.23.0"
}

group = "com.github.c64lib"
version = "1.1.0-SNAPSHOT"

if (project.hasProperty(tagPropertyName)) {
    version = project.property(tagPropertyName) ?: version
}

pluginBundle {
    website = "https://github.com/c64lib/gradle-retro-assembler-plugin"
    vcsUrl = "https://github.com/c64lib/gradle-retro-assembler-plugin"
    tags = listOf("assembly", "65xx", "mos6502", "mos6510")
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "com.github.c64lib.retro-assembler"
            displayName = "Retro Assembler Plugin"
            description = "Embeds various 6502 assemblers and provides basic functionality to have builds for your beloved C64"
            implementationClass = "com.github.c64lib.gradle.RetroAssemblerPlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    implementation("de.undercouch:gradle-download-task:${gradleDownloadTaskVersion}")
}

repositories {
    jcenter()
}

publishing {
    repositories {
        maven {
            url = uri("../consuming/maven-repo")
        }
    }
}
