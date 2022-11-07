package pt.mferreira.kgbc.domain.emu.cpu

import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mferreira.kgbc.domain.emu.cpu.Constants.A_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.B_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.C_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.D_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.E_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.F_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.GAMEBOY_BUS_TOTAL_ADDRESSES
import pt.mferreira.kgbc.domain.emu.cpu.Constants.H_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.L_IDX
import pt.mferreira.kgbc.domain.emu.cpu.Constants.NUMBER_OF_8_BIT_REGISTERS
import pt.mferreira.kgbc.domain.emu.cpu.Constants.SUB_MASK
import pt.mferreira.kgbc.domain.entities.RefUByte
import pt.mferreira.kgbc.domain.entities.RefUShort
import pt.mferreira.kgbc.utils.convertToBin
import pt.mferreira.kgbc.utils.shl
import pt.mferreira.kgbc.utils.shr

class CPUTest {

	private val bus = Array(GAMEBOY_BUS_TOTAL_ADDRESSES) { RefUByte() }
	private val reg = Array(NUMBER_OF_8_BIT_REGISTERS) { RefUByte() }

	private var sp: RefUShort = RefUShort()

	private fun write(byte: UByte, offset: Int) {
		bus[offset] = RefUByte(byte)
	}

	private fun conjoin(msb: RefUByte, lsb: RefUByte): RefUShort {
		return RefUShort().apply {
			value = value or RefUShort(msb.value.toUShort() shl 8).value
			value = value or RefUShort(lsb.value.toUShort()).value
		}
	}

	private fun addition(source: UByte) {
		reg[A_IDX].value = (reg[A_IDX].value + source).toUByte()
	}

	private fun subtract(source: UByte) {
		reg[A_IDX].value = (reg[A_IDX].value - source).toUByte()
	}

	@Test
	fun conjoin() {
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
		reg[A_IDX].value = 0x70u
		val source: UByte = 0x64u

		reg[A_IDX].value = (reg[A_IDX].value + source).toUByte()
		assertTrue(reg[A_IDX].value.toInt() == 0xD4)
	}

	@Test
	fun subtract() {
		reg[A_IDX].value = 0x32u
		val source: UByte = 0x16u

		reg[A_IDX].value = (reg[A_IDX].value - source).toUByte()
		assertTrue(reg[A_IDX].value.toInt() == 0x1C)
	}

	@Test
	fun setZeroFlagTrue() {
		reg[F_IDX].value = (reg[F_IDX].value.toInt() or 0x80).toUByte()

		val flag = (reg[F_IDX].value.toInt() and 0x80).toUByte()
		assertTrue((flag shr 7).toInt() == 0x1)
	}

	@Test
	fun setZeroFlagFalse() {
		reg[F_IDX].value = (reg[F_IDX].value.toInt() and 0x70).toUByte()

		val flag = (reg[F_IDX].value.toInt() and 0x80).toUByte()
		assertTrue((flag shr 7).toInt() == 0x0)
	}

	@Test
	fun setSubFlagTrue() {
		reg[F_IDX].value = (reg[F_IDX].value.toInt() or 0x40).toUByte()

		val flag = (reg[F_IDX].value.toInt() and 0x40).toUByte()
		assertTrue((flag shr 6).toInt() == 0x1)
	}

	@Test
	fun setSubFlagFalse() {
		reg[F_IDX].value = (reg[F_IDX].value.toInt() and 0xB0).toUByte()

		val flag = (reg[F_IDX].value.toInt() and 0x40).toUByte()
		assertTrue((flag shr 6).toInt() == 0x0)
	}

	@Test
	fun setHalfCarryFlagTrue() {
		reg[F_IDX].value = (reg[F_IDX].value.toInt() or 0x20).toUByte()

		val flag = (reg[F_IDX].value.toInt() and 0x20).toUByte()
		assertTrue((flag shr 5).toInt() == 0x1)
	}

	@Test
	fun setHalfCarryFlagFalse() {
		reg[F_IDX].value = (reg[F_IDX].value.toInt() and 0xD0).toUByte()

		val flag = (reg[F_IDX].value.toInt() and 0x20).toUByte()
		assertTrue((flag shr 5).toInt() == 0x0)
	}

