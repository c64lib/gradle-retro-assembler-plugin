# Gradle Retro Assembler Plugin

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) 
[![Build (master)](https://github.com/c64lib/gradle-retro-assembler-plugin/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/c64lib/gradle-retro-assembler-plugin/actions/workflows/build.yml)
[![Build (develop)](https://github.com/c64lib/gradle-retro-assembler-plugin/actions/workflows/build.yml/badge.svg?branch=develop)](https://github.com/c64lib/gradle-retro-assembler-plugin/actions/workflows/build.yml)

This is a plugin for [Gradle build tool](https://gradle.org/) that adds capability of building Assembly projects for MOS 65xx family of microprocessors.

Currently, the plugin supports [Kick Assembler](http://theweb.dk/KickAssembler/Main.html#frontpage) as the only ASM dialect.

Retro Build Tool is published at: https://plugins.gradle.org/plugin/com.github.c64lib.retro-assembler.

There is also a Linux-based docker image available: https://github.com/c64lib/c64libci. All functions of Retro Build Tool with work on this image. The image is designed to be used with CI environments (such as CircleCI).

## User's Manual

A complete User's Manual is available at the following address:

https://c64lib.github.io/gradle-retro-assembler-plugin/

## Architecture documentation

Technical architecture documentation (arc42, Markdown + Mermaid) for contributors and AI agents is in [`doc/arc42/`](doc/arc42/README.md). It covers the domain model, bounded contexts, use cases, ports & adapters, runtime scenarios, and cross-cutting concepts.

## Change log

A change log together with the list of available versions can be seen at:

https://c64lib.github.io/gradle-retro-assembler-plugin/#_change_log
