plugins {
    id("rbt.domain")
}

group = "com.github.c64lib.retro-assembler"

dependencies {
    implementation(project(":shared:domain"))
    // TestResult value object crosses into the TestPort signature (PLAN-0010 design decision).
    implementation(project(":testing:64spec"))
    testImplementation(project(":flows:adapters:out:charpad"))
    testImplementation(project(":flows:adapters:out:spritepad"))
}
