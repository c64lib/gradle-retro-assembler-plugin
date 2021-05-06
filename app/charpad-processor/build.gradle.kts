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
        ktfmt()
        endWithNewline()
        trimTrailingWhitespace()
        licenseHeaderFile(file("../../LICENSE"))
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("io.vavr:vavr:$vavrVersion")
    implementation("io.vavr:vavr-kotlin:$vavrKotlinVersion")
    implementation(project(":domain"))
    implementation(project(":app:binary-utils"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
