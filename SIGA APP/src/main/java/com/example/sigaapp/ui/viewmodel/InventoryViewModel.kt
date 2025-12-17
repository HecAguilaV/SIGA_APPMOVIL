package com.example.sigaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sigaapp.data.model.StockItem
import com.example.sigaapp.data.repository.SaaSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

import com.example.sigaapp.data.model.Local
import androidx.annotation.VisibleForTesting

class InventoryViewModel(
    private val repository: SaaSRepository,
    private val autoLoad: Boolean = true
) : ViewModel() {
    private val _stockItems = MutableStateFlow<List<StockItem>>(emptyList())
    // Expose filtered list or handle filtering in UI? Better here.
    // But we need the raw list to filter locally.
    private val _rawStockItems = MutableStateFlow<List<StockItem>>(emptyList())
    
    private val _locales = MutableStateFlow<List<Local>>(emptyList())
    val locales: StateFlow<List<Local>> = _locales
    
    private val _selectedLocal = MutableStateFlow<Local?>(null)
    val selectedLocal: StateFlow<Local?> = _selectedLocal
    
    // The exposed stockItems will be the filtered one
    val stockItems: StateFlow<List<StockItem>> = _selectedLocal
        .combine(_rawStockItems) { local, items ->
            if (local == null) items else items.filter { it.local_id == local.id || it.id < 0 }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating

    private val _categories = MutableStateFlow<List<com.example.sigaapp.data.model.Category>>(emptyList())
    val categories: StateFlow<List<com.example.sigaapp.data.model.Category>> = _categories

    init {
        if (autoLoad) {
            loadData()
        }
    }
    
    
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Load Locales
            repository.getLocales().onSuccess { 
                _locales.value = it 
            }
            // Load Categories
            repository.getCategories().onSuccess {
                _categories.value = it
            }
            
            // Load Products and Stock, then JOIN
            val productsResult = repository.getProducts()
            val stockResult = repository.getStock()
            
            // DEBUG: Log products response
            productsResult.onSuccess { products ->
                android.util.Log.e("INVENTORY_DEBUG", "=== PRODUCTOS RECIBIDOS ===")
                products.take(3).forEach { p ->
                    android.util.Log.e("INVENTORY_DEBUG", "ID: ${p.id}, Nombre: ${p.nombre}, PrecioUnitario: '${p.precioUnitario}', Activo: ${p.activo}")
                }
            }
            
            if (productsResult.isSuccess && stockResult.isSuccess) {
                val products = productsResult.getOrNull() ?: emptyList()
                val stock = stockResult.getOrNull() ?: emptyList()
                
                android.util.Log.e("INVENTORY_DEBUG", "Total productos: ${products.size}, Total stock: ${stock.size}")
                
                // Create product map for fast lookup
                val productsMap = products.associateBy { it.id }
                
                // Enrich stock with product data
                val enrichedStock = stock.mapNotNull { stockItem ->
                    val producto = productsMap[stockItem.producto_id]
                    if (producto == null) {
                        // Orphaned stock item (product was deleted but stock remains)
                        // Ignore it to prevent "Product s/n"
                        null
                    } else {
                        stockItem.copy(producto = producto)
                    }
                }.toMutableList()

                // Ensure products without stock entries are still listed in the app
                val fallbackLocalId = _selectedLocal.value?.id
                    ?: repository.getDefaultLocalId()
                    ?: _locales.value.firstOrNull()?.id
                    ?: -1
                // Add phantom stock entries so products sin stock también se muestren en la app
                val stockProductIds = stock.map { it.producto_id }.toSet()
                products.filter { it.id !in stockProductIds }.forEach { product ->
                    enrichedStock += StockItem(
                        id = -product.id, // Placeholder ID to avoid clashes con registros reales
                        producto_id = product.id,
                        local_id = fallbackLocalId,
                        cantidad = 0,
                        min_stock = 0,
                        producto = product
                    )
                }
                
                _rawStockItems.value = enrichedStock
            } else {
                _error.value = stockResult.exceptionOrNull()?.message 
                    ?: productsResult.exceptionOrNull()?.message 
                    ?: "Error al cargar inventario"
            }
            
            _isLoading.value = false
        }
    }

    fun loadInventory() {
        // Alias for refresh
        loadData()
    }
    
    fun selectLocal(local: Local?) {
        _selectedLocal.value = local
    }

    fun addProduct(nombre: String, precio: Int, descripcion: String?) {
        viewModelScope.launch {
            _isCreating.value = true
            repository.createProduct(nombre, precio, descripcion).fold(
                onSuccess = {
                    loadInventory() // Recargar para ver el nuevo producto (aunque sea stock 0)
                },
                onFailure = { e ->
                    _error.value = "Error al crear producto: ${e.message}"
                }
            )
            _isCreating.value = false
        }
    }

    fun updateProduct(id: Int, nombre: String, precio: Int, descripcion: String?) {
        viewModelScope.launch {
            _isCreating.value = true
            repository.updateProduct(id, nombre, precio, descripcion).fold(
                onSuccess = {
                    loadInventory()
                },
                onFailure = { e ->
                    _error.value = "Error al actualizar: ${e.message}"
                }
            )
            _isCreating.value = false
        }
    }

    fun createCategory(nombre: String, descripcion: String?) {
        viewModelScope.launch {
            _isCreating.value = true
            repository.createCategory(nombre, descripcion).fold(
                onSuccess = {
                    repository.getCategories().onSuccess { _categories.value = it }
                },
                onFailure = { e ->
                    _error.value = "Error al crear categoría: ${e.message}"
                }
            )
            _isCreating.value = false
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteCategory(id).fold(
                onSuccess = {
                    repository.getCategories().onSuccess { _categories.value = it }
                },
                onFailure = { e ->
                    _error.value = "Error al eliminar categoría: ${e.message}"
                }
            )
            _isLoading.value = false
        }
    }

    fun updateStock(productoId: Int, localId: Int, cantidad: Int, cantidadMinima: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateStock(productoId, localId, cantidad, cantidadMinima).fold(
                onSuccess = {
                    loadInventory()
                },
                onFailure = { e ->
                    _error.value = "Error al actualizar stock: ${e.message}"
                }
            )
            _isLoading.value = false
        }
    }

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteProduct(id).fold(
                onSuccess = {
                    // Filtrar el producto eliminado de la lista local
                    _rawStockItems.value = _rawStockItems.value.filterNot { it.producto_id == id }
                    _successMessage.value = "Producto eliminado correctamente"
                },
                onFailure = { e ->
                    _error.value = "Error al eliminar: ${e.message}"
                }
            )
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    @VisibleForTesting
    internal fun setRawStockForTest(items: List<StockItem>, selectedLocal: Local? = null) {
        _rawStockItems.value = items
        _selectedLocal.value = selectedLocal
    }
}
