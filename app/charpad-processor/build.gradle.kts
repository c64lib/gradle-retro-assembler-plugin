plugins {
    id("rbt.kotlin")
}

group = "com.github.c64lib.retro-assembler"

dependencies {
    implementation(project(":shared:domain"))
    implementation(project(":shared:processor"))
    implementation(project(":shared:binary-utils"))
    testImplementation(project(":shared:testutils"))
}
