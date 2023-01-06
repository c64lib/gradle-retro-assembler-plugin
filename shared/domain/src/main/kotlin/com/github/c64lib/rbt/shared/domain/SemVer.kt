/*
MIT License

Copyright (c) 2018-2023 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.rbt.shared.domain

data class SemVer(val major: Int, val minor: Int, val patch: Int? = null, val suffix: String = "") {

  companion object Factory {
    fun fromString(value: String): SemVer =
        fromStringOrNull(value)
            ?: throw IllegalArgumentException("Cannot determine version from \"$value\"")

    fun fromStringOrNull(value: String): SemVer? {
      val pattern = "^(\\d+)\\.(\\d+)(\\.\\d+)?(-.+)?$"
      val regex = Regex(pattern)
      val match = regex.find(value)
      return if (match != null) {
        val major = match.groupValues[1].toInt()
        val minor = match.groupValues[2].toInt()
        val patch =
            if (match.groupValues[3].isNotEmpty()) {
              match.groupValues[3].substring(1).toInt()
            } else {
              null
            }
        val suffix =
            if (match.groupValues[4].isNotEmpty()) {
              match.groupValues[4].substring(1)
            } else {
              ""
            }
        SemVer(major, minor, patch, suffix)
      } else {
        null
      }
    }
  }
  override fun toString() =
      "$major.$minor" +
          if (patch != null) {
            ".$patch"
          } else {
            ""
          } +
          if (suffix.isNotEmpty()) {
            "-${suffix}"
          } else {
            ""
          }

  operator fun compareTo(other: SemVer): Int {
    if (major != other.major) {
      return major.compareTo(other.major)
    }
    if (minor != other.minor) {
      return minor.compareTo(other.minor)
    }
    if (patch != null && other.patch != null) {
      if (patch != other.patch) {
        return patch.compareTo(other.patch)
      }
    }
    if (suffix.isNotEmpty() && other.suffix.isNotEmpty()) {
      if (suffix != other.suffix) {
        return suffix.compareTo(other.suffix)
      }
    }
    return 0
  }
}
