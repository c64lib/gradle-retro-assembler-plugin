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
package com.github.c64lib.rbt.shared.domain.source

sealed interface SourceItem

object Separator : SourceItem

data class Comment(val texts: Array<String>) : SourceItem {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Comment

    if (!texts.contentEquals(other.texts)) return false

    return true
  }

  override fun hashCode(): Int {
    return texts.contentHashCode()
  }
}

data class Label(val name: String, val value: Int) : SourceItem

data class Namespace(val name: String) : SourceItem

class SourceModelBuilder {
  private val items = mutableListOf<SourceItem>()

  fun comment(texts: Array<String>): SourceModelBuilder {
    items.add(Comment(texts))
    return this
  }

  fun label(name: String, value: Int): SourceModelBuilder {
    items.add(Label(name, value))
    return this
  }

  fun namespace(name: String): SourceModelBuilder {
    items.add(Namespace(name))
    return this
  }

  fun separator(): SourceModelBuilder {
    items.add(Separator)
    return this
  }

  fun build(): SourceModel {
    return SourceModel(items)
  }
}

class SourceModel(private val items: List<SourceItem>) {
  fun stream(closure: (SourceItem) -> Unit) {
    items.forEach(closure)
  }
}
