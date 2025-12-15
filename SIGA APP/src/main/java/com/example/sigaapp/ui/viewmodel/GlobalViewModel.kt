package com.example.sigaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sigaapp.data.model.DollarIndicatorResponse
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

    private val _dollarIndicator = MutableStateFlow(DollarIndicatorUiState(isLoading = true))
    val dollarIndicator: StateFlow<DollarIndicatorUiState> = _dollarIndicator

    init {
        loadLocales()
        refreshDollarIndicator()
    }

    fun loadLocales() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getLocales().onSuccess {
                _locales.value = it
                
                // Estrategia de Auto-Selección:
                // 1. Si hay un "Local por Defecto" (del login), seleccionarlo.
                // 2. Si no, pero solo hay 1 local en la lista, seleccionarlo.
                val defaultId = repository.getDefaultLocalId()
                if (defaultId != null) {
                    val match = it.find { local -> local.id == defaultId }
                    if (match != null) {
                        _selectedLocal.value = match
                        return@onSuccess
                    }
                }
                
                // Fallback: Si solo hay 1 local
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
        if (local != null) {
            repository.saveDefaultLocalId(local.id)
        }
    }

    fun refreshDollarIndicator() {
        viewModelScope.launch {
            _dollarIndicator.value = _dollarIndicator.value.copy(isLoading = true, error = null)
            runCatching { repository.fetchDollarIndicator() }
                .onSuccess { response ->
                    val lastValue = response.serie.firstOrNull()
                    _dollarIndicator.value = DollarIndicatorUiState(
                        value = lastValue?.valor,
                        unit = response.unidadMedida ?: "",
                        date = lastValue?.fecha ?: "",
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _dollarIndicator.value = DollarIndicatorUiState(isLoading = false, error = error.message)
                }
        }
    }
}

data class DollarIndicatorUiState(
    val value: Double? = null,
    val unit: String = "",
    val date: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class GlobalViewModelFactory(private val repository: SaaSRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GlobalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GlobalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
