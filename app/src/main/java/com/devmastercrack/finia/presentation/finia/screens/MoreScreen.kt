package com.devmastercrack.finia.presentation.finia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel

private data class MoreMenuItem(
    val emoji: String,
    val iconBg: Color,
    val label: String,
    val sub: String,
    val onClick: () -> Unit,
)

@Composable
fun MoreScreen(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val items = listOf(
        MoreMenuItem("🤝", FiniaColors.PastelPink, "Deudas", "Gastos compartidos con amigos", vm::goDebts),
        MoreMenuItem("🔁", FiniaColors.PastelGreen, "Gestionar gastos fijos", "Renta, servicios, suscripciones", {}),
        MoreMenuItem("📤", FiniaColors.PastelBlue, "Exportar informes", "Descarga tus movimientos", {}),
        MoreMenuItem("🔔", FiniaColors.PastelGold, "Gestionar notificaciones", "Configurar alertas y recordatorios", vm::goNotifSettings),
    )

    Column(modifier.fillMaxSize()) {
        Text(
            "Más opciones", style = FiniaText.ScreenTitle, color = FiniaColors.TextPrimary,
            modifier = Modifier.padding(top = 18.dp, start = 20.dp, end = 20.dp, bottom = 4.dp),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 20.dp, end = 20.dp, bottom = 100.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, FiniaColors.BorderSubtle, RoundedCornerShape(20.dp)),
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(FiniaColors.BorderSubtle2))
                }
                MoreRow(item)
            }
        }
    }
}

@Composable
private fun MoreRow(item: MoreMenuItem) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(CircleShape).background(item.iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(item.emoji, fontSize = 16.sp)
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(item.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FiniaColors.TextPrimary)
            Text(item.sub, style = FiniaText.Secondary, color = FiniaColors.TextSecondary, modifier = Modifier.padding(top = 1.dp))
        }
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null,
            tint = Color(0xFFC3C1B9), modifier = Modifier.size(20.dp),
        )
    }
}
