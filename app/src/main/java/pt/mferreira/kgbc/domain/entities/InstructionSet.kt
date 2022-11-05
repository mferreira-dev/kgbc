package pt.mferreira.kgbc.domain.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InstructionSet(
	val unprefixed: Unprefixed,
	val cbprefixed: Cbprefixed
) {
	@JsonClass(generateAdapter = true)
	data class Unprefixed(
		val instruction: List<Instruction>
	)

	@JsonClass(generateAdapter = true)
	data class Cbprefixed(
		val instruction: List<Instruction>
	)

	@JsonClass(generateAdapter = true)
	data class Instruction(
		val mnemonic: String,
		val bytes: Int,
		val cycles: List<Int>,
		val operands: List<Operand>,
		val immediate: Boolean,
		val flags: Flags
	) {
		@JsonClass(generateAdapter = true)
		data class Operand(
			val name: String,
			val bytes: Int?,
			val immediate: Boolean
		)

		@JsonClass(generateAdapter = true)
		data class Flags(
			@Json(name = "Z")
			val z: String,
			@Json(name = "N")
			val n: String,
			@Json(name = "H")
			val h: String,
			@Json(name = "C")
			val c: String
		)
	}
}