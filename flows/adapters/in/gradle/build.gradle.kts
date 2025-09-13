plugins {
  id("rbt.adapter.inbound.gradle")
}

group = "com.github.c64lib.retro-assembler.flows.in"

dependencies {
  implementation(project(":flows"))
  implementation(project(":shared:gradle"))
  implementation(project(":shared:domain"))
  implementation(project(":compilers:kickass"))
  implementation(project(":flows:adapters:out:charpad"))
}
