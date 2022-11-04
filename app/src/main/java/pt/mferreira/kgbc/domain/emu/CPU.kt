package pt.mferreira.kgbc.domain.emu

import android.content.Context
import pt.mferreira.kgbc.utils.convertToHex
import pt.mferreira.kgbc.utils.getCurrentDate
import java.io.File

object CPU {
	/**
	 * The GameBoy has a grand total of 64KB of memory.
	 */
	const val GAMEBOY_MEMORY_SIZE_BYTES = 65536
	const val NUMBER_OF_8_BIT_REGISTERS = 8
	const val NUMBER_OF_16_BIT_REGISTERS = 4

	/**
	 * Theoretically the GameBoy's CPU runs at 4.19 MHz clock cycles.
	 * However, an operation takes at the very least 4 cycles to complete which means that
	 * effectively the CPU runs at 4.19 / 4 = 1.05 MHz machine cycles.
	 */
	const val MACHINE_CYCLE_FREQUENCY = 1047500

	/**
	 * These constant values allow for short and concise syntax when accessing CPU registers.
	 *
	 * Another possible solution would be to use a map. However, using a map results in both much
	 * longer syntax and being forced into null safety checks.
	 *
	 * i.e. registers8bit.get(map.get('A')) would return an Int? type.
	 * While registers8bit.get(A) would return an Int type.
	 */
	const val A = 0
	const val F = 1
	const val B = 2
	const val C = 3
	const val D = 4
	const val E = 5
	const val H = 6
	const val L = 7

	const val AF = 0
	const val BC = 1
	const val DE = 2
	const val HL = 3

	/**
	 * An array is better for data that has a known size at compile time since it's stored
	 * on the stack rather than the heap (a list would be stored on the heap).
	 *
	 * Note: The GameBoy has an 8-bit CPU but a 16-bit address bus.
	 */
	private val mem = Array<UByte>(GAMEBOY_MEMORY_SIZE_BYTES) { 0u }

	/**
	 * A, F, B, C, D, E, H, L.
	 */
	private val r8b = Array<UByte>(NUMBER_OF_8_BIT_REGISTERS) { 0u }

	/**
	 *	AF, BC, DE, HL.
	 */
	private val r16b = Array<UShort>(NUMBER_OF_16_BIT_REGISTERS) { 0u }

	/**
	 * Stack pointer.
	 */
	private var sp: UShort = 0u

	/**
	 * Program counter.
	 */
	private var pc: UShort = 0u

	/**
	 * Writes an array of bytes to memory starting at offset.
	 *
	 * @param bytes The array of bytes to be written.
	 * @param offset Start writing at this offset.
	 */
	fun write(bytes: ByteArray, offset: Int = 0) {
		bytes.forEachIndexed { index, byte ->
			mem[index + offset] = byte.toUByte()
		}
	}

	fun registerHandler() {}

	/**
	 * Dump the GameBoy's current memory to an internal text file.
	 */
	fun dump(context: Context) {
		val dump = File(context.filesDir, "dump_" + getCurrentDate() + ".txt")

		dump.printWriter().use { printer ->
			mem.forEachIndexed { index, uByte ->
				var buffer = uByte.convertToHex()
				buffer += if ((index + 1) % 10 == 0) "\n" else " "
				printer.print(buffer)
			}
		}
	}
}