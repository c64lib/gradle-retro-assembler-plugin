plugins {
  id("org.jetbrains.kotlin.jvm")
  id("com.diffplug.spotless")
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions {
    jvmTarget = "11"
  }
}

spotless {
  kotlin {
    ktfmt()
    endWithNewline()
    licenseHeaderFile(rootProject.file("LICENSE"))
  }
}

repositories {
  mavenCentral()
  jcenter {
    url = uri("https://jcenter.bintray.com/")
  }
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.0")
  testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
  testImplementation("io.kotest:kotest-runner-junit5:4.5.0")
}

tasks.withType<Test> { useJUnitPlatform() }
