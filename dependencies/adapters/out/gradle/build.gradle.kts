plugins {
  id("rbt.adapter.outbound.gradle")
}

group = "com.github.c64lib.retro-assembler.dependencies.out"

dependencies {
  implementation(project(":dependencies"))
  implementation(project(":shared:domain"))
  implementation(project(":shared:filedownload"))
  implementation(project(":shared:gradle"))
}
