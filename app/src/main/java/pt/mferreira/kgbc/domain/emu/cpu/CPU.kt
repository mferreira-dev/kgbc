package pt.mferreira.kgbc.domain.emu.cpu

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.mferreira.kgbc.BuildConfig
import pt.mferreira.kgbc.R
import pt.mferreira.kgbc.domain.emu.cpu.Constants.A_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.BYPASS_BOOTSTRAP_ADDRESS
import pt.mferreira.kgbc.domain.emu.cpu.Constants.B_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.CPU_CYCLES_PER_SECOND
import pt.mferreira.kgbc.domain.emu.cpu.Constants.C_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.DEBUG_CPU
import pt.mferreira.kgbc.domain.emu.cpu.Constants.D_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.E_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.F_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.GAMEBOY_BUS_TOTAL_ADDRESSES
import pt.mferreira.kgbc.domain.emu.cpu.Constants.H_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.LD_MASK_4X
import pt.mferreira.kgbc.domain.emu.cpu.Constants.LD_MASK_5X
import pt.mferreira.kgbc.domain.emu.cpu.Constants.LD_MASK_6X
import pt.mferreira.kgbc.domain.emu.cpu.Constants.LD_MASK_7X
import pt.mferreira.kgbc.domain.emu.cpu.Constants.L_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.NUMBER_OF_8_BIT_REGISTERS
import pt.mferreira.kgbc.domain.emu.cpu.Constants.PREFIXED_INSTRUCTION
import pt.mferreira.kgbc.domain.emu.cpu.Constants.STACK_POINTER_STARTING_ADDRESS
import pt.mferreira.kgbc.domain.emu.cpu.Constants.SUB_MASK
import pt.mferreira.kgbc.domain.entities.RefUByte
import pt.mferreira.kgbc.domain.entities.RefUShort
import pt.mferreira.kgbc.utils.*
import pt.mferreira.kgbc.utils.Globals.DEV_FLAVOR
import java.io.File

class CPU {

	// region Memory

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
//	private var sp: RefUShort = RefUShort(BYPASS_BOOTSTRAP_ADDRESS)
	private var sp: RefUShort = RefUShort(STACK_POINTER_STARTING_ADDRESS)

	/**
	 * Program counter.
	 */
//	private var pc: RefUShort = RefUShort(BYPASS_BOOTSTRAP_ADDRESS)
	private var pc: RefUShort = RefUShort()

	/**
	 * Conjoin two bytes (usually registers).
	 *
	 * @param msb The most significant byte. Will be shifted left 8 bits.
	 * @param lsb The least significant byte. Will be positioned at the least significant 8 bits.
	 */
	private fun conjoin(msb: RefUByte, lsb: RefUByte): RefUShort {
		return RefUShort().apply {
			value = value or RefUShort(msb.value.toUShort() shl 8).value
			value = value or RefUShort(lsb.value.toUShort()).value
		}
	}

	fun fetchMemory(start: Int, length: Int): Array<RefUByte> {
		val array = Array(length) { RefUByte() }

		array.forEachIndexed { index, _ ->
			array[index] = bus[index + start]
		}

		return array
	}

	// endregion

	// region Run

	private var cpuCycles: Int = 0

	private var endTimestamp: Long = 0
	private var isCpuRunning: Boolean = false

	private val scope = CoroutineScope(Dispatchers.Default)

	fun bootFromBootRom(bytes: ByteArray) {
		scope.launch {
			powerOff()
			write(bytes, 0)
			startCoreLoop()
		}
	}

	fun bootFromCartridge(bytes: ByteArray) {
		scope.launch {
			powerOff()
			write(bytes, BYPASS_BOOTSTRAP_ADDRESS.toInt())
			startCoreLoop()
		}
	}

	fun powerOff() {
		isCpuRunning = false
		clearMemory()
	}

	/**
	 * Run the fetch -> decode -> execute loop indefinitely.
	 *
	 * The CPU will continously run at [Constants.CPU_CYCLES_PER_SECOND].
	 * This is achieved in the following way:
	 *
	 * 1. The system starts a new cycle batch every second, which means the CPU
	 * will loop exactly [Constants.CPU_CYCLES_PER_SECOND] times.
	 * A now() call provides the batch's beginning point while simply adding 1000
	 * to that same tell us when it should end.
	 *
	 * 2. At the beginning of each while loop cycle the system checks if it's time to start yet another batch.
	 * If the time elapsed is under a second the program flow will continue as normal as long as
	 * the [Constants.CPU_CYCLES_PER_SECOND] threshold hasn't been hit.
	 */
	private fun startCoreLoop() {
		isCpuRunning = true
		startNewCycleBatch()

		while (true) {
			if (!isCpuRunning)
				return

			if (now() >= endTimestamp) {
				if (BuildConfig.FLAVOR == DEV_FLAVOR)
					logBatchComplete()

				startNewCycleBatch()
			}

			if (cpuCycles < CPU_CYCLES_PER_SECOND) {
				val opcode = fetchOpcode()
				if (opcode.convertToBin() == PREFIXED_INSTRUCTION)
					runPrefixedInstruction(opcode)
				else
					runUnprefixedInstruction(opcode)
			}
		}
	}

