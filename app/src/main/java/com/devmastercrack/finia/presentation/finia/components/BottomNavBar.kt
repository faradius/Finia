package com.devmastercrack.finia.presentation.finia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.model.FiniaScreen

@Composable
fun BottomNavBar(
    screen: FiniaScreen,
    onHome: () -> Unit,
    onAccounts: () -> Unit,
    onAdd: () -> Unit,
    onAI: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(androidx.compose.foundation.BorderStroke(1.dp, FiniaColors.BorderSubtle2))
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 22.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        NavItem(Icons.Outlined.Home, "Inicio", screen == FiniaScreen.HOME, onHome)
        NavItem(Icons.Outlined.CreditCard, "Cuentas", screen == FiniaScreen.ACCOUNTS, onAccounts)
        AddNavButton(onAdd)
        NavItem(Icons.Filled.AutoAwesome, "Asistente", screen == FiniaScreen.AI, onAI)
        NavItem(Icons.Filled.MoreHoriz, "Más", screen == FiniaScreen.SETTINGS, onMore)
    }
}

@Composable
private fun NavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    val color = if (active) FiniaColors.Accent else FiniaColors.TextSecondary
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(21.dp))
        Text(label, style = FiniaText.NavLabel, color = color)
    }
}

@Composable
private fun AddNavButton(onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .offset(y = (-26).dp)
            .size(52.dp)
            .clip(CircleShape)
            .background(FiniaColors.Accent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Add, contentDescription = "Agregar gasto", tint = Color.White, modifier = Modifier.size(24.dp))
    }
}
