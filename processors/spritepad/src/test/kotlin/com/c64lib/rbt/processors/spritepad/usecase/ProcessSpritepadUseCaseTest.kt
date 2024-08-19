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
import com.c64lib.rbt.processors.spritepad.domain.InvalidSPDFormatException
import com.c64lib.rbt.processors.spritepad.domain.SpriteProducer
import com.c64lib.rbt.processors.spritepad.usecase.ProcessSpritepadUseCase
import com.c64lib.rbt.processors.spritepad.usecase.spd4.SPD4Processor
import com.github.c64lib.rbt.shared.processor.InputByteStream
import com.github.c64lib.rbt.shared.processor.OutputProducer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify

class ProcessSpritepadUseCaseTest :
    StringSpec({
      val spriteProducer: SpriteProducer = mockk()
      val outputProducers: Collection<OutputProducer<*>> = listOf(spriteProducer)
      val useCase = ProcessSpritepadUseCase(outputProducers)

      "apply with valid SPD4 input calls SPD4Processor" {
        val inputByteStream: InputByteStream = mockk()
        every { inputByteStream.read(3) } returns
            byteArrayOf('S'.code.toByte(), 'P'.code.toByte(), 'D'.code.toByte())
        every { inputByteStream.readByte() } returns 4

        mockkConstructor(SPD4Processor::class)
        every { anyConstructed<SPD4Processor>().process(inputByteStream) } returns Unit

        useCase.apply(inputByteStream)

        verify { anyConstructed<SPD4Processor>().process(inputByteStream) }
      }

      "apply with invalid SPD id throws InvalidSPDFormatException" {
        val inputByteStream: InputByteStream = mockk()
        every { inputByteStream.read(3) } returns
            byteArrayOf('X'.code.toByte(), 'Y'.code.toByte(), 'Z'.code.toByte())

        shouldThrow<InvalidSPDFormatException> { useCase.apply(inputByteStream) }
      }

      "apply with unsupported version throws InvalidSPDFormatException" {
        val inputByteStream: InputByteStream = mockk()
        every { inputByteStream.read(3) } returns
            byteArrayOf('S'.code.toByte(), 'P'.code.toByte(), 'D'.code.toByte())
        every { inputByteStream.readByte() } returns 6

        shouldThrow<InvalidSPDFormatException> { useCase.apply(inputByteStream) }
      }

      "processSprites calls action on each SpriteProducer" {
        val action: (SpriteProducer) -> Unit = mockk(relaxed = true)

        useCase.processSprites(action)

        verify { action(spriteProducer) }
      }
    })
