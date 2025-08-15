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

/**
 * Gradle extension that provides the flows DSL to build.gradle.kts files.
 *
 * Users can define execution flows like this:
 *
 * ```kotlin
 * flows {
 *     flow("preprocessing") {
 *         description = "Process all assets"
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *                 to("build/processed/sprites")
 *             }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *                 to("build/processed/sprites")
 *             }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *             step("image") {
 *                 from("src/assets/images")
 *         dependsOn("preprocessing", "dependencies")
 *             }
 *         }
 *     }
 *
 *     flow("dependencies") {
 *         step("resolve") {
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 *             to("build/dependencies")
 *         }
 *     }
 *
 *     flow("compilation") {
 *         dependsOn("preprocessing", "dependencies")
 *
 *         step("assemble") {
 *             from("src/main/asm")
 *             to("build/compiled")
 *         }
 *     }
 *
 *     flow("testing") {
 *         dependsOn("compilation")
 *
 *         step("test") {
 *             from("build/compiled")
 *             to("build/test-results")
 *         }
 *     }
 * }
 * ```
 */
open class FlowsExtension : FlowDslBuilder() {
  /** Returns all flows defined via the DSL. */
  fun getFlows(): List<Flow> = build()

  /** Validates that all flow dependencies exist and there are no circular dependencies. */
  fun validateFlows(): List<String> {
    val errors = mutableListOf<String>()
    val definedFlows = build()
    val flowNames = definedFlows.map { it.name }.toSet()

    // Check that all dependencies exist
    definedFlows.forEach { flow ->
      flow.dependsOn.forEach { dependency ->
        if (dependency !in flowNames) {
          errors.add("Flow '${flow.name}' depends on non-existent flow '$dependency'")
        }
      }
    }

    // Check for circular dependencies
    definedFlows.forEach { flow ->
      val visited = mutableSetOf<String>()
      val recursionStack = mutableSetOf<String>()

      if (hasCircularDependency(flow.name, visited, recursionStack, definedFlows)) {
        errors.add("Circular dependency detected involving flow '${flow.name}'")
      }
    }

    return errors
  }

  /** Returns flows that can run in parallel (have no dependencies between them). */
  fun getParallelFlows(): List<List<Flow>> {
    val definedFlows = build()
    val result = mutableListOf<List<Flow>>()
    val processed = mutableSetOf<String>()

    while (processed.size < definedFlows.size) {
      val readyFlows =
          definedFlows.filter { flow ->
            flow.name !in processed && flow.dependsOn.all { it in processed }
          }

      if (readyFlows.isEmpty()) {
        break // Circular dependency or other issue
      }

      // Group flows that can run in parallel
      // All ready flows run in parallel
      val parallelGroup = readyFlows.toList()

      if (parallelGroup.isNotEmpty()) {
        result.add(parallelGroup)
        processed.addAll(parallelGroup.map { it.name })
      }
    }

    return result
  }

  private fun hasCircularDependency(
      flowName: String,
      visited: MutableSet<String>,
      recursionStack: MutableSet<String>,
      flows: List<Flow>
  ): Boolean {
    if (recursionStack.contains(flowName)) {
      return true
    }

    if (visited.contains(flowName)) {
      return false
    }

    visited.add(flowName)
    recursionStack.add(flowName)

    val flow = flows.find { it.name == flowName }
    flow?.dependsOn?.forEach { dependency ->
      if (hasCircularDependency(dependency, visited, recursionStack, flows)) {
        return true
      }
    }

    recursionStack.remove(flowName)
    return false
  }
}
