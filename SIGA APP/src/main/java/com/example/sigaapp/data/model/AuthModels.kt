package com.example.sigaapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: User? = null,
    val message: String? = null
)

@Serializable
data class User(
    val id: Int,
    val email: String,
    val rol: String,
    val nombre: String? = null,
    val apellido: String? = null,
    val nombreEmpresa: String? = null,
    val localPorDefecto: com.example.sigaapp.data.model.Local? = null
)

@Serializable
data class PermissionResponse(
    val success: Boolean,
    val permisos: List<String>
)

@Serializable
data class ChatRequest(
    val message: String
)

@Serializable
data class ChatResponse(
    val success: Boolean = false,
    val succes: Boolean? = null, // Backend typo handling
    val response: String? = null,
    val message: String? = null,
    val action: ChatAction? = null
)

@Serializable
data class ChatAction(
    val executed: Boolean,
    val type: String? = null,
    val data: String? = null,
    val requiresConfirmation: Boolean
)
