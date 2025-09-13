# About this project
1. This application is a Gradle plugin for building Assembly projects for MOS 65xx family of microprocessors, using Kick Assembler as the only supported ASM dialect.

# Coding guidelines
1. This application is implemented in Kotlin and uses Gradle as the build tool.
2. This application uses Hexagonal Architecture, with the domain layer containing the business logic and the adapters layer containing the glue code between the domain layer and specific technology, such as Gradle.
3. Top level directories denote bounded context, each having internal hexagonal structure.
4. There are dedicated at-hoc gradle plugins declared for each kind of module, all being located in the `buildSrc` folder.
5. Each module should have its own `build.gradle.kts` file, with the root `build.gradle.kts` file aggregating all modules and applying the necessary plugins.
6. There is an end user documentation stored in `doc` folder that is implemented in AsciiDoctor, keep it up to date with the code changes.
7. There is `CHANGES.adoc` file in the root of the project that contains the change log for the project, keep it up to date with the code changes.

# Testing guidelines
1. This application uses Kotest as testing library for unit and integration tests.
2. This application prefers using BDD style of testing, using Given/When/Then DSL of Kotest.

# General notes on working approach relevant for Agent mode
## Tools
1. We use Powershell so always use syntax of powershell when running commands. In particular do not use `&&`.
2. Use `gradle build` to quickly compile the client code
3. Use `gradle test` to run all tests in the client code
4. use `gradle spotlessApply` to format the code according to the coding style
5. always run `gradle spotlessApply` after creating or editing any source files to ensure the code is formatted correctly
