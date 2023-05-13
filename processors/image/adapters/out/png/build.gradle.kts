plugins {
  id("rbt.kotlin")
}

group = "com.github.c64lib.retro-assembler.processors.image"

dependencies {
    implementation(project(":processors:image"))
    implementation(project(":shared:domain"))
    implementation("ar.com.hjg:pngj:2.1.0")
}
