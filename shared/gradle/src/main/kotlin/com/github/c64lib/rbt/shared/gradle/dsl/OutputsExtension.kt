/*
MIT License

Copyright (c) 2018-2023 c64lib: The Ultimate Commodore 64 Library
Copyright (c) 2018-2023 Maciej Małecki

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

abstract class OutputsExtension @Inject constructor(private val objectFactory: ObjectFactory) {

  val meta = LinkedList<MetadataExtension>()

  val charsets = LinkedList<StartEndExtension>()

  val charsetAttributes = LinkedList<StartEndExtension>()

  val charsetColours = LinkedList<StartEndExtension>()

  val charsetScreenColours = LinkedList<StartEndExtension>()

  val charsetMaterials = LinkedList<StartEndExtension>()

  val tiles = LinkedList<StartEndExtension>()

  val tileTags = LinkedList<StartEndExtension>()

  val tileColours = LinkedList<StartEndExtension>()

  val tileScreenColours = LinkedList<StartEndExtension>()

  val maps = LinkedList<MapExtension>()

  fun meta(action: Action<MetadataExtension>) {
    val ex = objectFactory.newInstance(MetadataExtension::class.java)
    action.execute(ex)
    meta.add(ex)
  }

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

  fun tileTags(action: Action<StartEndExtension>) {
    val ex = objectFactory.newInstance(StartEndExtension::class.java)
    action.execute(ex)
    tileTags.add(ex)
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
