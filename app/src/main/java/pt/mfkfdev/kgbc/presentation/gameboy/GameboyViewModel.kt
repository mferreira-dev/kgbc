package pt.mfkfdev.kgbc.presentation.gameboy

import android.app.Application
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResult
import androidx.lifecycle.AndroidViewModel
import pt.mfkfdev.kgbc.R
import pt.mfkfdev.kgbc.domain.emu.RomManager
import pt.mfkfdev.kgbc.utils.displayToast

class GameboyViewModel(private val app: Application) : AndroidViewModel(app) {

	companion object {
		const val GB_EXT = "gb"
		const val GBC_EXT = "gbc"
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

				val byteCursor = app.applicationContext.contentResolver?.openInputStream(uri)
				RomManager.copyRomToInternalStorage(
					app.applicationContext,
					byteCursor?.readBytes() ?: ByteArray(0)
				)
				byteCursor?.close()

				// TODO: Load banks.
			}
		}
	}

}