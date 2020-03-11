import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project
val vavrVersion: String by project
val gradleDownloadTaskVersion: String by project
val kotlinxCoroutinesVersion: String by project
val tagPropertyName = "tag"

plugins {
    kotlin("jvm") version "1.3.70"
    id("java-gradle-plugin")
    // id("maven-publish")
    id("com.gradle.plugin-publish") version "0.10.1"
    id("com.diffplug.gradle.spotless") version "3.27.2"
}

group = "com.github.c64lib"
version = "1.1.0-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_1_8

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

if (project.hasProperty(tagPropertyName)) {
    version = project.property(tagPropertyName) ?: version
}

spotless {
    kotlin {
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
}

pluginBundle {
    website = "https://github.com/c64lib/gradle-retro-assembler-plugin"
    vcsUrl = "https://github.com/c64lib/gradle-retro-assembler-plugin"
    tags = listOf("assembly", "65xx", "mos6502", "mos6510")
}

gradlePlugin {
    plugins {
        create("retroAssemblerPlugin") {
            id = "com.github.c64lib.retro-assembler"
            displayName = "Retro Assembler Plugin"
            description = "Embeds various 6502 assemblers and provides basic functionality to have builds for your beloved C64"
            implementationClass = "com.github.c64lib.gradle.RetroAssemblerPlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("io.vavr:vavr:$vavrVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinxCoroutinesVersion")
    implementation("de.undercouch:gradle-download-task:$gradleDownloadTaskVersion")
}

repositories {
    jcenter()
}

// publishing {
//    repositories {
//        maven {
//            url = uri("../consuming/maven-repo")
//        }
//    }
// }
