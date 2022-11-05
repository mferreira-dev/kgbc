package pt.mferreira.kgbc.utils

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import java.text.SimpleDateFormat
import java.util.*

infix fun UByte.shl(shift: Int): UByte {
	return (this.toInt() shl shift).toUByte()
}

infix fun UByte.shr(shift: Int): UByte {
	return (this.toInt() shr shift).toUByte()
}

infix fun UShort.shl(shift: Int): UShort {
	return (this.toInt() shl shift).toUShort()
}

infix fun UShort.shr(shift: Int): UShort {
	return (this.toInt() shr shift).toUShort()
}

fun UByte.convertToHex2(): String {
	return "%02X".format(this.toInt())
}

fun UShort.convertToHex2(): String {
	return "%02X".format(this.toInt())
}

fun UByte.convertToHex4(): String {
	return "%04X".format(this.toInt())
}

fun UShort.convertToHex4(): String {
	return "%04X".format(this.toInt())
}

fun UByte.convertToBin(): String {
	return String.format("%8s", Integer.toBinaryString(this.toInt())).replace(" ", "0")
}

fun UShort.convertToBin(): String {
	return String.format("%16s", Integer.toBinaryString(this.toInt())).replace(" ", "0")
}

fun getCurrentDate(): String {
	val sdf = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault())
	return sdf.format(Date(System.currentTimeMillis()))
}

fun now(): Long {
	return System.currentTimeMillis()
}

fun displayToast(ctx: Context, message: String) {
	Toast.makeText(ctx, message, LENGTH_SHORT).show()
}