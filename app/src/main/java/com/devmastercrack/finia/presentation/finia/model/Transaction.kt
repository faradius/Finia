package com.devmastercrack.finia.presentation.finia.model

/**
 * [fecha] is a pre-formatted display label ("Hoy" / "Ayer" / "20 jul"), matching the
 * mock dataset in Finia.dc.html — recent transactions are seeded with display strings,
 * not ISO dates.
 */
data class Transaction(
    val id: Int,
    val concepto: String,
    val categoria: String,
    val cuentaId: String,
    val monto: Double,
    val emoji: String,
    val fecha: String,
) {
    val isGasto: Boolean get() = monto < 0
}
