package com.example.sigaapp.data.repository

import com.example.sigaapp.data.network.ApiService
import com.example.sigaapp.data.local.SessionManager

class ChatRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun sendMessage(message: String): Result<String> {
        val token = sessionManager.getAccessToken() ?: return Result.failure(Exception("No hay sesión activa"))
        
        return try {
            val result = apiService.chat(message, token)
            
            result.map { chatResponse ->
                chatResponse.response ?: chatResponse.message ?: "Respuesta sin contenido"
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
