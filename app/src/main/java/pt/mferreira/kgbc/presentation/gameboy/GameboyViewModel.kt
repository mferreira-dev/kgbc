package pt.mferreira.kgbc.presentation.gameboy

import android.app.Application
import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import pt.mferreira.kgbc.R
import pt.mferreira.kgbc.domain.emu.RomManager
import pt.mferreira.kgbc.domain.emu.cpu.CPU
import pt.mferreira.kgbc.utils.displayToast

class GameboyViewModel(private val app: Application) : AndroidViewModel(app) {

	companion object {
		const val GB_EXT = "gb"
		const val GBC_EXT = "gbc"
	}

	init {
		RomManager.deleteTempRom(app.applicationContext)
	}

	fun handleFilePickerResult(result: ActivityResult) {
		result.data?.data?.let outer@{ uri ->
			val dataCursor = app.applicationContext.contentResolver?.query(uri, null, null, null, null)

			dataCursor?.let {
				if (!dataCursor.moveToFirst())
					return@outer

				val nameIndex = dataCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
				val fileDisplayName = dataCursor.getString(nameIndex)
				val extension = fileDisplayName.substring(fileDisplayName.indexOf(".") + 1)

				dataCursor.close()

				if (extension != GB_EXT && extension != GBC_EXT) {
					displayToast(
						app.applicationContext,
						app.applicationContext.getString(R.string.open_rom_error)
					)
					return@outer
				}

				// User selected an actual ROM file, it is now safe to delete the temp ROM.
				RomManager.deleteTempRom(app.applicationContext)

				val byteCursor = app.applicationContext.contentResolver?.openInputStream(uri)
				val bytes = byteCursor?.readBytes() ?: ByteArray(0)

				RomManager.copyRomToInternalStorage(app.applicationContext, bytes)
				CPU.bootFromCartridge(bytes)

				byteCursor?.close()
			}
		}
	}

	fun openRom(filePicker: ActivityResultLauncher<Intent>) {
		val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
		filePicker.launch(intent)
	}

}