/*
MIT License

Copyright (c) 2018-2021 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.gradle.preprocess.charpad

import java.util.*
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

abstract class OutputsExtension @Inject constructor(private val objectFactory: ObjectFactory) {

  internal val charsets = LinkedList<StartEndExtension>()

  internal val charsetAttributes = LinkedList<StartEndExtension>()

  internal val charsetColours = LinkedList<StartEndExtension>()

  internal val charsetScreenColours = LinkedList<StartEndExtension>()

  internal val charsetMaterials = LinkedList<StartEndExtension>()

  internal val tiles = LinkedList<StartEndExtension>()

  internal val tileColours = LinkedList<StartEndExtension>()

  internal val tileScreenColours = LinkedList<StartEndExtension>()

  internal val maps = LinkedList<MapExtension>()

  fun charset(action: Action<StartEndExtension>) {
    val ex = objectFactory.newInstance(StartEndExtension::class.java)
    action.execute(ex)
    charsets.add(ex)
  }

  fun charsetAttributes(action: Action<StartEndExtension>) {
    val ex = objectFactory.newInstance(StartEndExtension::class.java)
    action.execute(ex)
    charsetAttributes.add(ex)
  }

  fun charsetColours(action: Action<StartEndExtension>) {
    val ex = objectFactory.newInstance(StartEndExtension::class.java)
    action.execute(ex)
    charsetColours.add(ex)
  }

  fun charsetScreenColours(action: Action<StartEndExtension>) {
    val ex = objectFactory.newInstance(StartEndExtension::class.java)
    action.execute(ex)
    charsetScreenColours.add(ex)
  }

  fun charsetMaterials(action: Action<StartEndExtension>) {
    val ex = objectFactory.newInstance(StartEndExtension::class.java)
    action.execute(ex)
    charsetMaterials.add(ex)
  }

  fun tiles(action: Action<StartEndExtension>) {
    val ex = objectFactory.newInstance(StartEndExtension::class.java)
    action.execute(ex)
    tiles.add(ex)
  }

  fun tileColours(action: Action<StartEndExtension>) {
    val ex = objectFactory.newInstance(StartEndExtension::class.java)
    action.execute(ex)
    tileColours.add(ex)
  }

  fun tileScreenColours(action: Action<StartEndExtension>) {
    val ex = objectFactory.newInstance(StartEndExtension::class.java)
    action.execute(ex)
    tileScreenColours.add(ex)
  }

  fun map(action: Action<MapExtension>) {
    val ex = objectFactory.newInstance(MapExtension::class.java)
    action.execute(ex)
    maps.add(ex)
  }
}
