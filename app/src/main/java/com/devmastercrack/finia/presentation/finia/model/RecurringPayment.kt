package com.devmastercrack.finia.presentation.finia.model

data class RecurringPayment(
    val nombre: String,
    val emoji: String,
    val monto: Double,
    val frecuencia: String,
    val dias: Int,
)
