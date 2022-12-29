plugins {
  id("rbt.adapter.out.gradle")
}

tasks.jar {
  archiveFileName.set("emulators-vice-out-gradle.jar")
}

dependencies {
  implementation(project(":emulators:vice"))
  implementation(project(":shared:domain"))
}
