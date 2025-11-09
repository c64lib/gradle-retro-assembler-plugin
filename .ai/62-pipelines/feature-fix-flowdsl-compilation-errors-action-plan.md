# Action Plan for fix-flowdsl-compilation-errors

## Issue Description
Compilation of FlowDsl.kt fails due to mismatches between the Gradle DSL builders and the domain `Flow` constructor signature. Named parameters and property lists (`inputs`/`outputs` vs `consumes`/`produces`, `dependencies` vs `dependsOn`) do not align, causing errors.

## Relevant Codebase Parts
1. `FlowDslBuilder` and `FlowBuilder` in `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowDsl.kt` – builders for the DSL.
2. `ParallelStepsBuilder` and `StepBuilder` in the same file – intermediates that collect inputs/outputs.
3. Domain model in `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/Flow.kt` – defines the `Flow` constructor and property names.
4. `FlowsExtension.kt` in `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowsExtension.kt` – Gradle extension exposing the DSL, currently referencing `flow.dependencies` and `canRunInParallelWith` which no longer exist on `Flow`.

## Root Cause Hypothesis
The Gradle DSL builder API constructs a `Flow` using incorrect parameter names and mismatched collections (`inputs` and `outputs`), whereas the domain model expects `consumes` and `produces` lists and a `dependsOn` parameter. This signature mismatch leads to compilation errors. In addition to builder-to-domain mismatches, `FlowsExtension` uses outdated property names (`dependencies` vs `dependsOn`) and undefined helper methods (`canRunInParallelWith`), leading to unresolved reference errors.

## Investigation Questions
1. Are there any other DSL entry points (e.g., in `FlowsExtension`) that use the wrong parameter mapping?
2. Should `ParallelStepsBuilder` continue to return raw `FlowArtifact` lists, or be transformed before passing to `Flow`?
3. How are inputs/outputs intended to map to the domain model's `consumes` and `produces`?
4. Do any tests assume a different builder-to-domain mapping?
5. Is backward compatibility for existing build scripts required for this change?
6. Who maintains the DSL code (e.g., author, domain expert)?

## Next Steps
1. Update `FlowBuilder.build()` to call the domain `Flow` constructor with named parameters `name`, `steps`, `dependsOn`, `produces`, `consumes`, and `description`, mapping local `dependencies` → `dependsOn`, `outputs` → `produces`, and `inputs` → `consumes`.
2. Adjust any other builder (`ParallelStepsBuilder` logic) so that collected artifacts feed into correct domain fields.
3. Update imports or parameter names where needed (e.g., rename local variables for clarity).
4. Update `FlowsExtension.kt`:
   - Change all `flow.dependencies` references to `flow.dependsOn`.
   - Provide or import an implementation for `canRunInParallelWith(Flow)` on the `Flow` class or remove its usage.
   - Remove the unused `project` property if not needed.
5. Run `gradle build` to confirm compilation issues are resolved.
6. Execute existing tests (`gradle test`) to detect any regressions in DSL usage or behavior.
7. Document the changes in `CHANGES.adoc` under a new entry `fix-flowdsl-compilation-errors`.
8. Apply `gradle spotlessApply` to format the updated code.

## Additional Notes
- Ensure the DSL remains backwards-compatible if required by existing users.
- Verify any sample usage in documentation reflects the updated parameter mapping.
- Update or add unit tests around `FlowBuilder` and `FlowDslBuilder` to cover the corrected constructor mapping.
