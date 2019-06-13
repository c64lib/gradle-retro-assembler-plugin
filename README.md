# Gradle retro assembler plugin

Adds capability of building assembler projects for 65xx microprocessor family.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) [![Build Status](https://travis-ci.org/c64lib/gradle-retro-assembler-plugin.svg?branch=master)](https://travis-ci.org/c64lib/gradle-retro-assembler-plugin) [![Build Status](https://travis-ci.org/c64lib/gradle-retro-assembler-plugin.svg?branch=develop)](https://travis-ci.org/c64lib/gradle-retro-assembler-plugin)

Gradle is an universal build tool that is used for automation in IT industry. It supports Java
ecosystem out of the box but is also extensible via plugin system.

This is a plugin that allows 8-bit assembly projects to be build via Gradle. Currently it supports
Commodore 64 output and KickAssembler only, but there are plans to extend it further.

## Usage

If you have Gradle installed on your machine (get it from [GitHub](https://github.com/gradle/gradle/releases))
you just need to create `build.gradle` file in root folder of your project. Fill it with following content:

    plugins {
        id "com.github.c64lib.retro-assembler" version "0.2.0"
    }
    
    repositories {
        jcenter()
    }
    
    apply plugin: "com.github.c64lib.retro-assembler"
    
    retroProject {
        dialect = "KickAssembler"
        libDirs = ["..", ".ra/deps/c64lib"]
        srcDirs = ["lib", "spec"]
        libFromGitHub "c64lib/64spec", "0.7.0pr"
    }

You can of course adjust values of `libDirs` and `srcDirs` to your needs.

### Using external ASM dependencies
If you need to use some library code written in KickAssembler, Retro Gradle Plugin can download them 
for you automatically. You just need to specify these dependencies inside your `retroProject` section:

    retroProject {
        libFromGitHub "c64lib/64spec", "0.7.0pr"
    }

Please note, that currently only GitHub is supported as a source for library releases. In future this will
be extended.

## Using Gradle Wrapper
Gradle Wrapper is a recommended way to distribute sources of your projects. When Gradle Wrapper is
installed in your project, other people does not need to have Gradle installed locally. Gradle Wrapper takes
care on downloading appropriate Gradle version during build and then executing it using `build.gradle`
file. Actually only Java Environment (JDK) is necessary to build such projects. 

Read how to install Gradle Wrapper in Gradle documentation: https://docs.gradle.org/current/userguide/gradle_wrapper.html

## Change log

### 0.2.0
* Support for downloading ASM libraries released at GitHub

### 0.1.1
* Support for configurable source dirs

### 0.1.0
* Support for KickAssembler
* Download of KickAssembler
* clean and build tasks