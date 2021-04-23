val kotlinVersion: String by project
val vavrVersion: String by project
val vavrKotlinVersion: String by project
val tagPropertyName = "tag"

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
        indentWithSpaces(4)
        endWithNewline()
        ktfmt()
        licenseHeaderFile(file("../LICENSE"))
    }
    kotlinGradle {
        ktlint()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("io.vavr:vavr:$vavrVersion")
    implementation("io.vavr:vavr-kotlin:$vavrKotlinVersion")
}
