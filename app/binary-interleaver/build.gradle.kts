val kotlinVersion: String by project
val junitVersion: String by project
val vavrVersion: String by project
val vavrKotlinVersion: String by project

plugins {
    id("rbt.kotlin")
    id("com.diffplug.spotless")
}

group = "com.github.c64lib.retro-assembler"


spotless {
    kotlin {
        endWithNewline()
        trimTrailingWhitespace()
        ktfmt()
        licenseHeaderFile(file("../../LICENSE"))
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("io.vavr:vavr:$vavrVersion")
    implementation("io.vavr:vavr-kotlin:$vavrKotlinVersion")
    implementation(project(":domain"))
    implementation(project(":app:binary-utils"))
    implementation(project(":app:processor-commons"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
}

tasks.withType<Test> { useJUnitPlatform() }
