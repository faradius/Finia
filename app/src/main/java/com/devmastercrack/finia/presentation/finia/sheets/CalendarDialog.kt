package com.devmastercrack.finia.presentation.finia.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.components.ScrimDialogHost
import com.devmastercrack.finia.presentation.finia.util.DIAS_SEMANA
import com.devmastercrack.finia.presentation.finia.util.MESES_FULL
import com.devmastercrack.finia.presentation.finia.util.bgColor
import com.devmastercrack.finia.presentation.finia.util.buildCalendarCells
import com.devmastercrack.finia.presentation.finia.util.fgColor

@Composable
fun CalendarDialog(
    year: Int,
    month: Int,
    selectedIso: String,
    headerLabel: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDay: (String) -> Unit,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cells = remember(year, month, selectedIso) { buildCalendarCells(year, month, selectedIso) }
    ScrimDialogHost(onDismiss = onCancel, modifier = modifier) {
        Column(
            Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White),
        ) {
            Column(Modifier.fillMaxWidth().background(FiniaColors.Accent).padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text(year.toString(), style = FiniaText.Label.copy(fontSize = 12.sp), color = Color.White.copy(alpha = 0.85f))
                Text(headerLabel, style = FiniaText.RowTitleBold.copy(fontSize = 22.sp), color = Color.White, modifier = Modifier.padding(top = 2.dp))
            }
            Column(Modifier.padding(top = 14.dp, start = 16.dp, end = 16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${MESES_FULL[month]} de $year", style = FiniaText.RowTitleBold)
                    Row {
                        Icon(
                            Icons.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior", tint = Color(0xFF5F6359),
                            modifier = Modifier.size(20.dp).clickable(onClick = onPrevMonth),
                        )
                        Icon(
                            Icons.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente", tint = Color(0xFF5F6359),
                            modifier = Modifier.size(20.dp).clickable(onClick = onNextMonth),
                        )
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    DIAS_SEMANA.forEach { wd ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(wd, style = FiniaText.LabelSmall.copy(fontSize = 11.sp), color = FiniaColors.TextSecondary)
                        }
                    }
                }
                cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { cell ->
                            Box(Modifier.weight(1f).height(36.dp), contentAlignment = Alignment.Center) {
                                if (cell.day != null) {
                                    Box(
                                        Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(cell.bgColor())
                                            .then(
                                                if (cell.isToday && !cell.selected) {
                                                    Modifier.border(1.5.dp, FiniaColors.Accent, CircleShape)
                                                } else Modifier,
                                            )
                                            .clickable { onSelectDay(cell.dateIso!!) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(cell.day.toString(), style = FiniaText.LabelSmall.copy(fontSize = 13.sp), color = cell.fgColor())
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    "Cancelar", style = FiniaText.ButtonSmall, color = Color(0xFF5F6359),
                    modifier = Modifier.clickable(onClick = onCancel).padding(horizontal = 8.dp, vertical = 6.dp),
                )
                Text(
                    "Aceptar", style = FiniaText.ButtonSmall, color = FiniaColors.Accent,
                    modifier = Modifier.clickable(onClick = onAccept).padding(horizontal = 8.dp, vertical = 6.dp).padding(start = 8.dp),
                )
            }
        }
    }
}
