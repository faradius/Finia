package com.devmastercrack.finia.presentation.finia.model

enum class FiniaScreen { HOME, ACCOUNTS, DEBTS, AI, SETTINGS, PROFILE, NOTIFICATIONS, NOTIF_SETTINGS }

enum class AccountTypeFilter { TODO, CUENTAS, TARJETAS }

enum class DebtTab { OWED, IOWE }

/** "Mi tarjeta" (banking capacidad) vs "Mi límite" (user's self-imposed limite). */
enum class CreditPrimaryView { CAPACIDAD, LIMITE }

enum class CardPaymentMode { TOTAL, PARCIAL, ADELANTO }

enum class QuickBtnMode { MIC, CAMERA }

enum class AccCalField { CORTE, PAGO }

enum class CategoryConfidence { HIGH, LOW }

enum class CategoryOrigin { AUTO, MANUAL }

/** Which list "Pendientes y alertas" shows below: tapping the Me deben/Pagos próximos cards
 * swaps between them, same idea as HomeScreen's Ingresos/Gastos flow filter. */
enum class HomeAlertFilter { DEUDAS, PAGOS }
