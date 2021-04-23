val kotlinVersion: String by project
val vavrVersion: String by project
val gradleDownloadTaskVersion: String by project
val tagPropertyName = "tag"

// We need to bundle classes of all modules into this single JAR,
// otherwise they are not published with plugins portal publisher plugin.
// Specify references to all needed subprojects here.
val localDependencies = arrayOf("../../domain")

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.14.0"
    id("com.diffplug.gradle.spotless")
}

group = "com.github.c64lib.retro-assembler"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks {
    val copySubProjectClasses by register<Copy>("copySubProjectClasses") {
        from(localDependencies.map { "$it/build/classes" })
        into("build/classes")
        exclude("**/META-INF/*")
        group = "build"
    }
    named("jar") {
        dependsOn(copySubProjectClasses)
    }
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
        create("simplePlugin") {
            id = "com.github.c64lib.retro-assembler"
            displayName = "Retro Assembler Plugin"
            description =
                "Embeds various 6502 assemblers and provides basic functionality to have builds for your beloved C64"
            implementationClass = "com.github.c64lib.gradle.RetroAssemblerPlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("de.undercouch:gradle-download-task:$gradleDownloadTaskVersion")
    implementation("io.vavr:vavr:$vavrVersion")
    compileOnly(project(":domain"))
}

publishing {
    repositories {
        maven {
            url = uri("../../../consuming/maven-repo")
        }
    }
}
