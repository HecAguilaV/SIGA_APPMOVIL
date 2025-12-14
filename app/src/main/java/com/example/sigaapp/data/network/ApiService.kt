package com.example.sigaapp.data.network

import com.example.sigaapp.BuildConfig
import com.example.sigaapp.data.model.LoginRequest
import com.example.sigaapp.data.model.LoginResponse
import com.example.sigaapp.data.model.PermissionResponse
import com.example.sigaapp.data.model.ChatRequest
import com.example.sigaapp.data.model.ChatResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.example.sigaapp.data.model.ProductRequest
import com.example.sigaapp.data.model.ProductResponse
import com.example.sigaapp.data.model.ProductosListResponse
import com.example.sigaapp.data.model.LocalesResponse
import org.json.JSONObject

class ApiService {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        
        defaultRequest {
            url(BuildConfig.API_BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun login(email: String, pass: String): Result<LoginResponse> {
        return try {
            val response = client.post("/api/auth/login") { // Path relative to defaultRequest base URL
                setBody(LoginRequest(email, pass))
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                val message = parseErrorMessage(errorBody)
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPermisos(userId: Int, token: String): List<String> {
        return try {
            val response = client.get("/api/saas/usuarios/$userId/permisos") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                val data = response.body<PermissionResponse>()
                data.permisos
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun chat(message: String, token: String): Result<ChatResponse> {
        return try {
            val response = client.post("/api/saas/chat") {
                header("Authorization", "Bearer $token")
                setBody(ChatRequest(message))
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorBody)
                // Manejar 402 específicamente si es necesario
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStock(token: String): Result<com.example.sigaapp.data.model.StockListResponse> {
        return try {
            val response = client.get("/api/saas/stock") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVentas(token: String): Result<com.example.sigaapp.data.model.VentasListResponse> {
        return try {
            val response = client.get("/api/saas/ventas") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(jsonString: String): String {
        return try {
            val json = JSONObject(jsonString)
            json.optString("message", "Error desconocido")
        } catch (e: Exception) {
            "Error de conexión"
        }
    }

    suspend fun getProducts(token: String): Result<ProductosListResponse> {
        return try {
            val response = client.get("/api/saas/productos") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProduct(product: ProductRequest, token: String): Result<ProductResponse> {
        return try {
            val response = client.post("/api/saas/productos") {
                header("Authorization", "Bearer $token")
                setBody(product)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(id: Int, product: ProductRequest, token: String): Result<ProductResponse> {
        return try {
            val response = client.put("/api/saas/productos/$id") {
                header("Authorization", "Bearer $token")
                setBody(product)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(id: Int, token: String): Result<Boolean> {
        return try {
            val response = client.delete("/api/saas/productos/$id") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                val errorBody = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLocales(token: String): Result<LocalesResponse> {
        return try {
            val response = client.get("/api/saas/locales") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(token: String): Result<com.example.sigaapp.data.model.CategoriesResponse> {
        return try {
            val response = client.get("/api/saas/categorias") {
                header("Authorization", "Bearer $token")
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCategory(category: com.example.sigaapp.data.model.CategoryRequest, token: String): Result<com.example.sigaapp.data.model.CategoryResponse> {
        return try {
            val response = client.post("/api/saas/categorias") {
                header("Authorization", "Bearer $token")
                setBody(category)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(id: Int, token: String): Result<Boolean> {
        return try {
            val response = client.delete("/api/saas/categorias/$id") {
                header("Authorization", "Bearer $token")
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                val errorBody = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun postStock(request: com.example.sigaapp.data.model.StockUpdateRequest, token: String): Result<Boolean> {
        return try {
            val response = client.post("/api/saas/stock") {
                header("Authorization", "Bearer $token")
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                val errorBody = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
