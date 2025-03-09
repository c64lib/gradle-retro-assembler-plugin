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
package com.github.c64lib.rbt.shared.gradle.dsl

import java.util.*
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

open class FlowExtension @Inject constructor(private val objectFactory: ObjectFactory) {
  var name: String = UUID.randomUUID().toString()
  var dependsOn = emptyList<String>()
  val extensions = ArrayList<FlowStepExtension>()

  fun goattracker(action: Action<GoattrackerPipelineExtension>) {
    val goatTracker = objectFactory.newInstance(GoattrackerPipelineExtension::class.java)
    action.execute(goatTracker)
    extensions.add(goatTracker)
  }

  fun charpad(action: Action<CharpadPipelineExtension>) {
    val charpad = objectFactory.newInstance(CharpadPipelineExtension::class.java)
    action.execute(charpad)
    extensions.add(charpad)
  }

  fun spritepad(action: Action<SpritepadPipelineExtension>) {
    val spritepad = objectFactory.newInstance(SpritepadPipelineExtension::class.java)
    action.execute(spritepad)
    extensions.add(spritepad)
  }

  fun image(action: Action<ImagePipelineExtension>) {
    val image = objectFactory.newInstance(ImagePipelineExtension::class.java)
    action.execute(image)
    extensions.add(image)
  }

  fun kickAssemble(action: Action<KickAssemblerExtension>) {
    val kickAssembler = objectFactory.newInstance(KickAssemblerExtension::class.java)
    action.execute(kickAssembler)
    extensions.add(kickAssembler)
  }
}
