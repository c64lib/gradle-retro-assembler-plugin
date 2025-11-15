plugins {
  id("rbt.adapter.outbound.gradle")
}

group = "com.github.c64lib.retro-assembler.compilers.dasm.out"

dependencies {
  implementation(project(":compilers:dasm"))
  implementation(project(":shared:domain"))
}
