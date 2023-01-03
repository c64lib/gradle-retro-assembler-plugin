val kotlinVersion: String by project
val vavrVersion: String by project
val kotestVersion: String by project
val junitVersion: String by project

plugins {
    id("rbt.kotlin")
}

group = "com.github.c64lib.retro-assembler"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("io.vavr:vavr:$vavrVersion")
    implementation(project(":shared:domain"))
    implementation(project(":app:binary-utils"))
    implementation(project(":app:processor-commons"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
}

tasks.withType<Test> {
  useJUnitPlatform()
}
