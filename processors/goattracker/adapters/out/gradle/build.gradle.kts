plugins { id("rbt.adapter.outbound.gradle") }

group = "com.github.c64lib.retro-assembler.processors.goattracker.out"

dependencies {
    implementation(project(":processors:goattracker"))
    implementation(project(":shared:gradle"))
}
