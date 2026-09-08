plugins {
  id("rbt.adapter.outbound.gradle")
}

group = "com.github.c64lib.retro-assembler.compilers.64tass.out"

dependencies {
  implementation(project(":compilers:64tass"))
  implementation(project(":shared:domain"))
}
