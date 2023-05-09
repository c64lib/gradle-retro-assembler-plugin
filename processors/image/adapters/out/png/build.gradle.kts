plugins {
  id("rbt.kotlin")
}

dependencies {
    implementation(project(":processors:image"))
    implementation("ar.com.hjg:pngj:2.1.0")
}
