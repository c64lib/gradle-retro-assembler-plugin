val kotlinVersion: String by project
val vavrVersion: String by project
val vavrKotlinVersion: String by project
val tagPropertyName = "tag"

plugins {
  id("rbt.kotlin")
}

tasks.jar {
  archiveFileName.set("shared-domain.jar")
}

group = "com.github.c64lib.retro-assembler"

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
