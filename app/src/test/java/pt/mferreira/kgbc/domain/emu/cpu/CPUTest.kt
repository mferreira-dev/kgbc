package pt.mferreira.kgbc.domain.emu.cpu

import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mferreira.kgbc.domain.emu.cpu.CPUConstants.SUB_MASK
import pt.mferreira.kgbc.domain.emu.entities.RefUByte
import pt.mferreira.kgbc.domain.emu.entities.RefUShort
import pt.mferreira.kgbc.utils.convertToBin
import pt.mferreira.kgbc.utils.shl
import pt.mferreira.kgbc.utils.shr

class CPUTest {

	companion object {
		private const val F_IDX = 7
	}

	private val reg = Array(CPUConstants.NUMBER_OF_8_BIT_REGISTERS) { RefUByte() }

	@Test
	fun conjoinRegisters() {
		// Test code.
		val msb = RefUByte(100u)
		val lsb = RefUByte(248u)

		val conjoined = RefUShort().apply {
			value = value or RefUShort(msb.value.toUShort() shl 8).value
			value = value or RefUShort(lsb.value.toUShort()).value
		}

		assertTrue(conjoined.value.convertToBin() == "0110010011111000")
	}

	@Test
	fun maskOpcode() {
		var isMatch = true

		// Test code.
		val mask = SUB_MASK
		val target = "10010001"

		for (idx in mask.indices) {
			if (mask[idx] == '_')
				continue

			if (target[idx] != mask[idx]) {
				isMatch = false
				break
			}
		}

		assertTrue(isMatch)
	}

	@Test
	fun addition() {

	}

	@Test
	fun subtract() {
		
	}

	@Test
	fun setZeroFlagTrue() {
		reg[F_IDX].value = (reg[F_IDX].value.toInt() or 0x80).toUByte()

		// Test code.
		val zeroFlagValue = (reg[F_IDX].value.toInt() and 0x80).toUByte()
		assertTrue((zeroFlagValue shr 7).toInt() == 0x1)
	}

	@Test
	fun setZeroFlagFalse() {
		reg[F_IDX].value = (reg[F_IDX].value.toInt() and 0x70).toUByte()

		// Test code.
		val zeroFlagValue = (reg[F_IDX].value.toInt() and 0x80).toUByte()
		assertTrue((zeroFlagValue shr 7).toInt() == 0x0)
	}

}