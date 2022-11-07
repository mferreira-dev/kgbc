package pt.mferreira.kgbc.domain.emu.ppu

object Constants {
	const val VRAM_LENGTH = 8192
	const val VRAM_START_ADDRESS = 0x8000

	const val PPU_IO_LENGTH = 12
	const val PPU_IO_START_ADDRESS = 0xFF40

	const val PPU_CYCLES_PER_SECOND = 60

	const val DEFAULT_WIDTH = 160
	const val DEFAULT_HEIGHT = 144

	const val NATIVE_COLOR_1 = "#FFFFFF"
	const val NATIVE_COLOR_2 = "#DAB3B3"
	const val NATIVE_COLOR_3 = "#614F4F"
	const val NATIVE_COLOR_4 = "#000000"

	const val DISPLAY_SCALE = 2

	const val DEBUG_PPU = "DEBUG_PPU"
}