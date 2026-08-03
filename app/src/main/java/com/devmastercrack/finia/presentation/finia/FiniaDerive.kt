package com.devmastercrack.finia.presentation.finia

import androidx.compose.ui.graphics.Color
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.presentation.finia.model.Account
import com.devmastercrack.finia.presentation.finia.model.AccountTypeFilter
import com.devmastercrack.finia.presentation.finia.model.CreditPrimaryView
import com.devmastercrack.finia.presentation.finia.model.Debt
import com.devmastercrack.finia.presentation.finia.model.DebtDirection
import com.devmastercrack.finia.presentation.finia.model.Transaction
import com.devmastercrack.finia.presentation.finia.model.TxFlow
import kotlin.math.min
import kotlin.math.roundToInt

/** Per-account display values — mirrors the `accountsView` map in Finia.dc.html's renderVals(). */
data class AccountView(
    val account: Account,
    val available: Double,
    val pct: Int,
    val pctColor: Color,
    val pctLabel: String,
    val denomLabel: String,
    val denomAmount: Double,
    val usedAmount: Double,
    val saldoLabel: String,
    val secondaryLabel: String,
    val secondaryAmount: Double,
    val showCorteNote: Boolean,
)

fun deriveAccountView(acc: Account, primaryView: CreditPrimaryView): AccountView {
    if (!acc.isCredit) {
        return AccountView(
            account = acc, available = acc.saldo, pct = 0, pctColor = FiniaColors.Accent,
            pctLabel = "", denomLabel = "", denomAmount = 0.0, usedAmount = 0.0,
            saldoLabel = "Saldo disponible", secondaryLabel = "", secondaryAmount = 0.0,
            showCorteNote = false,
        )
    }
    val capacidad = acc.capacidad ?: 0.0
    val deudaTarjeta = acc.deudaTarjeta ?: 0.0
    val limite = acc.limite ?: 0.0
    val gastadoCorte = acc.gastadoCorte ?: 0.0
    val primIsCapacidad = primaryView == CreditPrimaryView.CAPACIDAD
    val primDenom = if (primIsCapacidad) capacidad else limite
    val primUsed = if (primIsCapacidad) deudaTarjeta else gastadoCorte
    val pctNum = if (primDenom <= 0) 0 else min(100.0, (primUsed / primDenom) * 100).roundToInt()
    return AccountView(
        account = acc,
        available = primDenom - primUsed,
        pct = pctNum,
        pctColor = FiniaColors.creditPctColor(pctNum),
        pctLabel = if (primIsCapacidad) "$pctNum% de tu tarjeta usado" else "$pctNum% de tu límite usado",
        denomLabel = if (primIsCapacidad) "Capacidad" else "Tu límite",
        denomAmount = primDenom,
        usedAmount = primUsed,
        saldoLabel = if (primIsCapacidad) "Crédito disponible" else "Disponible en tu límite",
        secondaryLabel = if (primIsCapacidad) "Crédito disponible en tu tarjeta" else "Disponible en tu límite",
        secondaryAmount = if (primIsCapacidad) (limite - gastadoCorte) else (capacidad - deudaTarjeta),
        showCorteNote = !primIsCapacidad,
    )
}

fun filterAccounts(accounts: List<Account>, filter: AccountTypeFilter): List<Account> = when (filter) {
    AccountTypeFilter.TODO -> accounts
    AccountTypeFilter.TARJETAS -> accounts.filter { it.isCard }
    AccountTypeFilter.CUENTAS -> accounts.filterNot { it.isCard }
}

fun owedToMe(debts: List<Debt>, settled: Set<Int>): Double =
    debts.filter { it.direction == DebtDirection.OWED && it.id !in settled }.sumOf { it.monto }

fun iOwe(debts: List<Debt>, settled: Set<Int>): Double =
    debts.filter { it.direction == DebtDirection.I_OWE && it.id !in settled }.sumOf { it.monto }

fun owedCount(debts: List<Debt>, settled: Set<Int>): Int =
    debts.count { it.direction == DebtDirection.OWED && it.id !in settled }

fun netWorth(accounts: List<Account>, debts: List<Debt>, settled: Set<Int>): Double {
    val accountsTotal = accounts.sumOf { if (it.isCredit) -(it.gastadoCorte ?: 0.0) else it.saldo }
    return accountsTotal + owedToMe(debts, settled) - iOwe(debts, settled)
}

fun totalIngresos(tx: List<Transaction>): Double = tx.filter { it.monto > 0 }.sumOf { it.monto }
fun totalGastos(tx: List<Transaction>): Double = tx.filter { it.monto < 0 }.sumOf { -it.monto }

data class TxGroup(val fecha: String, val items: List<Transaction>)

/** Groups by `fecha` preserving first-seen order, matching the JS `groups.find(...)` loop. */
fun groupByFecha(tx: List<Transaction>): List<TxGroup> {
    val groups = LinkedHashMap<String, MutableList<Transaction>>()
    for (t in tx) groups.getOrPut(t.fecha) { mutableListOf() }.add(t)
    return groups.map { (fecha, items) -> TxGroup(fecha, items) }
}

fun filterAccountTx(
    allTx: List<Transaction>,
    accountId: String,
    categoryFilter: String,
    flowFilter: TxFlow?,
    inCurrentMonth: Boolean,
): List<Transaction> {
    if (!inCurrentMonth) return emptyList()
    return allTx
        .filter { it.cuentaId == accountId }
        .filter { categoryFilter == "Todo" || it.categoria == categoryFilter }
        .filter { flow ->
            when (flowFilter) {
                null -> true
                TxFlow.GASTO -> flow.monto < 0
                TxFlow.INGRESO -> flow.monto > 0
                TxFlow.TRANSFERENCIA -> flow.categoria == "Transferencia"
            }
        }
}
