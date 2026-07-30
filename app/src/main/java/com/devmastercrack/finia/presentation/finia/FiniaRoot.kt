package com.devmastercrack.finia.presentation.finia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.presentation.finia.model.FiniaScreen
import com.devmastercrack.finia.presentation.finia.screens.AiScreen
import com.devmastercrack.finia.presentation.finia.screens.DebtsScreen
import com.devmastercrack.finia.presentation.finia.screens.HomeScreen
import com.devmastercrack.finia.presentation.finia.screens.MoreScreen
import com.devmastercrack.finia.presentation.finia.screens.NotifSettingsScreen
import com.devmastercrack.finia.presentation.finia.screens.NotificationsScreen
import com.devmastercrack.finia.presentation.finia.screens.ProfileScreen
import com.devmastercrack.finia.presentation.finia.screens.AccountsScreen
import com.devmastercrack.finia.presentation.finia.components.BottomNavBar
import com.devmastercrack.finia.presentation.finia.sheets.AccountDetailSheet
import com.devmastercrack.finia.presentation.finia.sheets.AccountPickerSheet
import com.devmastercrack.finia.presentation.finia.sheets.AddTransactionSheet
import com.devmastercrack.finia.presentation.finia.sheets.AdvancedDetailsSheet
import com.devmastercrack.finia.presentation.finia.sheets.CalendarDialog
import com.devmastercrack.finia.presentation.finia.sheets.CardPaymentSheet
import com.devmastercrack.finia.presentation.finia.sheets.CategoryPickerSheet
import com.devmastercrack.finia.presentation.finia.sheets.DestAccountPickerSheet
import com.devmastercrack.finia.presentation.finia.sheets.NewAccountSheet
import com.devmastercrack.finia.presentation.finia.util.calHeaderLabel

@Composable
fun FiniaRoot(modifier: Modifier = Modifier, vm: FiniaViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val showBottomNav = state.screen !in setOf(
        FiniaScreen.AI, FiniaScreen.PROFILE, FiniaScreen.NOTIFICATIONS, FiniaScreen.NOTIF_SETTINGS,
    )

    Box(modifier.fillMaxSize().background(FiniaColors.ScreenBg)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when (state.screen) {
                    FiniaScreen.HOME -> HomeScreen(state, vm)
                    FiniaScreen.ACCOUNTS -> AccountsScreen(state, vm)
                    FiniaScreen.DEBTS -> DebtsScreen(state, vm)
                    FiniaScreen.AI -> AiScreen(state, vm)
                    FiniaScreen.SETTINGS -> MoreScreen(state, vm)
                    FiniaScreen.PROFILE -> ProfileScreen(state, vm)
                    FiniaScreen.NOTIFICATIONS -> NotificationsScreen(state, vm)
                    FiniaScreen.NOTIF_SETTINGS -> NotifSettingsScreen(state, vm)
                }
            }
            if (showBottomNav) {
                BottomNavBar(
                    screen = state.screen,
                    onHome = vm::goHome,
                    onAccounts = vm::goAccounts,
                    onAdd = vm::openAdd,
                    onAI = vm::goAI,
                    onMore = vm::goSettings,
                )
            }
        }

        // Overlay stack, back-to-front, mirroring Finia.dc.html's z-index order.
        if (state.addOpen) {
            AddTransactionSheet(state, vm, noScrim = state.addFromAccounts)
        }
        if (state.advancedOpen) {
            AdvancedDetailsSheet(state, vm)
        }
        if (state.accountConfigOpen != null) {
            AccountDetailSheet(state, vm)
        }
        if (state.categoryPickerOpen) {
            CategoryPickerSheet(state, vm)
        }
        if (state.accountPickerOpen) {
            AccountPickerSheet(state, vm)
        }
        if (state.destAccountPickerOpen) {
            DestAccountPickerSheet(state, vm)
        }
        if (state.datePickerOpen) {
            CalendarDialog(
                year = state.calYear, month = state.calMonth, selectedIso = state.calSelected,
                headerLabel = calHeaderLabel(state.calSelected),
                onPrevMonth = vm::calPrevMonth, onNextMonth = vm::calNextMonth,
                onSelectDay = vm::selectCalDay, onCancel = vm::toggleDatePicker, onAccept = vm::acceptDate,
            )
        }
        if (state.newAccountOpen && !state.accountPickerOpen) {
            NewAccountSheet(state, vm)
        }
        if (state.cardPaymentOpen) {
            CardPaymentSheet(state, vm)
        }
        if (state.accCalOpen) {
            CalendarDialog(
                year = state.accCalYear, month = state.accCalMonth, selectedIso = state.accCalSelected,
                headerLabel = calHeaderLabel(state.accCalSelected),
                onPrevMonth = vm::accCalPrevMonth, onNextMonth = vm::accCalNextMonth,
                onSelectDay = vm::selectAccCalDay, onCancel = vm::closeAccCal, onAccept = vm::acceptAccCal,
            )
        }

        val snackbarText = state.snackbar
        if (snackbarText != null) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            ) {
                com.devmastercrack.finia.presentation.finia.components.FiniaSnackbar(snackbarText)
            }
        }
    }
}
