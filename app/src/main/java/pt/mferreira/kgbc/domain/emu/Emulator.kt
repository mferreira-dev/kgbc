package pt.mferreira.kgbc.domain.emu

import android.content.Context
import pt.mferreira.kgbc.R
import pt.mferreira.kgbc.domain.emu.cpu.CPU
import pt.mferreira.kgbc.domain.emu.ppu.PPU

class Emulator {

	private val cpu: CPU
	private val ppu: PPU
	private val romManager: RomManager

	init {
		cpu = CPU()
		ppu = PPU(cpu)
		romManager = RomManager()
	}

	fun powerOn(context: Context) {
		val rom = context.resources.openRawResource(R.raw.dmg_boot)
		val bytes = rom.readBytes()
		rom.close()
		cpu.bootFromBootRom(bytes)
	}

	fun powerOff() {
		cpu.powerOff()
	}

	fun startGame(context: Context, bytes: ByteArray) {
		romManager.copyRomToInternalStorage(context, bytes)
		cpu.bootFromCartridge(bytes)
	}

	fun dumpMemory(context: Context) {
		cpu.dump(context)
	}

}