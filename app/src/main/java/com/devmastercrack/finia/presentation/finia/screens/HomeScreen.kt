package com.devmastercrack.finia.presentation.finia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.devmastercrack.finia.presentation.finia.components.CircleIconButton
import com.devmastercrack.finia.presentation.finia.components.RowIconCircle
import com.devmastercrack.finia.presentation.finia.components.TransactionRow
import com.devmastercrack.finia.presentation.finia.iOwe
import com.devmastercrack.finia.presentation.finia.netWorth
import com.devmastercrack.finia.presentation.finia.owedCount
import com.devmastercrack.finia.presentation.finia.owedToMe
import com.devmastercrack.finia.presentation.finia.totalGastos
import com.devmastercrack.finia.presentation.finia.totalIngresos
import com.devmastercrack.finia.presentation.finia.util.fmt

@Composable
fun HomeScreen(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val netWorth = remember(state.accounts, state.debts, state.settledDebtIds) {
        netWorth(state.accounts, state.debts, state.settledDebtIds)
    }
    val ingresos = remember(state.recentTx) { totalIngresos(state.recentTx) }
    val gastos = remember(state.recentTx) { totalGastos(state.recentTx) }
    val owed = remember(state.debts, state.settledDebtIds) { owedToMe(state.debts, state.settledDebtIds) }
    val owedN = remember(state.debts, state.settledDebtIds) { owedCount(state.debts, state.settledDebtIds) }
    val recurringTotal = remember(state.recurring) { state.recurring.sumOf { it.monto } }
    val hasUnread = state.notifItems.any { it.unread }
    val accountsById = remember(state.accounts) { state.accounts.associateBy { it.id } }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Box(Modifier.fillMaxWidth().padding(top = 18.dp, start = 20.dp, end = 20.dp)) {
                CircleIconButton(onClick = vm::goProfile, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Outlined.Person, contentDescription = "Perfil", modifier = Modifier.size(17.dp))
                }
                Text("Julio", style = FiniaText.RowTitleBold, color = FiniaColors.TextPrimary, modifier = Modifier.align(Alignment.Center))
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    CircleIconButton(onClick = vm::goNotifications) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones", modifier = Modifier.size(17.dp))
                    }
                    if (hasUnread) {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FiniaColors.Danger),
                        )
                    }
                }
            }
        }

        item {
            Column(
                Modifier.fillMaxWidth().padding(top = 22.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Saldo en las cuentas",
                        style = androidx.compose.ui.text.TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, fontSize = 13.sp),
                        color = FiniaColors.TextMuted,
                    )
                    Icon(
                        Icons.Outlined.Visibility, contentDescription = "Mostrar/ocultar saldo", tint = FiniaColors.TextMuted,
                        modifier = Modifier.size(15.dp).clickable(onClick = vm::toggleBalanceVisible),
                    )
                }
                Text(
                    if (state.showBalance) fmt(netWorth) else "••••••",
                    style = FiniaText.AmountXL, color = FiniaColors.TextPrimary,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth().padding(top = 18.dp, start = 20.dp, end = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(Modifier.weight(1f), Icons.Filled.ArrowUpward, FiniaColors.Accent, "Ingresos", fmt(ingresos))
                StatCard(Modifier.weight(1f), Icons.Filled.ArrowDownward, FiniaColors.Danger, "Gastos", fmt(gastos))
            }
        }

        item {
            Text(
                "Pendientes y alertas", style = FiniaText.SectionTitle, color = FiniaColors.TextPrimary,
                modifier = Modifier.padding(top = 22.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
            )
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AlertCard(Modifier.weight(1f), "🤝", if (owedN > 0) owedN else null, FiniaColors.Danger, "Me deben", fmt(owed), FiniaColors.Danger)
                AlertCard(Modifier.weight(1f), "🏠", if (state.recurring.isNotEmpty()) state.recurring.size else null, FiniaColors.Accent, "Pagos próximos", fmt(recurringTotal), FiniaColors.Accent)
            }
        }

        item {
            Text(
                "Próximos pagos fijos", style = FiniaText.SectionTitle, color = FiniaColors.TextPrimary,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
            )
        }
        items(state.recurring) { r ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RowIconCircle(r.emoji, FiniaColors.PastelNeutral)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(r.nombre, style = FiniaText.RowTitle, color = FiniaColors.TextPrimary)
                    Text("${r.frecuencia} · vence en ${r.dias}d", style = FiniaText.Secondary, color = FiniaColors.TextSecondary)
                }
                Text(fmt(r.monto), style = FiniaText.RowTitleBold, color = FiniaColors.TextPrimary)
            }
        }

        item {
            Text(
                "Movimientos recientes", style = FiniaText.SectionTitle, color = FiniaColors.TextPrimary,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
            )
        }
        items(state.recentTx, key = { it.id }) { tx ->
            val cuentaNombre = accountsById[tx.cuentaId]?.nombre ?: tx.cuentaId
            TransactionRow(tx, "$cuentaNombre · ${tx.fecha}", modifier = Modifier.padding(horizontal = 20.dp))
        }
        item { Box(Modifier.size(1.dp, 100.dp)) }
    }
}

@Composable
private fun StatCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color, label: String, amount: String) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, FiniaColors.BorderSubtle, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(iconTint), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        }
        Column {
            Text(label, style = FiniaText.SecondarySmall, color = FiniaColors.TextSecondary)
            Text(amount, style = FiniaText.RowTitleBold.copy(fontSize = 15.sp), color = FiniaColors.TextPrimary)
        }
    }
}

@Composable
private fun AlertCard(modifier: Modifier, emoji: String, badgeCount: Int?, badgeColor: Color, label: String, amount: String, amountColor: Color) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, FiniaColors.BorderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(emoji, fontSize = 18.sp)
            if (badgeCount != null) {
                Box(
                    Modifier.clip(RoundedCornerShape(9.dp)).background(badgeColor).padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text("+$badgeCount", style = FiniaText.LabelTiny, color = Color.White)
                }
            }
        }
        Text(label, style = FiniaText.Secondary, color = FiniaColors.TextSecondary, modifier = Modifier.padding(top = 14.dp))
        Text(amount, style = FiniaText.RowTitleBold.copy(fontSize = 16.sp), color = amountColor, modifier = Modifier.padding(top = 2.dp))
    }
}
