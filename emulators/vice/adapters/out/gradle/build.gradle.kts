plugins {
  id("rbt.adapter.outbound.gradle")
}

tasks.jar {
  archiveFileName.set("emulators-vice-outbound-gradle2.jar")
}

dependencies {
  implementation(project(":emulators:vice"))
  implementation(project(":shared:domain"))
}
