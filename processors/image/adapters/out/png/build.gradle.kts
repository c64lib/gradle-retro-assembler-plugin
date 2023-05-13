plugins {
  id("rbt.kotlin")
}

val pngjVersion: String by project

group = "com.github.c64lib.retro-assembler.processors.image"

dependencies {
    implementation(project(":processors:image"))
    implementation(project(":shared:domain"))
    implementation("ar.com.hjg:pngj:$pngjVersion")
}
