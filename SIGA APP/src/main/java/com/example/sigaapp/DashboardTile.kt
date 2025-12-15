package com.example.sigaapp

import com.example.sigaapp.ui.viewmodel.CardSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sigaapp.ui.theme.*

enum class TileSize {
    SMALL,   // 1x1
    MEDIUM,  // 2x1 (ancho doble)
    LARGE    // 2x2 (cuadrado grande)
}

// Import moved to top

@Composable
fun DashboardTile(
    title: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    size: TileSize = TileSize.SMALL,
    cardSizePreference: CardSize = CardSize.MEDIUM
) {
    // Base height determined by user preference
    val baseHeight = when (cardSizePreference) {
        CardSize.SMALL -> 100.dp
        CardSize.MEDIUM -> 120.dp
        CardSize.LARGE -> 150.dp
    }

    val height = when (size) {
        TileSize.SMALL -> baseHeight
        TileSize.MEDIUM -> baseHeight
        TileSize.LARGE -> baseHeight * 2
    }
    
    val iconSize = when (size) {
        TileSize.SMALL -> 32.dp
        TileSize.MEDIUM -> 40.dp
        TileSize.LARGE -> 56.dp
    }
    
    val textStyle = when (size) {
        TileSize.SMALL -> MaterialTheme.typography.titleMedium
        TileSize.MEDIUM -> MaterialTheme.typography.titleLarge
        TileSize.LARGE -> MaterialTheme.typography.headlineSmall
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) color else DisabledGray
        ),
        modifier = Modifier
            .fillMaxWidth(if (size == TileSize.MEDIUM) 1f else 0.5f)
            .height(height)
            .shadow(
                elevation = if (enabled) 8.dp else 2.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (enabled) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (enabled) White else Color(0xFF757575),
                modifier = Modifier.size(iconSize)
            )
            Text(
                text = title,
                color = if (enabled) White else Color(0xFF757575),
                style = textStyle.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
