package com.example.sigaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sigaapp.data.model.Sale
import com.example.sigaapp.data.repository.SaaSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SalesViewModel(private val repository: SaaSRepository) : ViewModel() {
    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadSales()
    }

    fun loadSales() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getVentas().fold(
                onSuccess = { items ->
                    _sales.value = items
                },
                onFailure = { e ->
                    _error.value = e.message ?: "Error al cargar ventas"
                }
            )
            _isLoading.value = false
        }
    }
}
