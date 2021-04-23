val kotlinVersion: String by project
val vavrVersion: String by project

plugins {
    kotlin("jvm")
    id("com.diffplug.gradle.spotless")
}

group = "com.github.c64lib.retro-assembler"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

spotless {
    kotlin {
        endWithNewline()
        trimTrailingWhitespace()
        licenseHeaderFile(file("../../LICENSE"))
    }
    kotlinGradle {
        ktlint()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("io.vavr:vavr:$vavrVersion")
    implementation(project(":domain"))
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}
