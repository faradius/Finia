package com.devmastercrack.finia.presentation.finia.sheets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.components.SheetHandle
import com.devmastercrack.finia.presentation.finia.model.DefaultCategories
import com.devmastercrack.finia.presentation.finia.model.QuickBtnMode
import com.devmastercrack.finia.presentation.finia.model.TxFlow
import com.devmastercrack.finia.presentation.finia.util.formatDateLabel

/**
 * Real Material 3 [ModalBottomSheet] — native swipe-to-dismiss, drag physics, scrim, and
 * back-gesture handling instead of a hand-rolled Box/AnimatedVisibility/Animatable stack.
 * Trade-off: this always shows the standard full scrim, so the "see the account carousel
 * behind it" passthrough effect the old NoScrimSheetHost gave when opened from the
 * Accounts screen is gone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = vm::closeAdd,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        // No dragHandle slot — ModalBottomSheet wraps it in its own clickable Box for
        // expand/collapse a11y, whose ripple has no way to be suppressed from the outside.
        // Rendering the handle as plain, non-interactive content avoids that ripple entirely.
        dragHandle = null,
    ) {
        AddTransactionContent(state, vm)
    }
}

@Composable
private fun AddTransactionContent(state: FiniaUiState, vm: FiniaViewModel) {
    val montoColor = when (state.form.tipo) {
        TxFlow.GASTO -> FiniaColors.Danger
        TxFlow.INGRESO -> FiniaColors.Accent
        TxFlow.TRANSFERENCIA -> FiniaColors.SavingsCardBg
    }
    val isTransferencia = state.form.tipo == TxFlow.TRANSFERENCIA
    val allCats = DefaultCategories + state.customCategories
    val categoryEmoji = allCats.find { it.nombre == state.form.categoria }?.emoji ?: "💳"
    val accountEmoji = state.accounts.find { it.nombre == state.form.cuenta }?.emoji ?: "💳"
    val destAccountEmoji = state.accounts.find { it.nombre == state.form.cuentaDestino }?.emoji ?: "🏦"
    val isLowConfidence = state.categoryOrigin == com.devmastercrack.finia.presentation.finia.model.CategoryOrigin.AUTO &&
        state.categoryConfidence == com.devmastercrack.finia.presentation.finia.model.CategoryConfidence.LOW

    // ModalBottomSheet applies imePadding() to itself internally (verified in the Material3
    // 1.4.0 source — it's hard-coded, not something a caller can opt out of), so the sheet
    // always resizes to stay above the keyboard rather than letting the keyboard overlay it.
    // Without a scrollable content area, that reduced height just clipped/squished whatever
    // didn't fit (the quick-action + Guardar row). verticalScroll lets you reach it by
    // scrolling instead; Monto/Descripción stay visible at the top either way.
    Column(
        Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) { SheetHandle() }
        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
            Text(
                if (state.editingTxId != null) "Editar movimiento" else "Nuevo movimiento",
                style = FiniaText.SheetTitle, color = FiniaColors.TextPrimary, textAlign = TextAlign.Center,
            )
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                FlowToggle(
                    selected = state.form.tipo, onGasto = vm::setTipoGasto, onIngreso = vm::setTipoIngreso,
                    onTransferencia = vm::setTipoTransferencia,
                )
                BasicTextField(
                    value = state.form.monto,
                    onValueChange = vm::onMontoChange,
                    textStyle = TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Black, fontSize = 54.sp, color = montoColor, textAlign = TextAlign.Center),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    visualTransformation = CurrencyVisualTransformation,
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

            Column(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!isTransferencia) {
                    CompactFieldRow(
                        categoryEmoji, state.form.categoria, onClick = vm::toggleCategoryPicker,
                        modifier = if (isLowConfidence) Modifier.border(1.dp, FiniaColors.LowConfidenceWarn, RoundedCornerShape(16.dp)) else Modifier,
                    )
                    if (isLowConfidence) {
                        Text(
                            "⚠️ ¿Es esto correcto?", style = FiniaText.LabelSmall.copy(fontSize = 11.sp), color = FiniaColors.LowConfidenceWarn,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
                CompactFieldRow("📅", formatDateLabel(state.form.fecha), onClick = vm::toggleDatePicker)
                CompactFieldRow(
                    accountEmoji, if (isTransferencia) "De: ${state.form.cuenta}" else state.form.cuenta,
                    onClick = vm::toggleAccountPicker,
                )
                if (isTransferencia) {
                    CompactFieldRow(
                        destAccountEmoji,
                        if (state.form.cuentaDestino.isBlank()) "A: Elegir cuenta" else "A: ${state.form.cuentaDestino}",
                        onClick = vm::toggleDestAccountPicker,
                    )
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(FiniaColors.SurfaceNeutral)
                        .clickable(onClick = vm::openAdvanced)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.List, contentDescription = null, tint = Color(0xFF5F6359), modifier = Modifier.size(16.dp))
                    Text("Agregar más detalles", style = FiniaText.RowTitleSemibold.copy(fontSize = 14.sp), color = FiniaColors.TextPrimary)
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            var swipeAccum by remember { mutableFloatStateOf(0f) }
            Column(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(FiniaColors.SurfaceNeutral2)
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = vm::toggleQuickBtnMode,
                    )
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = { if (kotlin.math.abs(swipeAccum) > 30) vm.toggleQuickBtnMode(); swipeAccum = 0f },
                            onHorizontalDrag = { change, dragAmount -> change.consume(); swipeAccum += dragAmount },
                        )
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    androidx.compose.animation.AnimatedContent(
                        targetState = state.quickBtnMode,
                        transitionSpec = {
                            (androidx.compose.animation.scaleIn(initialScale = 0.5f, animationSpec = tween(220)) + androidx.compose.animation.fadeIn(tween(220)))
                                .togetherWith(androidx.compose.animation.scaleOut(targetScale = 0.5f, animationSpec = tween(150)) + androidx.compose.animation.fadeOut(tween(150)))
                        },
                        label = "quickBtnIcon",
                    ) { mode ->
                        Icon(
                            if (mode == QuickBtnMode.MIC) Icons.Outlined.Mic else Icons.Outlined.CameraAlt,
                            contentDescription = if (mode == QuickBtnMode.MIC) "Registrar por voz" else "Escanear ticket",
                            tint = FiniaColors.TextPrimary, modifier = Modifier.size(20.dp),
                        )
                    }
                }
                // Tiny two-dot indicator so which mode is active reads at a glance, not just
                // during the swap animation.
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                    listOf(QuickBtnMode.MIC, QuickBtnMode.CAMERA).forEach { mode ->
                        val dotColor by animateColorAsState(
                            targetValue = if (state.quickBtnMode == mode) FiniaColors.TextPrimary else FiniaColors.BorderDisabled,
                            animationSpec = tween(200),
                            label = "quickBtnDot",
                        )
                        Box(Modifier.size(4.dp).clip(androidx.compose.foundation.shape.CircleShape).background(dotColor))
                    }
                }
            }
            val submitEnabled = com.devmastercrack.finia.presentation.finia.util.num(state.form.monto) != 0.0
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

@Composable
private fun FlowToggle(selected: TxFlow, onGasto: () -> Unit, onIngreso: () -> Unit, onTransferencia: () -> Unit) {
    val density = LocalDensity.current
    var gastoWidthPx by remember { mutableIntStateOf(0) }
    var ingresoWidthPx by remember { mutableIntStateOf(0) }
    var transferWidthPx by remember { mutableIntStateOf(0) }
    var rowHeightPx by remember { mutableIntStateOf(0) }

    val indicatorSpring = spring<androidx.compose.ui.unit.Dp>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    val indicatorX by animateDpAsState(
        targetValue = with(density) {
            when (selected) {
                TxFlow.GASTO -> 0
                TxFlow.INGRESO -> gastoWidthPx
                TxFlow.TRANSFERENCIA -> gastoWidthPx + ingresoWidthPx
            }.toDp()
        },
        animationSpec = indicatorSpring,
        label = "flowIndicatorX",
    )
    val indicatorWidth by animateDpAsState(
        targetValue = with(density) {
            when (selected) {
                TxFlow.GASTO -> gastoWidthPx
                TxFlow.INGRESO -> ingresoWidthPx
                TxFlow.TRANSFERENCIA -> transferWidthPx
            }.toDp()
        },
        animationSpec = indicatorSpring,
        label = "flowIndicatorWidth",
    )

    Box(Modifier.background(FiniaColors.SurfaceNeutral2, RoundedCornerShape(20.dp)).padding(3.dp)) {
        // The sliding "drop" — a single pill that glides between Gasto/Ingreso/Transferir instead
        // of each option owning its own independent background.
        Box(
            Modifier
                .offset { IntOffset(indicatorX.roundToPx(), 0) }
                .width(indicatorWidth)
                .height(with(density) { rowHeightPx.toDp() })
                .clip(RoundedCornerShape(17.dp))
                .background(Color.White),
        )
        Row(Modifier.onSizeChanged { rowHeightPx = it.height }) {
            FlowOption("Gasto", selected == TxFlow.GASTO, FiniaColors.Danger, onGasto, Modifier.onSizeChanged { gastoWidthPx = it.width })
            FlowOption("Ingreso", selected == TxFlow.INGRESO, FiniaColors.Accent, onIngreso, Modifier.onSizeChanged { ingresoWidthPx = it.width })
            FlowOption("Transferir", selected == TxFlow.TRANSFERENCIA, FiniaColors.SavingsCardBg, onTransferencia, Modifier.onSizeChanged { transferWidthPx = it.width })
        }
    }
}

@Composable
private fun FlowOption(label: String, selected: Boolean, activeColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val textColor by animateColorAsState(
        targetValue = if (selected) activeColor else Color(0xFF5F6359),
        animationSpec = tween(150),
        label = "flowOptionTextColor",
    )
    Box(
        modifier
            .clip(RoundedCornerShape(17.dp))
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 8.dp),
    ) {
        Text(label, style = FiniaText.ChipMedium, color = textColor)
    }
}

/**
 * Prepends "$" and groups the integer part with commas for display only — [state.form.monto]
 * stays a plain, parseable number string ("8955" / "89.55"), and Compose maps cursor positions
 * between the raw and the displayed text via [OffsetMapping] so typing/deleting/selecting all
 * behave normally despite the extra "$"/"," characters not existing in the underlying value.
 */
