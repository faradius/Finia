package com.devmastercrack.finia.presentation.finia.model

enum class TxFlow { GASTO, INGRESO }

data class TransactionForm(
    val monto: String = "",
    val tipo: TxFlow = TxFlow.GASTO,
    val categoria: String = "Comida",
    val cuenta: String = "Débito",
    val nota: String = "",
    val notaLarga: String = "",
    val cuotas: Int = 3,
    val fecha: String = "2026-07-26",
    val hora: String = "12:00",
    val etiquetas: String = "",
    val ubicacion: String = "",
    val persona: String = "",
    val proyecto: String = "",
    val metodoPago: String = "Tarjeta",
    val cuentaDestino: String = "",
)
