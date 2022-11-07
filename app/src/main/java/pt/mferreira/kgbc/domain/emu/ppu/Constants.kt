package pt.mferreira.kgbc.domain.emu.ppu

object Constants {
	const val VRAM_LENGTH = 8192
	const val VRAM_START_ADDRESS = 0x8000

	const val PPU_IO_LENGTH = 12
	const val PPU_IO_START_ADDRESS = 0xFF40

	const val DEBUG_PPU = "DEBUG_PPU"
}