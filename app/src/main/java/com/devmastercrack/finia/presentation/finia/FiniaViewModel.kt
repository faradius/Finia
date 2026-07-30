package com.devmastercrack.finia.presentation.finia

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devmastercrack.finia.presentation.finia.model.AccCalField
import com.devmastercrack.finia.presentation.finia.model.Account
import com.devmastercrack.finia.presentation.finia.model.AccountType
import com.devmastercrack.finia.presentation.finia.model.AccountTypeFilter
import com.devmastercrack.finia.presentation.finia.model.CardBackground
import com.devmastercrack.finia.presentation.finia.model.CardNetwork
import com.devmastercrack.finia.presentation.finia.model.CardPaymentMode
import com.devmastercrack.finia.presentation.finia.model.CategoryConfidence
import com.devmastercrack.finia.presentation.finia.model.CategoryOrigin
import com.devmastercrack.finia.presentation.finia.model.ChatMessage
import com.devmastercrack.finia.presentation.finia.model.ChatMessageType
import com.devmastercrack.finia.presentation.finia.model.ChatSender
import com.devmastercrack.finia.presentation.finia.model.CreditPrimaryView
import com.devmastercrack.finia.presentation.finia.model.DebtTab
import com.devmastercrack.finia.presentation.finia.model.DefaultCategories
import com.devmastercrack.finia.presentation.finia.model.FiniaCategory
import com.devmastercrack.finia.presentation.finia.model.FiniaScreen
import com.devmastercrack.finia.presentation.finia.model.QuickBtnMode
import com.devmastercrack.finia.presentation.finia.model.TransactionForm
import com.devmastercrack.finia.presentation.finia.model.Transaction
import com.devmastercrack.finia.presentation.finia.model.TxFlow
import com.devmastercrack.finia.presentation.finia.util.MOCK_TODAY_ISO
import com.devmastercrack.finia.presentation.finia.util.detectCategory
import com.devmastercrack.finia.presentation.finia.util.fmt
import com.devmastercrack.finia.presentation.finia.util.num
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Presentation-layer state holder for the whole Finia mock flow — mirrors the single
 * `Component` class (state + actions) in Finia.dc.html. All data is in-memory mock data;
 * there is no repository/network layer yet.
 */
class FiniaViewModel : ViewModel() {

    private val _state = MutableStateFlow(FiniaUiState())
    val state: StateFlow<FiniaUiState> = _state.asStateFlow()

    private fun allCategories(s: FiniaUiState) = DefaultCategories + s.customCategories

    // ───────────────────────────── Navigation ─────────────────────────────

    fun goHome() = _state.update { it.copy(screen = FiniaScreen.HOME) }
    fun goAccounts() = _state.update { it.copy(screen = FiniaScreen.ACCOUNTS) }
    fun goDebts() = _state.update { it.copy(screen = FiniaScreen.DEBTS) }
    fun goAI() = _state.update { it.copy(screen = FiniaScreen.AI) }
    fun goSettings() = _state.update { it.copy(screen = FiniaScreen.SETTINGS) }
    fun goProfile() = _state.update { it.copy(screen = FiniaScreen.PROFILE) }
    fun goBackFromProfile() = goHome()
    fun goNotifications() = _state.update { it.copy(screen = FiniaScreen.NOTIFICATIONS) }
    fun goBackFromNotifications() = goHome()
    fun goNotifSettings() = _state.update { it.copy(screen = FiniaScreen.NOTIF_SETTINGS) }
    fun goBackFromNotifSettings() = goSettings()

    // ───────────────────────────── Home screen ─────────────────────────────

    fun toggleBalanceVisible() = _state.update { it.copy(showBalance = !it.showBalance) }
    fun setHomeCategory(name: String) = _state.update { it.copy(homeCategoryFilter = name) }
    fun toggleHomeFlowFilter(flow: TxFlow) = _state.update {
        it.copy(homeFlowFilter = if (it.homeFlowFilter == flow) null else flow)
    }
    fun markAllNotifsRead() = _state.update { s ->
        s.copy(notifItems = s.notifItems.map { it.copy(unread = false) })
    }

