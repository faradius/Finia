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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.components.ScrimSheetHost
import com.devmastercrack.finia.presentation.finia.model.CardPaymentMode
import com.devmastercrack.finia.presentation.finia.util.fmt

@Composable
fun CardPaymentSheet(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val acc = state.accounts.find { it.id == state.accountConfigOpen } ?: return
    val deuda = acc.deudaTarjeta ?: 0.0
    val accent = Color(0xFF9C7326)
    val needsAmount = state.cardPaymentMode == CardPaymentMode.PARCIAL || state.cardPaymentMode == CardPaymentMode.ADELANTO
    val amountValid = state.cardPaymentAmount.toDoubleOrNull()?.let { it > 0 } ?: false
    val disabled = state.cardPaymentMode == null || (needsAmount && !amountValid)

    ScrimSheetHost(onDismiss = vm::closeCardPayment, modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text("Pagar tarjeta", style = FiniaText.SheetTitle.copy(fontSize = 17.sp), color = FiniaColors.TextPrimary)
            Text(
                "Debes ${fmt(deuda)} de tu límite bancario", style = FiniaText.Secondary, color = FiniaColors.TextSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )

            Column(Modifier.fillMaxWidth().padding(top = 14.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                PaymentOptionRow("Pagar todo", fmt(deuda), null, state.cardPaymentMode == CardPaymentMode.TOTAL, accent) {
                    vm.selectPaymentMode(CardPaymentMode.TOTAL)
                }
                PaymentOptionRow("Pago parcial", null, "Tú eliges el monto", state.cardPaymentMode == CardPaymentMode.PARCIAL, accent) {
                    vm.selectPaymentMode(CardPaymentMode.PARCIAL)
                }
                PaymentOptionRow("Adelantar pago", null, "Antes de tu corte", state.cardPaymentMode == CardPaymentMode.ADELANTO, accent) {
                    vm.selectPaymentMode(CardPaymentMode.ADELANTO)
                }
            }

            if (needsAmount) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text("$", style = FiniaText.AmountMedium, color = accent)
                    BasicTextField(
                        value = state.cardPaymentAmount,
                        onValueChange = vm::onCardPaymentAmountChange,
                        textStyle = TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Black, fontSize = 34.sp, color = accent, textAlign = TextAlign.Center),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }
            }

            androidx.compose.material3.Button(
                onClick = vm::confirmCardPayment,
                enabled = !disabled,
                colors = ButtonDefaults.buttonColors(containerColor = accent, disabledContainerColor = accent.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(48.dp),
            ) {
                Text("Confirmar pago", style = FiniaText.Button.copy(fontSize = 14.sp), color = Color.White)
            }
        }
    }
}

@Composable
private fun PaymentOptionRow(
    label: String,
    amount: String?,
    hint: String?,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) FiniaColors.SurfaceNeutral else Color.Transparent)
            .border(1.5.dp, if (selected) accent else FiniaColors.BorderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = FiniaText.RowTitleSemibold.copy(fontSize = 13.sp), color = FiniaColors.TextPrimary)
        if (amount != null) {
            Text(amount, style = FiniaText.RowTitleBold.copy(fontSize = 14.sp), color = accent)
        } else if (hint != null) {
            Text(hint, style = FiniaText.Secondary, color = FiniaColors.TextSecondary)
        }
    }
}
