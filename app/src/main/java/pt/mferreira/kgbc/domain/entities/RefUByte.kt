package pt.mferreira.kgbc.domain.entities

/**
 * This class' sole purpose is to bypass the fact that the JVM does not support pass by reference.
 */
data class RefUByte(
	var value: UByte = 0x0u
)