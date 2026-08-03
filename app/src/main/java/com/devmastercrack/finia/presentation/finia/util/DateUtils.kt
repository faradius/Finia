package com.devmastercrack.finia.presentation.finia.util

import java.time.LocalDate

/**
 * The whole prototype is frozen at a fixed mock "today" so the seeded transactions'
 * "Hoy"/"Ayer" labels stay consistent — mirrors `TODAY = '2026-07-26'` in Finia.dc.html.
 */
val MOCK_TODAY: LocalDate = LocalDate.of(2026, 7, 26)
const val MOCK_TODAY_ISO = "2026-07-26"
const val MOCK_YESTERDAY_ISO = "2026-07-25"

val MESES = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
val MESES_FULL = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
)
fun formatDateLabel(fechaIso: String): String {
    if (fechaIso == MOCK_TODAY_ISO) return "Hoy"
    if (fechaIso == MOCK_YESTERDAY_ISO) return "Ayer"
    val (y, m, d) = fechaIso.split("-").map { it.toInt() }
    return "$d ${MESES_FULL[m - 1]} $y"
}

fun calShortLabel(selectedIso: String): String {
    val (_, m, d) = selectedIso.split("-").map { it.toInt() }
    return "$d ${MESES[m - 1]}"
}