    // ───────────────────────────── Accounts screen ─────────────────────────────

    fun setAccountTypeFilter(filter: AccountTypeFilter) =
        _state.update { it.copy(accountTypeFilter = filter, activeAccountIdx = 0) }

    fun setActiveAccountIdx(idx: Int) = _state.update { it.copy(activeAccountIdx = idx) }

    fun acctPrevMonth() = _state.update { it.copy(acctMonthOffset = it.acctMonthOffset - 1) }
    fun acctNextMonth() = _state.update { if (it.acctMonthOffset < 0) it.copy(acctMonthOffset = it.acctMonthOffset + 1) else it }

    // ───────────────────────────── Account detail sheet ─────────────────────────────

    fun openAccountDetail(id: String) = _state.update {
        it.copy(accountConfigOpen = id, accountEditMode = false, sheetDragY = 0f)
    }

    fun closeAccountConfig() = _state.update {
        it.copy(accountConfigOpen = null, accountEditMode = false, sheetDragY = 0f)
    }

    fun switchAccountPrev() = _state.update { s ->
        val list = s.accounts
        val i = list.indexOfFirst { it.id == s.accountConfigOpen }
        if (i < 0) return@update s
        val next = list[(i - 1 + list.size) % list.size]
        syncActiveIdxToAccount(s, next.id).copy(accountConfigOpen = next.id)
    }

    fun switchAccountNext() = _state.update { s ->
        val list = s.accounts
        val i = list.indexOfFirst { it.id == s.accountConfigOpen }
        if (i < 0) return@update s
        val next = list[(i + 1) % list.size]
        syncActiveIdxToAccount(s, next.id).copy(accountConfigOpen = next.id)
    }

    private fun syncActiveIdxToAccount(s: FiniaUiState, id: String): FiniaUiState {
        val filtered = filterAccounts(s.accounts, s.accountTypeFilter)
        val idx = filtered.indexOfFirst { it.id == id }
        return if (idx >= 0) s.copy(activeAccountIdx = idx) else s
    }

    fun toggleAccountEditMode() = _state.update { s ->
        val acc = s.accounts.find { it.id == s.accountConfigOpen } ?: return@update s
        if (!s.accountEditMode) {
            val primIsCap = s.creditPrimaryView == CreditPrimaryView.CAPACIDAD
            val view = deriveAccountView(acc, s.creditPrimaryView)
            return@update s.copy(
                accountEditMode = true,
                accountConfigName = acc.nombre,
                accountReajusteValue = if (acc.isCredit) view.available.toLong().toString() else acc.saldo.toLong().toString(),
                accountConfigTipo = acc.tipo,
                accountEmojiPickerOpen = false,
                tipoPickerOpen = false,
                accountConfigCorte = acc.corte ?: "",
                accountConfigPago = acc.pago ?: "",
                accountConfigDenomValue = if (acc.isCredit) (if (primIsCap) acc.capacidad ?: 0.0 else acc.limite ?: 0.0) else 0.0,
            )
        }
        val name = s.accountConfigName.trim()
        val v = s.accountReajusteValue.toDoubleOrNull()
        val denom = s.accountConfigDenomValue
        val primIsCap = s.creditPrimaryView == CreditPrimaryView.CAPACIDAD
        val updatedAccounts = s.accounts.map { a ->
            if (a.id != s.accountConfigOpen) return@map a
            if (a.isCredit) {
                a.copy(
                    nombre = name.ifBlank { a.nombre },
                    tipo = s.accountConfigTipo ?: a.tipo,
                    corte = s.accountConfigCorte.ifBlank { a.corte },
                    pago = s.accountConfigPago.ifBlank { a.pago },
                    capacidad = if (primIsCap) denom else a.capacidad,
                    limite = if (!primIsCap) denom else a.limite,
                    deudaTarjeta = if (v != null && primIsCap) maxOf(0.0, denom - v) else a.deudaTarjeta,
                    gastadoCorte = if (v != null && !primIsCap) maxOf(0.0, denom - v) else a.gastadoCorte,
                )
            } else {
                a.copy(
                    nombre = name.ifBlank { a.nombre },
                    tipo = s.accountConfigTipo ?: a.tipo,
                    saldo = v ?: a.saldo,
                )
            }
        }
        s.copy(accounts = updatedAccounts, accountEditMode = false, accountEmojiPickerOpen = false, tipoPickerOpen = false)
    }

