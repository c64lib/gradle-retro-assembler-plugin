plugins {
  id("rbt.adapter.inbound.gradle")
}

group = "com.github.c64lib.retro-assembler.testing.64spec.in"

dependencies {
  implementation(project(":testing:64spec"))
  implementation(project(":shared:gradle"))
}
