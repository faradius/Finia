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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.devmastercrack.finia.presentation.finia.components.FlowFilterCard
import com.devmastercrack.finia.presentation.finia.components.RowIconCircle
import com.devmastercrack.finia.presentation.finia.components.TransactionRow
import com.devmastercrack.finia.presentation.finia.iOwe
import com.devmastercrack.finia.presentation.finia.model.DebtDirection
import com.devmastercrack.finia.presentation.finia.model.HomeAlertFilter
import com.devmastercrack.finia.presentation.finia.model.TxFlow
import com.devmastercrack.finia.presentation.finia.netWorth
import com.devmastercrack.finia.presentation.finia.owedCount
import com.devmastercrack.finia.presentation.finia.owedToMe
import com.devmastercrack.finia.presentation.finia.totalGastos
import com.devmastercrack.finia.presentation.finia.totalIngresos
import com.devmastercrack.finia.presentation.finia.util.MESES_FULL
import com.devmastercrack.finia.presentation.finia.util.fmt
import java.time.YearMonth

@Composable
fun HomeScreen(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val netWorth = remember(state.accounts, state.debts, state.settledDebtIds) {
        netWorth(state.accounts, state.debts, state.settledDebtIds)
    }
    // Mirrors AccountsScreen's acctMonthOffset convention: the mock data isn't actually dated
    // per-month, so past/future months just show zero instead of pretending to have real data.
    val inCurrentMonth = state.homeScreenMonthOffset == 0
    val monthLabel = remember(state.homeScreenMonthOffset) {
        MESES_FULL[YearMonth.of(2026, 7).plusMonths(state.homeScreenMonthOffset.toLong()).monthValue - 1]
    }
    val ingresos = remember(state.recentTx, inCurrentMonth) { if (inCurrentMonth) totalIngresos(state.recentTx) else 0.0 }
    val gastos = remember(state.recentTx, inCurrentMonth) { if (inCurrentMonth) totalGastos(state.recentTx) else 0.0 }
    // Tapping Ingresos/Gastos filters "Movimientos recientes" by flow, same as AccountsScreen's
    // Gastado/Ingresos cards filter that screen's own transaction list.
    val filteredRecentTx = remember(state.recentTx, state.homeScreenFlowFilter) {
        when (state.homeScreenFlowFilter) {
            null -> state.recentTx
            TxFlow.GASTO -> state.recentTx.filter { it.monto < 0 }
            TxFlow.INGRESO -> state.recentTx.filter { it.monto > 0 }
            TxFlow.TRANSFERENCIA -> state.recentTx.filter { it.categoria == "Transferencia" }
        }
    }
    val owed = remember(state.debts, state.settledDebtIds) { owedToMe(state.debts, state.settledDebtIds) }
    val owedN = remember(state.debts, state.settledDebtIds) { owedCount(state.debts, state.settledDebtIds) }
    val owedDebts = remember(state.debts, state.settledDebtIds) {
        state.debts.filter { it.direction == DebtDirection.OWED && it.id !in state.settledDebtIds }
    }
    val recurringTotal = remember(state.recurring) { state.recurring.sumOf { it.monto } }
    val unreadCount = state.notifItems.count { it.unread }
    val accountsById = remember(state.accounts) { state.accounts.associateBy { it.id } }

    // Header/saldo/stat-cards/alerts stay pinned; only "Próximos pagos fijos" +
    // "Movimientos recientes" (and their rows) scroll, in their own nested LazyColumn — same
    // fixed-header pattern as AccountsScreen. Each section keeps a stickyHeader title so whoever
    // is mid-scroll through the rows always sees which section they're in; scrolling out of one
    // section's rows and into the next swaps which title is pinned, instead of both section
    // titles just being regular items that can scroll away and leave the rows unlabeled.
    Column(modifier = modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().padding(top = 18.dp, start = 20.dp, end = 20.dp)) {
            Box(modifier = Modifier.align(Alignment.CenterStart)) {
                CircleIconButton(onClick = vm::goProfile) {
                    Icon(Icons.Outlined.Person, contentDescription = "Perfil", modifier = Modifier.size(17.dp))
                }
                // Only shown for premium — absence of the badge already communicates "free",
                // no separate free-tier indicator needed.
                if (state.isPremiumUser) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(FiniaColors.PastelGold)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("👑", fontSize = 8.sp)
                    }
                }
            }
            Row(
                Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = vm::homePrevMonth, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior", tint = Color(0xFF5F6359), modifier = Modifier.size(18.dp))
                }
                // Tapping the month name itself opens the month/year picker — the arrows on
                // either side stay a quick ±1 month step. Kept the default ripple (unlike the
                // other custom-indicator toggles in the app) since there's no competing
                // selection animation here — without it, tapping showed no feedback at all.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = vm::toggleHomeMonthPicker)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                ) {
                    Text(monthLabel, style = FiniaText.RowTitleBold, color = FiniaColors.TextPrimary)
                }
                val nextEnabled = state.homeScreenMonthOffset < 0
                IconButton(onClick = vm::homeNextMonth, enabled = nextEnabled, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente",
                        tint = if (nextEnabled) Color(0xFF5F6359) else Color(0xFFC9C6BC), modifier = Modifier.size(18.dp),
                    )
                }
            }
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                CircleIconButton(onClick = vm::goNotifications) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones", modifier = Modifier.size(17.dp))
                }
                if (unreadCount > 0) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .heightIn(min = 16.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(FiniaColors.Danger)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (unreadCount > 99) "99+" else "$unreadCount", style = FiniaText.LabelTiny, color = Color.White)
                    }
                }
            }
        }

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
                IconButton(onClick = vm::toggleBalanceVisible, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.Visibility, contentDescription = "Mostrar/ocultar saldo", tint = FiniaColors.TextMuted, modifier = Modifier.size(15.dp))
                }
            }
            Text(
                if (state.showBalance) fmt(netWorth) else "••••••",
                style = FiniaText.AmountXL, color = FiniaColors.TextPrimary,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Row(Modifier.fillMaxWidth().padding(top = 18.dp, start = 20.dp, end = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FlowFilterCard(
                Modifier.weight(1f), "Ingresos", ingresos, Icons.Filled.ArrowUpward, FiniaColors.Accent,
                active = state.homeScreenFlowFilter == TxFlow.INGRESO,
                activeBg = FiniaColors.AccentSoft, activeBorder = FiniaColors.Accent,
                onClick = { vm.toggleHomeScreenFlowFilter(TxFlow.INGRESO) },
            )
            FlowFilterCard(
                Modifier.weight(1f), "Gastos", gastos, Icons.Filled.ArrowDownward, FiniaColors.Danger,
                active = state.homeScreenFlowFilter == TxFlow.GASTO,
                activeBg = FiniaColors.DangerSoft, activeBorder = FiniaColors.Danger,
                onClick = { vm.toggleHomeScreenFlowFilter(TxFlow.GASTO) },
            )
        }

        Text(
            "Pendientes y alertas", style = FiniaText.SectionTitle, color = FiniaColors.TextPrimary,
            modifier = Modifier.padding(top = 22.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AlertCard(
                Modifier.weight(1f), "🏠", if (state.recurring.isNotEmpty()) state.recurring.size else null, FiniaColors.Accent, "Pagos próximos", fmt(recurringTotal), FiniaColors.Accent,
                active = state.homeAlertFilter == HomeAlertFilter.PAGOS,
                onClick = { vm.setHomeAlertFilter(HomeAlertFilter.PAGOS) },
            )
            AlertCard(
                Modifier.weight(1f), "🤝", if (owedN > 0) owedN else null, FiniaColors.Danger, "Me deben", fmt(owed), FiniaColors.Danger,
                active = state.homeAlertFilter == HomeAlertFilter.DEUDAS,
                onClick = { vm.setHomeAlertFilter(HomeAlertFilter.DEUDAS) },
            )
        }

        // Only this part scrolls — its own LazyColumn, independent from the fixed section above.
        // "Próximos pagos fijos" is a plain item — it scrolls away normally along with its own
        // rows, no pinning. Only "Movimientos recientes" is a stickyHeader: it scrolls in
        // normally with the rest of the content, then once it naturally reaches the top it
        // pins there while its own rows keep scrolling underneath it.
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.homeAlertFilter == HomeAlertFilter.PAGOS) {
                item {
                    Text(
                        "Próximos pagos fijos", style = FiniaText.SectionTitle, color = FiniaColors.TextPrimary,
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
                    )
                }
                if (state.recurring.isEmpty()) {
                    item {
                        Text(
                            "Sin pagos próximos", style = FiniaText.Secondary.copy(fontSize = 13.sp), color = FiniaColors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        )
                    }
                }
                items(state.recurring, key = { it.nombre }) { r ->
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
            } else {
                item {
                    Text(
                        "Me deben", style = FiniaText.SectionTitle, color = FiniaColors.TextPrimary,
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
                    )
                }
                if (owedDebts.isEmpty()) {
                    item {
                        Text(
                            "Nadie te debe dinero por ahora", style = FiniaText.Secondary.copy(fontSize = 13.sp), color = FiniaColors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        )
                    }
                }
                items(owedDebts, key = { "debt-${it.id}" }) { debt ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RowIconCircle(debt.nombre.take(2).uppercase(), FiniaColors.PastelNeutral)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(debt.nombre, style = FiniaText.RowTitle, color = FiniaColors.TextPrimary)
                            Text(debt.detalle, style = FiniaText.Secondary, color = FiniaColors.TextSecondary)
                        }
                        Text(fmt(debt.monto), style = FiniaText.RowTitleBold, color = FiniaColors.Accent)
                    }
                }
            }

            stickyHeader {
                Text(
                    "Movimientos recientes", style = FiniaText.SectionTitle, color = FiniaColors.TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FiniaColors.ScreenBg)
                        .padding(top = 20.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
                )
            }
            items(filteredRecentTx, key = { it.id }) { tx ->
                val cuentaNombre = accountsById[tx.cuentaId]?.nombre ?: tx.cuentaId
                // Swiping left reveals a delete affordance; releasing past the threshold opens
                // the confirmation dialog instead of deleting outright, then always springs the
                // row back to Settled — actual removal only happens once the dialog is
                // confirmed (see DeleteTransactionDialog / vm.confirmDeleteTx).
                val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                            vm.requestDeleteTx(tx.id)
                        }
                        false
                    },
                )
                androidx.compose.material3.SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(FiniaColors.Danger)
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = Color.White)
                        }
                    },
                ) {
                    // background(ScreenBg) stays full-bleed so it fully occludes the red swipe
                    // panel behind it, but padding is applied *before* clip/clickable so the
                    // ripple itself is bounded to a rounded rect matching the row's own visible
                    // inset — not a square ripple running edge-to-edge past the content.
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(FiniaColors.ScreenBg)
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = { vm.openTxDetail(tx.id) }),
                    ) {
                        TransactionRow(tx, "$cuentaNombre · ${tx.fecha}")
                    }
                }
            }
            item { Box(Modifier.size(1.dp, 100.dp)) }
        }
    }
}

@Composable
private fun AlertCard(
    modifier: Modifier, emoji: String, badgeCount: Int?, badgeColor: Color, label: String, amount: String, amountColor: Color,
    active: Boolean = false, onClick: (() -> Unit)? = null,
) {
    val border by androidx.compose.animation.animateColorAsState(if (active) amountColor else FiniaColors.BorderSubtle, label = "alertCardBorder")
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.5.dp, border, RoundedCornerShape(16.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = onClick,
                    )
                } else Modifier,
            )
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
