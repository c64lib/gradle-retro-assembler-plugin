plugins {
  id("rbt.adapter.outbound.gradle")
}

group = "com.github.c64lib.retro-assembler.flows.out"

dependencies {
  implementation(project(":flows"))
  implementation(project(":processors:charpad"))
  implementation(project(":shared:domain"))
}