val CurrencyVisualTransformation = VisualTransformation { text ->
    val raw = text.text
    if (raw.isEmpty()) return@VisualTransformation TransformedText(AnnotatedString(""), OffsetMapping.Identity)

    val dotIndex = raw.indexOf('.')
    val intRaw = if (dotIndex == -1) raw else raw.substring(0, dotIndex)
    val decRaw = if (dotIndex == -1) null else raw.substring(dotIndex + 1)
    val groupedInt = if (intRaw.isEmpty()) "0" else intRaw.reversed().chunked(3).joinToString(",").reversed()
    val displayed = buildString {
        append('$')
        append(groupedInt)
        if (decRaw != null) {
            append('.')
            append(decRaw)
        }
    }

    // digitsConsumedAt[i] = how many raw int-digits are represented by groupedInt[0 until i]
    // (used to map a transformed position back to a raw one).
    val digitsConsumedAt = IntArray(groupedInt.length + 1)
    // transformedPosForDigits[k] = index within groupedInt right after k raw int-digits
    // (used to map a raw position forward to a transformed one).
    val transformedPosForDigits = IntArray(intRaw.length + 1)
    run {
        var count = 0
        for (i in groupedInt.indices) {
            digitsConsumedAt[i] = count
            if (groupedInt[i] != ',') {
                count++
                if (count <= intRaw.length) transformedPosForDigits[count] = i + 1
            }
        }
        digitsConsumedAt[groupedInt.length] = count
    }

    val offsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val o = offset.coerceIn(0, raw.length)
            return if (o <= intRaw.length) {
                1 + transformedPosForDigits[o]
            } else {
                val decOffset = o - intRaw.length - 1
                1 + groupedInt.length + 1 + decOffset
            }
        }

        override fun transformedToOriginal(offset: Int): Int {
            val o = offset.coerceIn(0, displayed.length)
            val afterDollar = (o - 1).coerceAtLeast(0)
            return if (afterDollar <= groupedInt.length) {
                digitsConsumedAt[afterDollar.coerceIn(0, groupedInt.length)]
            } else {
                val decOffset = afterDollar - groupedInt.length - 1
                intRaw.length + 1 + decOffset
            }
        }
    }

    TransformedText(AnnotatedString(displayed), offsetMapping)
}

/** Its own standalone rounded card — several of these stacked with a gap between them, instead
 * of one bordered box sliced by divider lines, so the field list reads as tappable rows rather
 * than a spreadsheet/table. */
@Composable
fun CompactFieldRow(emoji: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FiniaColors.SurfaceNeutral)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(32.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 16.sp)
        }
        Text(label, style = FiniaText.RowTitleSemibold.copy(fontSize = 14.sp), color = FiniaColors.TextPrimary, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = FiniaColors.TextSecondary, modifier = Modifier.size(16.dp))
    }
}
