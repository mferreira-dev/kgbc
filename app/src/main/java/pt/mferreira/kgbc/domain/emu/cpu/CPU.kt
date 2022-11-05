package pt.mferreira.kgbc.domain.emu.cpu

import android.content.Context
import android.util.Log
import pt.mferreira.kgbc.BuildConfig
import pt.mferreira.kgbc.R
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.A_IDX
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.BYPASS_BOOTSTRAP_ADDRESS
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.B_IDX
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.C_IDX
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.DEBUG_CPU
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.D_IDX
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.E_IDX
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.F_IDX
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.GAMEBOY_BUS_TOTAL_ADDRESSES
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.H_IDX
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.L_IDX
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.MAX_CYCLES_PER_SECOND
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.NUMBER_OF_8_BIT_REGISTERS
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.PREFIXED_INSTRUCTION
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.SUB_MASK
import pt.mferreira.kgbc.domain.emu.entities.RefUByte
import pt.mferreira.kgbc.domain.emu.entities.RefUShort
import pt.mferreira.kgbc.utils.*
import pt.mferreira.kgbc.utils.Globals.DEV_FLAVOR
import java.io.File

object CPU {

	/**
	 * An array is better for data that has a known size at compile time since it's stored
	 * on the stack rather than the heap (a list would be stored on the heap).
	 */
	private val bus = Array(GAMEBOY_BUS_TOTAL_ADDRESSES) { RefUByte() }

	/**
	 * B, C, D, E, H, L, A, F.
	 */
	private val reg = Array(NUMBER_OF_8_BIT_REGISTERS) { RefUByte() }

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
//	private var sp: RefUShort = RefUShort(BYPASS_BOOTSTRAP_ADDRESS.toUShort())
	private var sp: RefUShort = RefUShort()

	/**
	 * Program counter.
	 */
//	private var pc: RefUShort = RefUShort(BYPASS_BOOTSTRAP_ADDRESS.toUShort())
	private var pc: RefUShort = RefUShort()

