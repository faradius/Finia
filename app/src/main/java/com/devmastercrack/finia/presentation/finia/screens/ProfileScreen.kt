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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.MockData
import com.devmastercrack.finia.presentation.finia.components.BackButton

private data class ProfileMenuItem(
    val emoji: String,
    val iconBg: Color,
    val title: String,
    val subtitle: String,
    val titleColor: Color = FiniaColors.TextPrimary,
    val onClick: () -> Unit,
)

@Composable
fun ProfileScreen(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    // Only the back button + title row stay fixed — same fixed-header pattern as Home/Accounts/
    // Más opciones — everything else, including the identity/plan card, scrolls in its own
    // Column with weight(1f).
    Column(modifier.fillMaxSize()) {
        // M3 small top app bar: nav icon and title share the same row, not stacked — the
        // avatar row below shows the *user's* name, which isn't the same thing as the screen's
        // title.
        Row(
            Modifier.fillMaxWidth().padding(top = 14.dp, start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(vm::goBackFromProfile)
            Text(
                "Mi perfil", style = FiniaText.ScreenTitle, color = FiniaColors.TextPrimary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        // Only the back arrow + "Mi perfil" title stay fixed now — the identity/plan card
        // scrolls away with everything else instead of being pinned above it.
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
        // One unified card instead of two separate floating pieces (an identity row + a
        // detached plan row) — identity and plan status are two facts about the same thing
        // (this account), so they live inside one card now, split by a divider, matching the
        // exact white/border/rounded language the Cuenta/Preferencias groups use below.
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 20.dp, end = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, FiniaColors.BorderSubtle2, RoundedCornerShape(20.dp)),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(FiniaColors.AccentSoft, Color(0xFFCFE6D4)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = FiniaColors.Accent, modifier = Modifier.size(24.dp))
                    }
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(FiniaColors.Accent)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable {},
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Editar foto", tint = Color.White, modifier = Modifier.size(9.dp))
                    }
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        MockData.userName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FiniaColors.TextPrimary,
                        maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text(
                        MockData.userEmail, style = FiniaText.Secondary.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary,
                        maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(top = 1.dp),
                    )
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(FiniaColors.BorderSubtle2))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {}
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("👑", fontSize = 15.sp)
                Text(
                    "Plan Free", style = FiniaText.RowTitleSemibold.copy(fontSize = 14.sp), color = FiniaColors.TextPrimary,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FiniaColors.AccentSoft)
                        .clickable {}
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text("Mejorar", style = FiniaText.LabelSmall, color = FiniaColors.Accent)
                }
            }
        }
        ProfileGroup(
            label = "Cuenta",
            items = listOf(
                ProfileMenuItem("👤", FiniaColors.PastelGreen, "Perfil", "Ver/editar datos personales", onClick = {}),
                ProfileMenuItem("⭐", FiniaColors.PastelGold, "Suscripción", "Ver y gestionar tu plan", onClick = {}),
            ),
        )
        ProfileGroup(
            label = "Preferencias",
            items = listOf(
                ProfileMenuItem("⚙️", FiniaColors.PastelBlue, "Configuraciones", "Ajustes generales de la app", onClick = vm::goSettings),
                ProfileMenuItem("🔔", FiniaColors.PastelGold, "Gestionar notificaciones", "Configurar alertas y recordatorios", onClick = vm::goNotifSettings),
                ProfileMenuItem("💱", FiniaColors.PastelGreen, "Moneda", "Elige la moneda de tus movimientos", onClick = {}),
                ProfileMenuItem("🌐", FiniaColors.PastelBlue, "Idioma", "Del texto, el micrófono y el asistente", onClick = {}),
            ),
        )
        ProfileGroup(
            label = "Acerca de",
            items = listOf(
                ProfileMenuItem("❓", FiniaColors.PastelBlue, "Centro de ayuda", "¿Tienes dudas? Contáctanos", onClick = {}),
                ProfileMenuItem("⭐", FiniaColors.PastelGold, "Califica la app", "Cuéntanos qué te parece", onClick = {}),
                ProfileMenuItem("✉️", FiniaColors.PastelGreen, "Reportar un problema", "Ayúdanos a mejorar", onClick = {}),
                ProfileMenuItem("🛡️", FiniaColors.PastelNeutral, "Términos y privacidad", "Legal y política de privacidad", onClick = {}),
            ),
        )
        ProfileGroup(
            label = "Sesión",
            items = listOf(
                ProfileMenuItem("🚪", FiniaColors.DangerSoft, "Cerrar sesión", "Salir de tu cuenta", titleColor = FiniaColors.Danger, onClick = {}),
            ),
        )

        Text(
            "v0.0.1 (1)", style = FiniaText.SecondarySmall, color = FiniaColors.TextFaint,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Box(Modifier.size(1.dp, 32.dp))
        }
    }
}

@Composable
private fun ProfileGroup(label: String, items: List<ProfileMenuItem>) {
    Column(Modifier.fillMaxWidth().padding(top = 24.dp, start = 20.dp, end = 20.dp)) {
        Text(label, style = FiniaText.Overline, color = FiniaColors.TextSecondary, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, FiniaColors.BorderSubtle2, RoundedCornerShape(20.dp)),
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(FiniaColors.BorderSubtle2))
                }
                ProfileRow(item)
            }
        }
    }
}

@Composable
private fun ProfileRow(item: ProfileMenuItem) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(CircleShape).background(item.iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(item.emoji, fontSize = 16.sp)
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = item.titleColor)
            Text(item.subtitle, style = FiniaText.Secondary, color = FiniaColors.TextSecondary, modifier = Modifier.padding(top = 1.dp))
        }
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null,
            tint = Color(0xFFC3C1B9), modifier = Modifier.size(20.dp),
        )
    }
}