	private fun startNewCycleBatch() {
		val now = now()
		endTimestamp = now + 1000L
		cpuCycles = 0

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
		Log.d(DEBUG_CPU, "CPU CC: $cpuCycles")
		Log.d(DEBUG_CPU, "------------------------------")
	}

	private fun runUnprefixedInstruction(opcode: UByte) {
		// NOP.
		if (opcode.toInt() == 0x0) {
			cpuCycles++
			return
		}

		else if (maskOpcode(SUB_MASK, opcode.convertToBin())) {
			val ls3b = (opcode.toInt() and 0x7).toUByte()

			if (ls3b.toInt() == 0x6) {
				val address = conjoin(reg[H_IDX], reg[L_IDX]).value
				subtract(bus[address.toInt()].value)
				cpuCycles += 2
				return
			}

			if (ls3b.toInt() == 0x7) {
				subtract(reg[A_IDX].value)
				cpuCycles++
				return
			}

			subtract(reg[ls3b.toInt()].value)
			cpuCycles++
			return
		}

		else if (opcode.toInt() == 0xD6) {
			subtract(fetchOpcode())
			cpuCycles += 2
			return
		}

		else if (opcode.toInt() == 0x76) {
			// TOOD: Implement HALT instruction.
//			TODO("0x${opcode.convertToHex2()}")
			return
		}

		else if (maskOpcode(LD_MASK_4X, opcode.convertToBin()) ||
				   maskOpcode(LD_MASK_5X, opcode.convertToBin()) ||
				   maskOpcode(LD_MASK_6X, opcode.convertToBin()) ||
				   maskOpcode(LD_MASK_7X, opcode.convertToBin())
		) {

			val ms4b = (opcode.toInt() and 0xF0)
			val ls4b = (opcode.toInt() and 0xF)

			val dest = if (ms4b == 0x40) {
				if (ls4b < 0x8) reg[B_IDX]
				else reg[C_IDX]
			} else if (ms4b == 0x50) {
				if (ls4b < 0x8) reg[D_IDX]
				else reg[E_IDX]
			} else if (ms4b == 0x60) {
				if (ls4b < 0x8) reg[H_IDX]
				else reg[L_IDX]
			} else {
				if (ls4b < 0x8) bus[conjoin(reg[H_IDX], reg[L_IDX]).value.toInt()]
				else reg[A_IDX]
			}

			val source = if (ls4b == 0x6 || ls4b == 0xE)
				bus[conjoin(reg[H_IDX], reg[L_IDX]).value.toInt()]
			else if (ls4b == 0x7 || ls4b == 0xF)
				reg[A_IDX]
			else {
				if (ls4b < 0x8) reg[ls4b]
				else reg[ls4b - 8]
			}

			val incrementCyclesBy = if (ls4b == 0x6 || ls4b == 0xE) 2
			else {
				if (ms4b == 0x70) {
					if (ls4b < 0x8) 2
					else 1
				} else 1
			}

			load(dest, source)
			cpuCycles = cpuCycles + incrementCyclesBy
			return
		}

//		TODO("0x${opcode.convertToHex2()}")
	}

	private fun runPrefixedInstruction(opcode: UByte) {
//		TODO("Not yet implemented: ${opcode.convertToHex4()}")
	}

