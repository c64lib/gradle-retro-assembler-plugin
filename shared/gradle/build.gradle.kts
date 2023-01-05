plugins {
  id("rbt.kotlin")
}

group = "com.github.c64lib.retro-assembler.shared"

dependencies {
  implementation(gradleApi())
  implementation(project(":shared:domain"))
  implementation(project(":shared:processor"))
  // the one below should go out (move class)
  implementation(project(":emulators:vice"))
}
