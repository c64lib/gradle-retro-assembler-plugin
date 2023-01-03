val kotlinVersion: String by project
val vavrVersion: String by project
val vavrKotlinVersion: String by project
val tagPropertyName = "tag"

plugins {
  id("rbt.kotlin")
}

group = "com.github.c64lib.retro-assembler.shared"

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
