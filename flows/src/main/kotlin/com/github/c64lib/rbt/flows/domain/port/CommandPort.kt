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
package com.github.c64lib.rbt.flows.domain.port

import com.github.c64lib.rbt.flows.domain.config.CommandCommand

/**
 * Domain port for CLI command execution.
 *
 * This port defines the contract for executing CLI commands within the flows domain, abstracting
 * away the specific execution implementation details such as process management, platform
 * differences, and output handling.
 */
interface CommandPort {

  /**
   * Executes a single CLI command.
   *
   * @param command The command containing all necessary execution parameters
   * @throws RuntimeException if the command fails to execute or returns a non-zero exit code
   */
  fun execute(command: CommandCommand)

  /**
   * Executes multiple CLI commands in sequence.
   *
   * If any command fails, execution stops and an exception is thrown. This provides fail-fast
   * behavior for command sequences.
   *
   * @param commands The list of commands to execute
   * @throws RuntimeException if any command fails to execute or returns a non-zero exit code
   */
  fun execute(commands: List<CommandCommand>) {
    commands.forEach { command -> execute(command) }
  }

  /**
   * Executes a CLI command and returns the output.
   *
   * This method is useful when you need to capture the command output for further processing.
   *
   * @param command The command to execute
   * @return The standard output of the command as a string
   * @throws RuntimeException if the command fails to execute or returns a non-zero exit code
   */
  fun executeWithOutput(command: CommandCommand): String
}
