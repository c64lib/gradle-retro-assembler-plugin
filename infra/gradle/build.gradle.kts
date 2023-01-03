val kotlinVersion: String by project
val vavrVersion: String by project
val vavrKotlinVersion: String by project
val gradleDownloadTaskVersion: String by project
val tagPropertyName = "tag"

plugins {
    id("rbt.kotlin")
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.14.0"
}

group = "com.github.c64lib.retro-assembler"

 tasks {
    val copySubProjectClasses by
        register("copySubProjectClasses") {
            doLast {
              val localDependencies =
                  project.configurations.compileClasspath
                      .get()
                      .resolvedConfiguration
                      .firstLevelModuleDependencies
                      .filter { it.moduleGroup.startsWith(project.group.toString()) }
                      .flatMap { it.moduleArtifacts }
                      .map { it.file.parentFile.parentFile }
                copy {
                    from(localDependencies)
                    into(buildDir)
                    include("classes/**")
                    exclude("**/META-INF/*")
                }
            }
            group = "build"
        }
 }

project(":infra:gradle").tasks.jar { dependsOn(tasks.named("copySubProjectClasses")) }

tasks.named("copySubProjectClasses") { mustRunAfter(project(":infra:gradle").tasks.classes) }

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
    implementation("de.undercouch:gradle-download-task:$gradleDownloadTaskVersion")
    implementation("io.vavr:vavr:$vavrVersion")
    // new
    compileOnly(project(":shared:domain"))
    compileOnly(project(":shared:gradle"))
    compileOnly(project(":compilers:kickass"))
    compileOnly(project(":compilers:kickass:adapters:in:gradle"))
    compileOnly(project(":compilers:kickass:adapters:out:gradle"))
    compileOnly(project(":emulators:vice"))
    compileOnly(project(":emulators:vice:adapters:out:gradle"))
    compileOnly(project(":testing:64spec"))
    compileOnly(project(":testing:64spec:adapters:in:gradle"))
    // old
    compileOnly(project(":domain"))
    compileOnly(project(":app:binary-utils"))
    compileOnly(project(":app:processor-commons"))
    compileOnly(project(":app:charpad-processor"))
    compileOnly(project(":app:spritepad-processor"))
    compileOnly(project(":app:binary-interleaver"))
    compileOnly(project(":app:nybbler"))
}

publishing { repositories { maven { url = uri("../../../consuming/maven-repo") } } }
