package com.devmastercrack.finia.presentation.finia.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

private val esMx: Locale = Locale.Builder().setLanguage("es").setRegion("MX").build()

/** Mirrors the prototype's `fmt(n)`: signed, rounded, thousands-grouped `$` amount. */
fun fmt(n: Double): String {
    val sign = if (n < 0) "-" else ""
    val rounded = round(abs(n)).toLong()
    return sign + "$" + NumberFormat.getNumberInstance(esMx).format(rounded)
}

fun fmt(n: Int): String = fmt(n.toDouble())

/** Mirrors the prototype's `num(s)`: parses a comma-grouped numeric string, defaulting to 0. */
fun num(s: String?): Double = s?.replace(",", "")?.toDoubleOrNull() ?: 0.0

fun formatThousands(n: Double): String =
    if (n == 0.0) "" else NumberFormat.getNumberInstance(esMx).format(n)
