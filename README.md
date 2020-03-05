# Gradle Retro Assembler Plugin

Adds capability of building assembler projects for 65xx microprocessor family.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) 
[![CircleCI](https://circleci.com/gh/c64lib/gradle-retro-assembler-plugin/tree/master.svg?style=svg)](https://circleci.com/gh/c64lib/gradle-retro-assembler-plugin/tree/master)
[![CircleCI](https://circleci.com/gh/c64lib/gradle-retro-assembler-plugin/tree/develop.svg?style=svg)](https://circleci.com/gh/c64lib/gradle-retro-assembler-plugin/tree/develop)

Gradle is an universal build tool that is used for automation in IT industry. It supports Java
ecosystem out of the box but is also extensible via plugin system.

This is a plugin that allows 8-bit assembly projects to be build via Gradle. Currently it supports
Commodore 64 output and KickAssembler only, but there are plans to extend it further.

## Usage

If you have Gradle installed on your machine (get it from [GitHub](https://github.com/gradle/gradle/releases))
you just need to create `build.gradle` file in root folder of your project. Fill it with following content:

    plugins {
        id "com.github.c64lib.retro-assembler" version "1.0.0"
    }
    
    repositories {
        jcenter()
    }
    
    apply plugin: "com.github.c64lib.retro-assembler"
    
    retroProject {
        dialect = "KickAssembler"
        dialectVersion = "5.9"
        libDirs = ["..", ".ra/deps/c64lib"]
        srcDirs = ["lib", "spec"]
        
        // dependencies
        libFromGitHub "c64lib/64spec", "0.7.0pr"
    }

You can of course adjust all values inside `retroProject` to your needs.

As you can see you don't have to download Retro Assembler Plugin, it will be automatically downloaded and used once `gradle` 
command is run. The plugin is published into Plugins portal: [https://plugins.gradle.org](https://plugins.gradle.org/plugin/com.github.c64lib.retro-assembler)
and can be used as any other Gradle plugin.

### Customizing your build
The following properties can be customized:
* `dialect` - selects Assembler dialect and determines Assembler to be used (possible values: `KickAssembler`)
* `dialectVersion` - selects version of Assembler binary to be used (see https://github.com/c64lib/asm-ka/releases for available versions)
* `includes` - provides array of file patterns that contains sources you want to assembly; default value: `["**/*.asm"]`
* `excludes` - provides array of file patterns that matches source files to be excluded from assembling; default value: `[".ra/**/*.asm"]`
* `srcDirs` - provides array of directory locations where your source code (`asm` files) resides; default value: `["."]`
* `libDirs` - provides array of directory locations where your libraries are downloaded; default value: `[]`

### Launching your build
There are two core tasks that can be used from command line:
* `gradle build` performs assembling process and produces output files (actually `sym` and `prg` files are created 
alongside source `asm` files)
* `gradle clean` performs overall cleaning of the project by removing target files (that is `sym` and `prg` files)

If gradle command is issued without any task specified, a `build` task is assumed. It is also possible to run both of
tasks:

    gradle clean build
    
This ensures that old files are removed prior assembling process.

There are two supplementary tasks that are called automatically when `build` task is performed.
* `resolveDevDeps` - downloads and prepares to use software needed for assembling (i.e. KickAssembler binary)
* `downloadDeps`  - downloads and unzips libraries (dependencies) that are used by your project.

It is also possible to run supplementary tasks manually:

    gradle resolveDevDeps downloadDeps
    

### Using external ASM dependencies
If you need to use some library code written in KickAssembler, Retro Gradle Plugin can download them 
for you automatically. You just need to specify these dependencies inside your `retroProject` section:

    retroProject {
        libFromGitHub "c64lib/common", "1.0.0"
    }

Please note, that currently only GitHub is supported as a source for library releases. In future this will
be extended.

### Running Unit tests with 64spec
Version 1.0.0 supports now launching unit tests written in KickAssembler. In order to be able to do so, a Vice 3+
must be installed on a machine where tests will be launched. It is also assumed that `x64` command is available 
on the path. Also the 64spec library must be added to dependencies, as any other KickAssembler library:

    retroProject {
        libFromGitHub "c64lib/64spec", "0.7.0pr"
    }
    
Please note, that original version of 64spec does not work with newer versions of KickAssembler, therefore
forked version is used.

By default, Gradle Retro Assembler plugin detects whether there are any tests in your projects and if found
it tries to launch them. It is assumed that tests are located in `spec` directory of project's root (or its 
subdirectories) and that they are included in files ended with `spec.asm`. This default behavior can be 
customized by assigning new dir name and file masks in `build.gradle`:

    retroProject {
        specDirs = ['tests']
        specIncludes = ['**/*.test.asm']
    }
    
The following will reconfigure plugin to seek for tests in `tests` directory and execute each test
ended with `test.asm`.

## Building Retro Assembler projects on CI environments
Gradle Retro Assembler Plugin can be used in CI builds launched in the Cloud. As for now
two environments are supported: CircleCI (recommended) and TravisCI.

### CircleCI
If you keep your project on `github`, it is very easy then to configure https://circleci.com/ as your
CI (Continous Integration) environment. So, after each push to `GitHub`, a gradle build will be automatically
launched there, and all your `asm` sources will be assembled with Kick Assembler. If you have any 64spec
tests, they will be also launched there using GUI-Less Vice and their results will influence your build 
results.

If you break your code or break your tests, you will be then notified what's wrong. This will be all done 
automatically. In result your development speed will be increased as there will be an external "guard"
that looks after your code stability.

In order to be able to run your project on CircleCi, you have to add configuration file to your code
repository. The file is named `config.yml` and must be located in `.circleci` directory located right
in the root of your project. As for now the file should look similar to the example below:

    version: 2
    jobs:
      build:
        branches:
          only:
            - master
            - develop
        docker:
          - image: maciejmalecki/c64libci:0.1.4
    
        working_directory: ~/repo
    
        environment:
          JVM_OPTS: -Xmx3200m
          TERM: dumb
    
        steps:
          - checkout
    
          - run: ./gradlew
          
          
You still can modify few things in this file:
* if you want other branches to be built too, add them to the `branches/only` list
* if you want to customize build options, i.e. skip tests, you have to modify last line 
of the file, i.e.: `- run ./gradlew -x test`

You have to ensure, that `gradlew` launcher has executable rights on Linux machines, otherwise `run` command
will fail.

One thing you shouldn't modify is `docker/image` - the `maciejmalecki/c64libci:0.1.4` is a dedicated
image based on Debian Buster that has Java 11 and Vice 3.x preinstalled and is needed to run both
KickAssembler and 64spec tests.

### TravisCI
As for now it is not possible to install Vice 3.x on TravisCI due to outdated Linux images, so that
64spec tests are not working there. Remember to disable them with `-x` flag, i.e.:

    gradlew build -x test
    
In order to enable building on TravisCI, you have to provide configuration file in your repository.
The file is named `.travis.yml` and must be located in root of your project. The file should have
following content:

    language: asm
    sudo: false
    script:
      - ./gradlew build -x test
    notifications:
      email:
        on_success: change
        on_failure: change

Travis integrates well with GitHub and builds can be easily activated for each repository
hosted on GitHub.

You have to ensure, that `gradlew` launcher has executable rights on Linux machines, otherwise `run` command
will fail.

## Using Gradle Wrapper
Gradle Wrapper is a recommended way to distribute sources of your projects. When Gradle Wrapper is
installed in your project, other people does not need to have Gradle installed locally. Gradle Wrapper takes
care on downloading appropriate Gradle version during build and then executing it using `build.gradle`
file. Actually only Java Environment (JDK) is necessary to build such projects.

With Gradle Wrapper build can be run using `gradlew` command:

    gradlew clean build
    
or

    ./gradlew clean build
    
under linux-like OS. 

Read how to install Gradle Wrapper in Gradle documentation: https://docs.gradle.org/current/userguide/gradle_wrapper.html

## Change log

### 1.0.1
* Migrate build to Gradle 6

### 1.0.0
* Support for running 64spec tests using console mode of Vice
* Tested support for CircleCI environment via dedicated Docker image
* Work dir (.ra) can now be changed

### 0.3.0
* Support for source files patterns via includes
* Support for excluding source files via patterns

### 0.2.1
* Compatibility with Gradle 5.x.x
* Build works with OpenJDK 12

### 0.2.0
* Support for downloading ASM libraries released at GitHub
* Support for version number for selected assembler (KickAssembler is only supported)

### 0.1.1
* Support for configurable source dirs

### 0.1.0
* Support for KickAssembler
* Download of KickAssembler
* clean and build tasks