	/**
	 * The reason why we don't use Kotlin's property access syntax (and add private set) instead
	 * is because that would require said property to be a variable rather than a value.
	 */
	fun getRegisterValues(): Array<Int> {
		val list = mutableListOf<Int>().apply {
			reg.forEach { add(it.value.toInt()) }

			add(conjoinRegisters(reg[A_IDX], reg[F_IDX]).value.toInt())
			add(conjoinRegisters(reg[B_IDX], reg[C_IDX]).value.toInt())
			add(conjoinRegisters(reg[D_IDX], reg[E_IDX]).value.toInt())
			add(conjoinRegisters(reg[H_IDX], reg[L_IDX]).value.toInt())

			add(sp.value.toInt())
			add(pc.value.toInt())
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
				var buffer = refUByte.value.convertToHex4()
				buffer += if ((index + 1) % 20 == 0) "\n" else " "
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
	 * The CPU will continously run at [CPUConstants.MAX_CYCLES_PER_SECOND].
	 * This is achieved in the following way:
	 *
	 * 1. The system starts a new cycle batch every second, which means the CPU
	 * will loop exactly [CPUConstants.MAX_CYCLES_PER_SECOND] times.
	 * A now() call provides the batch's beginning point while simply adding 1000
	 * to that same tell us when it should end.
	 *
	 * 2. At the beginning of each while loop cycle the system checks if it's time to start yet another batch.
	 * If the time elapsed is under a second the program flow will continue as normal as long as
	 * the [CPUConstants.MAX_CYCLES_PER_SECOND] threshold hasn't been hit.
	 */
	fun run() {
		startNewCycleBatch()

		while (true) {
			if (now() >= endTimestamp) {
				if (BuildConfig.FLAVOR == DEV_FLAVOR)
					logBatchComplete()

				startNewCycleBatch()
			}

			if (cycles == MAX_CYCLES_PER_SECOND)
				continue

			val opcode = fetchOpcode()
			if (opcode == PREFIXED_INSTRUCTION)
				runPrefixedInstruction(opcode)
			else
				runUnprefixedInstruction(opcode)
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

	private fun runUnprefixedInstruction(opcode: String) {
		if (maskOpcode(SUB_MASK, opcode)) {

		}
	}

	private fun runPrefixedInstruction(opcode: String) {

	}

	/**
	 * Returns the value of the address currently pointed at by the PC in binary.
	 */
	private fun fetchOpcode(): String {
		return bus[pc.value.toInt()].value.convertToBin()
	}

	fun insertCartridge(bytes: ByteArray) {
		write(bytes, BYPASS_BOOTSTRAP_ADDRESS)
	}

	private fun conjoinRegisters(msb: RefUByte, lsb: RefUByte): RefUShort {
		return RefUShort().apply {
			value = value or RefUShort(msb.value.toUShort() shl 8).value
			value = value or RefUShort(lsb.value.toUShort()).value
		}
	}

	/**
	 * The Z80's processor has a variety of instruction groups that share a lot of the same bits.
	 * Using those bits as a mask we can find out to which group a particular opcode belongs to.
	 *
	 * @param mask The mask to use.
	 * @param target The value to be masked.
	 */
	private fun maskOpcode(mask: String, target: String): Boolean {
		var isMatch = true

		for (idx in mask.indices) {
			if (mask[idx] == '_')
				continue

			if (target[idx] != mask[idx]) {
				isMatch = false
				break
			}
		}

		return isMatch
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
		dest.apply {
			value = value and 0u
			value = value or RefUShort(msb.value.toUShort() shl 8).value
			value = value or RefUShort(lsb.value.toUShort()).value
		}
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

	private fun sub(source: UByte) {
//		reg[A_IDX].value = (reg[A_IDX].value - source).toUByte()

	}

	private fun carryAdd(operand1: UByte, operand2: UByte): Boolean {
		return (operand1.toInt() + operand2.toInt()) and 0x100 == 0x100
	}

	private fun carryAdd(operand1: UShort, operand2: UShort): Boolean {
		return (operand1.toInt() + operand2.toInt()) and 0x10000 == 0x10000
	}

	private fun carrySub(operand1: UByte, operand2: UByte): Boolean {
		return (operand1.toInt() - operand2.toInt()) and 0x100 == 0x100
	}

	private fun carrySub(operand1: UShort, operand2: UShort): Boolean {
		return (operand1.toInt() - operand2.toInt()) and 0x10000 == 0x10000
	}

	private fun halfCarryAdd(operand1: UByte, operand2: UByte): Boolean {
		return (((operand1.toInt() and 0xF) + (operand2.toInt() and 0xF)) and 0x10) == 0x10
	}

	private fun halfCarryAdd(operand1: UShort, operand2: UShort): Boolean {
		return (((operand1.toInt() and 0xFFF) + (operand2.toInt() and 0xFFF)) and 0x1000) == 0x1000
	}

	private fun halfCarrySub(operand1: UByte, operand2: UByte): Boolean {
		return (((operand1.toInt() and 0xF) - (operand2.toInt() and 0xF)) and 0x10) == 0x10
	}

	private fun halfCarrySub(operand1: UShort, operand2: UShort): Boolean {
		return (((operand1.toInt() and 0xFFF) - (operand2.toInt() and 0xFFF)) and 0x1000) == 0x1000
	}

	private fun zeroFlagAdd(operand1: UByte, operand2: UByte): Boolean {
		return (operand1 + operand2).toUByte() == RefUByte().value
	}

	private fun zeroFlagSub(operand1: UByte, operand2: UByte): Boolean {
		return (operand1 - operand2).toUByte() == RefUByte().value
	}

	private fun popStack() {
		sp.value = (sp.value + 2u).toUShort()
	}

	/**
	 * Remember that the top of the stack is at a the LOWEST address.
	 *
	 * @param bytes The bytes to be pushed to the stack.
	 */
	private fun pushToStack(bytes: UShort) {
		sp.value = (sp.value - 2u).toUShort()
		write((bytes shr 8).toUByte(), sp.value.toInt())
		write(bytes.toUByte(), sp.value.toInt() + 1)
	}

}