package com.example.sigaapp.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(private val context: Context) {

    companion object {
        private const val PREF_NAME = "SigaSession"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PERMISSIONS = "user_permissions"
        
        // Configuration
        private const val KEY_CARD_SIZE = "card_size"
        
        // Biometrics
        private const val KEY_BIOMETRIC_USER = "biometric_user"
        private const val KEY_BIOMETRIC_PASS = "biometric_pass"
        private const val KEY_SAVED_EMAIL = "saved_email"
        private const val KEY_SAVED_PASS = "saved_pass"
        
        // Notifications
        private const val KEY_NOTIF_PUSH = "notif_push"
        private const val KEY_NOTIF_STOCK = "notif_stock"

        // Version control for data migration/wipe
        private const val CURRENT_DATA_VERSION = 2
        private const val KEY_DATA_VERSION = "data_version"
        
        // Extended Session Info
        private const val KEY_COMPANY_NAME = "company_name"
        private const val KEY_DEFAULT_LOCAL_ID = "default_local_id"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private fun checkAndWipeOldData() {
        val savedVersion = prefs.getInt(KEY_DATA_VERSION, 0)
        android.util.Log.e("SESSION_DEBUG", "Checking version: Saved=$savedVersion, Current=$CURRENT_DATA_VERSION")
        if (savedVersion < CURRENT_DATA_VERSION) {
            android.util.Log.e("SESSION_WIPE", "WIPING DATA NOW")
            prefs.edit().clear().apply()
            prefs.edit().putInt(KEY_DATA_VERSION, CURRENT_DATA_VERSION).apply()
        }
    }

    fun saveAuthSession(token: String, userId: Int, role: String, nombre: String?, nombreEmpresa: String?, defaultLocalId: Int?) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_ROLE, role)
            putString(KEY_USER_NAME, nombre)
            putString(KEY_COMPANY_NAME, nombreEmpresa)
            if (defaultLocalId != null) {
                putInt(KEY_DEFAULT_LOCAL_ID, defaultLocalId)
            } else {
                remove(KEY_DEFAULT_LOCAL_ID)
            }
        }.apply()
    }
    
    fun saveDefaultLocalId(localId: Int?) {
        if (localId != null) {
            prefs.edit().putInt(KEY_DEFAULT_LOCAL_ID, localId).apply()
        } else {
            prefs.edit().remove(KEY_DEFAULT_LOCAL_ID).apply()
        }
    }

    fun getCompanyName(): String? = prefs.getString(KEY_COMPANY_NAME, null)
    
    fun getDefaultLocalId(): Int? {
        val id = prefs.getInt(KEY_DEFAULT_LOCAL_ID, -1)
        return if (id != -1) id else null
    }

    fun savePermissions(permissions: List<String>) {
        prefs.edit().putStringSet(KEY_PERMISSIONS, permissions.toSet()).apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    
    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, null)

    fun getPermissions(): List<String> {
        return prefs.getStringSet(KEY_PERMISSIONS, emptySet())?.toList() ?: emptyList()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun clearAuthOnly() {
        // Logout que preserva configuraciones y credenciales biométricas
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_ROLE)
            .remove(KEY_USER_NAME)
            .remove(KEY_PERMISSIONS)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        val token = getAccessToken()
        val userId = getUserId()
        val role = getUserRole()
        
        // Validación estricta: token debe existir, userId válido, y role no vacío
        return token != null && userId > 0 && !role.isNullOrBlank()
    }
    
    // Configuración

    fun saveCardSize(size: String) {
        prefs.edit().putString(KEY_CARD_SIZE, size).apply()
    }

    fun getCardSize(): String {
        return prefs.getString(KEY_CARD_SIZE, "MEDIUM") ?: "MEDIUM"
    }

    // Biometría (MVP: Guardar credenciales para login rápido)

    fun saveCredentials(email: String, pass: String) {
        prefs.edit()
            .putString(KEY_SAVED_EMAIL, email)
            .putString(KEY_SAVED_PASS, pass)
            .apply()
    }

    fun getSavedCredentials(): Pair<String, String>? {
        val email = prefs.getString(KEY_SAVED_EMAIL, null)
        val pass = prefs.getString(KEY_SAVED_PASS, null)
        if (email != null && pass != null) {
            return Pair(email, pass)
        }
        return null
    }

    fun clearCredentials() {
        prefs.edit()
            .remove(KEY_SAVED_EMAIL)
            .remove(KEY_SAVED_PASS)
            .apply()
    }

    fun isBiometricEnabled(): Boolean {
        val email = prefs.getString(KEY_SAVED_EMAIL, null)
        val pass = prefs.getString(KEY_SAVED_PASS, null)
        return !email.isNullOrBlank() && !pass.isNullOrBlank()
    }

    // Notificaciones

    fun getNotificationSettings(): Pair<Boolean, Boolean> {
        return Pair(
            prefs.getBoolean(KEY_NOTIF_PUSH, true),
            prefs.getBoolean(KEY_NOTIF_STOCK, true)
        )
    }

    fun saveNotificationSettings(push: Boolean, stock: Boolean) {
        prefs.edit()
            .putBoolean(KEY_NOTIF_PUSH, push)
            .putBoolean(KEY_NOTIF_STOCK, stock)
            .apply()
    }
}
