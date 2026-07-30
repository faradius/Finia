package com.devmastercrack.finia.presentation.finia.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.components.NoScrimSheetHost
import com.devmastercrack.finia.presentation.finia.components.ScrimSheetHost
import com.devmastercrack.finia.presentation.finia.components.SheetHandle
import com.devmastercrack.finia.presentation.finia.model.DefaultCategories
import com.devmastercrack.finia.presentation.finia.model.QuickBtnMode
import com.devmastercrack.finia.presentation.finia.model.TxFlow
import com.devmastercrack.finia.presentation.finia.util.formatDateLabel

@Composable
fun AddTransactionSheet(state: FiniaUiState, vm: FiniaViewModel, noScrim: Boolean, modifier: Modifier = Modifier) {
    if (noScrim) {
        NoScrimSheetHost(onDismiss = vm::closeAdd, modifier = modifier) { AddTransactionContent(state, vm) }
    } else {
        ScrimSheetHost(onDismiss = vm::closeAdd, modifier = modifier) { AddTransactionContent(state, vm) }
    }
}

@Composable
private fun AddTransactionContent(state: FiniaUiState, vm: FiniaViewModel) {
    val isGasto = state.form.tipo != TxFlow.INGRESO
    val montoColor = if (isGasto) FiniaColors.Danger else FiniaColors.Accent
    val allCats = DefaultCategories + state.customCategories
    val categoryEmoji = allCats.find { it.nombre == state.form.categoria }?.emoji ?: "💳"
    val accountEmoji = state.accounts.find { it.nombre == state.form.cuenta }?.emoji ?: "💳"
    val pickerNone = !state.categoryPickerOpen && !state.accountPickerOpen && !state.datePickerOpen
    val isLowConfidence = state.categoryOrigin == com.devmastercrack.finia.presentation.finia.model.CategoryOrigin.AUTO &&
        state.categoryConfidence == com.devmastercrack.finia.presentation.finia.model.CategoryConfidence.LOW

    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { SheetHandle() }
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(FiniaColors.SurfaceNeutral2).clickable(onClick = vm::closeAdd),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Close, contentDescription = "Cerrar", modifier = Modifier.size(15.dp)) }
            Text("Nuevo movimiento", style = FiniaText.SheetTitle, color = FiniaColors.TextPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Box(Modifier.size(36.dp))
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    Modifier.background(FiniaColors.SurfaceNeutral2, RoundedCornerShape(20.dp)).padding(3.dp),
                ) {
                    FlowOption("Gasto", isGasto, FiniaColors.Danger, vm::setTipoGasto)
                    FlowOption("Ingreso", !isGasto, FiniaColors.Accent, vm::setTipoIngreso)
                }
                BasicTextField(
                    value = state.form.monto,
                    onValueChange = vm::onMontoChange,
                    textStyle = TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Black, fontSize = 54.sp, color = montoColor, textAlign = TextAlign.Center),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.Center) {
                            if (state.form.monto.isEmpty()) {
                                Text("$0", style = TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Black, fontSize = 54.sp, color = montoColor.copy(alpha = 0.4f)))
                            }
                            inner()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
                BasicTextField(
                    value = state.form.nota,
                    onValueChange = vm::onNotaChange,
                    textStyle = TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Black, fontSize = 24.sp, color = FiniaColors.TextPrimary, textAlign = TextAlign.Center),
                    singleLine = true,
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.Center) {
                            if (state.form.nota.isEmpty()) {
                                Text("Descripción", style = TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Black, fontSize = 24.sp, color = FiniaColors.TextSecondary))
                            }
                            inner()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }

            if (pickerNone) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, if (isLowConfidence) FiniaColors.LowConfidenceWarn else FiniaColors.BorderSubtle, RoundedCornerShape(16.dp)),
                ) {
                    CompactFieldRow(categoryEmoji, state.form.categoria, onClick = vm::toggleCategoryPicker, showDivider = true)
                    if (isLowConfidence) {
                        Text(
                            "⚠️ ¿Es esto correcto?", style = FiniaText.LabelSmall.copy(fontSize = 11.sp), color = FiniaColors.LowConfidenceWarn,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                        )
                    }
                    CompactFieldRow("📅", formatDateLabel(state.form.fecha), onClick = vm::toggleDatePicker, showDivider = true)
                    CompactFieldRow(accountEmoji, state.form.cuenta, onClick = vm::toggleAccountPicker, showDivider = false)
                    Box(Modifier.fillMaxWidth().height(1.dp).background(FiniaColors.BorderSubtle))
                    Row(
                        Modifier.fillMaxWidth().clickable(onClick = vm::openAdvanced).padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Filled.List, contentDescription = null, tint = Color(0xFF5F6359), modifier = Modifier.size(16.dp))
                        Text("Agregar más detalles", style = FiniaText.RowTitleSemibold.copy(fontSize = 14.sp), color = FiniaColors.TextPrimary)
                    }
                }
            }
        }

        if (pickerNone) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                var swipeAccum by remember { mutableFloatStateOf(0f) }
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(FiniaColors.SurfaceNeutral2)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = { if (kotlin.math.abs(swipeAccum) > 30) vm.toggleQuickBtnMode(); swipeAccum = 0f },
                                onHorizontalDrag = { change, dragAmount -> change.consume(); swipeAccum += dragAmount },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (state.quickBtnMode == QuickBtnMode.MIC) Icons.Outlined.Mic else Icons.Outlined.CameraAlt,
                        contentDescription = if (state.quickBtnMode == QuickBtnMode.MIC) "Registrar por voz" else "Escanear ticket",
                        tint = FiniaColors.TextPrimary, modifier = Modifier.size(20.dp),
                    )
                }
                val submitEnabled = state.form.monto.toDoubleOrNull()?.let { it != 0.0 } ?: false
                androidx.compose.material3.Button(
                    onClick = vm::submitExpense,
                    enabled = submitEnabled,
                    colors = ButtonDefaults.buttonColors(containerColor = montoColor, disabledContainerColor = montoColor.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).height(52.dp),
                ) {
                    Text("Guardar", style = FiniaText.Button.copy(fontSize = 15.sp), color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FlowOption(label: String, selected: Boolean, activeColor: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(17.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
    ) {
        Text(label, style = FiniaText.ChipMedium, color = if (selected) activeColor else Color(0xFF5F6359))
    }
}

@Composable
fun CompactFieldRow(emoji: String, label: String, onClick: () -> Unit, showDivider: Boolean, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(emoji, fontSize = 16.sp)
            Text(label, style = FiniaText.RowTitleSemibold.copy(fontSize = 14.sp), color = FiniaColors.TextPrimary, modifier = Modifier.weight(1f))
            Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = FiniaColors.TextSecondary, modifier = Modifier.size(16.dp))
        }
        if (showDivider) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(FiniaColors.BorderSubtle))
        }
    }
}
