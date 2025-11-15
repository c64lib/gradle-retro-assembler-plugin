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

configurations {
  // Create a new configuration that extends from 'implementation'
  create("resolvableImplementation") {
    extendsFrom(project.configurations.implementation.get())
    isCanBeResolved = true
  }
}


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
}

publishing { repositories { maven { url = uri("../../../consuming/maven-repo") } } }
