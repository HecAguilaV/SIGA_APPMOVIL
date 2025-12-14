package com.example.sigaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sigaapp.data.model.Local
import com.example.sigaapp.data.repository.SaaSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GlobalViewModel(private val repository: SaaSRepository) : ViewModel() {

    private val _locales = MutableStateFlow<List<Local>>(emptyList())
    val locales: StateFlow<List<Local>> = _locales

    private val _selectedLocal = MutableStateFlow<Local?>(null)
    val selectedLocal: StateFlow<Local?> = _selectedLocal
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadLocales()
    }

    fun loadLocales() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getLocales().onSuccess {
                _locales.value = it
                // Si solo hay un local, seleccionarlo automáticamente
                if (it.size == 1) {
                    _selectedLocal.value = it.first()
                }
            }.onFailure {
                // Log or handle error?
            }
            _isLoading.value = false
        }
    }

    fun selectLocal(local: Local?) {
        _selectedLocal.value = local
    }
}

class GlobalViewModelFactory(private val repository: SaaSRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GlobalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GlobalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