    fun onReajusteChange(value: String) = _state.update { it.copy(accountReajusteValue = value) }
    fun onDenomSliderChange(value: Double) = _state.update { it.copy(accountConfigDenomValue = value) }
    fun onAccountConfigNameChange(value: String) = _state.update { it.copy(accountConfigName = value) }
    fun onAccountConfigCorteChange(value: String) = _state.update { it.copy(accountConfigCorte = value) }
    fun onAccountConfigPagoChange(value: String) = _state.update { it.copy(accountConfigPago = value) }

    fun setCreditPrimaryView(view: CreditPrimaryView) = _state.update { it.copy(creditPrimaryView = view) }

    fun toggleEmojiPicker() = _state.update { it.copy(accountEmojiPickerOpen = !it.accountEmojiPickerOpen, tipoPickerOpen = false) }
    fun selectAccountEmoji(emoji: String) = _state.update { s ->
        s.copy(
            accounts = s.accounts.map { if (it.id == s.accountConfigOpen) it.copy(emoji = emoji) else it },
            accountEmojiPickerOpen = false,
        )
    }
    fun toggleTipoPicker() = _state.update { it.copy(tipoPickerOpen = !it.tipoPickerOpen, accountEmojiPickerOpen = false) }
    fun selectTipo(tipo: AccountType) = _state.update { it.copy(accountConfigTipo = tipo, tipoPickerOpen = false) }

    fun onSheetDragBy(dy: Float) = _state.update { it.copy(sheetDragY = maxOf(0f, dy)) }
    fun onSheetDragEnd() = _state.update { s ->
        if (s.sheetDragY > 90f) s.copy(accountConfigOpen = null, accountEditMode = false, sheetDragY = 0f)
        else s.copy(sheetDragY = 0f)
    }

    // Corte / pago calendar (inside account detail edit mode)
    fun openAccCal(field: AccCalField) = _state.update {
        it.copy(accCalOpen = true, accCalField = field, accCalYear = 2026, accCalMonth = 6, accCalSelected = MOCK_TODAY_ISO)
    }
    fun closeAccCal() = _state.update { it.copy(accCalOpen = false) }
    fun accCalPrevMonth() = _state.update { s ->
        val m = s.accCalMonth - 1
        if (m < 0) s.copy(accCalMonth = 11, accCalYear = s.accCalYear - 1) else s.copy(accCalMonth = m)
    }
    fun accCalNextMonth() = _state.update { s ->
        val m = s.accCalMonth + 1
        if (m > 11) s.copy(accCalMonth = 0, accCalYear = s.accCalYear + 1) else s.copy(accCalMonth = m)
    }
    fun selectAccCalDay(iso: String) = _state.update { it.copy(accCalSelected = iso) }
    fun acceptAccCal() = _state.update { s ->
        val label = com.devmastercrack.finia.presentation.finia.util.calShortLabel(s.accCalSelected)
        val next = when (s.accCalField) {
            AccCalField.CORTE -> s.copy(accountConfigCorte = label)
            AccCalField.PAGO -> s.copy(accountConfigPago = label)
            null -> s
        }
        next.copy(accCalOpen = false)
    }

