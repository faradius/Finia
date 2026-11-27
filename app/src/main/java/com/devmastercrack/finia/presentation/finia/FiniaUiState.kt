package com.devmastercrack.finia.presentation.finia

import androidx.compose.ui.graphics.Color
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.presentation.finia.model.Account
import com.devmastercrack.finia.presentation.finia.model.AccCalField
import com.devmastercrack.finia.presentation.finia.model.AccountType
import com.devmastercrack.finia.presentation.finia.model.AccountTypeFilter
import com.devmastercrack.finia.presentation.finia.model.CardNetwork
import com.devmastercrack.finia.presentation.finia.model.CardPaymentMode
import com.devmastercrack.finia.presentation.finia.model.CategoryConfidence
import com.devmastercrack.finia.presentation.finia.model.CategoryOrigin
import com.devmastercrack.finia.presentation.finia.model.ChatMessage
import com.devmastercrack.finia.presentation.finia.model.CreditPrimaryView
import com.devmastercrack.finia.presentation.finia.model.Debt
import com.devmastercrack.finia.presentation.finia.model.DebtTab
import com.devmastercrack.finia.presentation.finia.model.FiniaCategory
import com.devmastercrack.finia.presentation.finia.model.FiniaScreen
import com.devmastercrack.finia.presentation.finia.model.HomeAlertFilter
import com.devmastercrack.finia.presentation.finia.model.NotificationItem
import com.devmastercrack.finia.presentation.finia.model.NotifPrefs
import com.devmastercrack.finia.presentation.finia.model.QuickBtnMode
import com.devmastercrack.finia.presentation.finia.model.RecurringPayment
import com.devmastercrack.finia.presentation.finia.model.Transaction
import com.devmastercrack.finia.presentation.finia.model.TransactionForm
import com.devmastercrack.finia.presentation.finia.model.TxFlow
import com.devmastercrack.finia.presentation.finia.util.MOCK_TODAY_ISO

/**
 * Raw, hoisted state mirroring the `state = {...}` block of the Component class in
 * Finia.dc.html. Display/formatting derivations live in FiniaDerive.kt instead of here,
 * matching the prototype's separation between `state` and `renderVals()`.
 */
