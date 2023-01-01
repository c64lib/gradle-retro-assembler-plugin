plugins {
  id("rbt.kotlin")
}

group = "com.github.c64lib.retro-assembler.compilers.kickass.out"

dependencies {
  implementation(project(":compilers:kickass"))
  implementation(project(":shared:filedownload"))
}
