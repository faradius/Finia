package com.devmastercrack.finia.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Finia design tokens, taken verbatim from the design handoff
 * (design_handoff_finia_app/README.md + Finia.dc.html).
 */
object FiniaColors {
    // Brand accent
    val Accent = Color(0xFF1B6C4B)
    val AccentSoft = Color(0xFFE4F0E8)

    // Alert / expense
    val Danger = Color(0xFFC0392B)
    val DangerSoft = Color(0xFFFBE4E0)

    // Surfaces
    val ScreenBg = Color(0xFFFAF9F6)
    val White = Color(0xFFFFFFFF)
    val SurfaceNeutral = Color(0xFFF6F5F2)
    val SurfaceNeutral2 = Color(0xFFF1F0EE)

    // Text
    val TextPrimary = Color(0xFF1A1C19)
    val TextSecondary = Color(0xFF8A8D84)
    val TextMuted = Color(0xFF5F6359)
    val TextFaint = Color(0xFFB0AEA8)

    // Borders
    val BorderSubtle = Color(0xFFE6E4DD)
    val BorderSubtle2 = Color(0xFFECEAE3)
    val BorderDashed = Color(0xFFC9C6BC)
    val BorderDisabled = Color(0xFFC3C1B9)
    val SegmentedTrack = Color(0xFFECEAE3)
    val ToggleTrackOff = Color(0xFFDCDAD3)

    // Account cards
    val WalletCardBg = Color(0xFF153F37)
    val WalletCardFg = Color(0xFFEEF6F1)
    val SavingsCardBg = Color(0xFF2B3D63)
    val SavingsCardFg = Color(0xFFEEF1FA)
    val DebitCardBg = Color(0xFF3D3A33)
    val DebitCardFg = Color(0xFFF7F4EC)
    val GoldCardGradientStart = Color(0xFFE8C874)
    val GoldCardGradientEnd = Color(0xFF9C7326)
    val GoldCardFg = Color(0xFF3A2A08)
    val GoldCardBrush = Brush.linearGradient(listOf(GoldCardGradientStart, GoldCardGradientEnd))

    // Credit usage thresholds (progress bar / accent color)
    val CreditPctBase = Color(0xFFC99A1F)
    val CreditPct80 = Color(0xFFA56B1F)
    val CreditPct90 = Color(0xFF8B5E11)
    val CreditPct100 = Color(0xFF7A3D10)

    // Card network marks
    val MastercardRed = Color(0xFFEB001B)
    val MastercardOrange = Color(0xFFF79E1B)

    // Pastel icon backgrounds
    val PastelGreen = Color(0xFFE4F0E8)
    val PastelGold = Color(0xFFF6E6C8)
    val PastelPink = Color(0xFFF4D7D3)
    val PastelBlue = Color(0xFFE0EAF2)
    val PastelNeutral = Color(0xFFEEF1EC)

    // Assistant (FinanIA) — distinct Material green from the main brand accent
    val AiGreen = Color(0xFF2E7D32)
    val AiGreenSoft = Color(0xFFE8F2E9)
    val AiUserBubble = Color(0xFFECEAE3)
    val AiSuggestionBorder = Color(0xFFD7E8D9)
    val AiSuggestionBg = Color(0xFFF1F8F1)

    // Misc
    val SnackbarBg = Color(0xFF1A1C19)
    val LowConfidenceWarn = Color(0xFFC99A1F)
    val CategoryPopBg = Color(0xFFF1F0EE)

    fun creditPctColor(pct: Int): Color = when {
        pct >= 100 -> CreditPct100
        pct >= 90 -> CreditPct90
        pct >= 80 -> CreditPct80
        else -> CreditPctBase
    }
}

val NewAccountSwatches = listOf(
    Color(0xFF1B6C4B),
    Color(0xFF2F6690),
    Color(0xFFA4453A),
    Color(0xFFB8863D),
    Color(0xFF5C5AA6),
    Color(0xFF3D8F7A),
    Color(0xFFC0577A),
)
