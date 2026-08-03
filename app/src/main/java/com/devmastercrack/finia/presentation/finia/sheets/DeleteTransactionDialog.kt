package com.devmastercrack.finia.presentation.finia.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.components.TransactionRow

/**
 * Swipe-to-delete's confirmation step — deliberately not a copy of the dark, iOS-style action
 * sheet used as a design reference; built with Finia's own rounded-card/white/Danger-accent
 * language instead, matching the other confirmation-style surfaces in the app.
 */
@Composable
fun DeleteTransactionDialog(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val tx = state.recentTx.find { it.id == state.deleteTxPending } ?: return
    val cuentaNombre = state.accounts.find { it.id == tx.cuentaId }?.nombre ?: tx.cuentaId

    Dialog(onDismissRequest = vm::cancelDeleteTx) {
        Column(
            modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(24.dp),
        ) {
            Text("¿Eliminar movimiento?", style = FiniaText.SheetTitle, color = FiniaColors.TextPrimary)
            // The exact same row composable/subtitle format used in the list itself, so the
            // transaction being confirmed for deletion reads identically here.
            TransactionRow(tx, "$cuentaNombre · ${tx.fecha}", modifier = Modifier.padding(top = 12.dp))
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FiniaColors.SurfaceNeutral)
                        .clickable(onClick = vm::cancelDeleteTx)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Cancelar", style = FiniaText.Button.copy(fontSize = 14.sp), color = FiniaColors.TextPrimary)
                }
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FiniaColors.Danger)
                        .clickable(onClick = vm::confirmDeleteTx)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Eliminar", style = FiniaText.Button.copy(fontSize = 14.sp), color = Color.White)
                }
            }
        }
    }
}
