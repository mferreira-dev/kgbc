package pt.mferreira.kgbc.domain.emu

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object RomManager {
	private const val TEMP_ROM_NAME = "temp_rom.gb"

	var hasRomInserted: Boolean = false
		private set

	/**
	 * Unfortunately a URI does not provide us with a file's given path.
	 * This will cause problems later on because that same path is required in order to switch banks.
	 *
	 * The solution I came up with for this issue is to simply copy the ROM into the app's local storage
	 * while it's being used and making sure it's deleted as soon as possible.
	 */
	fun copyRomToInternalStorage(context: Context, bytes: ByteArray) {
		deleteTempRom(context)
		val fos = FileOutputStream(File(context.filesDir, TEMP_ROM_NAME))
		fos.write(bytes)
		hasRomInserted = true
	}

	private fun doesFileExist(file: File): Boolean {
		return file.exists()
	}

	fun deleteTempRom(context: Context) {
		val tempRom = File(context.filesDir, TEMP_ROM_NAME)
		if (!doesFileExist(tempRom)) return
		else tempRom.delete()
	}
}