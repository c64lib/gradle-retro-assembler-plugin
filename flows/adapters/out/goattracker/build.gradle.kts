plugins {
  id("rbt.adapter.outbound.gradle")
}

group = "com.github.c64lib.retro-assembler.flows.out"

dependencies {
  implementation(project(":flows"))
  implementation(project(":processors:goattracker"))
  implementation(project(":processors:goattracker:adapters:out:gradle"))
  implementation(project(":shared:domain"))
  implementation(project(":shared:gradle"))
}
