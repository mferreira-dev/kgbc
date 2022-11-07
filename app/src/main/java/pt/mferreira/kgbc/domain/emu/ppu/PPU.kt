package pt.mferreira.kgbc.domain.emu.ppu

import pt.mferreira.kgbc.domain.emu.cpu.CPU
import pt.mferreira.kgbc.domain.emu.ppu.Constants.PPU_IO_LENGTH
import pt.mferreira.kgbc.domain.emu.ppu.Constants.PPU_IO_START_ADDRESS
import pt.mferreira.kgbc.domain.emu.ppu.Constants.VRAM_LENGTH
import pt.mferreira.kgbc.domain.emu.ppu.Constants.VRAM_START_ADDRESS

class PPU(private val cpu: CPU) {

	val vram = cpu.fetchMemory(VRAM_START_ADDRESS, VRAM_LENGTH)
	val io = cpu.fetchMemory(PPU_IO_START_ADDRESS, PPU_IO_LENGTH)

	private fun drawBackground() {

	}

}