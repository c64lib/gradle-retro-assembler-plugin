plugins {
  id("rbt.adapter.inbound.gradle")
}

group = "com.github.c64lib.retro-assembler.crunchers.exomizer.adapters.in"

dependencies {
  implementation(project(":crunchers:exomizer"))
  implementation(project(":shared:domain"))
  implementation(project(":shared:gradle"))
}
