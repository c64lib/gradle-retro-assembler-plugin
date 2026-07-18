val kotlinVersion: String by project
val vavrVersion: String by project
val vavrKotlinVersion: String by project
val gradleDownloadTaskVersion: String by project
val pngjVersion: String by project
val tagPropertyName = "tag"


plugins {
    id("rbt.kotlin")
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.14.0"
}

group = "com.github.c64lib.retro-assembler"

// Gradle's ProjectBuilder needs reflective access to java.lang on JDK 16+
tasks.withType<Test> { jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED") }

// Bundles the classes of every project dependency (all compileOnly modules below) into the
// published plugin jar. Merging at the jar level (rather than copying resolved classes dirs into
// this project's own build/classes, as the previous copySubProjectClasses task did) gives Gradle
// implicit task dependencies on each subproject's jar task, correct up-to-date checks, and
// removal propagation: a class deleted or renamed in a subproject simply isn't in the jar that
// gets zipped in, with no stale leftovers and no manual `clean` required.
val bundledProjectJars: Provider<List<FileTree>> = provider {
  configurations.compileClasspath
      .get()
      .incoming
      .artifactView { componentFilter { it is ProjectComponentIdentifier } }
      .files
      .map { zipTree(it) }
}

tasks.jar {
  from(bundledProjectJars) { exclude("META-INF/**") }
  duplicatesStrategy = DuplicatesStrategy.FAIL
}

pluginBundle {
    website = "https://c64lib.github.io/gradle-retro-assembler-plugin/"
    vcsUrl = "https://github.com/c64lib/gradle-retro-assembler-plugin"
    tags = listOf("assembly", "65xx", "mos6502", "mos6510")
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "com.github.c64lib.retro-assembler"
            displayName = "Retro Assembler Plugin"
            description =
                "Embeds various 6502 assemblers and provides basic functionality to have builds for your beloved C64"
            implementationClass = "com.github.c64lib.gradle.RetroAssemblerPlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("io.vavr:vavr:$vavrVersion")
    implementation("ar.com.hjg:pngj:$pngjVersion")

    compileOnly(project(":shared:domain"))
    compileOnly(project(":shared:gradle"))
    compileOnly(project(":shared:filedownload"))
    compileOnly(project(":shared:binary-utils"))
    compileOnly(project(":shared:processor"))

    compileOnly(project(":compilers:kickass"))
    compileOnly(project(":compilers:kickass:adapters:in:gradle"))
    compileOnly(project(":compilers:kickass:adapters:out:gradle"))
    compileOnly(project(":compilers:kickass:adapters:out:filedownload"))

    compileOnly(project(":compilers:dasm"))
    compileOnly(project(":compilers:dasm:adapters:out:gradle"))

    compileOnly(project(":flows:"))
    compileOnly(project(":flows:adapters:in:gradle"))
    compileOnly(project(":flows:adapters:out:gradle"))
    compileOnly(project(":flows:adapters:out:charpad"))
    compileOnly(project(":flows:adapters:out:spritepad"))
    compileOnly(project(":flows:adapters:out:image"))
    compileOnly(project(":flows:adapters:out:goattracker"))
    compileOnly(project(":flows:adapters:out:exomizer"))

    compileOnly(project(":emulators:vice"))
    compileOnly(project(":emulators:vice:adapters:out:gradle"))

    compileOnly(project(":testing:64spec"))
    compileOnly(project(":testing:64spec:adapters:in:gradle"))

    compileOnly(project(":dependencies"))
    compileOnly(project(":dependencies:adapters:in:gradle"))
    compileOnly(project(":dependencies:adapters:out:gradle"))

    compileOnly(project(":processors:goattracker"))
    compileOnly(project(":processors:goattracker:adapters:in:gradle"))
    compileOnly(project(":processors:goattracker:adapters:out:gradle"))

    compileOnly(project(":processors:spritepad"))
    compileOnly(project(":processors:spritepad:adapters:in:gradle"))

    compileOnly(project(":processors:charpad"))
    compileOnly(project(":processors:charpad:adapters:in:gradle"))

    compileOnly(project(":processors:image"))
    compileOnly(project(":processors:image:adapters:in:gradle"))
    compileOnly(project(":processors:image:adapters:out:png"))
    compileOnly(project(":processors:image:adapters:out:file"))

    compileOnly(project(":crunchers:exomizer"))
    compileOnly(project(":crunchers:exomizer:adapters:in:gradle"))

    // Test-time equivalents of the compileOnly deps above: RetroAssemblerPluginTest actually
    // applies the plugin via ProjectBuilder, so it needs these on the runtime test classpath.
    testImplementation(project(":shared:domain"))
    testImplementation(project(":shared:gradle"))
    testImplementation(project(":shared:filedownload"))
    testImplementation(project(":shared:binary-utils"))
    testImplementation(project(":shared:processor"))

    testImplementation(project(":compilers:kickass"))
    testImplementation(project(":compilers:kickass:adapters:in:gradle"))
    testImplementation(project(":compilers:kickass:adapters:out:gradle"))
    testImplementation(project(":compilers:kickass:adapters:out:filedownload"))

    testImplementation(project(":compilers:dasm"))
    testImplementation(project(":compilers:dasm:adapters:out:gradle"))

    testImplementation(project(":flows:"))
    testImplementation(project(":flows:adapters:in:gradle"))
    testImplementation(project(":flows:adapters:out:gradle"))
    testImplementation(project(":flows:adapters:out:charpad"))
    testImplementation(project(":flows:adapters:out:spritepad"))
    testImplementation(project(":flows:adapters:out:image"))
    testImplementation(project(":flows:adapters:out:goattracker"))
    testImplementation(project(":flows:adapters:out:exomizer"))

    testImplementation(project(":emulators:vice"))
    testImplementation(project(":emulators:vice:adapters:out:gradle"))

    testImplementation(project(":testing:64spec"))
    testImplementation(project(":testing:64spec:adapters:in:gradle"))

    testImplementation(project(":dependencies"))
    testImplementation(project(":dependencies:adapters:in:gradle"))
    testImplementation(project(":dependencies:adapters:out:gradle"))

    testImplementation(project(":processors:goattracker"))
    testImplementation(project(":processors:goattracker:adapters:in:gradle"))
    testImplementation(project(":processors:goattracker:adapters:out:gradle"))

    testImplementation(project(":processors:spritepad"))
    testImplementation(project(":processors:spritepad:adapters:in:gradle"))

    testImplementation(project(":processors:charpad"))
    testImplementation(project(":processors:charpad:adapters:in:gradle"))

    testImplementation(project(":processors:image"))
    testImplementation(project(":processors:image:adapters:in:gradle"))
    testImplementation(project(":processors:image:adapters:out:png"))
    testImplementation(project(":processors:image:adapters:out:file"))

    testImplementation(project(":crunchers:exomizer"))
    testImplementation(project(":crunchers:exomizer:adapters:in:gradle"))
}

publishing { repositories { maven { url = uri("../../../consuming/maven-repo") } } }