    // Card payment sheet
    fun openCardPayment() = _state.update { it.copy(cardPaymentOpen = true, cardPaymentMode = null, cardPaymentAmount = "") }
    fun closeCardPayment() = _state.update { it.copy(cardPaymentOpen = false) }
    fun selectPaymentMode(mode: CardPaymentMode) = _state.update { it.copy(cardPaymentMode = mode, cardPaymentAmount = if (mode == CardPaymentMode.TOTAL) it.cardPaymentAmount else "") }
    fun onCardPaymentAmountChange(value: String) = _state.update { it.copy(cardPaymentAmount = value.filter { c -> c.isDigit() || c == '.' }) }
    fun confirmCardPayment() = _state.update { s ->
        val acc = s.accounts.find { it.id == s.accountConfigOpen } ?: return@update s
        val totalDeuda = acc.deudaTarjeta ?: 0.0
        val amountRaw = if (s.cardPaymentMode == CardPaymentMode.TOTAL) totalDeuda else s.cardPaymentAmount.toDoubleOrNull()
        if (amountRaw == null || amountRaw <= 0.0) return@update s
        val amount = minOf(amountRaw, totalDeuda)
        s.copy(
            accounts = s.accounts.map {
                if (it.id != s.accountConfigOpen) it else it.copy(
                    deudaTarjeta = maxOf(0.0, (it.deudaTarjeta ?: 0.0) - amount),
                    gastadoCorte = maxOf(0.0, (it.gastadoCorte ?: 0.0) - amount),
                )
            },
            cardPaymentOpen = false,
            snackbar = "Pago de ${fmt(amount)} aplicado a ${acc.nombre}",
        )
    }

    // ───────────────────────────── New account sheet ─────────────────────────────

    fun openNewAccountSheet() = _state.update {
        it.copy(
            newAccountOpen = true, newAccountName = "", newAccountTipo = AccountType.CASH, newAccountSaldo = "",
            newAccountEmoji = "🏦", newAccountEmojiPickerOpen = false, newAccountCapacidad = "", newAccountUsado = "",
            newAccountCorte = "", newAccountPago = "", newAccountNetwork = CardNetwork.VISA,
            newAccountColor = com.devmastercrack.finia.core.theme.FiniaColors.Accent, newAccountBanco = "", newAccountDigitos = "",
        )
    }
    fun closeNewAccountSheet() = _state.update { it.copy(newAccountOpen = false) }
    fun openNewAccountFromPicker() {
        _state.update { it.copy(accountPickerOpen = false) }
        openNewAccountSheet()
    }

    fun onNewAccountNameChange(value: String) = _state.update { it.copy(newAccountName = value) }
    fun setNewAccountTipo(tipo: AccountType) = _state.update { s ->
        val currentIsDefault = s.newAccountEmoji == "🏦" ||
            s.newAccountEmoji in listOf("💵", "🐷", "💳", "🪙")
        s.copy(
            newAccountTipo = tipo,
            newAccountEmoji = if (currentIsDefault) com.devmastercrack.finia.presentation.finia.model.defaultEmojiFor(tipo) else s.newAccountEmoji,
        )
    }
    fun toggleNewAccountEmojiPicker() = _state.update { it.copy(newAccountEmojiPickerOpen = !it.newAccountEmojiPickerOpen) }
    fun setNewAccountEmoji(emoji: String) = _state.update { it.copy(newAccountEmoji = emoji, newAccountEmojiPickerOpen = false) }
    fun setNewAccountColor(color: Color) = _state.update { it.copy(newAccountColor = color) }
    fun setNewAccountNetwork(network: CardNetwork) = _state.update { it.copy(newAccountNetwork = network) }
    fun onNewAccountBancoChange(value: String) = _state.update { it.copy(newAccountBanco = value) }
    fun onNewAccountDigitosChange(value: String) = _state.update { it.copy(newAccountDigitos = value.filter { c -> c.isDigit() }.take(4)) }
    fun onNewAccountSaldoChange(value: String) = _state.update { it.copy(newAccountSaldo = value) }
    fun onNewAccountCapacidadChange(value: String) = _state.update { it.copy(newAccountCapacidad = value.replace(",", "")) }
    fun onNewAccountUsadoChange(value: String) = _state.update { it.copy(newAccountUsado = value.replace(",", "")) }
    fun onNewAccountCorteChange(value: String) = _state.update { it.copy(newAccountCorte = value.filter { c -> c.isDigit() }.take(2)) }
    fun onNewAccountPagoChange(value: String) = _state.update { it.copy(newAccountPago = value.filter { c -> c.isDigit() }.take(2)) }

