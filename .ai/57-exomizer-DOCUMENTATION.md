# Exomizer Cruncher Integration Documentation

## Overview

The Gradle Retro Assembler Plugin now includes support for **Exomizer**, a data compression utility used for reducing binary file sizes in retro computing projects, particularly for Commodore 64 development.

Exomizer integration allows you to compress binary assets and code within your Gradle build pipelines using the flows DSL.

## Prerequisites

- Exomizer binary must be available in your system PATH
- Exomizer is available at: https://github.com/asm6502/exomizer

## Basic Usage

### Raw Mode Compression

The raw mode performs basic file compression:

```kotlin
flow {
  exomizerStep("compress_raw") {
    from("src/assets/data.bin")
    to("build/data.bin.crunched")
    raw()
  }
}
```

### Memory Mode Compression

Memory mode compression is optimized for decompression with memory settings:

```kotlin
flow {
  exomizerStep("compress_mem") {
    from("src/assets/code.bin")
    to("build/code.bin.crunched")
    mem {
      loadAddress = "0x0800"
      forward = false
    }
  }
}
```

## Configuration Options

### Memory Mode Settings

- **loadAddress**: Controls where the compressed data is loaded (default: "auto")
  - `"auto"` - Automatically determine load address
  - `"none"` - No load address in output
  - `"0x0800"` - Hex notation with 0x prefix
  - `"$2000"` - Hex notation with $ prefix
  - `"2048"` - Decimal notation

- **forward**: Compression direction (default: false)
  - `true` - Compress forward
  - `false` - Compress backward (default)

### Raw Mode Settings

Raw mode supports the following additional options (all optional):

```kotlin
raw {
  backwards = true        // Crunch backwards instead of forward
  reverse = true          // Write output in reverse order
  compatibility = true    // Disable literal sequences
  speedOverRatio = true   // Favor compression speed over ratio
  encoding = "custom"     // Use custom encoding
  skipEncoding = true     // Don't write encoding to output
  maxOffset = 32768       // Max sequence offset
  maxLength = 1024        // Max sequence length
  passes = 50             // Optimization passes
  bitStreamTraits = 5     // Bit stream traits
  bitStreamFormat = 20    // Bit stream format
  controlAddresses = "1234" // Control addresses not to be read
  quiet = true            // Quiet mode
  brief = true            // Brief mode (less output)
}
```

## Complete Example

```kotlin
extensions.getByType<FlowsExtension>().flow {
  // Compress game data
  exomizerStep("compress_game_data") {
    from("src/assets/sprite_data.bin")
    to("build/sprite_data.bin.crunched")
    mem {
      loadAddress = "0x2000"
      forward = false
    }
  }

  // Compress code
  exomizerStep("compress_code") {
    from("build/game_code.bin")
    to("build/game_code.bin.crunched")
    raw()
  }

  // Assemble compressed code
  assembleStep("assemble_crunched") {
    from(listOf("src/code.asm"))
    to("build/game.prg")
    includeFiles(
      "src/common/*.asm",
      "src/sprites/*.asm"
    )
  }
}
```

## File Handling

- **Input files**: Must exist in the file system. Relative paths are resolved from the project root directory.
- **Output files**: Directory must be writable. Relative paths are resolved from the project root directory.
- **Absolute paths**: Both input and output can use absolute paths.

## Error Handling

The plugin validates:
- Input file exists and is readable
- Output directory exists and is writable
- Load address format (for memory mode) is valid
- Mode is either "raw" or "mem"

If validation fails, you'll see clear error messages indicating what needs to be fixed.

## Integration with Other Steps

Exomizer steps integrate seamlessly with other flow steps:

```kotlin
flow {
  // Process graphics
  charpadStep("process_charset") {
    from("src/charset.ctm")
    charset { output = "build/charset.chr" }
  }

  // Compress processed data
  exomizerStep("compress_charset") {
    from("build/charset.chr")
    to("build/charset.chr.crunched")
    raw()
  }

  // Assemble with compressed data
  assembleStep("assemble") {
    from(listOf("src/game.asm"))
    to("build/game.prg")
  }
}
```

## Troubleshooting

### "Exomizer execution failed with exit code..."

- Ensure exomizer binary is in your PATH: `which exomizer` or `where exomizer`
- Verify input file exists and is readable
- Check that output directory exists and is writable
- Verify option combinations are valid

### "Invalid load address"

Load addresses must be in one of these formats:
- `auto` or `none` (keywords)
- `0x0800` (hex with 0x prefix)
- `$2000` (hex with $ prefix)
- `2048` (decimal)

### Step validation errors

Run `./gradlew build` with verbose output to see detailed validation errors:
```bash
./gradlew build --stacktrace
```

## Performance Considerations

- Exomizer compression can be time-consuming, especially with high pass counts
- For large files, consider reducing the `passes` option to speed up builds
- The `speedOverRatio` flag prioritizes compression speed over compression ratio

## Advanced Usage

For advanced compression tuning, experiment with:
- Different `maxOffset` and `maxLength` values
- `bitStreamTraits` and `bitStreamFormat` settings (0-7 and 0-63 respectively)
- `encoding` and `controlAddresses` options for specific optimizations

Consult the Exomizer documentation for details on these advanced options.
