package pt.mferreira.kgbc.presentation.container

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pt.mferreira.kgbc.utils.Event

class ContainerViewModel : ViewModel() {
	private val _isLoading = MutableLiveData<Event<Boolean>>()
	val isLoading: LiveData<Event<Boolean>>
		get() = _isLoading

	fun isLoading(isLoading: Boolean) {
		_isLoading.value = Event(isLoading)
	}
}