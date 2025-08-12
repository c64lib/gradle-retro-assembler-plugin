plugins {
  id("rbt.adapter.outbound.gradle")
}

group = "com.github.c64lib.retro-assembler.flows.out"

dependencies {
  implementation(project(":flows"))
  implementation(project(":shared:gradle"))
  implementation(project(":shared:domain"))
}
