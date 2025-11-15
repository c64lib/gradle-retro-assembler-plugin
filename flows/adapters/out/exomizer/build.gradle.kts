plugins {
  id("rbt.adapter.outbound.gradle")
}

group = "com.github.c64lib.retro-assembler.flows.out"

dependencies {
  implementation(project(":flows"))
  implementation(project(":crunchers:exomizer"))
  implementation(project(":crunchers:exomizer:adapters:in:gradle"))
  implementation(project(":shared:domain"))
}
