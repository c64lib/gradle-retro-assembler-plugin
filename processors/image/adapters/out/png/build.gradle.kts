plugins {
  id("rbt.kotlin")
}

dependencies {
    implementation(project(":processors:image"))
    implementation(project(":shared:domain"))
    implementation("ar.com.hjg:pngj:2.1.0")
}
