plugins {
  id("rbt.adapter.out.gradle")
}

dependencies {
  implementation(project(":emulators:vice"))
  implementation(project(":shared:domain"))
}
