plugins {
  id("rbt.adapter.outbound.gradle")
}

group = "com.github.c64lib.retro-assembler.compilers.kickass"

dependencies {
  implementation(project(":compilers:kickass"))
  implementation(project(":shared:domain"))
  implementation(project(":shared:filedownload"))
}
