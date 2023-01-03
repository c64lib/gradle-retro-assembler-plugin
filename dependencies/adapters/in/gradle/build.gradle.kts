plugins {
  id("rbt.adapter.inbound.gradle")
}

group = "com.github.c64lib.retro-assembler.dependencies.in"

dependencies {
  implementation(project(":shared:domain"))
  implementation(project(":shared:gradle"))
  implementation(project(":dependencies"))
}
