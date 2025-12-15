package com.example.sigaapp

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sigaapp.ui.theme.*
import com.example.sigaapp.ui.viewmodel.CardSize
import com.example.sigaapp.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    val cardSize by viewModel.cardSize.collectAsState()
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showBiometricDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ajustes",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDark
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Personalización",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceLight
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Tamaño de Cards",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Personaliza el tamaño de las cards en el dashboard.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CardSizeOption(
                                label = "Pequeña",
                                selected = cardSize == CardSize.SMALL,
                                color = AccentCyan,
                                onClick = { viewModel.setCardSize(CardSize.SMALL) },
                                modifier = Modifier.weight(1f)
                            )
                            CardSizeOption(
                                label = "Mediana",
                                selected = cardSize == CardSize.MEDIUM,
                                color = AccentTurquoise,
                                onClick = { viewModel.setCardSize(CardSize.MEDIUM) },
                                modifier = Modifier.weight(1f)
                            )
                            CardSizeOption(
                                label = "Grande",
                                selected = cardSize == CardSize.LARGE,
                                color = EmeraldOps,
                                onClick = { viewModel.setCardSize(CardSize.LARGE) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones",
                    subtitle = "Gestiona las notificaciones de la app",
                    onClick = { showNotificationsDialog = true }
                )
            }
// ...
// Notification dialog moved to end of file

            item {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Seguridad",
                    subtitle = "Cambiar contraseña y configuración biometrica",
                    onClick = { showBiometricDialog = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Acerca de",
                    subtitle = "Versión 1.0.0 - SIGA Mobile",
                    onClick = { showAboutDialog = true }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Support,
                    title = "Soporte",
                    subtitle = "hdaguila@gmail.com",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:hdaguila@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Soporte SIGA Mobile")
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
        
        if (showNotificationsDialog) {
            var pushEnabled by remember { mutableStateOf(viewModel.getNotificationSettings().first) }
            var stockEnabled by remember { mutableStateOf(viewModel.getNotificationSettings().second) }

            AlertDialog(
                onDismissRequest = { showNotificationsDialog = false },
                title = { Text("Configurar Notificaciones") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                             modifier = Modifier.fillMaxWidth(),
                             horizontalArrangement = Arrangement.SpaceBetween,
                             verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notificaciones Push")
                            Switch(
                                checked = pushEnabled,
                                onCheckedChange = { pushEnabled = it }
                            )
                        }
                        Row(
                             modifier = Modifier.fillMaxWidth(),
                             horizontalArrangement = Arrangement.SpaceBetween,
                             verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Alertas de Stock Bajo")
                            Switch(
                                checked = stockEnabled,
                                onCheckedChange = { stockEnabled = it }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.saveNotificationSettings(pushEnabled, stockEnabled)
                            showNotificationsDialog = false
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNotificationsDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        if (showBiometricDialog) {
            val isEnabled by viewModel.biometricEnabled.collectAsState()
            
            AlertDialog(
                onDismissRequest = { showBiometricDialog = false },
                title = { Text("Seguridad Biométrica") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Gestiona el inicio de sesión rápido mediante huella o rostro.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Row(
                             modifier = Modifier.fillMaxWidth(),
                             horizontalArrangement = Arrangement.SpaceBetween,
                             verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Habilitar Biometría")
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    if (!checked) {
                                        viewModel.toggleBiometric(false)
                                    } else {
                                        // User trying to enable
                                        // We show a message that re-login is required if not already enabled
                                    }
                                }
                            )
                        }
                        if (!isEnabled) {
                             Text(
                                text = "⚠️ Para habilitar, cierra sesión e ingresa nuevamente marcando 'Guardar credenciales'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AlertRed
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBiometricDialog = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
        
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("Acerca de SIGA") },
                text = {
                    Column {
                        Text("SIGA es un proyecto nacido de la experiencia frustrada de un operario cumpliendo el rol multiorquesta.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Desarrollado por Héctor Águila.")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Un Soñador con Poca RAM 👨🏻‍💻",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}

@Composable
fun CardSizeOption(
    label: String,
    selected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color else color.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, White) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (selected) White else TextPrimary
            )
            if (selected) {
                 Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceLight
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = DisabledGray
            )
        }
    }
}

