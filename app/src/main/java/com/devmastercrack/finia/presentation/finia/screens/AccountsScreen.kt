package com.devmastercrack.finia.presentation.finia.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.components.AccountCard
import com.devmastercrack.finia.presentation.finia.components.AddAccountCard
import com.devmastercrack.finia.presentation.finia.components.FiniaChip
import com.devmastercrack.finia.presentation.finia.components.SegmentedControl
import com.devmastercrack.finia.presentation.finia.components.TransactionRow
import com.devmastercrack.finia.presentation.finia.deriveAccountView
import com.devmastercrack.finia.presentation.finia.filterAccountTx
import com.devmastercrack.finia.presentation.finia.filterAccounts
import com.devmastercrack.finia.presentation.finia.groupByFecha
import com.devmastercrack.finia.presentation.finia.model.AccountTypeFilter
import com.devmastercrack.finia.presentation.finia.model.TxFlow
import com.devmastercrack.finia.presentation.finia.util.MESES_FULL
import com.devmastercrack.finia.presentation.finia.util.fmt
import java.time.YearMonth

@Composable
fun AccountsScreen(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val filtered = remember(state.accounts, state.accountTypeFilter) { filterAccounts(state.accounts, state.accountTypeFilter) }
    val activeAccount = filtered.getOrNull(state.activeAccountIdx) ?: filtered.firstOrNull()
    val listState = rememberLazyListState()

    LaunchedEffect(state.activeAccountIdx, filtered) {
        if (filtered.isNotEmpty() && listState.firstVisibleItemIndex != state.activeAccountIdx) {
            listState.animateScrollToItem(state.activeAccountIdx.coerceIn(0, filtered.lastIndex))
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { scrolling ->
            if (!scrolling && filtered.isNotEmpty()) {
                val info = listState.layoutInfo
                if (info.visibleItemsInfo.isNotEmpty()) {
                    val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
                    val closest = info.visibleItemsInfo
                        .filter { it.index < filtered.size }
                        .minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }
                    if (closest != null && closest.index != state.activeAccountIdx) {
                        vm.setActiveAccountIdx(closest.index)
                    }
                }
            }
        }
    }

    val inCurrentMonth = state.acctMonthOffset == 0
    val accountTx = remember(state.recentTx, activeAccount) {
        if (activeAccount == null) emptyList() else state.recentTx.filter { it.cuentaId == activeAccount.id }
    }
    val spentThisMonth = if (inCurrentMonth) accountTx.filter { it.monto < 0 }.sumOf { -it.monto } else 0.0
    val paidThisMonth = if (inCurrentMonth) accountTx.filter { it.monto > 0 }.sumOf { it.monto } else 0.0
    val categories = remember(accountTx) { listOf("Todo") + accountTx.map { it.categoria }.distinct() }
    val filteredTx = remember(state.recentTx, activeAccount, state.homeCategoryFilter, state.homeFlowFilter, inCurrentMonth) {
        if (activeAccount == null) emptyList()
        else filterAccountTx(state.recentTx, activeAccount.id, state.homeCategoryFilter, state.homeFlowFilter, inCurrentMonth)
    }
    val groups = remember(filteredTx) { groupByFecha(filteredTx) }
    val base = remember(state.acctMonthOffset) { YearMonth.of(2026, 7).plusMonths(state.acctMonthOffset.toLong()) }
    val monthLabel = "${MESES_FULL[base.monthValue - 1]} ${base.year}"

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Text(
                "Mis cuentas", style = FiniaText.ScreenTitle, color = FiniaColors.TextPrimary,
                modifier = Modifier.padding(top = 18.dp, start = 20.dp, end = 20.dp),
            )
        }
        item {
            SegmentedControl(
                options = listOf("Todo", "Cuentas", "Tarjetas"),
                selectedIndex = when (state.accountTypeFilter) {
                    AccountTypeFilter.TODO -> 0
                    AccountTypeFilter.CUENTAS -> 1
                    AccountTypeFilter.TARJETAS -> 2
                },
                onSelect = { i ->
                    vm.setAccountTypeFilter(listOf(AccountTypeFilter.TODO, AccountTypeFilter.CUENTAS, AccountTypeFilter.TARJETAS)[i])
                },
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp, start = 20.dp, end = 20.dp),
            )
        }
        item {
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(filtered, key = { _, a -> a.id }) { i, acc ->
                    val view = remember(acc, state.creditPrimaryView) { deriveAccountView(acc, state.creditPrimaryView) }
                    AccountCard(view = view, isActive = i == state.activeAccountIdx, onClick = { vm.openAccountDetail(acc.id) })
                }
                item {
                    AddAccountCard(onClick = vm::openNewAccountSheet)
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp), horizontalArrangement = Arrangement.Center,
            ) {
                filtered.forEachIndexed { i, _ ->
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (i == state.activeAccountIdx) FiniaColors.Accent else Color(0xFFDCDAD3)),
                    )
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior", tint = Color(0xFF5F6359),
                    modifier = Modifier.size(20.dp).clickable(onClick = vm::acctPrevMonth),
                )
                Text(monthLabel, style = FiniaText.RowTitleBold, color = FiniaColors.TextPrimary, modifier = Modifier.padding(horizontal = 14.dp))
                val nextEnabled = state.acctMonthOffset < 0
                Icon(
                    Icons.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente",
                    tint = if (nextEnabled) Color(0xFF5F6359) else Color(0xFFC9C6BC),
                    modifier = Modifier.size(20.dp).clickable(enabled = nextEnabled, onClick = vm::acctNextMonth),
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(top = 10.dp, start = 20.dp, end = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowFilterCard(
                    Modifier.weight(1f), "Gastado", fmt(spentThisMonth), FiniaColors.Danger,
                    active = state.homeFlowFilter == TxFlow.GASTO,
                    activeBg = FiniaColors.DangerSoft, activeBorder = FiniaColors.Danger,
                    onClick = { vm.toggleHomeFlowFilter(TxFlow.GASTO) },
                )
                FlowFilterCard(
                    Modifier.weight(1f), "Ingresos", fmt(paidThisMonth), FiniaColors.Accent,
                    active = state.homeFlowFilter == TxFlow.INGRESO,
                    activeBg = FiniaColors.AccentSoft, activeBorder = FiniaColors.Accent,
                    onClick = { vm.toggleHomeFlowFilter(TxFlow.INGRESO) },
                )
            }
        }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(categories) { cat ->
                    FiniaChip(label = cat, selected = state.homeCategoryFilter == cat, onClick = { vm.setHomeCategory(cat) })
                }
            }
        }
        item { androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp)) }
        if (groups.isEmpty()) {
            item {
                Text(
                    if (inCurrentMonth) "Sin movimientos en esta cuenta" else "Sin movimientos registrados este mes",
                    style = FiniaText.SecondarySmall.copy(fontSize = 13.sp), color = FiniaColors.TextSecondary,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        } else {
            groups.forEach { group ->
                item {
                    Text(
                        group.fecha.uppercase(), style = FiniaText.Overline, color = FiniaColors.TextSecondary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 20.dp, end = 20.dp),
                    )
                }
                items(group.items, key = { it.id }) { tx ->
                    TransactionRow(tx, tx.categoria, modifier = Modifier.padding(horizontal = 20.dp))
                }
            }
        }
        item { Box(Modifier.size(1.dp, 100.dp)) }
    }
}

@Composable
private fun FlowFilterCard(
    modifier: Modifier,
    label: String,
    amount: String,
    amountColor: androidx.compose.ui.graphics.Color,
    active: Boolean,
    activeBg: androidx.compose.ui.graphics.Color,
    activeBorder: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(if (active) activeBg else androidx.compose.ui.graphics.Color.White, label = "flowBg")
    val border by animateColorAsState(if (active) activeBorder else FiniaColors.BorderSubtle, label = "flowBorder")
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(label, style = FiniaText.Secondary, color = FiniaColors.TextSecondary)
        Text(amount, style = FiniaText.RowTitleBold.copy(fontSize = 17.sp), color = amountColor, modifier = Modifier.padding(top = 4.dp))
    }
}
