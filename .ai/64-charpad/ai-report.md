/plan Integrate CharpadStep with the existing charpad preprocessor, retaining all capabilities of the charpad processor from the original, non-flows-based implementation.

/plan-update answer to question 1 is that it should be done as with compilers, that is, there should be an adapter

/plan-update answer to question 2 is no, there are no specific conventions; user should be able to provide any name for an output file

/plan-update answer to question 3 is yes, all existing output producers should be supported

/plan-update answer to question 4 is, that metadata output should support all parameters explicitly, per step definition

/exec step 1 (o4-mini, didn't work)

/exec step 1 (sonnet 4, but wrong dependency was added)

/plan-update update question 1 to be staightforward: flows module cannot depend onprocessors/charpad. There must be an intermendiate inbound adapter declared to indirectly set up this dependency.
