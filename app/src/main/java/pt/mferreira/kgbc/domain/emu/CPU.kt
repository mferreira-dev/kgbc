package pt.mferreira.kgbc.domain.emu

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import pt.mferreira.kgbc.BuildConfig
import pt.mferreira.kgbc.R
import pt.mferreira.kgbc.domain.entities.InstructionSet
import pt.mferreira.kgbc.domain.entities.RefUByte
import pt.mferreira.kgbc.domain.entities.RefUShort
import pt.mferreira.kgbc.utils.*
import pt.mferreira.kgbc.utils.Globals.DEV_FLAVOR
import java.io.File

object CPU {

	/**
	 * The GameBoy address bus is 64 KB long.
	 */
	private const val GAMEBOY_MEMORY_SIZE_BYTES = 65536
	private const val NUMBER_OF_8_BIT_REGISTERS = 8
	private const val NUMBER_OF_16_BIT_REGISTERS = 4

	/**
	 * Theoretically the GameBoy's CPU runs at 4.19 MHz (T-Cycles).
	 *
	 * However, an operation takes at the very least 4 cycles to complete which means that
	 * effectively the CPU runs at 4.19 / 4 = 1.05 MHz (M-Cycles).
	 */
	private const val MAX_CYCLES_PER_SECOND = 1048576

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
	 * The GameBoy has a bootstrap process that involves reading from a boot ROM.
	 * However, you cannot legally include a copy of a boot ROM with an emulator so we're left with
	 * one of two choices: either ask the user for a copy of their own or to skip this process entirely.
	 */
	private const val BYPASS_BOOTSTRAP_ADDRESS = 0x0100
	private const val STACK_POINTER_STARTING_ADDRESS = 0xFFFEu

	/**
	 * An array is better for data that has a known size at compile time since it's stored
	 * on the stack rather than the heap (a list would be stored on the heap).
	 */
	private val bus = Array(GAMEBOY_MEMORY_SIZE_BYTES) { RefUByte() }

	/**
	 * A, F, B, C, D, E, H, L.
	 */
	private val r8b = Array(NUMBER_OF_8_BIT_REGISTERS) { RefUByte() }

	/**
	 *	AF, BC, DE, HL.
	 */
	private val r16b = Array(NUMBER_OF_16_BIT_REGISTERS) { RefUShort() }

	/**
	 * Stack pointer.
	 *
	 * Remember, the stack in the GameBoy grows top-down, so the top of the stack is the
	 * lowest address occupied by the stack, which is indexed by the Stack Pointer.
	 *
	 * Popping from the stack INCREMENTS the stack pointer by 2.
	 *
	 * Pushing into the stack DECREMENTS the stack pointer by 2.
	 */
	private var sp: RefUShort = RefUShort(STACK_POINTER_STARTING_ADDRESS.toUShort())

	/**
	 * Program counter.
	 */
	private var pc: RefUShort = RefUShort(BYPASS_BOOTSTRAP_ADDRESS.toUShort())

	/**
	 * Will contain the Z80's complete instruction set (loaded from iset.json).
	 */
	private lateinit var iset: InstructionSet

	private const val DEBUG_CPU = "kgbc-cpu"

	/**
	 * Return the 8-bit registers values.
	 *
	 * The reason why we don't use Kotlin's property access syntax (and add private set) instead
	 * is because that would require said property to be a variable rather than a value.
	 */
	fun get8BitRegisters(): Array<UByte> {
		val list = mutableListOf<UByte>().apply {
			r8b.forEach { add(it.value) }
		}
		return list.toTypedArray()
	}

	/**
	 * Return the 16-bit registers values.
	 */
	fun get16BitRegisters(): Array<UShort> {
		val list = mutableListOf<UShort>().apply {
			r16b.forEach { add(it.value) }
			add(sp.value)
			add(pc.value)
		}

		return list.toTypedArray()
	}

	/**
	 * Dump the GameBoy's current address bus to an internal text file.
	 */
	fun dump(context: Context) {
		val dump = File(context.filesDir, "dump_" + getCurrentDate() + ".txt")

		dump.printWriter().use { printer ->
			bus.forEachIndexed { index, refUByte ->
				var buffer = refUByte.value.convertToHex()
				buffer += if ((index + 1) % 10 == 0) "\n" else " "
				printer.print(buffer)
			}
		}

		displayToast(context, context.getString(R.string.dumped_bus, dump.name))
	}

	private var cycles: Int = 0
	private var endTimestamp: Long = 0

