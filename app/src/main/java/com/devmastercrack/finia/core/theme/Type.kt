package com.devmastercrack.finia.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Roboto is the Android system default sans-serif, matching the design's font family.
private val Roboto = FontFamily.Default

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, letterSpacing = (-0.01).em),
    headlineMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    headlineSmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 21.sp),
    titleLarge = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 17.sp),
    titleMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 10.sp),
)

/**
 * Spec-exact text styles for combos that don't map cleanly onto a single M3 role.
 * The design hands each label an explicit `font:<weight> <px>` shorthand, so we
 * mirror that directly rather than forcing everything through the M3 scale.
 */
object FiniaText {
    val AmountHero = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Black, fontSize = 54.sp)
    val AmountXL = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, letterSpacing = (-0.01).em)
    val AmountLarge = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-0.01).em)
    val AmountMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
    val AmountSmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)

    val ScreenTitle = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 21.sp)
    val ScreenTitleLarge = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 22.sp)
    val SectionTitle = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    val SheetTitle = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 16.sp)

    val RowTitle = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    val RowTitleBold = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    val RowTitleSemibold = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

    val Secondary = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 12.sp)
    val SecondarySmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 11.sp)
    val SecondaryTiny = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 10.sp)
    val Label = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    val LabelSmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
    val LabelTiny = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
    val Overline = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 0.4.sp)

    val Chip = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    val ChipMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    val NavLabel = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 10.sp)
    val Button = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    val ButtonSmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    // Closer to the real Visa wordmark's slant/weight than an upright serif label, without
    // pulling in the actual trademarked artwork — italic + heavy sans-serif is the two traits
    // that read as "Visa" at a glance.
    val VisaMark = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 19.sp, letterSpacing = (-0.3).sp)
}
