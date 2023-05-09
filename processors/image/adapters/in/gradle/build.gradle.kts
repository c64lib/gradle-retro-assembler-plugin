plugins {
  id("rbt.adapter.inbound.gradle")
}

group = "com.github.c64lib.retro-assembler.processors.image.adapters.in"

dependencies {
  implementation(project(":processors:image"))
  implementation(project(":shared:domain"))
  implementation(project(":shared:gradle"))
  implementation(project(":shared:binary-utils"))
  implementation(project(":shared:processor"))
}
