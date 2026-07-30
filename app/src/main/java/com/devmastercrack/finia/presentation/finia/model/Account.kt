package com.devmastercrack.finia.presentation.finia.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.devmastercrack.finia.core.theme.FiniaColors

enum class AccountType(val label: String) {
    CASH("Efectivo"),
    SAVINGS("Cuenta de ahorro"),
    DEBIT("Tarjeta de débito"),
    CREDIT("Tarjeta de crédito"),
}

enum class CardNetwork { VISA, MASTERCARD, OTHER }

sealed class CardBackground {
    data class Solid(val color: Color) : CardBackground()
    data class Gradient(val brush: Brush) : CardBackground()
}

/**
 * Mirrors the single flat `accounts[]` shape from Finia.dc.html: one model for every
 * account type, with card-specific fields left null for cash/savings accounts.
 */
data class Account(
    val id: String,
    val nombre: String,
    val tipo: AccountType,
    val emoji: String,
    val iconBg: Color,
    val cardBg: CardBackground,
    val cardFg: Color,
    // Cash / savings / debit balance
    val saldo: Double = 0.0,
    val saldoInicial: Double = 0.0,
    // Debit / credit card extras
    val digitos: String? = null,
    val network: CardNetwork? = null,
    val banco: String? = null,
    // Credit only
    val capacidad: Double? = null,
    val deudaTarjeta: Double? = null,
    val limite: Double? = null,
    val gastadoCorte: Double? = null,
    val corte: String? = null,
    val pago: String? = null,
) {
    val isCredit: Boolean get() = tipo == AccountType.CREDIT
}

fun CardBackground.solidOrFallback(): Color = when (this) {
    is CardBackground.Solid -> color
    is CardBackground.Gradient -> FiniaColors.GoldCardGradientEnd
}

fun defaultEmojiFor(type: AccountType): String = when (type) {
    AccountType.CASH -> "💵"
    AccountType.SAVINGS -> "🐷"
    AccountType.DEBIT -> "💳"
    AccountType.CREDIT -> "🪙"
}

val NewAccountEmojiOptions = listOf("🏦", "💵", "🐷", "💳", "🪙", "🎯", "✈️", "🛍️")
val AccountEmojiOptions = listOf("💵", "🐷", "💳", "🏦", "🪙", "💰", "🎯", "📱")
val NewCategoryEmojiOptions = listOf("🏷️", "🍔", "🚗", "🏠", "💼", "🎉", "❤️", "🎓", "✈️", "🎮", "🛒", "☕")

val PastelIconBg = FiniaColors.PastelBlue
