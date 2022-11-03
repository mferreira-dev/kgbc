package pt.mfkfdev.kgbc.utils

class Event<out T>(private val content: T) {
	var hasBeenHandled = false
		get() = if (field) true
		else {
			hasBeenHandled = true
			false
		}
		private set

	/**
	 * Returns the content and prevents unwanted behavior.
	 */
	fun getContentIfNotHandled(): T? {
		return if (hasBeenHandled) null
		else {
			hasBeenHandled = true
			content
		}
	}

	/**
	 * Returns the content even if it's already been handled.
	 */
	fun peekContent(): T = content
}