plugins {
  id("rbt.domain")
}

group = "com.github.c64lib.retro-assembler"

dependencies {
  implementation(project(":emulators:vice"))
}
