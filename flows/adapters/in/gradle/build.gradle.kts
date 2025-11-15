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
  implementation(project(":flows:adapters:out:spritepad"))
  implementation(project(":flows:adapters:out:goattracker"))
  implementation(project(":processors:goattracker"))
  implementation(project(":processors:goattracker:adapters:in:gradle"))
  implementation(project(":processors:goattracker:adapters:out:gradle"))
  implementation(project(":crunchers:exomizer"))
  implementation(project(":crunchers:exomizer:adapters:in:gradle"))
}
