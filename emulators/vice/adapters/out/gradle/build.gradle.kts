plugins {
  id("rbt.adapter.outbound.gradle")
}

group = "com.github.c64lib.retro-assembler.emulators.vice"

tasks.jar {
  archiveFileName.set("emulators-vice-outbound-gradle2.jar")
}

dependencies {
  implementation(project(":emulators:vice"))
  implementation(project(":shared:domain"))
}
