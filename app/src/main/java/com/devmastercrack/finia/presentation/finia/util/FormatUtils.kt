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

/**
 * Cleans raw amount-field input: digits and at most one decimal point, capped to 2 decimal
 * digits. Deliberately does NOT insert thousands separators here — the stored value stays a
 * plain parseable number string, and the "$" + comma grouping is applied only for display via
 * [com.devmastercrack.finia.presentation.finia.sheets.CurrencyVisualTransformation]. Reformatting
 * the stored value itself (inserting commas on every keystroke) broke [BasicTextField]'s cursor
 * tracking — the field would type into the wrong position or hide the cursor entirely.
 */
fun cleanAmountInput(raw: String): String {
    val cleaned = raw.filter { it.isDigit() || it == '.' }
    val dotIndex = cleaned.indexOf('.')
    return if (dotIndex == -1) {
        cleaned
    } else {
        val intPart = cleaned.substring(0, dotIndex)
        val decPart = cleaned.substring(dotIndex + 1).filter { it.isDigit() }.take(2)
        "$intPart.$decPart"
    }
}
