package com.devmastercrack.finia.presentation.finia.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.components.SheetHandle
import com.devmastercrack.finia.presentation.finia.util.MESES
import java.time.YearMonth

/**
 * Month/year picker, adapted to Finia's own bottom-sheet/green-accent language instead of the
 * purple centered-dialog reference (year header with prev/next arrows + a 6x2 month grid +
 * "Mes actual" quick-jump).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearPickerDialog(
    selectedYearMonth: YearMonth,
    currentYearMonth: YearMonth,
    onCancel: () -> Unit,
    onSelect: (YearMonth) -> Unit,
) {
    var displayedYear by remember { mutableIntStateOf(selectedYearMonth.year) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), contentAlignment = Alignment.Center) { SheetHandle() }

            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { displayedYear-- }) {
                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Año anterior", tint = FiniaColors.TextPrimary)
                }
                Text("$displayedYear", style = FiniaText.SheetTitle, color = FiniaColors.TextPrimary)
                val nextYearEnabled = displayedYear < currentYearMonth.year
                IconButton(onClick = { displayedYear++ }, enabled = nextYearEnabled) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight, contentDescription = "Año siguiente",
                        tint = if (nextYearEnabled) FiniaColors.TextPrimary else Color(0xFFC9C6BC),
                    )
                }
            }

            MESES.chunked(6).forEach { rowMonths ->
                Row(Modifier.fillMaxWidth()) {
                    rowMonths.forEach { monthAbbrev ->
                        val monthIndex = MESES.indexOf(monthAbbrev)
                        val candidate = YearMonth.of(displayedYear, monthIndex + 1)
                        val isFuture = candidate.isAfter(currentYearMonth)
                        val isSelected = candidate == selectedYearMonth
                        Box(
                            Modifier
                                .weight(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) FiniaColors.AccentSoft else Color.Transparent)
                                .clickable(enabled = !isFuture) { onSelect(candidate) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                monthAbbrev.uppercase(),
                                style = FiniaText.LabelSmall,
                                color = when {
                                    isSelected -> FiniaColors.Accent
                                    isFuture -> Color(0xFFC9C6BC)
                                    else -> FiniaColors.TextPrimary
                                },
                            )
                        }
                    }
                }
            }

            // Homologated with CategoryPickerSheet's Cancelar/Crear: the confirming action gets
            // a filled button instead of matching Cancelar's plain-text weight.
            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    "Cancelar", style = FiniaText.RowTitleSemibold.copy(fontSize = 13.sp), color = FiniaColors.TextSecondary,
                    modifier = Modifier.clickable(onClick = onCancel).padding(horizontal = 10.dp, vertical = 10.dp),
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
                androidx.compose.material3.Button(
                    onClick = { onSelect(currentYearMonth) },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = FiniaColors.Accent),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(40.dp),
                ) {
                    Text("Mes actual", style = FiniaText.ButtonSmall, color = Color.White)
                }
            }
        }
    }
}
