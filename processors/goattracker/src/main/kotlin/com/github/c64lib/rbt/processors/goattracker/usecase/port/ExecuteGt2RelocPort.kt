package com.github.c64lib.rbt.processors.goattracker.usecase.port

import com.github.c64lib.rbt.processors.goattracker.usecase.PackSongCommand

interface ExecuteGt2RelocPort {
  fun execute(packSongCommand: PackSongCommand)
}
