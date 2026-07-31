package com.devmastercrack.finia.presentation.finia.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.devmastercrack.finia.presentation.finia.components.BackButton
import com.devmastercrack.finia.presentation.finia.model.ChatMessage
import com.devmastercrack.finia.presentation.finia.model.ChatMessageType
import com.devmastercrack.finia.presentation.finia.model.ChatSender
import com.devmastercrack.finia.presentation.finia.model.RecurringPayment
import com.devmastercrack.finia.presentation.finia.netWorth
import com.devmastercrack.finia.presentation.finia.totalGastos
import com.devmastercrack.finia.presentation.finia.totalIngresos
import com.devmastercrack.finia.presentation.finia.util.fmt

private val AiMessageAreaBg = Color(0xFFFAFAF9)

private val SuggestionLabels = listOf(
    "¿Cómo voy este mes?",
    "¿En qué gasto más?",
    "Ayúdame a ahorrar",
    "Próximos pagos",
    "Analizar gastos",
    "Crear presupuesto",
)

private val AnalysisCategories = listOf(
    "Comida" to 34,
    "Servicios" to 22,
    "Transporte" to 18,
    "Compras" to 14,
    "Ocio" to 12,
)

@Composable
fun AiScreen(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val netWorthValue = remember(state.accounts, state.debts, state.settledDebtIds) {
        netWorth(state.accounts, state.debts, state.settledDebtIds)
    }
    val ingresos = remember(state.recentTx) { totalIngresos(state.recentTx) }
    val gastos = remember(state.recentTx) { totalGastos(state.recentTx) }

    Column(modifier.fillMaxSize()) {
        AiHeader(vm::goHome)

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().background(AiMessageAreaBg),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(state.aiMessages, key = { it.id }) { msg ->
                MessageRow(msg, netWorthValue, ingresos, gastos, state.recurring)
            }
            if (state.aiTyping) {
                item { TypingIndicatorRow() }
            }
        }

        AiInputBar(state, vm)
    }
}

@Composable
private fun AiHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(androidx.compose.foundation.BorderStroke(1.dp, FiniaColors.BorderSubtle2))
            .padding(top = 16.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(onBack)
        Box(Modifier.padding(start = 8.dp)) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(FiniaColors.AiGreen),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(FiniaColors.AiGreen),
            )
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text("Asistente IA", style = FiniaText.RowTitleBold.copy(fontSize = 16.sp), color = FiniaColors.TextPrimary)
            Text("Tu asesor financiero personal", style = FiniaText.Secondary, color = FiniaColors.TextSecondary)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(FiniaColors.AiGreen))
            Text("En línea", style = FiniaText.SecondarySmall.copy(fontWeight = FontWeight.Medium), color = FiniaColors.AiGreen)
        }
    }
}

@Composable
private fun MessageRow(
    msg: ChatMessage,
    netWorthValue: Double,
    ingresos: Double,
    gastos: Double,
    recurring: List<RecurringPayment>,
) {
    val isUser = msg.from == ChatSender.USER
    Box(Modifier.fillMaxWidth()) {
        when (msg.type) {
            ChatMessageType.TEXT -> TextBubble(
                text = msg.text.orEmpty(),
                isUser = isUser,
                modifier = Modifier.align(if (isUser) Alignment.CenterEnd else Alignment.CenterStart).fillMaxWidth(0.8f),
            )
            ChatMessageType.SUMMARY -> SummaryCard(
                netWorthValue = netWorthValue,
                ahorro = ingresos - gastos,
                ingresos = ingresos,
                gastos = gastos,
                modifier = Modifier.align(Alignment.CenterStart).fillMaxWidth(0.88f),
            )
            ChatMessageType.PAYMENTS -> PaymentsCard(
                recurring = recurring,
                modifier = Modifier.align(Alignment.CenterStart).fillMaxWidth(0.88f),
            )
            ChatMessageType.TIP -> TipCard(modifier = Modifier.align(Alignment.CenterStart).fillMaxWidth(0.88f))
            ChatMessageType.ANALYSIS -> AnalysisCard(modifier = Modifier.align(Alignment.CenterStart).fillMaxWidth(0.88f))
        }
    }
}

@Composable
private fun TextBubble(text: String, isUser: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isUser) FiniaColors.AiUserBubble else FiniaColors.AiGreenSoft)
            .padding(horizontal = 15.dp, vertical = 11.dp),
    ) {
        Text(text, fontSize = 14.sp, color = FiniaColors.TextPrimary)
    }
}

@Composable
private fun AiCardShell(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        content = content,
    )
}

