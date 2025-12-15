package com.example.sigaapp.data.repository

import com.example.sigaapp.data.model.StockItem
import com.example.sigaapp.data.model.Sale
import com.example.sigaapp.data.network.ApiService
import com.example.sigaapp.data.local.SessionManager
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.example.sigaapp.data.model.DollarIndicatorResponse
import io.ktor.client.call.body

class SaaSRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val indicatorsClient: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
) {
    suspend fun getStock(): Result<List<StockItem>> {
        val token = sessionManager.getAccessToken() ?: return Result.failure(Exception("No hay sesión activa"))
        return apiService.getStock(token).map { it.stock }
    }

    suspend fun getVentas(): Result<List<Sale>> {
        val token = sessionManager.getAccessToken() ?: return Result.failure(Exception("No hay sesión activa"))
        return apiService.getVentas(token).map { it.ventas }
    }

    suspend fun getProducts(): Result<List<com.example.sigaapp.data.model.Product>> {
        val token = sessionManager.getAccessToken() ?: return Result.failure(Exception("No hay sesión activa"))
        return apiService.getProducts(token).map { it.productos }
    }

    suspend fun createProduct(nombre: String, precio: Int, descripcion: String?): Result<com.example.sigaapp.data.model.Product> {
        val token = sessionManager.getAccessToken() ?: return Result.failure(Exception("No hay sesión activa"))
        val request = com.example.sigaapp.data.model.ProductRequest(nombre, precio.toString(), descripcion)
        return apiService.createProduct(request, token).map { it.producto }
    }

    suspend fun updateProduct(id: Int, nombre: String, precio: Int, descripcion: String?): Result<com.example.sigaapp.data.model.Product> {
        val token = sessionManager.getAccessToken() ?: return Result.failure(Exception("No hay sesión activa"))
        val request = com.example.sigaapp.data.model.ProductRequest(nombre, precio.toString(), descripcion)
        return apiService.updateProduct(id, request, token).map { it.producto }
    }

    suspend fun deleteProduct(id: Int): Result<Boolean> {
        val token = sessionManager.getAccessToken() ?: return Result.failure(Exception("No hay sesión activa"))
        return apiService.deleteProduct(id, token)
    }

    suspend fun getLocales(): Result<List<com.example.sigaapp.data.model.Local>> {
        val token = sessionManager.getAccessToken() ?: return Result.failure(Exception("No hay sesión activa"))
        return apiService.getLocales(token).map { it.locales }
    }

    suspend fun getCategories(): Result<List<com.example.sigaapp.data.model.Category>> {
        val token = sessionManager.getAccessToken() ?: return Result.failure(Exception("No hay sesión activa"))
        return apiService.getCategories(token).map { it.categorias }
    }

    suspend fun createCategory(nombre: String, descripcion: String?): Result<com.example.sigaapp.data.model.Category> {
        val token = sessionManager.getAccessToken() ?: return Result.failure(Exception("No hay sesión activa"))
        val request = com.example.sigaapp.data.model.CategoryRequest(nombre, descripcion)
        return apiService.createCategory(request, token).map { it.categoria }
    }

    suspend fun deleteCategory(id: Int): Result<Boolean> {
        val token = sessionManager.getAccessToken() ?: return Result.failure(Exception("No hay sesión activa"))
        return apiService.deleteCategory(id, token)
    }

    suspend fun updateStock(productoId: Int, localId: Int, cantidad: Int, cantidadMinima: Int = 0): Result<Boolean> {
        val token = sessionManager.getAccessToken() ?: return Result.failure(Exception("No hay sesión activa"))
        val payload = com.example.sigaapp.data.model.StockUpdateRequest(productoId, localId, cantidad, cantidadMinima)
        return apiService.postStock(payload, token)
    }
    
    fun saveDefaultLocalId(localId: Int) {
        sessionManager.saveDefaultLocalId(localId)
    }

    fun getDefaultLocalId(): Int? = sessionManager.getDefaultLocalId()

    suspend fun fetchDollarIndicator(): DollarIndicatorResponse {
        return indicatorsClient.get("https://mindicador.cl/api/dolar").body()
    }
}
