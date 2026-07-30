package com.devmastercrack.finia.presentation.finia.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.components.SegmentedControl
import com.devmastercrack.finia.presentation.finia.components.SheetHandle
import com.devmastercrack.finia.presentation.finia.components.dashedBorder
import com.devmastercrack.finia.presentation.finia.deriveAccountView
import com.devmastercrack.finia.presentation.finia.model.AccCalField
import com.devmastercrack.finia.presentation.finia.model.AccountType
import com.devmastercrack.finia.presentation.finia.model.CreditPrimaryView
import com.devmastercrack.finia.presentation.finia.model.solidOrFallback
import com.devmastercrack.finia.presentation.finia.util.fmt
import kotlin.math.roundToInt

@Composable
fun AccountDetailSheet(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val acc = state.accounts.find { it.id == state.accountConfigOpen } ?: return
    val view = remember(acc, state.creditPrimaryView) { deriveAccountView(acc, state.creditPrimaryView) }
    val detailTx = remember(state.recentTx, acc.id) { state.recentTx.filter { it.cuentaId == acc.id } }
    val gastosCount = detailTx.count { it.monto < 0 }
    val ingresosCount = detailTx.count { it.monto > 0 }
    val transferCount = detailTx.count { it.categoria == "Transferencia" }
    val accent = if (acc.isCredit) Color(0xFF9C7326) else acc.cardBg.solidOrFallback()

    Box(modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = 230.dp)
                .clickable(indication = null, interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, onClick = vm::closeAccountConfig),
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .offset { IntOffset(0, state.sheetDragY.roundToInt()) }
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .pointerInput(Unit) {
                    var accum = 0f
                    detectVerticalDragGestures(
                        onDragEnd = { vm.onSheetDragEnd() },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            accum += dragAmount
                            vm.onSheetDragBy(accum)
                        },
                    )
                }
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Column(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { SheetHandle() }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    val editBg = if (state.accountEditMode) FiniaColors.AccentSoft else FiniaColors.SurfaceNeutral
                    val editColor = if (state.accountEditMode) FiniaColors.Accent else Color(0xFF5F6359)
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(editBg)
                            .clickable(onClick = vm::toggleAccountEditMode),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (state.accountEditMode) Icons.Filled.Check else Icons.Outlined.Edit,
                            contentDescription = if (state.accountEditMode) "Confirmar" else "Editar cuenta",
                            tint = editColor, modifier = Modifier.size(15.dp),
                        )
                    }
                }

                var swipeAccum by remember { mutableFloatStateOf(0f) }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .pointerInput(state.accountEditMode) {
                            if (state.accountEditMode) return@pointerInput
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (swipeAccum > 40) vm.switchAccountPrev() else if (swipeAccum < -40) vm.switchAccountNext()
                                    swipeAccum = 0f
                                },
                                onHorizontalDrag = { change, dragAmount -> change.consume(); swipeAccum += dragAmount },
                            )
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!state.accountEditMode) {
                        Icon(
                            Icons.Filled.KeyboardArrowLeft, contentDescription = "Cuenta anterior", tint = Color(0xFFC3C1B9),
                            modifier = Modifier.size(26.dp).clickable(onClick = vm::switchAccountPrev),
                        )
                    }
                    if (state.accountEditMode) {
                        Box(
                            Modifier
                                .size(24.dp)
                                .dashedBorder(FiniaColors.BorderDashed, 8.dp)
                                .clickable(onClick = vm::toggleEmojiPicker),
                            contentAlignment = Alignment.Center,
                        ) { Text(acc.emoji, fontSize = 16.sp) }
                    } else {
                        Text(acc.emoji, fontSize = 19.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                    }
                    if (state.accountEditMode) {
                        BasicTextField(
                            value = state.accountConfigName,
                            onValueChange = vm::onAccountConfigNameChange,
                            textStyle = TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 18.sp, color = FiniaColors.TextPrimary, textAlign = TextAlign.Center),
                            singleLine = true,
                            modifier = Modifier
                                .wrapContentWidth()
                                .dashedBorder(FiniaColors.BorderDashed, 8.dp)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    } else {
                        Text(acc.nombre, style = FiniaText.RowTitleBold.copy(fontSize = 18.sp), color = FiniaColors.TextPrimary)
                    }
                    if (!state.accountEditMode) {
                        Icon(
                            Icons.Filled.KeyboardArrowRight, contentDescription = "Siguiente cuenta", tint = Color(0xFFC3C1B9),
                            modifier = Modifier.size(26.dp).clickable(onClick = vm::switchAccountNext),
                        )
                    }
                }

                if (state.accountEmojiPickerOpen) {
                    EmojiGrid(com.devmastercrack.finia.presentation.finia.model.AccountEmojiOptions, vm::selectAccountEmoji)
                }

                Column(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(view.saldoLabel, style = FiniaText.Secondary, color = FiniaColors.TextSecondary)
                    if (state.accountEditMode) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$", style = FiniaText.AmountMedium, color = accent)
                            BasicTextField(
                                value = state.accountReajusteValue,
                                onValueChange = vm::onReajusteChange,
                                textStyle = TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Black, fontSize = 34.sp, color = accent, textAlign = TextAlign.Center),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .dashedBorder(FiniaColors.BorderDashed, 10.dp)
                                    .padding(horizontal = 10.dp, vertical = 2.dp),
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$", style = FiniaText.AmountMedium, color = accent)
                            Text(fmt(view.available).removePrefix("$").removePrefix("-"), style = FiniaText.AmountXL, color = accent)
                        }
                    }
                }

                Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                    if (state.accountEditMode) {
                        Box {
                            Row(
                                Modifier
                                    .dashedBorder(FiniaColors.BorderDashed, 12.dp)
                                    .clickable(onClick = vm::toggleTipoPicker)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = FiniaColors.TextSecondary, modifier = Modifier.size(13.dp))
                                Text((state.accountConfigTipo ?: acc.tipo).label, style = FiniaText.LabelSmall.copy(fontSize = 13.sp), color = Color(0xFF5F6359))
                            }
                            if (state.tipoPickerOpen) {
                                Column(
                                    Modifier
                                        .padding(top = 40.dp)
                                        .background(Color.White, RoundedCornerShape(16.dp))
                                        .border(1.dp, FiniaColors.BorderSubtle, RoundedCornerShape(16.dp))
                                        .padding(6.dp)
                                        .width(180.dp),
                                ) {
                                    AccountType.entries.forEach { tp ->
                                        val selected = tp == (state.accountConfigTipo ?: acc.tipo)
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (selected) FiniaColors.AccentSoft else Color.Transparent)
                                                .clickable { vm.selectTipo(tp) }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(tp.label, style = FiniaText.RowTitleSemibold.copy(fontSize = 13.sp), color = FiniaColors.TextPrimary)
                                            if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = FiniaColors.Accent, modifier = Modifier.size(13.dp))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = FiniaColors.TextSecondary, modifier = Modifier.size(13.dp))
                            Text(acc.tipo.label, style = FiniaText.LabelSmall.copy(fontSize = 13.sp), color = FiniaColors.TextSecondary)
                        }
                    }
                }

                if (acc.isCredit) {
                    Column(Modifier.fillMaxWidth().padding(top = 14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(view.pctLabel, style = FiniaText.LabelSmall, color = FiniaColors.TextSecondary)
                            Text("${view.denomLabel} ${fmt(if (state.accountEditMode) state.accountConfigDenomValue else view.denomAmount)}", style = FiniaText.LabelSmall, color = FiniaColors.TextSecondary)
                        }
                        Spacer(Modifier.height(5.dp))
                        if (!state.accountEditMode) {
                            Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(FiniaColors.SegmentedTrack)) {
                                Box(Modifier.fillMaxWidth(view.pct / 100f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(view.pctColor))
                            }
                        } else {
                            Slider(
                                value = state.accountConfigDenomValue.toFloat(),
                                onValueChange = { vm.onDenomSliderChange(it.toDouble()) },
                                valueRange = 500f..100000f,
                                colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
                            )
                        }
                        if (view.showCorteNote && !state.accountEditMode) {
                            Text("Se reinicia el ${acc.corte} (fecha de corte)", style = FiniaText.SecondarySmall, color = FiniaColors.TextSecondary, modifier = Modifier.padding(top = 5.dp))
                        }
                    }

                    SegmentedControl(
                        options = listOf("Mi tarjeta", "Mi límite"),
                        selectedIndex = if (state.creditPrimaryView == CreditPrimaryView.CAPACIDAD) 0 else 1,
                        onSelect = { vm.setCreditPrimaryView(if (it == 0) CreditPrimaryView.CAPACIDAD else CreditPrimaryView.LIMITE) },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    )

                    Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStatBox(Modifier.weight(1f), "Gastado", fmt(view.usedAmount))
                        MiniStatBox(
                            Modifier.weight(1f), "Corte",
                            if (state.accountEditMode) null else acc.corte ?: "",
                            editableValue = if (state.accountEditMode) state.accountConfigCorte else null,
                            onEditClick = { vm.openAccCal(AccCalField.CORTE) },
                            dashed = state.accountEditMode,
                        )
                        MiniStatBox(
                            Modifier.weight(1f), "Próx. pago",
                            if (state.accountEditMode) null else acc.pago ?: "",
                            editableValue = if (state.accountEditMode) state.accountConfigPago else null,
                            onEditClick = { vm.openAccCal(AccCalField.PAGO) },
                            dashed = state.accountEditMode,
                        )
                    }
                }

                Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPill(Modifier.weight(1f), FiniaColors.DangerSoft, gastosCount.toString(), "Gastos", FiniaColors.Danger)
                    StatPill(Modifier.weight(1f), FiniaColors.AccentSoft, ingresosCount.toString(), if (acc.isCredit) "Ingresos" else "Abonos", FiniaColors.Accent)
                    StatPill(Modifier.weight(1f), FiniaColors.SegmentedTrack, transferCount.toString(), "Transf.", Color(0xFF5F6359))
                }

                if (acc.isCredit && !state.accountEditMode) {
                    androidx.compose.material3.Button(
                        onClick = vm::openCardPayment,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = accent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(48.dp),
                    ) {
                        Text("Pagar tarjeta", style = FiniaText.Button.copy(fontSize = 14.sp), color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmojiGrid(options: List<String>, onSelect: (String) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(FiniaColors.SurfaceNeutral, RoundedCornerShape(16.dp))
            .padding(10.dp),
    ) {
        options.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { emoji ->
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).background(Color.White).clickable { onSelect(emoji) },
                        contentAlignment = Alignment.Center,
                    ) { Text(emoji, fontSize = 17.sp) }
                }
            }
        }
    }
}