	@Test
	fun setCarryFlagTrue() {
		reg[F_IDX].value = (reg[F_IDX].value.toInt() or 0x10).toUByte()

		val flag = (reg[F_IDX].value.toInt() and 0x10).toUByte()
		assertTrue((flag shr 4).toInt() == 0x1)
	}

	@Test
	fun setCarryFlagFalse() {
		reg[F_IDX].value = (reg[F_IDX].value.toInt() and 0xE0).toUByte()

		val flag = (reg[F_IDX].value.toInt() and 0x10).toUByte()
		assertTrue((flag shr 4).toInt() == 0x0)
	}

	@Test
	fun popStack() {
		// Test code.
		val initial = sp.value

		sp.value = (sp.value + 2u).toUShort()

		assertTrue(sp.value.toUInt() == initial + 2u)
	}

	@Test
	fun pushToStack() {
		// Test code.
		sp.value = 0x64u
		val bytes: UShort = 0x3E8u
		val expected = "0000001111101000"

		sp.value = (sp.value - 2u).toUShort()
		write((bytes shr 8).toUByte(), sp.value.toInt())
		write(bytes.toUByte(), sp.value.toInt() + 1)

		val result = conjoin(bus[sp.value.toInt()], bus[sp.value.toInt() + 1])
		assertTrue(result.value.convertToBin() == expected)
	}

	@Test
	fun subOp() {
		val opcode: UByte = 0x93u
		bus[0x0504].value = 0xDu
		reg[A_IDX].value = 0x64u
		reg[H_IDX].value = 0x5u
		reg[L_IDX].value = 0x4u

		val ls3b = (opcode.toInt() and 0x7).toUByte()

		if (ls3b.toInt() == 0x6) {
			val address = conjoin(reg[H_IDX], reg[L_IDX]).value
			subtract(bus[address.toInt()].value)
			assertTrue(reg[A_IDX].value.toInt() == 0x57)
		}

		if (ls3b.toInt() == 0x7) {
			subtract(reg[A_IDX].value)
			assertTrue(reg[A_IDX].value.toInt() == 0x0)
		}

		val oldRegA = reg[A_IDX].value
		subtract(reg[ls3b.toInt()].value)
		assertTrue(reg[A_IDX].value.toInt() + reg[ls3b.toInt()].value.toInt() == oldRegA.toInt())
	}
	
	@Test
	fun largeLoadGroup() {
		val opcode: UByte = 0x7Au

		val ms4b = (opcode.toInt() and 0xF0)
		val ls4b = (opcode.toInt() and 0xF)

		val destPath: Int
		val sourcePath: Int

		val dest = if (ms4b == 0x40) {
			if (ls4b < 0x8) {
				reg[B_IDX]
				destPath = 0
			}else {
				reg[C_IDX]
				destPath = 1
			}
		} else if (ms4b == 0x50) {
			if (ls4b < 0x8) {
				reg[D_IDX]
				destPath = 2
			} else {
				reg[E_IDX]
				destPath = 3
			}
		} else if (ms4b == 0x60) {
			if (ls4b < 0x8) {
				reg[H_IDX]
				destPath = 4
			} else {
				reg[L_IDX]
				destPath = 5
			}
		} else {
			if (ls4b < 0x8) {
				bus[conjoin(reg[H_IDX], reg[L_IDX]).value.toInt()]
				destPath = 6
			} else {
				reg[A_IDX]
				destPath = 7
			}
		}

		val source = if (ls4b == 0x6 || ls4b == 0xE) {
			bus[conjoin(reg[H_IDX], reg[L_IDX]).value.toInt()]
			sourcePath = 0
		} else if (ls4b == 0x7 || ls4b == 0xF) {
			reg[A_IDX]
			sourcePath = 1
		} else {
			if (ls4b < 0x8) {
				reg[ls4b]
				sourcePath = 2
			} else {
				reg[ls4b - 8]
				sourcePath = 3
			}
		}

		val incrementCyclesBy = if (ls4b == 0x6 || ls4b == 0xE)
			2
		else {
			if (ms4b == 0x70) {
				if (ls4b < 0x8)
					2
				else
					1
			} else
				1
		}

		assertTrue(destPath == 7)
		assertTrue(sourcePath == 3)
		assertTrue(incrementCyclesBy == 1)
	}

}