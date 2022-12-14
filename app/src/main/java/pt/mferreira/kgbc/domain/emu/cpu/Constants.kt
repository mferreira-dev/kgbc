package pt.mferreira.kgbc.domain.emu.cpu

object Constants {

	const val GAMEBOY_BUS_TOTAL_ADDRESSES = 65536

	const val NUMBER_OF_8_BIT_REGISTERS = 8

	/**
	 * Theoretically the GameBoy's CPU runs at 4.19 MHz (T-Cycles).
	 *
	 * However, an operation takes at the very least 4 cycles to complete which means that
	 * effectively the CPU runs at 4.19 / 4 = 1.05 MHz (M-Cycles).
	 */
	const val CPU_CYCLES_PER_SECOND = 1048576

	/**
	 * The GameBoy has a bootstrap process that involves reading from a boot ROM.
	 * However, you cannot legally include a copy of a boot ROM with an emulator so we're left with
	 * one of two choices: either ask the user for a copy of their own or to skip this process entirely.
	 */
	const val BYPASS_BOOTSTRAP_ADDRESS: UShort = 0x0100u

	const val STACK_POINTER_STARTING_ADDRESS: UShort = 0xFFFEu

	const val B_IDX = 0
	const val C_IDX = 1
	const val D_IDX = 2
	const val E_IDX = 3
	const val H_IDX = 4
	const val L_IDX = 5
	const val A_IDX = 6
	const val F_IDX = 7

	const val PREFIXED_INSTRUCTION = "CB"

	const val SUB_MASK = "10010___"
	const val LD_MASK_4X = "0100____"
	const val LD_MASK_5X = "0101____"
	const val LD_MASK_6X = "0110____"
	const val LD_MASK_7X = "0111____"

	const val DEBUG_CPU = "DEBUG_CPU"

}