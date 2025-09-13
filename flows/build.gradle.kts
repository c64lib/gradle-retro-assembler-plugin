plugins {
    id("rbt.domain")
}

group = "com.github.c64lib.retro-assembler"

dependencies {
    implementation(project(":shared:domain"))
    testImplementation(project(":flows:adapters:out:charpad"))
}
