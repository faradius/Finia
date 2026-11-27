package com.devmastercrack.finia.presentation.finia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.devmastercrack.finia.presentation.finia.components.BackButton
import com.devmastercrack.finia.presentation.finia.components.FiniaSwitch

private data class NotifPrefRow(val label: String, val sub: String, val checked: Boolean, val key: String)

@Composable
fun NotifSettingsScreen(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val rows = listOf(
        NotifPrefRow("Recordatorios de pago", "3 días antes de vencer", state.notifPrefs.pago, "pago"),
        NotifPrefRow("Cobrar a amigos", "Avisar si llevan +7 días pendientes", state.notifPrefs.cobrar, "cobrar"),
        NotifPrefRow("Límite de tarjeta", "Al llegar a 80%, 90% y 100%", state.notifPrefs.limite, "limite"),
        NotifPrefRow("Resumen semanal", "Domingos por la noche", state.notifPrefs.resumen, "resumen"),
    )

    Column(modifier.fillMaxSize()) {
        // M3 small top app bar: nav icon and title share the same row instead of stacking.
        Row(
            Modifier.fillMaxWidth().padding(top = 14.dp, start = 12.dp, end = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(vm::goBackFromNotifSettings)
            Text(
                "Gestionar notificaciones", style = FiniaText.ScreenTitle, color = FiniaColors.TextPrimary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 100.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, FiniaColors.BorderSubtle, RoundedCornerShape(20.dp)),
        ) {
            rows.forEachIndexed { index, row ->
                if (index > 0) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(FiniaColors.BorderSubtle2))
                }
                NotifPrefToggleRow(row, onToggle = { vm.toggleNotifPref(row.key) })
            }
        }
    }
}

@Composable
private fun NotifPrefToggleRow(row: NotifPrefRow, onToggle: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(row.label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = FiniaColors.TextPrimary)
            Text(row.sub, style = FiniaText.Secondary, color = FiniaColors.TextSecondary, modifier = Modifier.padding(top = 2.dp))
        }
        FiniaSwitch(checked = row.checked, onCheckedChange = onToggle)
    }
}
