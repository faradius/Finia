package com.devmastercrack.finia.presentation.finia.model

enum class DebtDirection { OWED, I_OWE }

data class Debt(
    val id: Int,
    val nombre: String,
    val monto: Double,
    val detalle: String,
    val direction: DebtDirection,
)
