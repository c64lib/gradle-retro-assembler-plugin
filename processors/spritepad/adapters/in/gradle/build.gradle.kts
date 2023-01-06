plugins {
  id("rbt.adapter.inbound.gradle")
}

group = "com.github.c64lib.retro-assembler.processors.spritepad.in"

dependencies {
    implementation(project(":processors:spritepad"))
    implementation(project(":shared:gradle"))
    implementation(project(":shared:binary-utils"))
    implementation(project(":shared:processor"))
}