@Composable
private fun SummaryCard(netWorthValue: Double, ahorro: Double, ingresos: Double, gastos: Double, modifier: Modifier = Modifier) {
    AiCardShell(modifier) {
        Text("📊 Resumen del mes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FiniaColors.TextPrimary, modifier = Modifier.padding(bottom = 12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatCell("Saldo actual", fmt(netWorthValue), FiniaColors.TextPrimary, 16.sp)
            StatCell("Ahorro", fmt(ahorro), FiniaColors.AiGreen, 15.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatCell("Ingresos", fmt(ingresos), FiniaColors.AiGreen, 15.sp)
            StatCell("Gastos", fmt(gastos), FiniaColors.Danger, 15.sp)
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, valueColor: Color, valueSize: androidx.compose.ui.unit.TextUnit) {
    Column {
        Text(label, style = FiniaText.SecondarySmall, color = FiniaColors.TextSecondary)
        Text(value, fontSize = valueSize, fontWeight = FontWeight.Bold, color = valueColor, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun PaymentsCard(recurring: List<RecurringPayment>, modifier: Modifier = Modifier) {
    AiCardShell(modifier) {
        Text("📅 Próximos pagos", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FiniaColors.TextPrimary, modifier = Modifier.padding(bottom = 10.dp))
        recurring.forEachIndexed { index, item ->
            if (index > 0) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(FiniaColors.BorderSubtle2))
            }
            Row(
                Modifier.fillMaxWidth().padding(vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val dotColor = when {
                    item.dias <= 5 -> FiniaColors.Danger
                    item.dias <= 10 -> Color(0xFFD9752C)
                    else -> FiniaColors.AiGreen
                }
                Box(Modifier.size(8.dp).clip(CircleShape).background(dotColor))
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(item.nombre, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = FiniaColors.TextPrimary)
                    Text("Vence en ${item.dias} días", style = FiniaText.SecondarySmall, color = FiniaColors.TextSecondary)
                }
                Text(fmt(item.monto), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FiniaColors.TextPrimary)
            }
        }
    }
}

@Composable
private fun TipCard(modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
    ) {
        Text("💡", fontSize = 20.sp)
        Column(Modifier.padding(start = 12.dp)) {
            Text("Reduce gastos hormiga", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FiniaColors.TextPrimary)
            Text(
                "Detectamos $650 en cafés y snacks este mes. Fija un límite semanal para ahorrar sin sacrificar tus gustos.",
                fontSize = 12.sp, color = FiniaColors.TextMuted, modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "Ver más", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FiniaColors.AiGreen,
                modifier = Modifier.padding(top = 8.dp).clickable {},
            )
        }
    }
}

@Composable
private fun AnalysisCard(modifier: Modifier = Modifier) {
    AiCardShell(modifier) {
        Text("📈 Análisis de gastos", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FiniaColors.TextPrimary, modifier = Modifier.padding(bottom = 12.dp))
        AnalysisCategories.forEachIndexed { index, (name, pct) ->
            if (index > 0) Spacer(Modifier.height(10.dp))
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = FiniaColors.TextPrimary)
                    Text("$pct%", fontSize = 12.sp, color = FiniaColors.TextSecondary)
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(FiniaColors.BorderSubtle2),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(pct / 100f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(FiniaColors.AiGreen),
                    )
                }
            }
        }
    }
}

@Composable
private fun TypingIndicatorRow() {
    Box(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(16.dp))
                .background(FiniaColors.AiGreenSoft)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                val transition = rememberInfiniteTransition(label = "typingDot$index")
                val dotAlpha by transition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = index * 150),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "typingDotAlpha$index",
                )
                Box(Modifier.size(6.dp).clip(CircleShape).background(FiniaColors.AiGreen).alpha(dotAlpha))
            }
        }
    }
}

@Composable
private fun AiInputBar(state: FiniaUiState, vm: FiniaViewModel) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(androidx.compose.foundation.BorderStroke(1.dp, FiniaColors.BorderSubtle2)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 10.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SuggestionLabels.forEach { label ->
                Box(
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(FiniaColors.AiSuggestionBg)
                        .border(1.dp, FiniaColors.AiSuggestionBorder, RoundedCornerShape(16.dp))
                        .clickable { vm.sendAiMessage(label) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FiniaColors.AiGreen)
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 22.dp, start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RoundIconButton(icon = Icons.Outlined.AttachFile, contentDescription = "Adjuntar", onClick = {})

            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, FiniaColors.BorderSubtle, RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (state.aiInput.isEmpty()) {
                    Text("Pregúntale a FinanIA...", fontSize = 14.sp, color = FiniaColors.TextFaint)
                }
                BasicTextField(
                    value = state.aiInput,
                    onValueChange = vm::onAiInputChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = FiniaColors.TextPrimary),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(FiniaColors.AiGreen),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            RoundIconButton(icon = Icons.Outlined.Mic, contentDescription = "Mensaje de voz", onClick = {})

            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(FiniaColors.AiGreen)
                    .clickable { vm.sendAiMessage() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun RoundIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(FiniaColors.SurfaceNeutral2)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = FiniaColors.TextMuted, modifier = Modifier.size(18.dp))
    }
}
