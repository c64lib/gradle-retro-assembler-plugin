plugins {
  id("rbt.kotlin")
}

dependencies {
    implementation(project(":processors:image"))
    implementation(project(":shared:domain"))
}
