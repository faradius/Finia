package com.devmastercrack.finia.presentation.finia.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.components.SheetHandle
import com.devmastercrack.finia.presentation.finia.util.fmt

/**
 * Transaction detail, opened by tapping a row. Deliberately limited to the fields
 * [com.devmastercrack.finia.presentation.finia.model.Transaction] actually stores (concepto,
 * monto, categoria, cuentaId, fecha) — the design reference had a lot more fields (factura,
 * recordatorio, etiquetas, observación, tarjeta de crédito, an "ignorar gasto" toggle), but this
 * app has nowhere those are captured or persisted, so showing them here would just be fake data.
 * Fecha/Categoría share a MiniStatBox-style row (matches AccountDetailSheet's own stat boxes)
 * and Cuenta gets its own icon row below — two different shapes instead of three identical
 * icon+label+value rows stacked, which read as repetitive.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val tx = state.recentTx.find { it.id == state.txDetailOpen } ?: return
    val account = state.accounts.find { it.id == tx.cuentaId }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isIngreso = tx.monto > 0
    val amountColor = if (isIngreso) FiniaColors.Accent else FiniaColors.Danger

    ModalBottomSheet(
        onDismissRequest = vm::closeTxDetail,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), contentAlignment = Alignment.Center) { SheetHandle() }

            Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(FiniaColors.PastelNeutral), contentAlignment = Alignment.Center) {
                    Text(tx.emoji, fontSize = 26.sp)
                }
                Text(
                    tx.concepto, style = FiniaText.SheetTitle, color = FiniaColors.TextPrimary,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Text(
                    (if (isIngreso) "+ " else "- ") + fmt(kotlin.math.abs(tx.monto)),
                    style = FiniaText.AmountXL, color = amountColor,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Box(
                    Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isIngreso) FiniaColors.AccentSoft else FiniaColors.DangerSoft)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(if (isIngreso) "Ingreso" else "Gasto", style = FiniaText.LabelSmall, color = amountColor)
                }
            }

            Row(Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailStatBox(Modifier.weight(1f), "Fecha", tx.fecha)
                DetailStatBox(Modifier.weight(1f), "Categoría", tx.categoria)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FiniaColors.SurfaceNeutral)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.size(32.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                    Text(account?.emoji ?: "💳", fontSize = 16.sp)
                }
                Column {
                    Text("Cuenta", style = FiniaText.LabelSmall, color = FiniaColors.TextSecondary)
                    Text(
                        account?.nombre ?: tx.cuentaId, style = FiniaText.RowTitleSemibold.copy(fontSize = 14.sp),
                        color = FiniaColors.TextPrimary,
                    )
                }
            }

            Button(
                onClick = { vm.openEditTx(tx.id) },
                colors = ButtonDefaults.buttonColors(containerColor = FiniaColors.Accent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(48.dp),
            ) {
                Text("Editar movimiento", style = FiniaText.Button.copy(fontSize = 14.sp), color = Color.White)
            }
        }
    }
}

@Composable
private fun DetailStatBox(modifier: Modifier, label: String, value: String) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(FiniaColors.SurfaceNeutral)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(label, style = FiniaText.LabelSmall, color = FiniaColors.TextSecondary)
        Text(value, style = FiniaText.RowTitleSemibold.copy(fontSize = 14.sp), color = FiniaColors.TextPrimary, modifier = Modifier.padding(top = 2.dp))
    }
}
