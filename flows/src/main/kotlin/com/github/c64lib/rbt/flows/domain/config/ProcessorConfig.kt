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
package com.github.c64lib.rbt.flows.domain.config

/** Configuration enums and data classes for processor-specific options */

// Charpad Configuration
enum class CharpadCompression {
  NONE,
  RLE,
  EXOMIZER
}

enum class CharpadFormat {
  STANDARD,
  OPTIMIZED,
  C64LIB
}

data class CharpadConfig(
    val compression: CharpadCompression = CharpadCompression.NONE,
    val exportFormat: CharpadFormat = CharpadFormat.STANDARD,
    val tileSize: Int = 8,
    val charsetOptimization: Boolean = true,
    val generateMap: Boolean = true,
    val generateCharset: Boolean = true
)

// Spritepad Configuration
enum class SpriteFormat {
  HIRES,
  MULTICOLOR
}

enum class SpriteOptimization {
  NONE,
  SIZE,
  SPEED
}

data class SpritepadConfig(
    val format: SpriteFormat = SpriteFormat.MULTICOLOR,
    val optimization: SpriteOptimization = SpriteOptimization.SIZE,
    val exportRaw: Boolean = true,
    val exportOptimized: Boolean = false,
    val animationSupport: Boolean = false
)

// GoatTracker Configuration
enum class GoattrackerFormat {
  SID_ONLY,
  ASM_ONLY,
  SID_AND_ASM
}

enum class Frequency {
  PAL,
  NTSC
}

data class GoattrackerConfig(
    val exportFormat: GoattrackerFormat = GoattrackerFormat.SID_AND_ASM,
    val optimization: Boolean = true,
    val frequency: Frequency = Frequency.PAL,
    val channels: Int = 3,
    val filterSupport: Boolean = true
)

// Assembly Configuration
enum class CpuType {
  MOS6502,
  MOS6510,
  MOS65C02
}

enum class AssemblyOptimization {
  NONE,
  SIZE,
  SPEED
}

data class AssemblyConfig(
    val cpu: CpuType = CpuType.MOS6510,
    val generateSymbols: Boolean = true,
    val optimization: AssemblyOptimization = AssemblyOptimization.SPEED,
    val includePaths: List<String> = emptyList(),
    val defines: Map<String, String> = emptyMap(),
    val verbose: Boolean = false
)

// Image Configuration
enum class ImageFormat {
  KOALA,
  ART_STUDIO,
  HIRES_BITMAP,
  MULTICOLOR_BITMAP
}

enum class PaletteOptimization {
  NONE,
  REDUCE_COLORS,
  QUANTIZE
}

enum class DitheringAlgorithm {
  NONE,
  FLOYD_STEINBERG,
  ORDERED
}

data class ImageConfig(
    val targetFormat: ImageFormat = ImageFormat.KOALA,
    val paletteOptimization: PaletteOptimization = PaletteOptimization.REDUCE_COLORS,
    val dithering: DitheringAlgorithm = DitheringAlgorithm.FLOYD_STEINBERG,
    val backgroundColor: Int = 0,
    val transparencySupport: Boolean = false
)
