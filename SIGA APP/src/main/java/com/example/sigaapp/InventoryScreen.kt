package com.example.sigaapp

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sigaapp.ui.theme.*
import com.example.sigaapp.data.model.StockItem
import com.example.sigaapp.data.model.Product
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.sigaapp.ui.viewmodel.InventoryViewModel
import com.example.sigaapp.ui.viewmodel.GlobalViewModel

// Product data class removed (using StockItem from models)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    viewModel: InventoryViewModel,
    globalViewModel: GlobalViewModel // Injected
) {
    val stockItems by viewModel.stockItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    
    // Global State Sync
    val globalSelectedLocal by globalViewModel.selectedLocal.collectAsState()
    val locales by globalViewModel.locales.collectAsState()

    // Sync Global Local Selection to InventoryViewModel
    LaunchedEffect(globalSelectedLocal) {
        viewModel.selectLocal(globalSelectedLocal)
    }

    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val sessionManager = remember { com.example.sigaapp.data.local.SessionManager(context) }
    
    // Permission Logic
    val userRole = remember { sessionManager.getUserRole() }
    val permissions = remember { sessionManager.getPermissions() }
    val canCreateProduct = remember(userRole, permissions) {
        userRole == "ADMINISTRADOR" || userRole == "OPERADOR" || permissions.contains("PRODUCTOS_CREATE")
    }
    val canDeleteProduct = remember(userRole, permissions) {
        userRole == "ADMINISTRADOR" || permissions.contains("PRODUCTOS_ELIMINAR")
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    // Dialog State
    var newProductName by remember { mutableStateOf("") }
    var newProductPrice by remember { mutableStateOf("") }
    var newProductDesc by remember { mutableStateOf("") }
    var newProductStock by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryDesc by remember { mutableStateOf("") }

    // Categories State
    val categories by viewModel.categories.collectAsState()

    val lowStockProducts = stockItems.filter { it.cantidad <= it.min_stock }

    var editingStockItem by remember { mutableStateOf<StockItem?>(null) }
    
    LaunchedEffect(showAddDialog) {
        if (!showAddDialog) {
            editingStockItem = null
            newProductName = ""
            newProductPrice = ""
            newProductDesc = ""
            newProductStock = ""
            nameError = null
            priceError = null
            stockError = null
        }
    }
    
    // Feedback Toast (Super 8 issue)
    LaunchedEffect(isCreating) {
        if (!isCreating && !showAddDialog && error == null) {
            // If just finished creating/editing and no error
            // Check if we actually did something? 
            // Better to rely on a specific event or check if dialog just closed
            // Simple generic toast for now if we know an action was pending?
            // Need a "SuccessMessage" in ViewModel to be sure.
        }
    }

    // Category Creation Dialog
    if (showCreateCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCreateCategoryDialog = false },
            title = { Text("Nueva Categoría") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Nombre") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newCategoryDesc,
                        onValueChange = { newCategoryDesc = it },
                        label = { Text("Descripción (Opcional)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.createCategory(newCategoryName, newCategoryDesc)
                            showCreateCategoryDialog = false
                            newCategoryName = ""
                            newCategoryDesc = ""
                            android.widget.Toast.makeText(context, "Categoría creada", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCategoryDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // List Categories Dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Categorías") },
            text = {
                Column {
                    Button(
                        onClick = { showCreateCategoryDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Text("Nueva Categoría")
                    }
                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cat.nombre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (canCreateProduct) { // Same permission for delete
                                        IconButton(
                                            onClick = { viewModel.deleteCategory(cat.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = AlertRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) { Text("Cerrar") }
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false 
                editingStockItem = null
            },
            title = { Text(if (editingStockItem == null) "Nuevo Producto" else "Editar Producto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newProductName,
                        onValueChange = {
                            newProductName = it
                            nameError = null
                        },
                        label = { Text("Nombre") },
                        singleLine = true,
                        isError = nameError != null,
                        supportingText = {
                            nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    )
                    OutlinedTextField(
                        value = newProductPrice,
                        onValueChange = {
                            newProductPrice = it
                            priceError = null
                        },
                        label = { Text("Precio") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        isError = priceError != null,
                        supportingText = {
                            priceError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    )
                    OutlinedTextField(
                        value = newProductDesc,
                        onValueChange = { newProductDesc = it },
                        label = { Text("Descripción (Opcional)") }
                    )
                    if (editingStockItem != null) {
                        OutlinedTextField(
                            value = newProductStock,
                            onValueChange = {
                                newProductStock = it
                                stockError = null
                            },
                            label = { Text("Stock (Cantidad)") },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = AccentTurquoise.copy(alpha=0.1f),
                                unfocusedContainerColor = AccentTurquoise.copy(alpha=0.1f)
                            ),
                            isError = stockError != null,
                            supportingText = {
                                stockError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceValue = newProductPrice.toIntOrNull()
                        val stockValue = newProductStock.toIntOrNull()

                        var hasError = false
                        if (newProductName.isBlank()) {
                            nameError = "El nombre es obligatorio"
                            hasError = true
                        } else nameError = null

                        if (priceValue == null || priceValue <= 0) {
                            priceError = "Ingrese un precio válido"
                            hasError = true
                        } else priceError = null

                        if (editingStockItem != null) {
                            if (stockValue == null || stockValue < 0) {
                                stockError = "Ingrese una cantidad válida"
                                hasError = true
                            } else stockError = null
                        }

                        if (hasError) {
                            android.widget.Toast.makeText(context, "Revise los campos", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (editingStockItem == null) {
                            viewModel.addProduct(newProductName.trim(), priceValue!!, newProductDesc.ifBlank { null })
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            android.widget.Toast.makeText(context, "Producto creado. Actualizando...", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            val item = editingStockItem!!
                            viewModel.updateProduct(item.producto_id, newProductName.trim(), priceValue!!, newProductDesc.ifBlank { null })

                            if (stockValue != null && stockValue != item.cantidad) {
                                viewModel.updateStock(item.producto_id, item.local_id, stockValue, item.min_stock)
                            }

                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            android.widget.Toast.makeText(context, "Cambios guardados", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        showAddDialog = false
                    },
                    enabled = !isCreating
                ) {
                    Text(if (editingStockItem == null) "Crear" else "Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false 
                    editingStockItem = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Inventario",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadInventory() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AccentCyan
                )
            )
        },
        floatingActionButton = {
            if (canCreateProduct) {
                FloatingActionButton(
                    onClick = { 
                        editingStockItem = null
                        newProductName = ""
                        newProductPrice = ""
                        newProductDesc = ""
                        newProductStock = ""
                        showAddDialog = true 
                    },
                    containerColor = AccentCyan,
                    contentColor = White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
                }
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(
                targetState = stockItems,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) { animatedStock ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Actions Row (Just Categories now, Local Selector is Global)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Categories Button
                            Button(
                                onClick = { showCategoryDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLight, contentColor = TextPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp) // Full width now? Or maybe a filter chip style
                            ) {
                                Text("Gestionar Categorías", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Alertas de stock bajo
                    if (lowStockProducts.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = AlertRed.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = AlertRed,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Stock Bajo",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = AlertRed
                                        )
                                        Text(
                                            text = "${lowStockProducts.size} producto(s) requieren atención",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Resumen rápido
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = AccentCyan
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${stockItems.size}",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = White
                                    )
                                    Text(
                                        text = "Productos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = AccentTurquoise
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${stockItems.sumOf { it: StockItem -> it.cantidad }}",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = White
                                    )
                                    Text(
                                        text = "Total Stock",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // Lista de productos
                    item {
                        Text(
                            text = "Productos ${if (globalSelectedLocal != null) "- " + globalSelectedLocal!!.nombre else ""}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(animatedStock) { item: StockItem ->
                        val isLowStock = item.cantidad <= item.min_stock
                        val itemNombre = item.producto?.nombre ?: "Producto s/n"
                        val itemPrecio = item.producto?.getPrecioDisplay() ?: "Sin precio"
                        val itemDesc = item.producto?.descripcion ?: ""
                        val localName = locales.find { it.id == item.local_id }?.nombre ?: "Sucursal ${item.local_id}"

                        var showMenu by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceLight
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            onClick = { showMenu = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Indicador de stock
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = if (isLowStock) AlertRed else AccentCyan,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${item.cantidad}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = White
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = itemNombre,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = itemPrecio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "📍 $localName", // Display Name
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "📦 Mín: ${item.min_stock}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }

                                    if (canCreateProduct || canDeleteProduct) {
                                         // Quick Actions
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                            if (canCreateProduct) {
                                                IconButton(
                                                    onClick = {
                                                        editingStockItem = item
                                                        newProductName = itemNombre
                                                        // Fix: Take only integer part for editing to avoid validation error
                                                        newProductPrice = item.producto?.precioUnitario?.substringBefore(".") ?: ""
                                                        newProductDesc = itemDesc
                                                        newProductStock = item.cantidad.toString()
                                                        showAddDialog = true
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, "Editar", tint = AccentCyan)
                                                }
                                            }
                                            if (canDeleteProduct) {
                                                IconButton(
                                                    onClick = {
                                                        android.widget.Toast.makeText(
                                                            context,
                                                            "Eliminando ${itemNombre}...",
                                                            android.widget.Toast.LENGTH_SHORT
                                                        ).show()
                                                        viewModel.deleteProduct(item.producto_id)
                                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, "Eliminar", tint = AlertRed)
                                                }
                                            }
                                        }
                                    }
                                }

                                if (isLowStock) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "Stock bajo",
                                        tint = AlertRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading || isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentCyan
                )
            }
            
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
    }
}
