package com.example.sigaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.sigaapp.ui.theme.SIGAAPPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar dependencias manuales (DI simple)
        val sessionManager = com.example.sigaapp.data.local.SessionManager(applicationContext)
        val apiService = com.example.sigaapp.data.network.ApiService()
        val authRepository = com.example.sigaapp.data.repository.AuthRepository(apiService, sessionManager)
        val authViewModelFactory = com.example.sigaapp.ui.viewmodel.AuthViewModelFactory(authRepository)
        
        val saasRepository = com.example.sigaapp.data.repository.SaaSRepository(apiService, sessionManager)
        val saasViewModelFactory = com.example.sigaapp.ui.viewmodel.SaaSViewModelFactory(saasRepository)
        
        val settingsViewModelFactory = com.example.sigaapp.ui.viewmodel.SettingsViewModelFactory(sessionManager)

        setContent {
            SIGAAPPTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val authViewModel: com.example.sigaapp.ui.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = authViewModelFactory)
                    val settingsViewModel: com.example.sigaapp.ui.viewmodel.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = settingsViewModelFactory)
                    
                    // Global ViewModel (Shared)
                    val globalViewModelFactory = com.example.sigaapp.ui.viewmodel.GlobalViewModelFactory(saasRepository)
                    val globalViewModel: com.example.sigaapp.ui.viewmodel.GlobalViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = globalViewModelFactory)

                    val cardSize by settingsViewModel.cardSize.collectAsState()
                    
                    // Determinar destino inicial
                    val isLoggedIn = sessionManager.isLoggedIn()
                    android.util.Log.e("STARTUP_DEBUG", "App started. isLoggedIn: $isLoggedIn")
                    
                    val startDestination = if (isLoggedIn) {
                        val roleRaw = sessionManager.getUserRole() ?: "OPERADOR"
                        val token = sessionManager.getAccessToken()
                        android.util.Log.e("STARTUP_DEBUG", "Restoring session. Role: $roleRaw, Token present: ${token != null}")
                        
                        val uRole = roleRaw.uppercase()
                        val role = when {
                            uRole.contains("ADMIN") -> "ADMINISTRADOR"
                            uRole.contains("CAJERO") -> "CAJERO"
                            else -> "OPERADOR"
                        }
                        "dashboard/$role"
                    } else {
                        android.util.Log.e("STARTUP_DEBUG", "No session. Going to login.")
                        "login"
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") { 
                            LoginScreen(navController, authViewModel) 
                        }
                        composable(
                            "dashboard/{userRole}",
                            arguments = listOf(navArgument("userRole") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val roleString = backStackEntry.arguments?.getString("userRole") ?: "OPERADOR"
                            val userRole = try {
                                val uRole = roleString.uppercase()
                                when {
                                    uRole.contains("ADMIN") -> UserRole.ADMINISTRADOR
                                    uRole.contains("CAJERO") -> UserRole.CAJERO
                                    else -> UserRole.OPERADOR
                                }
                            } catch (e: Exception) {
                                UserRole.OPERADOR
                            }
                            
                            // Obtener permisos actualizados de la sesión
                            val permissions = sessionManager.getPermissions()
                            android.util.Log.e("STARTUP_DEBUG", "Navigating to Dashboard. Permissions count: ${permissions.size}")
                            val chatRepository = com.example.sigaapp.data.repository.ChatRepository(apiService, sessionManager)
                            
                            DashboardScreen(
                                navController = navController,
                                userRole = userRole,
                                permissions = permissions,
                                chatRepository = chatRepository,
                                cardSize = cardSize, // Pass the collected state
                                globalViewModel = globalViewModel, // Pass Global VM
                                onLogout = {
                                    sessionManager.clearAuthOnly() // Preservar settings y biometrics
                                    authViewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("inventory") { 
                            val viewModel: com.example.sigaapp.ui.viewmodel.InventoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = saasViewModelFactory)
                            // Inject GlobalVM into InventoryVM via property or params?
                            // Or just pass GlobalVM state to InventoryScreen?
                            // Passing GlobalVM to InventoryScreen is easiest for now.
                            InventoryScreen(navController, viewModel, globalViewModel) 
                        }
                        composable("sales") { 
                            val viewModel: com.example.sigaapp.ui.viewmodel.SalesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = saasViewModelFactory)
                            SalesScreen(navController, viewModel) 
                        }
                        composable("settings") { 
                            SettingsScreen(navController, settingsViewModel) 
                        }
                    }
                }
            }
        }
    }
}
