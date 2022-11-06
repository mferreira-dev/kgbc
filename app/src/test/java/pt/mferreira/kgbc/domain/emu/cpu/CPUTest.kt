package pt.mferreira.kgbc.domain.emu.cpu

import org.junit.Assert.*
import org.junit.Test
import pt.mferreira.kgbc.domain.emu.entities.RefUByte
import pt.mferreira.kgbc.utils.shr

class CPUTest {

	companion object {
		private const val F_IDX = 7
	}

	private val reg = Array(CPUConstants.NUMBER_OF_8_BIT_REGISTERS) { RefUByte() }

	@Test
	fun setZeroFlagTrue() {
		reg[F_IDX].value = (reg[F_IDX].value.toInt() or 0x80).toUByte()

		// Test.
		val zeroFlagValue = (reg[F_IDX].value.toInt() and 0x80).toUByte()
		assertTrue((zeroFlagValue shr 7).toInt() == 0x1)
	}

	@Test
	fun setZeroFlagFalse() {
		reg[F_IDX].value = (reg[F_IDX].value.toInt() and 0x70).toUByte()

		// Test.
		val zeroFlagValue = (reg[F_IDX].value.toInt() and 0x80).toUByte()
		assertTrue((zeroFlagValue shr 7).toInt() == 0x0)
	}

}