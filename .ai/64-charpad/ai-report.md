/plan Integrate CharpadStep with the existing charpad preprocessor, retaining all capabilities of the charpad processor from the original, non-flows-based implementation.

/plan-update answer to question 1 is that it should be done as with compilers, that is, there should be an adapter

/plan-update answer to question 2 is no, there are no specific conventions; user should be able to provide any name for an output file

/plan-update answer to question 3 is yes, all existing output producers should be supported

/plan-update answer to question 4 is, that metadata output should support all parameters explicitly, per step definition

/exec step 1 (o4-mini, didn't work)

/exec step 1 (sonnet 4, but wrong dependency was added)

/plan-update update question 1 to be staightforward: flows module cannot depend onprocessors/charpad. There must be an intermendiate inbound adapter declared to indirectly set up this dependency.

/exec step 1

> I need to execute Step 1 from the action plan. Looking at the current plan, I can see that Step 1 was previously cancelled due to the architectural constraint that flows module cannot directly depend on processors/charpad. However, I need to first revert the changes made in the previous attempt and update the execution log accordingly.
> Let me first check the current state of the flows/build.gradle.kts file and then proceed with the corrected approach:
> I can see that the flows/build.gradle.kts file currently has the direct dependency on processors:charpad that was added in the previous attempt. Since Step 1 is marked as CANCELLED in the action plan due to architectural constraints, I need to revert this change and update the execution log to reflect the correct understanding.

/exec step 2

/exec step 3

/plan-update in step 4, class should be named CharpadAdapter, not CharpadPortAdapter (worked but also started to execute the plan)

steps 5-7 were implemented together as part of step 4

/exec step 8

/exec step 9

/exec step 10

/exec step 11

/exec step 12 (two test failures that were fixed by manually running the tests, and pasting errors into the agent, asking for a fix)

> There is a test failure: Expected :"header.h" Actual :"metadata.inc" <click></click> org.opentest4j.AssertionFailedError: expected:<"header.h"> but was:<"metadata.inc"> at com.github.c64lib.rbt.flows.domain.steps.CharpadStepTest<span>1</span>6<span>1</span>1.invokeSuspend(CharpadStepTest.kt:301) at com.github.c64lib.rbt.flows.domain.steps.CharpadStepTest<span>1</span>6<span>1</span>1.invoke(CharpadStepTest.kt) at com.github.c64lib.rbt.flows.domain.steps.CharpadStepTest<span>1</span>6<span>1</span>1.invoke(CharpadStepTest.kt) fix it

> there is another test failure:
Expected :1733631364 Actual :-1070626948 <click></click>
org.opentest4j.AssertionFailedError: expected:<1733631364> but was:<-1070626948> at com.github.c64lib.rbt.flows.domain.steps.CharpadStepTest<span>1</span>10<span>2</span>1.invokeSuspend(CharpadStepTest.kt:501) at com.github.c64lib.rbt.flows.domain.steps.CharpadStepTest<span>1</span>10<span>2</span>1.invoke(CharpadStepTest.kt) at com.github.c64lib.rbt.flows.domain.steps.CharpadStepTest<span>1</span>10<span>2</span>1.invoke(CharpadStepTest.kt)
fix it

/plan-update there is still error in the test, denoted by:
java.lang.RuntimeException: Charpad processing failed for step 'integrationTest': Insufficient data in CTM file 'test.ctm': Unexpected end of file reached while reading CTM data. The CTM file appears to be corrupted or truncated. at com.github.c64lib.rbt.flows.domain.steps.CharpadStep.execute(CharpadStep.kt:96) at com.github.c64lib.rbt.flows.domain.steps.CharpadStepTest<span>1</span>9<span>1.invokeSuspend(CharpadStepTest.kt:447) at com.github.c64lib.rbt.flows.domain.steps.CharpadStepTest</span>1<span>9</span>1.invoke(CharpadStepTest.kt) at com.github.c64lib.rbt.flows.domain.steps.CharpadStepTest<span>1</span>9$1.invoke(CharpadStepTest.kt)
If a real CTM file is needed, let human generate it manually via appropriate editor and plug it into the test as a resource, similarily as it is done in processors/charpad tests

> > Task :flows:adapters:out:charpad:compileTestKotlin FAILED
e: C:\prj\cbm\gradle-retro-assembler-plugin\flows\adapters\out\charpad\src\test\kotlin\com\github\c64lib\rbt\flows\adapters\out\charpad\CharpadAdapterTest.kt: (203, 81): Unresolved reference. None of the following candidates is applicable because of receiver type mismatch:
public fun <T : Comparable<TypeVariable(T)>> Array<out TypeVariable(T)>.sorted(): List<TypeVariable(T)> defined in kotlin.collections
public fun ByteArray.sorted(): List<Byte> defined in kotlin.collections
public fun CharArray.sorted(): List<Char> defined in kotlin.collections
public fun DoubleArray.sorted(): List<Double> defined in kotlin.collections
public fun FloatArray.sorted(): List<Float> defined in kotlin.collections
public fun IntArray.sorted(): List<Int> defined in kotlin.collections
public fun LongArray.sorted(): List<Long> defined in kotlin.collections
public fun ShortArray.sorted(): List<Short> defined in kotlin.collections
public fun UByteArray.sorted(): List<UByte> defined in kotlin.collections
public fun UIntArray.sorted(): List<UInt> defined in kotlin.collections
public fun ULongArray.sorted(): List<ULong> defined in kotlin.collections
public fun UShortArray.sorted(): List<UShort> defined in kotlin.collections
public fun <T : Comparable<TypeVariable(T)>> Iterable<TypeVariable(T)>.sorted(): List<TypeVariable(T)> defined in kotlin.collections
public fun <T : Comparable<TypeVariable(T)>> Sequence<TypeVariable(T)>.sorted(): Sequence<TypeVariable(T)> defined in kotlin.sequences