	/**
	 * Returns the value of the address currently pointed at by the PC in binary.
	 */
	private fun fetchOpcode(): UByte {
		val opcode = bus[pc.value.toInt()].value
		pc.value++
		return opcode
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

	private fun clearMemory() {
		bus.forEach { it.value = 0x0u }
		reg.forEach { it.value = 0x0u }
		sp.value = STACK_POINTER_STARTING_ADDRESS
//		pc.value = BYPASS_BOOTSTRAP_ADDRESS
		pc.value = 0x0u
	}

	// endregion

	// region Load

	/**
	 * Example:
	 *
	 * LD A, ($3FFF)
	 * This instruction loads the accumulator from memory location $3FFF.
	 *
	 * @param dest Where to write.
	 * @param source What to write.
	 */
	private fun load(dest: RefUByte, source: RefUByte) {
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

	// endregion

	// region Write

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

	// endregion

	// region Arithmetic

	private fun addition(source: UByte) {
		reg[A_IDX].value = (reg[A_IDX].value + source).toUByte()

		setSubFlag(false)
		setZeroFlagAdd(reg[A_IDX].value, source)
		setHalfCarryFlagAdd(reg[A_IDX].value, source)
		setCarryFlagAdd(reg[A_IDX].value, source)
	}

	private fun subtract(source: UByte) {
		reg[A_IDX].value = (reg[A_IDX].value - source).toUByte()

		setSubFlag(true)
		setZeroFlagSub(reg[A_IDX].value, source)
		setHalfCarryFlagSub(reg[A_IDX].value, source)
		setCarryFlagSub(reg[A_IDX].value, source)
	}

	// endregion

	// region Flag handlers

	private fun setZeroFlagAdd(operand1: UByte, operand2: UByte) {
		setZeroFlag((operand1 + operand2).toUByte() == RefUByte().value)
	}

	private fun setZeroFlagSub(operand1: UByte, operand2: UByte) {
		setZeroFlag((operand1 - operand2).toUByte() == RefUByte().value)
	}

	private fun setZeroFlag(set: Boolean) {
		if (set)
			reg[F_IDX].value = (reg[F_IDX].value.toInt() or 0x80).toUByte()
		else
			reg[F_IDX].value = (reg[F_IDX].value.toInt() and 0x70).toUByte()
	}

	private fun setSubFlag(set: Boolean) {
		if (set)
			reg[F_IDX].value = (reg[F_IDX].value.toInt() or 0x40).toUByte()
		else
			reg[F_IDX].value = (reg[F_IDX].value.toInt() and 0xB0).toUByte()
	}

	private fun setHalfCarryFlagAdd(operand1: UByte, operand2: UByte) {
		val result = (((operand1.toInt() and 0xF) + (operand2.toInt() and 0xF)) and 0x10) == 0x10
		setHalfCarryFlag(result)
	}

	private fun setHalfCarryFlagAdd(operand1: UShort, operand2: UShort) {
		val result = (((operand1.toInt() and 0xFFF) + (operand2.toInt() and 0xFFF)) and 0x1000) == 0x1000
		setHalfCarryFlag(result)
	}

	private fun setHalfCarryFlagSub(operand1: UByte, operand2: UByte) {
		val result = (((operand1.toInt() and 0xF) - (operand2.toInt() and 0xF)) and 0x10) == 0x10
		setHalfCarryFlag(result)
	}

	private fun setHalfCarryFlagSub(operand1: UShort, operand2: UShort) {
		val result = (((operand1.toInt() and 0xFFF) - (operand2.toInt() and 0xFFF)) and 0x1000) == 0x1000
		setHalfCarryFlag(result)
	}

	private fun setHalfCarryFlag(set: Boolean) {
		if (set)
			reg[F_IDX].value = (reg[F_IDX].value.toInt() or 0x20).toUByte()
		else
			reg[F_IDX].value = (reg[F_IDX].value.toInt() and 0xD0).toUByte()
	}

	private fun setCarryFlagAdd(operand1: UByte, operand2: UByte) {
		val result = (operand1 + operand2).toInt() and 0x100 == 0x100
		setCarryFlag(result)
	}

	private fun setCarryFlagAdd(operand1: UShort, operand2: UShort) {
		val result = (operand1 + operand2).toInt() and 0x10000 == 0x10000
		setCarryFlag(result)
	}

	private fun setCarryFlagSub(operand1: UByte, operand2: UByte) {
		val result = (operand1 - operand2).toInt() and 0x100 == 0x100
		setCarryFlag(result)
	}

	private fun setCarryFlagSub(operand1: UShort, operand2: UShort) {
		val result = (operand1 - operand2).toInt() and 0x10000 == 0x10000
		setCarryFlag(result)
	}

	private fun setCarryFlag(set: Boolean) {
		if (set)
			reg[F_IDX].value = (reg[F_IDX].value.toInt() or 0x10).toUByte()
		else
			reg[F_IDX].value = (reg[F_IDX].value.toInt() and 0xE0).toUByte()
	}

	// endregion

	// region Stack

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

	// endregion

	// region Misc

	/**
	 * Dump the GameBoy's current address bus to an internal text file.
	 */
	fun dump(context: Context) {
		val dump = File(context.filesDir, "dump_" + getCurrentDate() + ".txt")

		dump.printWriter().use { printer ->
			bus.forEachIndexed { index, refUByte ->
				var buffer = refUByte.value.convertToHex2()
				buffer += if ((index + 1) % 16 == 0) "\n" else " "
				printer.print(buffer)
			}
		}

		displayToast(context, context.getString(R.string.dumped_bus, dump.name))
	}

	// endregion

}