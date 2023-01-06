plugins {
  id("rbt.adapter.inbound.gradle")
}

group = "com.github.c64lib.retro-assembler.processors.charpad.adapters.in"

dependencies {
  implementation(project(":processors:charpad"))
  implementation(project(":shared:domain"))
  implementation(project(":shared:gradle"))
  implementation(project(":shared:binary-utils"))
  implementation(project(":shared:processor"))
  implementation(project(":compilers:kickass"))
}
