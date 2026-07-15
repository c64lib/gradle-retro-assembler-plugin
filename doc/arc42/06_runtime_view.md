# 6. Runtime View

This section documents the architecturally significant runtime scenarios as Mermaid sequence diagrams. Every participant maps to a real class named in [§5 Building Block View](05_building_block_view.md).

## 6.1 Plugin application & wiring

When Gradle applies the plugin, `RetroAssemblerPlugin.apply` registers the DSL extensions immediately, then defers all task creation and dependency-injection wiring to `afterEvaluate` (so the user's `retroProject { }`, `preprocess { }`, and `flows { }` configuration is fully read first).

```mermaid
sequenceDiagram
    participant G as Gradle
    participant P as RetroAssemblerPlugin
    participant E as Extensions (retroProject/preprocess/flows)
    participant T as Gradle TaskContainer
    participant F as FlowTasksGenerator

    G->>P: apply(project)
    P->>E: extensions.create(...)
    P->>G: project.afterEvaluate { ... }
    Note over G,P: user build script evaluated
    G-->>P: afterEvaluate callback
    P->>T: create resolveDevDeps, downloadDeps
    P->>T: create charpad/spritepad/goattracker/image
    P->>T: create preprocess (dependsOn processors)
    P->>T: create asm (KickAssembleUseCase(KickAssembleAdapter))
    P->>F: FlowTasksGenerator(...).registerTasks()
    F->>T: create flow step + aggregation + flows tasks
    P->>T: asm.dependsOn(flows, resolveDevDeps, downloadDeps, preprocess)
    P->>T: create build (dependsOn asm, test)
```

## 6.2 Full build lifecycle

Running `build` triggers the whole graph. Preprocessing (asset processing + flows), dependency resolution, and dev-dependency download all complete before assembly; assembly and spec/test complete before `build`.

```mermaid
sequenceDiagram
    participant U as User (gradlew build)
    participant B as build
    participant A as asm
    participant D as resolveDevDeps / downloadDeps
    participant PP as preprocess
    participant FL as flows
    participant T as test

    U->>B: gradlew build
    B->>A: (dependsOn)
    A->>D: (dependsOn) resolve/download deps
    A->>PP: (dependsOn) run processors
    A->>FL: (dependsOn) run all flows
    Note over PP,FL: independent tasks run in parallel under --parallel
    A->>A: KickAssemble sources → PRG
    B->>T: (dependsOn) run 64spec tests
    B-->>U: build result
```

## 6.3 Flow execution with port delegation

A flow step (here a CharPad step) runs as a dedicated Gradle task. The step task injects the flows-owned port, calls `step.execute()`, and the out-adapter bridges into the `processors:charpad` context — the orchestrator never touches the processor internals directly.

```mermaid
sequenceDiagram
    participant G as Gradle
    participant ST as CharpadTask (flow step task)
    participant S as CharpadStep (domain)
    participant PT as CharpadPort
    participant AD as CharpadAdapter (flows out)
    participant UC as ProcessCharpadUseCase (processors:charpad)
    participant OP as OutputProducers (shared:processor)

    G->>ST: execute task
    ST->>S: inject port, step.execute()
    S->>PT: process(CharpadCommand)
    PT->>AD: process(command)
    AD->>UC: ProcessCharpadUseCase(outputProducers, ...).apply(inputStream)
    UC->>OP: parse CTM → drive producers (charset/map/tiles/...)
    OP-->>G: output files written
```

## 6.4 64spec test run via VICE

The `test` task composes contexts: `Run64SpecTestUseCase` (testing) wraps `RunTestOnViceUseCase` (emulators), which drives the native VICE process through its adapter, then parses the emulator's PETSCII result file.

```mermaid
sequenceDiagram
    participant G as Gradle
    participant TT as Test task (test)
    participant R as Run64SpecTestUseCase (testing:64spec)
    participant V as RunTestOnViceUseCase (emulators:vice)
    participant VP as RunTestOnVicePort
    participant VA as RunTestOnViceAdapter
    participant VICE as VICE emulator (native)

    G->>TT: execute (dependsOn asmSpec)
    TT->>R: apply(testSource)
    R->>V: apply(RunTestOnViceCommand(autostart PRG, mon commands))
    V->>VP: run(ViceParameters)
    VP->>VA: run(...)
    VA->>VICE: launch, autostart PRG, feed monitor commands
    VICE-->>VA: result file (PETSCII)
    VA-->>R: (test executed)
    R->>R: parse (passed/total) → TestResult
    R-->>TT: TestResult
```

## 6.5 Dependency resolution / download

`downloadDeps` runs `ResolveGitHubDependencyUseCase` per configured dependency. It reads the recorded version and skips the download entirely when it already matches (unless `force`); otherwise it downloads the GitHub archive, untars it, and records the new version.

```mermaid
sequenceDiagram
    participant G as Gradle
    participant DT as DownloadDependencies task (downloadDeps)
    participant UC as ResolveGitHubDependencyUseCase
    participant RV as ReadDependencyVersionPort
    participant DL as DownloadDependencyPort (FileDownloader)
    participant UT as UntarDependencyPort
    participant SV as SaveDependencyVersionPort

    G->>DT: execute
    DT->>UC: apply(ResolveGitHubDependencyCommand)
    UC->>RV: readVersion(versionFile)
    alt version unchanged and not forced
        UC-->>DT: skip (up to date)
    else changed or forced
        UC->>DL: download(GitHub tar.gz URL, name)
        DL-->>UC: libFile
        UC->>UT: untar(libFile)
        UC->>SV: saveVersion(versionFile, version)
        UC->>UC: libFile.delete()
    end
    DT-->>G: done
```
