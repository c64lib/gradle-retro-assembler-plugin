# 2. Architecture Constraints

## 2.1 Technical Constraints

| Constraint | Detail |
|------------|--------|
| **Delivery mechanism is a Gradle plugin** | The system has no independent runtime — it only exists as code that executes inside a host Gradle build (`java-gradle-plugin`, `infra/gradle/build.gradle.kts`). All capability must be exposed through Gradle's extension/task/DSL model. |
| **Implementation language: Kotlin** | Kotlin 1.7.0 (`gradle.properties`: `kotlinVersion = 1.7.0`), targeting the JVM. |
| **JVM target: Java 11** | Enforced project-wide by the shared convention plugin `buildSrc/src/main/kotlin/rbt.kotlin.gradle.kts` (`sourceCompatibility`/`targetCompatibility` = `JavaVersion.VERSION_11`, `jvmTarget = "11"`). Every module compiles against this baseline. |
| **Publishing target: Gradle Plugin Portal** | Published via the `com.gradle.plugin-publish` plugin (`infra/gradle/build.gradle.kts`) as `com.github.c64lib.retro-assembler`; releases are triggered by semver tags (`.github/workflows/publish.yml`), requiring `GRADLE_PUBLISH_KEY`/`GRADLE_PUBLISH_SECRET` secrets. |
| **Hexagonal architecture is mandatory, not optional** | `CLAUDE.md` mandates ports/adapters separation per domain; this is enforced by convention and code review, not by a build-time architecture-linting tool. |
| **Gradle itself is a technology concern** | Per `CLAUDE.md`, Gradle APIs (tasks, extensions, Workers API) must be isolated in adapters (`adapters/in/gradle`, `adapters/out/gradle`) — domain/use-case code must not import Gradle types. |
| **New domain modules require explicit `infra/gradle` wiring** | Every new module must be added as a `compileOnly` dependency of `infra/gradle`, or the plugin fails at runtime with `ClassNotFoundError` — there is no auto-discovery mechanism. |
| **Parallel execution must use Gradle's Workers API** | No custom threading is permitted for parallel task execution (`CLAUDE.md`); `flows` derives parallelism from Gradle's own task dependency graph rather than manual concurrency. |
| **External tool dependencies are native, out-of-process** | Kick Assembler (downloaded JAR, runs on JVM), DASM, VICE, Exomizer, and `gt2reloc` are invoked as external processes/artifacts — the plugin does not reimplement assembler or emulator logic. |

## 2.2 Organizational Constraints

| Constraint | Detail |
|------------|--------|
| **Open-source project under c64lib** | Hosted at `github.com/c64lib/gradle-retro-assembler-plugin`; contributions via PR, issues tracked on GitHub. |
| **Commit and plan conventions** | Commit messages follow the domain-based convention in `CLAUDE.md`; structured development is tracked via `plans/PLAN-nnnn_*.md` through the `plan`/`execute` skill workflow — this document set is itself the output of [PLAN-0002](../../plans/PLAN-0002_arc42-technical-documentation.md). |
| **CI enforced via GitHub Actions** | `build.yml` (build/test/quality reports on push and PR), `publish.yml` (Plugin Portal release on tags), `documentation.yml` (AsciiDoc User's Manual → gh-pages). This arc42 set is deliberately Markdown+Mermaid and rendered by GitHub directly, so it is *not* part of `documentation.yml`. |
| **Coverage and static-analysis targets are advisory, not hard gates** | Detekt runs in warning mode (violations don't fail the build); JaCoCo coverage target is ≥70% for domain modules, ≥50% for infra/test-utility modules, verified via `verifyCodeCoverage` — see `CLAUDE.md` Quality Metrics. |

## 2.3 Conventions

| Convention | Detail |
|------------|--------|
| **Use-case naming** | Every use case is a Kotlin class named `*UseCase.kt`, single public method `apply(payload)`, optionally returning a result object. |
| **Port naming** | Interfaces isolating technology-specific code are named `*Port.kt`, defined in the consuming domain's `usecase/port/` (or `domain/port/` in `flows` — see [§11](11_risks_and_technical_debt.md) for that inconsistency). |
| **Module layout** | `domain-name/src/{domain,usecase}` for business logic, `domain-name/adapters/{in,out}/<technology>` for adapters — see [§5](05_building_block_view.md). |
| **Test file naming** | Test classes end with `Test.kt`, mirrored under `src/test/kotlin/`. |
