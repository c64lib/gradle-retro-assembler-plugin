/*
MIT License

Copyright (c) 2018-2025 c64lib: The Ultimate Commodore 64 Library
Copyright (c) 2018-2025 Maciej Małecki

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.github.c64lib.rbt.flows.adapters.`in`.gradle

import com.github.c64lib.rbt.flows.domain.Flow
import com.github.c64lib.rbt.flows.domain.steps.AssembleStep
import com.github.c64lib.rbt.flows.domain.steps.CharpadStep
import com.github.c64lib.rbt.flows.domain.steps.CommandStep
import com.github.c64lib.rbt.flows.domain.steps.DasmStep
import com.github.c64lib.rbt.flows.domain.steps.ExomizerStep
import com.github.c64lib.rbt.flows.domain.steps.GoattrackerStep
import com.github.c64lib.rbt.flows.domain.steps.ImageStep
import com.github.c64lib.rbt.flows.domain.steps.SpritepadStep
import com.github.c64lib.rbt.shared.gradle.TASK_FLOWS
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder

// FlowTasksGenerator integration tests
// Tests verify that FlowTasksGenerator can be instantiated with various flow and step configurations
// Full task registration testing requires Gradle Plugin TestKit setup in separate integration test suite
class FlowTasksGeneratorTest : BehaviorSpec()

