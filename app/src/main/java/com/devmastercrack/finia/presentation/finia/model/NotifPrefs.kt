package com.devmastercrack.finia.presentation.finia.model

data class NotifPrefs(
    val pago: Boolean = true,
    val cobrar: Boolean = true,
    val limite: Boolean = true,
    val resumen: Boolean = false,
)
