plugins {
  id("rbt.adapter.inbound.gradle")
}

group = "com.github.c64lib.retro-assembler.compilers.kickass.in"

dependencies {
  implementation(project(":compilers:kickass"))
  implementation(project(":shared:gradle"))
}
