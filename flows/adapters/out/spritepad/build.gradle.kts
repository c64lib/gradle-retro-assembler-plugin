plugins {
  id("rbt.adapter.outbound.gradle")
}

group = "com.github.c64lib.retro-assembler.flows.out"

dependencies {
  implementation(project(":flows"))
  implementation(project(":processors:spritepad"))
  implementation(project(":shared:domain"))
  implementation(project(":shared:processor"))
  implementation(project(":shared:gradle"))
}