    fun addNewAccount() = _state.update { s ->
        val name = s.newAccountName.trim()
        if (name.isBlank()) return@update s
        val isCredit = s.newAccountTipo == AccountType.CREDIT
        val isDebit = s.newAccountTipo == AccountType.DEBIT
        if (isCredit && num(s.newAccountUsado) > num(s.newAccountCapacidad)) return@update s
        val id = "acc" + System.currentTimeMillis()
        val newAcc = Account(
            id = id, nombre = name, tipo = s.newAccountTipo, emoji = s.newAccountEmoji,
            iconBg = com.devmastercrack.finia.core.theme.FiniaColors.PastelBlue,
            cardBg = CardBackground.Solid(s.newAccountColor), cardFg = com.devmastercrack.finia.core.theme.FiniaColors.DebitCardFg,
            digitos = if (isCredit || isDebit) s.newAccountDigitos.ifBlank { "0000" } else null,
            network = if (isCredit || isDebit) s.newAccountNetwork else null,
            banco = if (isCredit || isDebit) s.newAccountBanco else null,
            saldo = if (!isCredit) num(s.newAccountSaldo) else 0.0,
            saldoInicial = if (!isCredit) num(s.newAccountSaldo) else 0.0,
            capacidad = if (isCredit) num(s.newAccountCapacidad) else null,
            deudaTarjeta = if (isCredit) num(s.newAccountUsado) else null,
            limite = if (isCredit) num(s.newAccountCapacidad) else null,
            gastadoCorte = if (isCredit) num(s.newAccountUsado) else null,
            corte = if (isCredit) s.newAccountCorte.ifBlank { "28" } else null,
            pago = if (isCredit) s.newAccountPago.ifBlank { "15" } else null,
        )
        s.copy(
            accounts = s.accounts + newAcc,
            newAccountOpen = false, newAccountName = "", newAccountTipo = AccountType.CASH, newAccountSaldo = "",
            newAccountEmoji = "🏦", newAccountCapacidad = "", newAccountUsado = "", newAccountCorte = "", newAccountPago = "",
            newAccountColor = com.devmastercrack.finia.core.theme.FiniaColors.Accent, newAccountBanco = "", newAccountDigitos = "",
        )
    }

    // ───────────────────────────── Add-transaction sheet ─────────────────────────────

    fun openAdd() = _state.update { s ->
        val filtered = filterAccounts(s.accounts, s.accountTypeFilter)
        val active = filtered.getOrNull(s.activeAccountIdx) ?: filtered.firstOrNull()
        s.copy(
            screen = FiniaScreen.ACCOUNTS, addOpen = true, addFromAccounts = true,
            categoryPickerOpen = false, datePickerOpen = false, accountPickerOpen = false,
            splitOn = false, cuotasOn = false, recordatorioOn = false, recurrenteOn = false, selectedPeople = emptyList(),
            form = TransactionForm(cuenta = active?.nombre ?: "Débito"),
            categoryOrigin = null, categoryConfidence = null, snackbar = null,
        )
    }
    fun closeAdd() = _state.update { it.copy(addOpen = false) }
    fun openAdvanced() = _state.update { it.copy(advancedOpen = true) }
    fun closeAdvanced() = _state.update { it.copy(advancedOpen = false) }

    fun toggleCategoryPicker() = _state.update { it.copy(categoryPickerOpen = !it.categoryPickerOpen, datePickerOpen = false, accountPickerOpen = false) }
    fun toggleAccountPicker() = _state.update { it.copy(accountPickerOpen = !it.accountPickerOpen, categoryPickerOpen = false, datePickerOpen = false) }
    fun toggleDestAccountPicker() = _state.update { it.copy(destAccountPickerOpen = !it.destAccountPickerOpen) }
    fun toggleDatePicker() = _state.update { s ->
        if (s.datePickerOpen) return@update s.copy(datePickerOpen = false)
        val (y, m) = s.form.fecha.split("-").let { it[0].toInt() to it[1].toInt() }
        s.copy(datePickerOpen = true, categoryPickerOpen = false, accountPickerOpen = false, calYear = y, calMonth = m - 1, calSelected = s.form.fecha)
    }
    fun calPrevMonth() = _state.update { s ->
        val m = s.calMonth - 1
        if (m < 0) s.copy(calMonth = 11, calYear = s.calYear - 1) else s.copy(calMonth = m)
    }
    fun calNextMonth() = _state.update { s ->
        val m = s.calMonth + 1
        if (m > 11) s.copy(calMonth = 0, calYear = s.calYear + 1) else s.copy(calMonth = m)
    }
    fun selectCalDay(iso: String) = _state.update { it.copy(calSelected = iso) }
    fun acceptDate() = _state.update { it.copy(form = it.form.copy(fecha = it.calSelected), datePickerOpen = false) }

