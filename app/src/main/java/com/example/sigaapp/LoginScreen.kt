package com.example.sigaapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sigaapp.ui.theme.*
import com.example.sigaapp.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { com.example.sigaapp.data.local.SessionManager(context) }
    var showBiometricButton by remember { mutableStateOf(false) }

    // Navegar al éxito
    LaunchedEffect(loginSuccess, userRole) {
        if (loginSuccess && userRole != null) {
            // Guardar credenciales para biometria si ingresó manualmente
            if (username.isNotBlank() && password.isNotBlank()) {
                 sessionManager.saveCredentials(username, password)
            }
            
            // DEBUG ROLL
            android.widget.Toast.makeText(context, "Login Exitoso. Rol: $userRole", android.widget.Toast.LENGTH_LONG).show()

            navController.navigate("dashboard/${userRole}") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
         val credentials = sessionManager.getSavedCredentials()
         if (credentials != null) {
             showBiometricButton = true
         }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Logo placeholder or Title
                Text(
                    text = "SIGA",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryDark,
                        fontSize = androidx.compose.ui.unit.TextUnit(32f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                )

                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentCyan
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        viewModel.clearError() 
                    },
                    label = { Text("Usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DisabledGray,
                        focusedLabelColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        viewModel.clearError() 
                    },
                    label = { Text("Contraseña") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DisabledGray,
                        focusedLabelColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Default.Visibility
                        else
                            Icons.Default.VisibilityOff

                        val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )

                if (error != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.login(username, password)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryDark,
                        contentColor = White
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = White
                        )
                    } else {
                        Text(
                            text = "INGRESAR",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                if (showBiometricButton && !isLoading) {
                     OutlinedButton(
                        onClick = {
                            val fragmentActivity = context as? androidx.fragment.app.FragmentActivity
                            if (fragmentActivity != null) {
                                val executor = androidx.core.content.ContextCompat.getMainExecutor(context)
                                val biometricPrompt = androidx.biometric.BiometricPrompt(fragmentActivity, executor,
                                    object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                                            super.onAuthenticationSucceeded(result)
                                            val credentials = sessionManager.getSavedCredentials()
                                            if (credentials != null) {
                                                viewModel.login(credentials.first, credentials.second)
                                            }
                                        }
                                    })

                                val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                                    .setTitle("Ingreso Biométrico")
                                    .setSubtitle("Usa tu huella o rostro para ingresar")
                                    .setNegativeButtonText("Cancelar")
                                    .build()

                                biometricPrompt.authenticate(promptInfo)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                         colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryDark
                        )
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Ingresar con Biometría")
                    }
                }
            }
        }
    }
}