@Composable
private fun MiniStatBox(
    modifier: Modifier,
    label: String,
    value: String?,
    editableValue: String? = null,
    onEditClick: (() -> Unit)? = null,
    dashed: Boolean = false,
) {
    Column(
        modifier
            .background(FiniaColors.SurfaceNeutral, RoundedCornerShape(16.dp))
            .then(if (dashed) Modifier.dashedBorder(FiniaColors.BorderDashed, 16.dp) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(label, style = FiniaText.LabelTiny.copy(fontSize = 10.sp), color = FiniaColors.TextSecondary)
        if (editableValue != null && onEditClick != null) {
            Text(
                editableValue, style = FiniaText.RowTitleBold.copy(fontSize = 14.sp), color = FiniaColors.TextPrimary,
                modifier = Modifier.padding(top = 2.dp).clickable(onClick = onEditClick),
            )
        } else {
            Text(value ?: "", style = FiniaText.RowTitleBold.copy(fontSize = 15.sp), color = FiniaColors.TextPrimary, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun StatPill(modifier: Modifier, bg: Color, count: String, label: String, color: Color) {
    Column(
        modifier.background(bg, RoundedCornerShape(20.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(count, style = FiniaText.RowTitleBold.copy(fontSize = 16.sp), color = color)
        Text(label, style = FiniaText.LabelTiny.copy(fontSize = 10.sp), color = color, modifier = Modifier.padding(top = 3.dp))
    }
}