    fun setTipoGasto() = _state.update { it.copy(form = it.form.copy(tipo = TxFlow.GASTO)) }
    fun setTipoIngreso() = _state.update { it.copy(form = it.form.copy(tipo = TxFlow.INGRESO)) }
    fun onMontoChange(value: String) = _state.update { it.copy(form = it.form.copy(monto = value)) }
    fun onNotaLargaChange(value: String) = _state.update { it.copy(form = it.form.copy(notaLarga = value)) }

    fun onNotaChange(nota: String) = _state.update { s ->
        val detection = detectCategory(nota)
        var next = s.copy(form = s.form.copy(nota = nota))
        if (detection != null && s.categoryOrigin != CategoryOrigin.MANUAL) {
            next = next.copy(
                form = next.form.copy(categoria = detection.category),
                categoryOrigin = CategoryOrigin.AUTO,
                categoryConfidence = detection.confidence,
            )
        } else if (detection == null && s.categoryOrigin == CategoryOrigin.AUTO) {
            next = next.copy(categoryOrigin = null, categoryConfidence = null)
        }
        next
    }

    fun selectCategoryManually(nombre: String) = _state.update {
        it.copy(form = it.form.copy(categoria = nombre), categoryOrigin = CategoryOrigin.MANUAL, categoryConfidence = null, categoryPickerOpen = false)
    }

    fun selectAccountForForm(nombre: String) = _state.update {
        it.copy(form = it.form.copy(cuenta = nombre), accountPickerOpen = false)
    }
    fun selectDestAccountForForm(nombre: String) = _state.update {
        it.copy(form = it.form.copy(cuentaDestino = nombre), destAccountPickerOpen = false)
    }

    fun toggleNewCategory() = _state.update { it.copy(newCategoryOpen = !it.newCategoryOpen, newCategoryName = "", newCategoryEmoji = "🏷️", newCategoryEmojiPickerOpen = false) }
    fun toggleNewCategoryEmojiPicker() = _state.update { it.copy(newCategoryEmojiPickerOpen = !it.newCategoryEmojiPickerOpen) }
    fun setNewCategoryEmoji(emoji: String) = _state.update { it.copy(newCategoryEmoji = emoji, newCategoryEmojiPickerOpen = false) }
    fun onNewCategoryNameChange(value: String) = _state.update { it.copy(newCategoryName = value) }
    fun addNewCategory() = _state.update { s ->
        val name = s.newCategoryName.trim()
        if (name.isBlank()) return@update s
        s.copy(
            customCategories = s.customCategories + FiniaCategory(name, s.newCategoryEmoji.ifBlank { "🏷️" }),
            newCategoryOpen = false, newCategoryName = "", newCategoryEmoji = "🏷️",
        )
    }

    fun toggleSplit() = _state.update { it.copy(splitOn = !it.splitOn, selectedPeople = if (it.splitOn) emptyList() else it.selectedPeople) }
    fun toggleCuotas() = _state.update { it.copy(cuotasOn = !it.cuotasOn) }
    fun toggleRecordatorio() = _state.update { it.copy(recordatorioOn = !it.recordatorioOn) }
    fun toggleRecurrente() = _state.update { it.copy(recurrenteOn = !it.recurrenteOn) }
    fun togglePerson(name: String) = _state.update { s ->
        s.copy(selectedPeople = if (name in s.selectedPeople) s.selectedPeople - name else s.selectedPeople + name)
    }
    fun setCuotas(n: Int) = _state.update { it.copy(form = it.form.copy(cuotas = n)) }
    fun updateFormField(patch: (TransactionForm) -> TransactionForm) = _state.update { it.copy(form = patch(it.form)) }

