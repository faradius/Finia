package com.devmastercrack.finia.presentation.finia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel

private data class MoreMenuItem(
    val emoji: String,
    val iconBg: Color,
    val label: String,
    val sub: String,
    val onClick: () -> Unit,
)

// premium sections are shown (not hidden) so the freemium build still communicates what
// upgrading unlocks, but every row in them opens the "Premium" message instead of its real
// action — see MoreScreen's use of section.premium below.
private data class MoreSection(val title: String, val premium: Boolean = false, val items: List<MoreMenuItem>)

@Composable
fun MoreScreen(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    // "Gestión de tu cuenta" + "Preferencias" are the freemium scope for this build. Everything
    // else (net-new money features / insights) is Premium — visible so it still markets the
    // upgrade, but locked.
    val sections = listOf(
        // Freemium (this build's actual scope) sections first, Premium (visible but locked)
        // sections below them.
        MoreSection(
            "Gestión de tu cuenta",
            items = listOf(
                MoreMenuItem("🏦", FiniaColors.PastelPink, "Gestionar cuentas", "Efectivo, ahorro y otras cuentas", {}),
                MoreMenuItem("💳", FiniaColors.PastelNeutral, "Gestionar tarjetas", "Tarjetas de débito y crédito", {}),
                MoreMenuItem("🗂️", FiniaColors.PastelBlue, "Gestionar categorías", "Crea y organiza tus categorías", {}),
                MoreMenuItem("🏷️", FiniaColors.PastelGold, "Gestionar etiquetas", "Organiza tus gastos con etiquetas", {}),
                MoreMenuItem("🔁", FiniaColors.PastelGreen, "Gestionar gastos fijos", "Renta, servicios, suscripciones", {}),
                MoreMenuItem("📝", FiniaColors.PastelGreen, "Listas de gastos", "Solo o compartidas con amigos", {}),
            ),
        ),
        MoreSection(
            "Finanzas en grupo", premium = true,
            items = listOf(
                MoreMenuItem("🤝", FiniaColors.PastelPink, "Gastos compartidos", "Divide cuentas con amigos", vm::goDebts),
                MoreMenuItem("🏢", FiniaColors.PastelGold, "Control de tesorería", "Lleva el control de cobros de tu grupo", {}),
                MoreMenuItem("👥", FiniaColors.PastelPink, "Tandas", "Organiza el ahorro rotativo de tu grupo", {}),
                MoreMenuItem("📒", FiniaColors.PastelNeutral, "Control de préstamos", "Registra el dinero que prestas a otros", {}),
            ),
        ),
        MoreSection(
            "Ahorro e inversión", premium = true,
            items = listOf(
                MoreMenuItem("🎯", FiniaColors.PastelGold, "Metas", "Ahorra para tus objetivos", {}),
                MoreMenuItem("📈", FiniaColors.PastelGreen, "Inversiones", "Da seguimiento a tus inversiones", {}),
                MoreMenuItem("💰", FiniaColors.PastelPink, "Presupuestos", "Limita cuánto gastas por categoría", {}),
            ),
        ),
        MoreSection(
            "Informes y planificación", premium = true,
            items = listOf(
                MoreMenuItem("🗓️", FiniaColors.PastelBlue, "Planificación de gastos", "Visualiza tus próximos gastos", {}),
                MoreMenuItem("📊", FiniaColors.PastelGreen, "Balance mensual", "Resumen de ingresos y gastos", {}),
                MoreMenuItem("🥧", FiniaColors.PastelPink, "Gráficos", "Tus gastos por categoría y en el tiempo", {}),
                MoreMenuItem("📥", FiniaColors.PastelNeutral, "Importar datos", "Desde tu banco o un archivo", {}),
                MoreMenuItem("📤", FiniaColors.PastelBlue, "Exportar informes", "Descarga tus movimientos", {}),
            ),
        ),
    )

    // "Más opciones" stays fixed at the top — same fixed-header pattern as Home/Accounts —
    // only the section list below it scrolls, in its own Column with weight(1f).
    Column(modifier.fillMaxSize()) {
        Text(
            "Más opciones", style = FiniaText.ScreenTitle, color = FiniaColors.TextPrimary,
            modifier = Modifier.padding(top = 18.dp, start = 20.dp, end = 20.dp, bottom = 4.dp),
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
        sections.forEachIndexed { sectionIndex, section ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(
                    top = if (sectionIndex == 0) 12.dp else 22.dp,
                    bottom = 8.dp, start = 20.dp, end = 20.dp,
                ),
            ) {
                Text(section.title, style = FiniaText.SectionTitle, color = FiniaColors.TextSecondary)
                if (section.premium) PremiumTag()
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp, end = 20.dp,
                        bottom = if (sectionIndex == sections.lastIndex) 100.dp else 0.dp,
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, FiniaColors.BorderSubtle, RoundedCornerShape(20.dp)),
            ) {
                section.items.forEachIndexed { index, item ->
                    if (index > 0) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(FiniaColors.BorderSubtle2))
                    }
                    // Locked rows still respond to a tap — with the "upgrade to Premium"
                    // message — rather than doing nothing, which would just look broken.
                    MoreRow(item, locked = section.premium, onClick = if (section.premium) vm::showPremiumLocked else item.onClick)
                }
            }
        }
        }
    }
}

@Composable
private fun PremiumTag() {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FiniaColors.PastelGold)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text("👑 Premium", style = FiniaText.LabelTiny, color = Color(0xFF9C7326))
    }
}

@Composable
private fun MoreRow(item: MoreMenuItem, locked: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .alpha(if (locked) 0.55f else 1f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(CircleShape).background(item.iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(item.emoji, fontSize = 16.sp)
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(item.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FiniaColors.TextPrimary)
            Text(item.sub, style = FiniaText.Secondary, color = FiniaColors.TextSecondary, modifier = Modifier.padding(top = 1.dp))
        }
        if (locked) {
            Icon(Icons.Filled.Lock, contentDescription = "Función Premium", tint = Color(0xFFC3C1B9), modifier = Modifier.size(18.dp))
        } else {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null,
                tint = Color(0xFFC3C1B9), modifier = Modifier.size(20.dp),
            )
        }
    }
}
