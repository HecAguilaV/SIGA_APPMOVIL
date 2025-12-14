package com.example.sigaapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.compose.ui.res.painterResource
import com.example.sigaapp.service.VoiceService
import com.example.sigaapp.ui.theme.*
import com.example.sigaapp.ui.viewmodel.CardSize
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable // Required for clickable modifier
import com.example.sigaapp.data.local.SessionManager
import java.util.*

enum class UserRole { ADMINISTRADOR, OPERADOR, CAJERO }

// Data class para mensajes del chat
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController, 
    userRole: UserRole,
    permissions: List<String> = emptyList(),
    chatRepository: com.example.sigaapp.data.repository.ChatRepository,
    onLogout: () -> Unit,
    cardSize: CardSize = CardSize.MEDIUM,
    globalViewModel: com.example.sigaapp.ui.viewmodel.GlobalViewModel
) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var chatMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var userInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    var persistedRole by remember { mutableStateOf(sessionManager.getUserRole()) }
    var companyName by remember { mutableStateOf(sessionManager.getCompanyName()) }
    LaunchedEffect(Unit) {
        persistedRole = sessionManager.getUserRole()
        globalViewModel.loadLocales()
        companyName = sessionManager.getCompanyName()
    }
    val effectiveUserRole = remember(userRole, persistedRole) {
        fun mapRole(raw: String?): UserRole? {
            val value = raw?.uppercase(Locale.getDefault()) ?: return null
            return when {
                value.contains("ADMIN") -> UserRole.ADMINISTRADOR
                value.contains("CAJERO") -> UserRole.CAJERO
                value.contains("OPERADOR") -> UserRole.OPERADOR
                else -> null
            }
        }
        mapRole(persistedRole) ?: userRole
    }

    // Global State
    val locales by globalViewModel.locales.collectAsState()
    val selectedLocal by globalViewModel.selectedLocal.collectAsState()
    var expandedLocalMenu by remember { mutableStateOf(false) }
    
    // Servicios de voz
    var isVoiceInputEnabled by remember { mutableStateOf(false) }
    var isVoiceOutputEnabled by remember { mutableStateOf(false) }
    val tts = VoiceService.rememberTextToSpeech()
    
    // Launcher para reconocimiento de voz
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.getOrNull(0) ?: ""
            if (spokenText.isNotEmpty()) {
                userInput = spokenText
                // Opcional: enviar automáticamente
                // sendMessage()
            }
        }
    }
    
    fun startVoiceInput() {
        if (!android.speech.SpeechRecognizer.isRecognitionAvailable(context)) {
            errorMessage = "El reconocimiento de voz no está disponible en este dispositivo"
            return
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES") // Español
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...")
        }
        speechRecognizerLauncher.launch(intent)
    }

    // Mensaje inicial cuando se abre el chat
    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet && chatMessages.isEmpty()) {
            chatMessages = listOf(
                ChatMessage(
                    text = "Hola, soy SIGA, tu asistente virtual. ¿En qué puedo ayudarte hoy?",
                    isUser = false
                )
            )
        }
        // Detener voz al cerrar el chat
        if (!showBottomSheet) {
            VoiceService.stopSpeaking(tts)
        }
    }

    // Scroll automático al final cuando hay nuevos mensajes
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    fun sendMessage() {
        if (userInput.isBlank() || isLoading) return

        val userMessage = userInput.trim()
        userInput = ""
        errorMessage = null
        keyboardController?.hide() // Ocultar teclado al enviar

        // Agregar mensaje del usuario
        chatMessages = chatMessages + ChatMessage(text = userMessage, isUser = true)
        isLoading = true

        scope.launch {
            val result = chatRepository.sendMessage(userMessage)
            isLoading = false

            result.fold(
                onSuccess = { response ->
                    chatMessages = chatMessages + ChatMessage(text = response, isUser = false)
                    // Reproducir respuesta por voz si está habilitado
                    if (isVoiceOutputEnabled) {
                        VoiceService.speak(tts, response)
                    }
                },
                onFailure = { exception ->
                    errorMessage = exception.message ?: "Error al comunicarse con el asistente"
                    chatMessages = chatMessages + ChatMessage(
                        text = "Lo siento, hubo un error al procesar tu mensaje. (${exception.message})",
                        isUser = false
                    )
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SIGA",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = White
                        )
                        // Local Selector Dropdown
                        Box {
                             Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .clickable { expandedLocalMenu = true }
                            ) {
                                Text(
                                    text = selectedLocal?.nombre ?: "Todas las Tiendas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AccentTurquoise
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown, 
                                    contentDescription = "Seleccionar Local",
                                    tint = AccentTurquoise,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = expandedLocalMenu,
                                onDismissRequest = { expandedLocalMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todas las Tiendas") },
                                    onClick = {
                                        globalViewModel.selectLocal(null)
                                        expandedLocalMenu = false
                                    }
                                )
                                locales.forEach { local ->
                                    DropdownMenuItem(
                                        text = { Text(local.nombre) },
                                        onClick = {
                                            globalViewModel.selectLocal(local)
                                            expandedLocalMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 16.dp, top = 8.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = companyName ?: "Empresa no definida",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentTurquoise
                        )
                        Surface(
                            color = AccentCyan,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .padding(top = 2.dp)
                        ) {
                            Text(
                                text = effectiveUserRole.name,
                                color = White,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDark,
                    titleContentColor = White,
                    actionIconContentColor = White
                )
            )
        },
        floatingActionButton = {
            if (permissions.contains("ASISTENTE_USAR") || effectiveUserRole == UserRole.ADMINISTRADOR) {
                FloatingActionButton(
                    onClick = { 
                        showBottomSheet = true
                     },
                    containerColor = AccentCyan,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logosiga),
                        contentDescription = "Asistente IA",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Background
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = padding,
            modifier = Modifier
                .background(Background)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tile principal de Inventario
            item(span = { GridItemSpan(2) }) {
                DashboardTile(
                    title = "Inventario",
                    icon = Icons.Default.Inventory,
                    color = AccentCyan,
                    enabled = permissions.contains("PRODUCTOS_VER") || effectiveUserRole == UserRole.ADMINISTRADOR,
                    size = TileSize.LARGE,
                    cardSizePreference = cardSize,
                    onClick = {
                        navController.navigate("inventory")
                    }
                )
            }
            // Tile de Ventas
            item(span = { GridItemSpan(2) }) {
                DashboardTile(
                    title = "Ventas",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = AccentTurquoise,
                    enabled = permissions.contains("VENTAS_VER") || permissions.contains("VENTAS_CREAR") || effectiveUserRole == UserRole.ADMINISTRADOR,
                    size = TileSize.MEDIUM,
                    cardSizePreference = cardSize,
                    onClick = { 
                        navController.navigate("sales")
                    }
                )
            }
            // Tiles secundarios
            item {
                 DashboardTile(
                    title = "Documentos",
                    icon = Icons.Default.Description,
                    color = AccentCyan,
                    enabled = effectiveUserRole == UserRole.ADMINISTRADOR,
                    size = TileSize.SMALL,
                    cardSizePreference = cardSize,
                    onClick = { 
                        showBottomSheet = true
                        userInput = "¿Qué documentos puedo gestionar en SIGA?"
                        sendMessage()
                    }
                )
            }
            item {
                DashboardTile(
                    title = "Gastos",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = AlertRed,
                    enabled = permissions.contains("COSTOS_VER") || effectiveUserRole == UserRole.ADMINISTRADOR,
                    size = TileSize.SMALL,
                    cardSizePreference = cardSize,
                    onClick = { 
                        showBottomSheet = true
                        userInput = "Muéstrame información sobre gastos"
                        sendMessage()
                    }
                )
            }
            item { 
                DashboardTile(
                    title = "Ajustes",
                    icon = Icons.Default.Settings,
                    color = PrimaryDark,
                    enabled = effectiveUserRole == UserRole.ADMINISTRADOR,
                    size = TileSize.SMALL,
                    cardSizePreference = cardSize,
                    onClick = {
                        navController.navigate("settings")
                    }
                )
            }
            item { 
                DashboardTile(
                    title = "Soporte",
                    icon = Icons.Default.Support,
                    color = AccentCyan,
                    enabled = true,
                    size = TileSize.SMALL,
                    cardSizePreference = cardSize,
                    onClick = {
                        showBottomSheet = true
                        userInput = "Necesito ayuda con SIGA"
                        sendMessage()
                    }
                )
            }
            item { 
                DashboardTile(
                    title = "Salir",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    color = AlertRed,
                    enabled = true,
                    size = TileSize.SMALL,
                    cardSizePreference = cardSize,
                    onClick = {
                        onLogout()
                    }
                )
            }
        }

                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState,
                        containerColor = SurfaceLight,
                        dragHandle = {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .width(40.dp)
                                    .height(4.dp)
                                    .background(color = DisabledGray, shape = RoundedCornerShape(2.dp))
                            )
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .heightIn(min = 320.dp, max = 640.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Asistente SIGA",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryDark
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        isVoiceOutputEnabled = !isVoiceOutputEnabled
                                        if (!isVoiceOutputEnabled) VoiceService.stopSpeaking(tts)
                                    }) {
                                        Icon(
                                            if (isVoiceOutputEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                                            contentDescription = if (isVoiceOutputEnabled) "Desactivar voz" else "Activar voz",
                                            tint = if (isVoiceOutputEnabled) AccentCyan else DisabledGray
                                        )
                                    }
                                    IconButton(onClick = { isVoiceInputEnabled = !isVoiceInputEnabled }) {
                                        Icon(
                                            imageVector = if (isVoiceInputEnabled) Icons.Default.Stop else Icons.Default.RecordVoiceOver,
                                            contentDescription = if (isVoiceInputEnabled) "Desactivar micrófono" else "Activar micrófono",
                                            tint = if (isVoiceInputEnabled) AccentCyan else DisabledGray
                                        )
                                    }
                                    IconButton(onClick = { showBottomSheet = false }) {
                                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                                    }
                                }
                            }
                            HorizontalDivider(color = DisabledGray)

                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(chatMessages) { ChatBubble(message = it) }
                                if (isLoading) {
                                    item {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                            Card(colors = CardDefaults.cardColors(containerColor = Background), shape = RoundedCornerShape(16.dp)) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AccentCyan)
                                                    Text("Pensando...", color = TextSecondary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            errorMessage?.let { error ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(error, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isVoiceInputEnabled) {
                                    IconButton(
                                        onClick = { startVoiceInput() },
                                        enabled = !isLoading,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(color = AccentCyan, shape = RoundedCornerShape(50))
                                    ) {
                                        Icon(Icons.Default.Mic, contentDescription = "Hablar", tint = White)
                                    }
                                }
                                OutlinedTextField(
                                    value = userInput,
                                    onValueChange = { userInput = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text(if (isVoiceInputEnabled) "Toca el micrófono o escribe..." else "Escribe tu mensaje...") },
                                    enabled = !isLoading,
                                    maxLines = 3,
                                    shape = RoundedCornerShape(24.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentCyan,
                                        unfocusedBorderColor = DisabledGray
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { sendMessage() })
                                )
                                IconButton(
                                    onClick = { sendMessage() },
                                    enabled = userInput.isNotBlank() && !isLoading,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = if (userInput.isNotBlank() && !isLoading) AccentCyan else DisabledGray,
                                            shape = RoundedCornerShape(50)
                                        )
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

