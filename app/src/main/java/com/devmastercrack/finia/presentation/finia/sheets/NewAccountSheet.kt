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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.core.theme.NewAccountSwatches
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.components.SheetHandle
import com.devmastercrack.finia.presentation.finia.model.AccountType
import com.devmastercrack.finia.presentation.finia.model.CardNetwork
import com.devmastercrack.finia.presentation.finia.model.NewAccountEmojiOptions
import com.devmastercrack.finia.presentation.finia.util.fmt
import com.devmastercrack.finia.presentation.finia.util.formatThousands
import com.devmastercrack.finia.presentation.finia.util.num

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAccountSheet(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val isCredit = state.newAccountTipo == AccountType.CREDIT
    val isCardType = state.newAccountTipo == AccountType.DEBIT || isCredit
    val usadoOverCapacidad = isCredit && num(state.newAccountUsado) > num(state.newAccountCapacidad)
    val disabled = state.newAccountName.isBlank() || usadoOverCapacidad
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = vm::closeNewAccountSheet,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), contentAlignment = Alignment.Center) { SheetHandle() }
                Text("Nueva cuenta", style = FiniaText.SheetTitle, color = FiniaColors.TextPrimary, modifier = Modifier.padding(bottom = 14.dp))

                Text("Tipo de cuenta", style = FiniaText.Label.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 14.dp)) {
                    items(AccountType.entries) { tp ->
                        val selected = tp == state.newAccountTipo
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) FiniaColors.AccentSoft else FiniaColors.SurfaceNeutral)
                                .clickable { vm.setNewAccountTipo(tp) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(com.devmastercrack.finia.presentation.finia.model.defaultEmojiFor(tp), fontSize = 14.sp)
                            Text(tp.label, style = FiniaText.Chip, color = if (selected) FiniaColors.Accent else FiniaColors.TextPrimary)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 14.dp)) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(state.newAccountColor).clickable(onClick = vm::toggleNewAccountEmojiPicker),
                        contentAlignment = Alignment.Center,
                    ) { Text(state.newAccountEmoji, fontSize = 17.sp) }
                    BasicTextField(
                        value = state.newAccountName,
                        onValueChange = vm::onNewAccountNameChange,
                        textStyle = TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, fontSize = 14.sp, color = FiniaColors.TextPrimary),
                        singleLine = true,
                        decorationBox = { inner ->
                            Box(
                                Modifier
                                    .background(Color.White, RoundedCornerShape(14.dp))
                                    .border(1.dp, FiniaColors.BorderSubtle, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                            ) {
                                if (state.newAccountName.isEmpty()) {
                                    Text("Nombre de cuenta", style = FiniaText.RowTitleSemibold.copy(fontSize = 14.sp), color = FiniaColors.TextSecondary)
                                }
                                inner()
                            }
                        },
                        modifier = Modifier.weight(1f).padding(start = 10.dp).height(46.dp),
                    )
                }

                if (state.newAccountEmojiPickerOpen) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 14.dp)) {
                        NewAccountEmojiOptions.forEach { emoji ->
                            Box(
                                Modifier.size(34.dp).clip(CircleShape).background(FiniaColors.SurfaceNeutral).clickable { vm.setNewAccountEmoji(emoji) },
                                contentAlignment = Alignment.Center,
                            ) { Text(emoji, fontSize = 15.sp) }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 14.dp)) {
                    Text("Color", style = FiniaText.Label.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary)
                    NewAccountSwatches.forEach { color ->
                        Box(
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.dp, if (state.newAccountColor == color) FiniaColors.TextPrimary else Color.Transparent, CircleShape)
                                .clickable { vm.setNewAccountColor(color) },
                        )
                    }
                }

                if (isCardType) {
                    Text("Red", style = FiniaText.Label.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                        NetworkOption(Modifier.weight(1f), state.newAccountNetwork == CardNetwork.VISA, { vm.setNewAccountNetwork(CardNetwork.VISA) }) {
                            com.devmastercrack.finia.presentation.finia.components.VisaMark(color = if (state.newAccountNetwork == CardNetwork.VISA) FiniaColors.Accent else FiniaColors.TextPrimary)
                        }
                        NetworkOption(Modifier.weight(1f), state.newAccountNetwork == CardNetwork.MASTERCARD, { vm.setNewAccountNetwork(CardNetwork.MASTERCARD) }) {
                            com.devmastercrack.finia.presentation.finia.components.MastercardMark(size = 14.dp)
                        }
                        NetworkOption(Modifier.weight(1f), state.newAccountNetwork == CardNetwork.OTHER, { vm.setNewAccountNetwork(CardNetwork.OTHER) }) {
                            Text("AMEX", style = FiniaText.RowTitleBold.copy(fontSize = 12.sp), color = if (state.newAccountNetwork == CardNetwork.OTHER) FiniaColors.Accent else FiniaColors.TextPrimary)
                        }
                    }
                }

                if (!isCredit) {
                    Text("Saldo inicial", style = FiniaText.Label.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    FiniaTextInput(
                        value = state.newAccountSaldo, onValueChange = vm::onNewAccountSaldoChange, placeholder = "0",
                        keyboardType = KeyboardType.Decimal, modifier = Modifier.padding(bottom = 14.dp),
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("Corte", style = FiniaText.Label.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                            FiniaTextInput(state.newAccountCorte, vm::onNewAccountCorteChange, "28", KeyboardType.Number)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Pago", style = FiniaText.Label.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                            FiniaTextInput(state.newAccountPago, vm::onNewAccountPagoChange, "15", KeyboardType.Number)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("Límite", style = FiniaText.Label.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                            FiniaTextInput(formatThousands(num(state.newAccountCapacidad)), vm::onNewAccountCapacidadChange, "20,000", KeyboardType.Decimal)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Ya usado", style = FiniaText.Label.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                            FiniaTextInput(
                                formatThousands(num(state.newAccountUsado)), vm::onNewAccountUsadoChange, "0", KeyboardType.Decimal,
                                borderColor = if (usadoOverCapacidad) FiniaColors.Danger else FiniaColors.BorderSubtle,
                            )
                        }
                    }
                    if (usadoOverCapacidad) {
                        Text(
                            "El monto usado no puede superar el límite.", style = FiniaText.SecondarySmall, color = FiniaColors.Danger,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    val disponible = maxOf(0.0, num(state.newAccountCapacidad) - num(state.newAccountUsado))
                    val pct = if (num(state.newAccountCapacidad) <= 0) 0f else (num(state.newAccountUsado) / num(state.newAccountCapacidad)).toFloat().coerceIn(0f, 1f)
                    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Crédito disponible", style = FiniaText.Label.copy(fontSize = 11.sp), color = FiniaColors.TextSecondary)
                            Text(fmt(disponible), style = FiniaText.AmountSmall, color = FiniaColors.TextPrimary)
                        }
                        Box(Modifier.fillMaxWidth().height(6.dp).padding(top = 4.dp).clip(RoundedCornerShape(3.dp)).background(FiniaColors.SegmentedTrack)) {
                            Box(Modifier.fillMaxWidth(pct).height(6.dp).clip(RoundedCornerShape(3.dp)).background(FiniaColors.Accent))
                        }
                        Text(
                            "${(pct * 100).toInt()}% usado", style = FiniaText.Label.copy(fontSize = 11.sp), color = FiniaColors.TextSecondary,
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        )
                    }
                }

                Box(Modifier.height(8.dp))
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    "Cancelar", style = FiniaText.ButtonSmall, color = Color(0xFF5F6359),
                    modifier = Modifier.clickable(onClick = vm::closeNewAccountSheet).padding(horizontal = 10.dp, vertical = 10.dp),
                )
                androidx.compose.material3.Button(
                    onClick = vm::addNewAccount,
                    enabled = !disabled,
                    colors = ButtonDefaults.buttonColors(containerColor = FiniaColors.Accent, disabledContainerColor = FiniaColors.Accent.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(start = 8.dp).height(40.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp),
                ) {
                    Text("Crear", style = FiniaText.ButtonSmall, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun NetworkOption(modifier: Modifier, selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) FiniaColors.AccentSoft else FiniaColors.SurfaceNeutral)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun FiniaTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    borderColor: Color = FiniaColors.BorderSubtle,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 14.sp, color = FiniaColors.TextPrimary),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        decorationBox = { inner ->
            Box(
                Modifier
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 13.dp),
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, style = TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 14.sp), color = FiniaColors.TextSecondary)
                }
                inner()
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
}
