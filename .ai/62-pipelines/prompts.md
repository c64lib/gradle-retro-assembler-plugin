# Implementing paralleized flows

Currently, all tasks in the problem are executed sequentially, not leveraging parallelization feature of Gradle. This results in very long build times for complex projects that execute compilation, preprocessing and postprocessing of long projects. Propose a new bounded context named 'flows' that will allow to organize tasks into chains (flows) that can depend on each other (outputs of one flow can feed input of another flows). This new flow mechanism should have a separate, new DSL syntax. Create an action plan.

# Enhancing source file input validation

We need to distinguish input (consumables) that are derived directly from source files from inputs that are produced as outputs by other steps/flows. Inputs that are not producted by anything and are taken from source files should not be checked by validation and identified as not provided. Steps for charpad, spritepad, image, goattracker and assemble are taken from always src. Modify execution plan and add separate step for this fix.

# Removing redundant parallelization in DSL

The parallelization explicitely definable in DSL is redundant. It should be rather derived from dependencies between steps and between flows. Modify action plan with step to remove this function, remember to update existing documentation.

# Action plan progress and next steps

I have executed step 8 manually and have following observations:
In flows DSL, flows DSL element is now nested in flows DSL element which is unnecessary.
In error messages printed to the gradle output some interpolation syntax is not properly resolved (see example below)
<hr></hr>
What went wrong: Execution failed for task ':flowAsset-preprocessing'.
Flow 'asset-preprocessing' validation failed: ${validation.issues.filter { it.severity.name == ERROR }.joinToString { it.message }}
<hr></hr>
Update action plan with appropriate fixing steps

# Implementing extensible step architecture

Update action plan and add yet another step:
Prepare action step for extensibility, that is:
remove configuration part (Map), it is not necessary
make a step an abstract class as a base for concrete steps
implement a command step that will be able to execute any CLI command - it should be able to provide command name and list of parameters in a convenient form, perhaps by using + operator. command step should accept any from and to

# Enhancing gradle DSL constructs for flow steps

Update action plan by adding another step: for concrete flow steps we would like to have a dedicated gradle tasks that are implemented in inbound adapter module, these tasks should have an @Input marked resource corresponding set of input files, @Output marked resource corresponding set of outputs and dependency with other step tasks where there is a relaction between output file of that task to the input file of this task.


