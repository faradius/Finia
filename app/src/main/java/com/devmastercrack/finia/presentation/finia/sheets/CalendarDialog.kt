package com.devmastercrack.finia.presentation.finia.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.components.SheetHandle
import com.devmastercrack.finia.presentation.finia.util.MOCK_TODAY
import java.time.LocalDate
import kotlinx.coroutines.launch

private val WeekdayLabels = listOf("L", "M", "M", "J", "V", "S", "D")
private val MonthNames = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
)
private val WeekdayNamesFull = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

/**
 * Custom calendar grid presented as a bottom sheet — same shell (drag handle, rounded top
 * corners, native swipe/scrim dismiss) as the rest of the app's picker sheets, rather than the
 * centered stock Material 3 [androidx.compose.material3.DatePickerDialog], which reads as a
 * foreign dialog style next to everything else in the flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDialog(
    selectedIso: String,
    onCancel: () -> Unit,
    onAccept: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialDate = remember(selectedIso) { LocalDate.parse(selectedIso) }
    var selected by remember(selectedIso) { mutableStateOf(initialDate) }
    var displayedMonth by remember(selectedIso) { mutableStateOf(initialDate.withDayOfMonth(1)) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val confirm: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible) onAccept(selected.toString()) }
    }

    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), contentAlignment = Alignment.Center) { SheetHandle() }
            Text("Fecha", style = FiniaText.SheetTitle, color = FiniaColors.TextPrimary)
            Text(
                "${WeekdayNamesFull[selected.dayOfWeek.value - 1]}, ${selected.dayOfMonth} de ${MonthNames[selected.monthValue - 1].lowercase()}",
                style = FiniaText.Secondary, color = FiniaColors.TextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(FiniaColors.BorderSubtle2))

            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.IconButton(
                    onClick = { displayedMonth = displayedMonth.minusMonths(1) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior", tint = FiniaColors.TextSecondary, modifier = Modifier.size(22.dp))
                }
                Text(
                    "${MonthNames[displayedMonth.monthValue - 1]} ${displayedMonth.year}",
                    style = FiniaText.RowTitleBold, color = FiniaColors.TextPrimary,
                )
                androidx.compose.material3.IconButton(
                    onClick = { displayedMonth = displayedMonth.plusMonths(1) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente", tint = FiniaColors.TextSecondary, modifier = Modifier.size(22.dp))
                }
            }

            Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                WeekdayLabels.forEach { wd ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(wd, style = FiniaText.LabelSmall, color = FiniaColors.TextFaint)
                    }
                }
            }

            val firstWeekdayIndex = displayedMonth.dayOfWeek.value - 1
            val daysInMonth = displayedMonth.lengthOfMonth()
            val cells = buildList {
                repeat(firstWeekdayIndex) { add(null) }
                for (d in 1..daysInMonth) add(d)
            }
            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    week.forEach { day ->
                        Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                            if (day != null) {
                                val date = displayedMonth.withDayOfMonth(day)
                                val isSelected = date == selected
                                val isToday = date == MOCK_TODAY
                                // Real M3 day-selection affordance: a selectable Surface gives the
                                // native ripple + ripple-through-selection semantics, and the fill
                                // color animates instead of snapping between states.
                                val containerColor by androidx.compose.animation.animateColorAsState(
                                    targetValue = if (isSelected) FiniaColors.Accent else Color.Transparent,
                                    animationSpec = androidx.compose.animation.core.tween(200),
                                    label = "dayContainerColor",
                                )
                                val contentColor by androidx.compose.animation.animateColorAsState(
                                    targetValue = if (isSelected) Color.White else FiniaColors.TextPrimary,
                                    animationSpec = androidx.compose.animation.core.tween(200),
                                    label = "dayContentColor",
                                )
                                androidx.compose.material3.Surface(
                                    selected = isSelected,
                                    onClick = { selected = date },
                                    modifier = Modifier.size(34.dp),
                                    shape = CircleShape,
                                    color = containerColor,
                                    border = if (isToday && !isSelected) {
                                        androidx.compose.foundation.BorderStroke(1.dp, FiniaColors.Accent)
                                    } else null,
                                ) {
                                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(day.toString(), style = FiniaText.RowTitle.copy(fontSize = 14.sp), color = contentColor)
                                    }
                                }
                            }
                        }
                    }
                    repeat(7 - week.size) { Box(Modifier.weight(1f)) }
                }
            }

            androidx.compose.material3.Button(
                onClick = confirm,
                colors = ButtonDefaults.buttonColors(containerColor = FiniaColors.Accent),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 4.dp).height(52.dp),
            ) {
                Text("Guardar", style = FiniaText.Button.copy(fontSize = 15.sp), color = Color.White)
            }
        }
    }
}