data class FiniaUiState(
    // Top-level navigation
    val screen: FiniaScreen = FiniaScreen.HOME,
    val showBalance: Boolean = true,
    // Single source of truth for premium status — drives both the crown badge on Home's
    // profile icon and (eventually) the Plan card in Mi perfil. Defaults to false since this
    // build's freemium scope has no real subscription flow yet.
    val isPremiumUser: Boolean = false,

    // Data
    val accounts: List<Account> = MockData.accounts,
    val recurring: List<RecurringPayment> = MockData.recurring,
    val recentTx: List<Transaction> = MockData.recentTx,
    val debts: List<Debt> = MockData.debts,
    val notifItems: List<NotificationItem> = MockData.notifications,
    val settledDebtIds: Set<Int> = emptySet(),
    val customCategories: List<FiniaCategory> = emptyList(),
    val notifPrefs: NotifPrefs = NotifPrefs(),
    val nextTxId: Int = 100,

    // Home screen
    // Named distinctly from the (historically misnamed, actually Accounts-screen-only)
    // homeCategoryFilter/homeFlowFilter below to avoid implying this belongs to the same screen.
    val homeScreenMonthOffset: Int = 0,
    val homeMonthPickerOpen: Boolean = false,
    // Tapping the Ingresos/Gastos stat card filters "Movimientos recientes" by flow, same as
    // AccountsScreen's Gastado/Ingresos cards filter that screen's transaction list.
    val homeScreenFlowFilter: TxFlow? = null,
    // Which list "Pendientes y alertas" shows below — tapping Me deben/Pagos próximos swaps it.
    val homeAlertFilter: HomeAlertFilter = HomeAlertFilter.PAGOS,
    // Id of a transaction the user swiped-to-delete, awaiting confirmation in a dialog —
    // nothing is actually removed from recentTx until that's confirmed.
    val deleteTxPending: Int? = null,
    // Id of a transaction whose read-only detail sheet is open (tapped a row).
    val txDetailOpen: Int? = null,
    // Id of the transaction being edited via "Editar" in the detail sheet — reuses the
    // Nuevo-movimiento form/sheet (addOpen), submitExpense() updates this id in place instead
    // of appending a new transaction when it's set.
    val editingTxId: Int? = null,

    // Accounts screen
    val accountTypeFilter: AccountTypeFilter = AccountTypeFilter.TODO,
    val activeAccountIdx: Int = 0,
    val acctMonthOffset: Int = 0,
    val acctMonthPickerOpen: Boolean = false,
    val homeCategoryFilter: String = "Todo",
    val homeFlowFilter: TxFlow? = null,
    val acctSearchOpen: Boolean = false,
    val acctSearchQuery: String = "",

    // Debts screen
    val debtTab: DebtTab = DebtTab.OWED,

    // Add-transaction sheet
    val addOpen: Boolean = false,
    val advancedOpen: Boolean = false,
    val categoryPickerOpen: Boolean = false,
    val datePickerOpen: Boolean = false,
    val accountPickerOpen: Boolean = false,
    val destAccountPickerOpen: Boolean = false,
    val form: TransactionForm = TransactionForm(),
    val categoryOrigin: CategoryOrigin? = null,
    val categoryConfidence: CategoryConfidence? = null,
    val snackbar: String? = null,
    val quickBtnMode: QuickBtnMode = QuickBtnMode.MIC,
    val calSelected: String = MOCK_TODAY_ISO,
    val splitOn: Boolean = false,
    val cuotasOn: Boolean = false,
    val recordatorioOn: Boolean = false,
    val recurrenteOn: Boolean = false,
    val selectedPeople: List<String> = emptyList(),

    // New category (inline, from category picker)
    val newCategoryOpen: Boolean = false,
    val newCategoryName: String = "",
    val newCategoryEmoji: String = "🏷️",
    val newCategoryEmojiPickerOpen: Boolean = false,

    // New account sheet
    val newAccountOpen: Boolean = false,
    val newAccountName: String = "",
    val newAccountTipo: AccountType = AccountType.CASH,
    val newAccountSaldo: String = "",
    val newAccountEmoji: String = "🏦",
    val newAccountEmojiPickerOpen: Boolean = false,
    val newAccountCapacidad: String = "",
    val newAccountUsado: String = "",
    val newAccountCorte: String = "",
    val newAccountPago: String = "",
    val newAccountNetwork: CardNetwork = CardNetwork.VISA,
    val newAccountColor: Color = FiniaColors.Accent,
    val newAccountBanco: String = "",
    val newAccountDigitos: String = "",

    // Account detail sheet
    val accountConfigOpen: String? = null,
    val accountEditMode: Boolean = false,
    val accountEmojiPickerOpen: Boolean = false,
    val tipoPickerOpen: Boolean = false,
    val accountConfigName: String = "",
    val accountConfigTipo: AccountType? = null,
    val accountConfigCorte: String = "",
    val accountConfigPago: String = "",
    val accountConfigDenomValue: Double = 0.0,
    val accountReajusteValue: String = "",
    val creditPrimaryView: CreditPrimaryView = CreditPrimaryView.CAPACIDAD,

    // Account detail calendar (corte/pago)
    val accCalOpen: Boolean = false,
    val accCalField: AccCalField? = null,
    val accCalSelected: String = MOCK_TODAY_ISO,

    // Card payment sheet
    val cardPaymentOpen: Boolean = false,
    val cardPaymentMode: CardPaymentMode? = null,
    val cardPaymentAmount: String = "",

    // AI assistant
    val aiInput: String = "",
    val aiTyping: Boolean = false,
    val aiMessages: List<ChatMessage> = MockData.initialAiMessages,
    val nextAiMessageId: Int = 2,
)
