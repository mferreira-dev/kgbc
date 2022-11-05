package pt.mferreira.kgbc.utils

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import java.text.SimpleDateFormat
import java.util.*

inline infix fun UByte.shl(shift: Int): UByte {
	return (this.toInt() shl shift).toUByte()
}

inline infix fun UByte.shr(shift: Int): UByte {
	return (this.toInt() shr shift).toUByte()
}

inline infix fun UShort.shl(shift: Int): UShort {
	return (this.toInt() shl shift).toUShort()
}

inline infix fun UShort.shr(shift: Int): UShort {
	return (this.toInt() shr shift).toUShort()
}

fun UByte.convertToHex(): String {
	return "%04X".format(this.toInt())
}

fun UShort.convertToHex(): String {
	return "%04X".format(this.toInt())
}

fun getCurrentDate(): String {
	val sdf = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault())
	return sdf.format(Date(System.currentTimeMillis()))
}

fun displayToast(ctx: Context, message: String) {
	Toast.makeText(ctx, message, LENGTH_SHORT).show()
}