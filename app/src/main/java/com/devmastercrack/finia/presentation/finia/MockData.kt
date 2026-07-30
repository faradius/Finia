package com.devmastercrack.finia.presentation.finia

import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.presentation.finia.model.Account
import com.devmastercrack.finia.presentation.finia.model.AccountType
import com.devmastercrack.finia.presentation.finia.model.CardBackground
import com.devmastercrack.finia.presentation.finia.model.CardNetwork
import com.devmastercrack.finia.presentation.finia.model.ChatMessage
import com.devmastercrack.finia.presentation.finia.model.ChatMessageType
import com.devmastercrack.finia.presentation.finia.model.ChatSender
import com.devmastercrack.finia.presentation.finia.model.Debt
import com.devmastercrack.finia.presentation.finia.model.DebtDirection
import com.devmastercrack.finia.presentation.finia.model.NotificationItem
import com.devmastercrack.finia.presentation.finia.model.RecurringPayment
import com.devmastercrack.finia.presentation.finia.model.Transaction

/** Seed data mirroring the mock `state` block in Finia.dc.html verbatim. */
object MockData {

    val accounts = listOf(
        Account(
            id = "wallet", nombre = "Billetera", tipo = AccountType.CASH, emoji = "💵",
            saldo = 850.0, saldoInicial = 1200.0,
            iconBg = FiniaColors.PastelGreen,
            cardBg = CardBackground.Solid(FiniaColors.WalletCardBg), cardFg = FiniaColors.WalletCardFg,
        ),
        Account(
            id = "ahorro", nombre = "Ahorro", tipo = AccountType.SAVINGS, emoji = "🐷",
            saldo = 12400.0, saldoInicial = 10000.0,
            iconBg = FiniaColors.PastelBlue,
            cardBg = CardBackground.Solid(FiniaColors.SavingsCardBg), cardFg = FiniaColors.SavingsCardFg,
        ),
        Account(
            id = "debito", nombre = "Débito", tipo = AccountType.DEBIT, emoji = "💳",
            digitos = "5521", network = CardNetwork.VISA,
            saldo = 3120.0, saldoInicial = 4500.0,
            iconBg = FiniaColors.PastelGold,
            cardBg = CardBackground.Solid(FiniaColors.DebitCardBg), cardFg = FiniaColors.DebitCardFg,
        ),
        Account(
            id = "credito", nombre = "Tarjeta Oro", tipo = AccountType.CREDIT, emoji = "🪙",
            digitos = "4821", network = CardNetwork.MASTERCARD,
            capacidad = 20000.0, deudaTarjeta = 8600.0,
            limite = 7000.0, gastadoCorte = 4200.0,
            saldoInicial = -6000.0, corte = "28 jul", pago = "15 ago",
            iconBg = FiniaColors.PastelGold,
            cardBg = CardBackground.Gradient(FiniaColors.GoldCardBrush), cardFg = FiniaColors.GoldCardFg,
        ),
    )

    val recurring = listOf(
        RecurringPayment("Renta", "🏠", 9000.0, "Mensual", 4),
        RecurringPayment("Netflix", "🎬", 219.0, "Mensual", 9),
        RecurringPayment("Gimnasio", "🏋️", 550.0, "Mensual", 15),
    )

    val recentTx = listOf(
        Transaction(1, "Supermercado", "Comida", "debito", -1450.0, "🍔", "Hoy"),
        Transaction(2, "Café", "Comida", "wallet", -95.0, "☕", "Hoy"),
        Transaction(3, "Uber", "Transporte", "wallet", -120.0, "🚕", "Ayer"),
        Transaction(4, "Cena con Carlos y Sofía", "Comida", "credito", -900.0, "🍽️", "Ayer"),
        Transaction(5, "Netflix", "Servicios", "debito", -219.0, "📶", "Ayer"),
        Transaction(6, "Gasolina", "Transporte", "credito", -800.0, "⛽", "20 jul"),
        Transaction(7, "Cobro a Mateo", "Otros", "wallet", 300.0, "🤝", "20 jul"),
        Transaction(8, "Depósito ahorro", "Otros", "ahorro", 2000.0, "🐷", "18 jul"),
        Transaction(9, "Cine", "Ocio", "debito", -280.0, "🎬", "18 jul"),
    )

    val debts = listOf(
        Debt(1, "Carlos", 450.0, "Cena del sábado", DebtDirection.OWED),
        Debt(2, "Sofía", 300.0, "Renta compartida", DebtDirection.OWED),
        Debt(3, "Mateo", 180.0, "Gasolina viaje", DebtDirection.I_OWE),
    )

    val notifications = listOf(
        NotificationItem(1, "💳", FiniaColors.PastelGold, "Tarjeta Oro al 86% de su límite", "Acércate con cuidado a tu límite de crédito", "2h", "Hoy", unread = true),
        NotificationItem(2, "🏠", FiniaColors.PastelGreen, "La renta vence en 4 días", "$9,000 · Pago fijo mensual", "5h", "Hoy", unread = true),
        NotificationItem(3, "🤝", FiniaColors.PastelPink, "Carlos te debe $450", "Cena del sábado sin cobrar", "1d", "Ayer", unread = false),
    )

    val initialAiMessages = listOf(
        ChatMessage(
            from = ChatSender.AI, type = ChatMessageType.TEXT, time = "11:30",
            text = "Hola Alex 👋 soy FinanIA, tu asesor financiero. Puedo mostrarte tu resumen del mes, próximos pagos o un análisis de tus gastos. ¿Qué revisamos?",
        ),
    )

    const val userName = "Alex Torres"
    const val userEmail = "alex@correo.com"
}
