package com.github.c64lib.rbt.processors.goattracker.usecase

import com.github.c64lib.rbt.processors.goattracker.usecase.port.ExecuteGt2RelocPort
import java.io.File

class PackSongUseCase(private val executeGt2RelocPort: ExecuteGt2RelocPort) {

    fun apply(packSongCommand: PackSongCommand) = executeGt2RelocPort.execute(packSongCommand)
}

data class PackSongCommand(
    val source: File,
    val output: File,
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
