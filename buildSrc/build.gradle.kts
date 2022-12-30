plugins {
  `kotlin-dsl`
  id("com.diffplug.spotless") version "6.12.0"
}

repositories {
  gradlePluginPortal()
  mavenCentral()
  jcenter {
    url = uri("https://jcenter.bintray.com/")
  }
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0")
}
