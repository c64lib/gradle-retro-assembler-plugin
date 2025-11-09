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

import com.github.c64lib.rbt.shared.domain.OutputFormat

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
    val generateCharset: Boolean = true,
    val ctm8PrototypeCompatibility: Boolean = false,
    // Metadata configuration options - aligned with original processor defaults
    val namespace: String = "",
    val prefix: String = "",
    val includeVersion: Boolean = false,
    val includeBgColours: Boolean = true, // Align with original processor default
    val includeCharColours: Boolean = true, // Align with original processor default
    val includeMode: Boolean = false
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
enum class Frequency {
  PAL,
  NTSC
}

data class GoattrackerConfig(
    val frequency: Frequency = Frequency.PAL,
    val channels: Int = 3,
    val optimization: Boolean = true,
    val executable: String = "gt2reloc",
    val bufferedSidWrites: Boolean? = null,
    val disableOptimization: Boolean? = null,
    val playerMemoryLocation: Int? = null,
    val sfxSupport: Boolean? = null,
    val sidMemoryLocation: Int? = null,
    val storeAuthorInfo: Boolean? = null,
    val volumeChangeSupport: Boolean? = null,
    val zeroPageLocation: Int? = null,
    val zeropageGhostRegisters: Boolean? = null
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
    val generateSymbols: Boolean = true,
    val includePaths: List<String> = emptyList(),
    val defines: Map<String, String> = emptyMap(),
    val verbose: Boolean = false,
    val outputFormat: OutputFormat = OutputFormat.PRG,
    val srcDirs: List<String> = listOf("."),
    val includes: List<String> = listOf("**/*.asm"),
    val excludes: List<String> = listOf(".ra/**/*.asm"),
    val workDir: String = ".ra",
    val additionalInputs: List<String> = emptyList()
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