	/**
	 * Run the fetch -> decode -> execute loop indefinitely.
	 *
	 * The CPU will continously run at [MAX_CYCLES_PER_SECOND].
	 * This is achieved in the following way:
	 *
	 * 1. The system starts a new cycle batch every second, which means the CPU
	 * will loop exactly [MAX_CYCLES_PER_SECOND] times.
	 * A now() call provides the batch's beginning point while simply adding 1000
	 * to that same tell us when it should end.
	 *
	 * 2. At the beginning of each while loop cycle the system checks if it's time to start yet another batch.
	 * If the time elapsed is under a second the program flow will continue as normal as long as
	 * the [MAX_CYCLES_PER_SECOND] threshold hasn't been hit.
	 */
	fun run(context: Context) {
		if (!prepareInstructionSet(context)) {
			if (BuildConfig.FLAVOR == DEV_FLAVOR)
				displayToast(context, context.getString(R.string.iset_load_error))
			else
				displayToast(context, context.getString(R.string.general_error))

			return
		}

		startNewCycleBatch()

		while (true) {
			if (now() >= endTimestamp) {
				if (BuildConfig.FLAVOR == DEV_FLAVOR)
					logBatchComplete()

				startNewCycleBatch()
			}

			if (cycles == MAX_CYCLES_PER_SECOND)
				continue

			// Decode opcode.

			cycles++
		}
	}

	private fun startNewCycleBatch() {
		val now = now()
		endTimestamp = now + 1000L
		cycles = 0

		if (BuildConfig.FLAVOR != DEV_FLAVOR)
			return

		Log.d(DEBUG_CPU, "Starting new batch...")
		Log.d(DEBUG_CPU, "Now: $now")
		Log.d(DEBUG_CPU, "End: $endTimestamp")
		Log.d(DEBUG_CPU, "------------------------------")
	}

	private fun logBatchComplete() {
		Log.d(DEBUG_CPU, "Batch complete.")
		Log.d(DEBUG_CPU, "Now: ${now()}")
		Log.d(DEBUG_CPU, "End: $endTimestamp")
		Log.d(DEBUG_CPU, "------------------------------")
	}

	fun insertCartridge(bytes: ByteArray) {
		write(bytes, BYPASS_BOOTSTRAP_ADDRESS)
	}

	private fun prepareInstructionSet(context: Context): Boolean {
		val moshi = Moshi.Builder().build()
		val adapter = moshi.adapter(InstructionSet::class.java)
		val json = context.resources.openRawResource(R.raw.iset).bufferedReader().use { it.readText() }

		iset = adapter.fromJson(json) ?: return false
		return true
	}

	/**
	 * Example:
	 *
	 * LD A, ($3FFF)
	 * This instruction loads the accumulator from memory location $3FFF.
	 *
	 * @param dest Where to write.
	 * @param source What to write.
	 */
	fun load(dest: RefUByte, source: RefUByte) {
		dest.value = source.value
	}

	/**
	 * Clears the destination's current content then loads a new value.
	 *
	 * Example:
	 *
	 * LD SP, ($4050)
	 * This instruction loads the Stack Pointer from locations $4050 (least significant byte)
	 * and $4051 (most significant byte). Do keep in mind that the GameBoy is little-endian.
	 *
	 * The JVM has quite a few limitations, and a really good example of this is the fact that
	 * you can only perform bitshift operations on 32-bit integers. So in order to put two bytes into a short
	 * we first we need to transform each byte to a short. Then, they'll be converted to integers, shifted,
	 * and converted back to shorts.
	 *
	 * @param dest Where to write.
	 * @param msb Most significant byte (left).
	 * @param lsb Least significant byte (right).
	 */
	private fun load(dest: RefUShort, msb: RefUByte, lsb: RefUByte) {
		dest.value = dest.value and 0u
		dest.value = dest.value or RefUShort(msb.value.toUShort() shl 8).value
		dest.value = dest.value or RefUShort(lsb.value.toUShort()).value
	}

	/**
	 * Writes a byte to memory at offset.
	 *
	 * @param byte The byte to be written.
	 * @param offset Start writing at this offset.
	 */
	private fun write(byte: UByte, offset: Int) {
		bus[offset] = RefUByte(byte)
	}

	/**
	 * Writes an array of bytes to memory starting at offset.
	 *
	 * @param bytes The array of bytes to be written.
	 * @param offset Start writing at this offset.
	 */
	private fun write(bytes: ByteArray, offset: Int) {
		bytes.forEachIndexed { index, byte ->
			bus[index + offset] = RefUByte(byte.toUByte())
		}
	}

	/**
	 * Adds source to destination.
	 *
	 * @param dest Will store the result.
	 * @param source Second operand.
	 */
//	fun add(dest: RefUByte, source: RefUByte) {
//		dest.value = (dest.value + source.value).toUByte()
//	}

	/**
	 * Adds source to destination.
	 *
	 * @param dest Will store the result.
	 * @param source Points to the memory address that contains the second operand.
	 */
//	fun add(dest: RefUByte, source: RefUShort) {
//		dest.value = (dest.value + bus[source.value.toInt()].value).toUByte()
//	}

	private fun popStack() {
		sp.value = (sp.value + 2u).toUShort()
	}

	/**
	 * Remember that the top of the stack is at a the LOWEST address.
	 *
	 * @param uShort The bytes to be written.
	 */
	private fun pushToStack(uShort: UShort) {
		sp.value = (sp.value - 2u).toUShort()
		write((uShort shr 8).toUByte(), sp.value.toInt())
		write(uShort.toUByte(), sp.value.toInt() + 1)
	}

}