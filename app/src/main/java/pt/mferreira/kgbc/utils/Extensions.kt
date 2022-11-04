package pt.mferreira.kgbc.utils

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import java.text.SimpleDateFormat
import java.util.*

fun UByte.convertToHex(): String {
	return "%02X".format(this.toInt())
}

fun getCurrentDate(): String {
	val sdf = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault())
	return sdf.format(Date(System.currentTimeMillis()))
}

fun displayToast(ctx: Context, message: String) {
	Toast.makeText(ctx, message, LENGTH_SHORT).show()
}