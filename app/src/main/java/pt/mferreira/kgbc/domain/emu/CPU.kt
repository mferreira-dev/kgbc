package pt.mferreira.kgbc.domain.emu

import android.content.Context
import pt.mferreira.kgbc.utils.convertToHex
import pt.mferreira.kgbc.utils.getCurrentDate
import java.io.File

object CPU {
	/**
	 * The GameBoy address bus is 64 KB long.
	 */
	private const val GAMEBOY_MEMORY_SIZE_BYTES = 65536
	private const val NUMBER_OF_8_BIT_REGISTERS = 8
	private const val NUMBER_OF_16_BIT_REGISTERS = 4

	/**
	 * Theoretically the GameBoy's CPU runs at 4.19 MHz.
	 *
	 * However, an operation takes at the very least 4 cycles to complete which means that
	 * effectively the CPU runs at 4.19 / 4 = 1.05 MHz.
	 */
	private const val MACHINE_CYCLE_FREQUENCY = 1047500

	/**
	 * These constant values allow for short and concise syntax when accessing CPU registers.
	 *
	 * Another possible solution would be to use a map. However, using a map results in both much
	 * longer syntax and being forced into null safety checks.
	 *
	 * i.e. r8b.get(map.get('A')) would return an Int? type.
	 * While r8b.get(A) would return an Int type.
	 */
	private const val A = 0
	private const val F = 1
	private const val B = 2
	private const val C = 3
	private const val D = 4
	private const val E = 5
	private const val H = 6
	private const val L = 7

	private const val AF = 0
	private const val BC = 1
	private const val DE = 2
	private const val HL = 3

	/**
	 * An array is better for data that has a known size at compile time since it's stored
	 * on the stack rather than the heap (a list would be stored on the heap).
	 */
	private val bus = Array<UByte>(GAMEBOY_MEMORY_SIZE_BYTES) { 0u }

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
	private fun write(bytes: ByteArray, offset: Int = 0) {
		bytes.forEachIndexed { index, byte ->
			bus[index + offset] = byte.toUByte()
		}
	}

	private fun registerHandler() {}

	/**
	 * Return the 8-bit registers values.
	 *
	 * The reason why we don't use Kotlin's property access syntax (and add private set) instead
	 * is because that would require said property to be a variable rather than a value.
	 */
	fun get8BitRegisters(): Array<UByte> {
		return r8b
	}

	/**
	 * Return the 16-bit registers values.
	 *
	 * The reason why we don't use Kotlin's property access syntax (and add private set) instead
	 * is because that would require said property to be a variable rather than a value.
	 */
	fun get16BitRegisters(): Array<UShort> {
		return r16b + sp + pc
	}

	/**
	 * Dump the GameBoy's current memory to an internal text file.
	 */
	fun dump(context: Context) {
		val dump = File(context.filesDir, "dump_" + getCurrentDate() + ".txt")

		dump.printWriter().use { printer ->
			bus.forEachIndexed { index, uByte ->
				var buffer = uByte.convertToHex()
				buffer += if ((index + 1) % 10 == 0) "\n" else " "
				printer.print(buffer)
			}
		}
	}
}