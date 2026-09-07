plugins {
  id("rbt.adapter.inbound.gradle")
}

group = "com.github.c64lib.retro-assembler.compilers.dasm.in"

// ProjectBuilder-based tests need java.lang opened on JDK 9+
tasks.withType<Test> { jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED") }

dependencies {
  implementation(project(":compilers:dasm"))
  implementation(project(":shared:gradle"))
  implementation(project(":shared:domain"))
}
