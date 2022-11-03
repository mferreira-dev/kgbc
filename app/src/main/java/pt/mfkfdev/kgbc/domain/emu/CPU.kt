package pt.mfkfdev.kgbc.domain.emu

import android.content.Context
import pt.mfkfdev.kgbc.utils.convertToHex
import pt.mfkfdev.kgbc.utils.getCurrentDate
import java.io.File

object CPU {
	/**
	 * The GameBoy has a grand total of 64KB of memory.
	 */
	const val GAMEBOY_MEMORY_SIZE_BYTES = 65536

	/**
	 * An array is better for data that has a known size at compile time since it's stored
	 * on the stack rather than the heap (a list would be stored on the heap).
	 */
	private val memory = Array<UByte>(GAMEBOY_MEMORY_SIZE_BYTES) { 0u }

	/**
	 * Writes an array of bytes to memory starting at offset.
	 *
	 * @param bytes The array of bytes to be written.
	 * @param offset Start writing at this offset.
	 */
	fun writeToMemory(bytes: ByteArray, offset: Int = 0) {
		bytes.forEachIndexed { index, byte ->
			memory[index + offset] = byte.toUByte()
		}
	}

	/**
	 * Dump the GameBoy's current memory to a local text file.
	 */
	fun dumpMemory(context: Context) {
		val dump = File(context.filesDir, "dump_" + getCurrentDate() + ".txt")

		dump.printWriter().use { printer ->
			memory.forEachIndexed { index, uByte ->
				var buffer = uByte.convertToHex()
				buffer += if ((index + 1) % 10 == 0) "\n" else " "
				printer.print(buffer)
			}
		}
	}
}