    fun toggleQuickBtnMode() = _state.update { it.copy(quickBtnMode = if (it.quickBtnMode == QuickBtnMode.MIC) QuickBtnMode.CAMERA else QuickBtnMode.MIC) }

    fun submitExpense() = _state.update { s ->
        val monto = s.form.monto.toDoubleOrNull()
        if (monto == null || monto == 0.0) return@update s
        val signed = if (s.form.tipo == TxFlow.INGRESO) kotlin.math.abs(monto) else -kotlin.math.abs(monto)
        val wasAuto = s.categoryOrigin == CategoryOrigin.AUTO
        val cuentaId = s.accounts.find { it.nombre == s.form.cuenta }?.id ?: s.accounts.firstOrNull()?.id ?: ""
        val emoji = allCategories(s).find { it.nombre == s.form.categoria }?.emoji ?: "💳"
        val newTx = Transaction(
            id = s.nextTxId, concepto = s.form.nota.ifBlank { s.form.categoria }, categoria = s.form.categoria,
            cuentaId = cuentaId, monto = signed, emoji = emoji, fecha = "Hoy",
        )
        val next = s.copy(
            recentTx = listOf(newTx) + s.recentTx, nextTxId = s.nextTxId + 1,
            addOpen = wasAuto, advancedOpen = false,
            snackbar = if (wasAuto) "Categorizado como ${s.form.categoria} ✓" else null,
        )
        if (wasAuto) {
            viewModelScope.launch {
                delay(1400)
                _state.update { it.copy(addOpen = false, snackbar = null) }
            }
        }
        next
    }

    // ───────────────────────────── Debts ─────────────────────────────

    fun setDebtTab(tab: DebtTab) = _state.update { it.copy(debtTab = tab) }
    fun settleDebt(id: Int) = _state.update { it.copy(settledDebtIds = it.settledDebtIds + id) }

    // ───────────────────────────── AI assistant ─────────────────────────────

    fun onAiInputChange(value: String) = _state.update { it.copy(aiInput = value) }

    fun sendAiMessage(presetText: String? = null) {
        val text = (presetText ?: _state.value.aiInput).trim()
        if (text.isBlank()) return
        val userMsg = ChatMessage(ChatSender.USER, ChatMessageType.TEXT, "Ahora", text)
        _state.update { it.copy(aiMessages = it.aiMessages + userMsg, aiInput = "", aiTyping = true) }
        viewModelScope.launch {
            delay(900)
            _state.update { it.copy(aiMessages = it.aiMessages + buildAiReply(text), aiTyping = false) }
        }
    }

    private fun buildAiReply(text: String): ChatMessage {
        val t = text.lowercase()
        val time = "Ahora"
        return when {
            t.contains("cómo voy") || t.contains("como voy") || t.contains("resumen") ->
                ChatMessage(ChatSender.AI, ChatMessageType.SUMMARY, time)
            t.contains("gasto más") || t.contains("analiz") ->
                ChatMessage(ChatSender.AI, ChatMessageType.ANALYSIS, time)
            t.contains("ahorr") || t.contains("presupuesto") ->
                ChatMessage(ChatSender.AI, ChatMessageType.TIP, time)
            t.contains("pago") ->
                ChatMessage(ChatSender.AI, ChatMessageType.PAYMENTS, time)
            else -> ChatMessage(
                ChatSender.AI, ChatMessageType.TEXT, time,
                "Puedo mostrarte tu resumen del mes, próximos pagos, un análisis de gastos o consejos para ahorrar. ¿Qué te gustaría revisar?",
            )
        }
    }

    // ───────────────────────────── Notification settings ─────────────────────────────

    fun toggleNotifPref(key: String) = _state.update { s ->
        s.copy(
            notifPrefs = when (key) {
                "pago" -> s.notifPrefs.copy(pago = !s.notifPrefs.pago)
                "cobrar" -> s.notifPrefs.copy(cobrar = !s.notifPrefs.cobrar)
                "limite" -> s.notifPrefs.copy(limite = !s.notifPrefs.limite)
                "resumen" -> s.notifPrefs.copy(resumen = !s.notifPrefs.resumen)
                else -> s.notifPrefs
            },
        )
    }
}
