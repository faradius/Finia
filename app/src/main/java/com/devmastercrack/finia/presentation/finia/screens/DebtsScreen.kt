package com.devmastercrack.finia.presentation.finia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.components.SegmentedControl
import com.devmastercrack.finia.presentation.finia.iOwe
import com.devmastercrack.finia.presentation.finia.model.Debt
import com.devmastercrack.finia.presentation.finia.model.DebtDirection
import com.devmastercrack.finia.presentation.finia.model.DebtTab
import com.devmastercrack.finia.presentation.finia.owedToMe
import com.devmastercrack.finia.presentation.finia.util.fmt

@Composable
fun DebtsScreen(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val owed = remember(state.debts, state.settledDebtIds) { owedToMe(state.debts, state.settledDebtIds) }
    val owe = remember(state.debts, state.settledDebtIds) { iOwe(state.debts, state.settledDebtIds) }
    val direction = if (state.debtTab == DebtTab.OWED) DebtDirection.OWED else DebtDirection.I_OWE
    val list = remember(state.debts, state.settledDebtIds, state.debtTab) {
        state.debts.filter { it.direction == direction && it.id !in state.settledDebtIds }
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Text(
                "Deudas", style = FiniaText.ScreenTitle, color = FiniaColors.TextPrimary,
                modifier = Modifier.padding(top = 18.dp, start = 20.dp, end = 20.dp),
            )
        }
        item {
            SegmentedControl(
                options = listOf("Me deben (${fmt(owed)})", "Debo (${fmt(owe)})"),
                selectedIndex = if (state.debtTab == DebtTab.OWED) 0 else 1,
                onSelect = { vm.setDebtTab(if (it == 0) DebtTab.OWED else DebtTab.IOWE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            )
        }
        if (list.isEmpty()) {
            item {
                Text(
                    "Sin pendientes aquí",
                    style = FiniaText.Secondary.copy(fontSize = 13.sp),
                    color = FiniaColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                )
            }
        } else {
            items(list, key = { it.id }) { debt ->
                DebtRow(
                    debt = debt,
                    onSettle = { vm.settleDebt(debt.id) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                )
            }
        }
        item { Box(Modifier.size(1.dp, 100.dp)) }
    }
}

@Composable
private fun DebtRow(debt: Debt, onSettle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(FiniaColors.White)
            .border(1.dp, FiniaColors.BorderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(FiniaColors.PastelNeutral),
            contentAlignment = Alignment.Center,
        ) {
            Text(debt.nombre.take(2).uppercase(), style = FiniaText.RowTitleBold, color = FiniaColors.TextPrimary)
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(debt.nombre, style = FiniaText.RowTitle, color = FiniaColors.TextPrimary)
            Text(debt.detalle, style = FiniaText.Secondary, color = FiniaColors.TextSecondary)
        }
        val amountColor = if (debt.direction == DebtDirection.OWED) FiniaColors.Accent else FiniaColors.Danger
        Column(horizontalAlignment = Alignment.End) {
            Text(fmt(debt.monto), style = FiniaText.RowTitleBold.copy(fontSize = 15.sp), color = amountColor)
            Text(
                if (debt.direction == DebtDirection.OWED) "Marcar cobrado" else "Marcar pagado",
                style = FiniaText.SecondarySmall.copy(fontWeight = FontWeight.SemiBold),
                color = FiniaColors.Accent,
                modifier = Modifier.padding(top = 4.dp).clickable(onClick = onSettle),
            )
        }
    }
}
