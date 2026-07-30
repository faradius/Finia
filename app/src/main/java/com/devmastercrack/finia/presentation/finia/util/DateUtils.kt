package com.devmastercrack.finia.presentation.finia.util

import androidx.compose.ui.graphics.Color
import com.devmastercrack.finia.core.theme.FiniaColors
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
val DIAS_SEMANA = listOf("D", "L", "M", "M", "J", "V", "S")
val DIAS_SEMANA_FULL = listOf(
    "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado",
)

fun formatDateLabel(fechaIso: String): String {
    if (fechaIso == MOCK_TODAY_ISO) return "Hoy"
    if (fechaIso == MOCK_YESTERDAY_ISO) return "Ayer"
    val (y, m, d) = fechaIso.split("-").map { it.toInt() }
    return "$d ${MESES_FULL[m - 1]} $y"
}

fun isoDate(year: Int, monthIndex0: Int, day: Int): String =
    "%04d-%02d-%02d".format(year, monthIndex0 + 1, day)

/** Sunday=0..Saturday=6, matching JS `Date#getDay()`. */
fun weekdayIndexSundayFirst(date: LocalDate): Int = date.dayOfWeek.value % 7

data class CalCell(
    val day: Int?,
    val dateIso: String?,
    val selected: Boolean,
    val isToday: Boolean,
)

fun buildCalendarCells(year: Int, monthIndex0: Int, selectedIso: String): List<CalCell> {
    val firstOfMonth = LocalDate.of(year, monthIndex0 + 1, 1)
    val firstWeekday = weekdayIndexSundayFirst(firstOfMonth)
    val daysInMonth = firstOfMonth.lengthOfMonth()
    val cells = mutableListOf<CalCell>()
    repeat(firstWeekday) { cells.add(CalCell(null, null, false, false)) }
    for (d in 1..daysInMonth) {
        val iso = isoDate(year, monthIndex0, d)
        cells.add(CalCell(d, iso, iso == selectedIso, iso == MOCK_TODAY_ISO))
    }
    return cells
}

fun CalCell.bgColor(): Color = if (selected) FiniaColors.Accent else Color.Transparent
fun CalCell.fgColor(): Color = if (selected) Color.White else FiniaColors.TextPrimary

fun calHeaderLabel(selectedIso: String): String {
    val (y, m, d) = selectedIso.split("-").map { it.toInt() }
    val weekday = DIAS_SEMANA_FULL[weekdayIndexSundayFirst(LocalDate.of(y, m, d))]
    return "$weekday, $d de ${MESES[m - 1]}"
}

fun calShortLabel(selectedIso: String): String {
    val (_, m, d) = selectedIso.split("-").map { it.toInt() }
    return "$d ${MESES[m - 1]}"
}
