plugins {
  `kotlin-dsl`
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
  implementation("com.diffplug.spotless:spotless-plugin-gradle:6.12.0")
}
