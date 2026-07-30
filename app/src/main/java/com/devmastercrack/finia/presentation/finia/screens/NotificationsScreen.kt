package com.devmastercrack.finia.presentation.finia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.components.BackButton
import com.devmastercrack.finia.presentation.finia.components.CircleIconButton
import com.devmastercrack.finia.presentation.finia.model.NotificationItem

private val DiaPriority = listOf("Hoy", "Ayer", "Esta semana")
private val UnreadCardBg = Color(0xFFF2F7F4)

@Composable
fun NotificationsScreen(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val hasUnread = state.notifItems.any { it.unread }
    val groups = remember(state.notifItems) {
        state.notifItems
            .map { it.dia }
            .distinct()
            .sortedBy { dia -> DiaPriority.indexOf(dia).let { if (it < 0) Int.MAX_VALUE else it } }
            .map { dia -> dia to state.notifItems.filter { it.dia == dia } }
    }

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(top = 14.dp, start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(vm::goBackFromNotifications)
            Box(Modifier.weight(1f))
            if (hasUnread) {
                CircleIconButton(onClick = vm::markAllNotifsRead, bg = Color.Transparent, contentColor = FiniaColors.TextMuted) {
                    Icon(Icons.Filled.Check, contentDescription = "Marcar todas como leídas", modifier = Modifier.size(18.dp))
                }
            }
        }
        Text(
            "Notificaciones", style = FiniaText.ScreenTitleLarge, color = FiniaColors.TextPrimary,
            modifier = Modifier.padding(top = 2.dp, start = 20.dp, end = 20.dp, bottom = 16.dp),
        )

        if (state.notifItems.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(FiniaColors.SurfaceNeutral2),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🔔", fontSize = 30.sp)
                }
                Text(
                    "Sin notificaciones por ahora", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = FiniaColors.TextPrimary, modifier = Modifier.padding(top = 14.dp),
                )
                Text(
                    "Te avisaremos cuando haya algo nuevo", style = FiniaText.Secondary.copy(fontSize = 13.sp),
                    color = FiniaColors.TextSecondary, modifier = Modifier.padding(top = 4.dp),
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                groups.forEach { (dia, items) ->
                    item {
                        Text(
                            dia, style = FiniaText.Overline, color = FiniaColors.TextSecondary,
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
                        )
                    }
                    items(items, key = { it.id }) { notif ->
                        NotificationRow(notif, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                    }
                }
                item { Box(Modifier.size(1.dp, 24.dp)) }
            }
        }
    }
}

@Composable
private fun NotificationRow(notif: NotificationItem, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (notif.unread) UnreadCardBg else Color.White)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(notif.iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(notif.emoji, fontSize = 16.sp)
            }
            if (notif.unread) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(1.dp)
                        .clip(CircleShape)
                        .background(FiniaColors.Danger),
                )
            }
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                notif.titulo, fontSize = 13.sp,
                fontWeight = if (notif.unread) FontWeight.Bold else FontWeight.Medium,
                color = FiniaColors.TextPrimary,
            )
            Text(
                notif.detalle, style = FiniaText.Secondary, color = FiniaColors.TextSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text(notif.tiempo, style = FiniaText.SecondarySmall, color = FiniaColors.TextSecondary, modifier = Modifier.padding(start = 8.dp))
    }
}
