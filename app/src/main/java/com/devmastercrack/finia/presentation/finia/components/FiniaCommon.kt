package com.devmastercrack.finia.presentation.finia.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.util.fmt

@Composable
fun CircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bg: Color = Color(0xFFE7E4DC),
    contentColor: Color = FiniaColors.TextPrimary,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides contentColor,
            content = content,
        )
    }
}

@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CircleIconButton(onClick = onClick, bg = Color.Transparent, modifier = modifier) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
    }
}

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var containerWidthPx by remember { mutableIntStateOf(0) }
    var rowHeightPx by remember { mutableIntStateOf(0) }
    val segmentWidthPx = if (options.isNotEmpty() && containerWidthPx > 0) containerWidthPx / options.size else 0

    // Same sliding "drop" indicator as the Gasto/Ingreso toggle: one pill that glides between
    // segments instead of each option owning its own independent white background.
    val indicatorSpring = spring<Dp>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    val indicatorX by animateDpAsState(
        targetValue = with(density) { (segmentWidthPx * selectedIndex).toDp() },
        animationSpec = indicatorSpring,
        label = "segIndicatorX",
    )
    val indicatorWidth by animateDpAsState(
        targetValue = with(density) { segmentWidthPx.toDp() },
        animationSpec = indicatorSpring,
        label = "segIndicatorWidth",
    )

    Box(
        modifier = modifier
            .background(FiniaColors.SegmentedTrack, RoundedCornerShape(20.dp))
            .padding(3.dp)
            .onSizeChanged { containerWidthPx = it.width },
    ) {
        Box(
            Modifier
                .offset { IntOffset(indicatorX.roundToPx(), 0) }
                .width(indicatorWidth)
                .height(with(density) { rowHeightPx.toDp() })
                .clip(RoundedCornerShape(17.dp))
                .background(Color.White),
        )
        Row(Modifier.onSizeChanged { rowHeightPx = it.height }) {
            options.forEachIndexed { i, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onSelect(i) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, style = FiniaText.ChipMedium, color = FiniaColors.TextPrimary)
                }
            }
        }
    }
}

@Composable
fun FiniaChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedBg: Color = FiniaColors.Accent,
    selectedFg: Color = Color.White,
    unselectedFg: Color = FiniaColors.TextPrimary,
    unselectedBorder: Color = FiniaColors.BorderSubtle,
) {
    val bg by animateColorAsState(if (selected) selectedBg else Color.Transparent, label = "chipBg")
    val border by animateColorAsState(if (selected) selectedBg else unselectedBorder, label = "chipBorder")
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(label, style = FiniaText.Chip, color = if (selected) selectedFg else unselectedFg)
    }
}

@Composable
fun FiniaSwitch(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = FiniaColors.Accent,
) {
    val trackColor by animateColorAsState(if (checked) activeColor else FiniaColors.ToggleTrackOff, label = "switchTrack")
    Box(
        modifier = modifier
            .size(width = 44.dp, height = 26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(trackColor)
            .clickable { onCheckedChange() }
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Composable
fun SheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(vertical = 10.dp)
            .size(width = 36.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFFDCDAD3)),
    )
}

fun Modifier.dashedBorder(color: Color, cornerRadius: androidx.compose.ui.unit.Dp, strokeWidth: androidx.compose.ui.unit.Dp = 2.dp): Modifier = this.then(
    Modifier.drawWithCache {
        val stroke = strokeWidth.toPx()
        val radius = cornerRadius.toPx()
        val effect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        onDrawBehind {
            drawRoundRect(
                color = color,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, pathEffect = effect),
            )
        }
    },
)

@Composable
fun FiniaSnackbar(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FiniaColors.SnackbarBg)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Text(text, style = FiniaText.ButtonSmall, color = Color.White)
    }
}

@Composable
fun RowIconCircle(
    emoji: String,
    bg: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = (size.value * 0.4f).sp)
    }
}

/**
 * Shared by HomeScreen's Ingresos/Gastos and AccountsScreen's Gastado/Ingresos: a circular
 * colored icon badge + muted label + black amount (not a colored amount), with border/background
 * that swap to [activeBg]/[activeBorder] when acting as a Gasto/Ingreso filter toggle — pass
 * `active = false` and an always-no-op-looking [onClick] for a purely informational (non-filter)
 * usage. One composable so both screens can never visually drift apart from each other again.
 */
@Composable
fun FlowFilterCard(
    modifier: Modifier,
    label: String,
    amountValue: Double,
    icon: ImageVector,
    iconTint: Color,
    active: Boolean,
    activeBg: Color,
    activeBorder: Color,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(if (active) activeBg else Color.White, label = "flowBg")
    val border by animateColorAsState(if (active) activeBorder else FiniaColors.BorderSubtle, label = "flowBorder")
    // Counts up/down through the intermediate values instead of cutting straight to the new
    // total — animates the actual number, not just a crossfade of the text.
    val animatedAmount by animateFloatAsState(
        targetValue = amountValue.toFloat(),
        animationSpec = tween(600, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "flowAmountCounter",
    )
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(16.dp))
            // Background/border already animate on selection — a ripple on top is redundant
            // feedback competing with it, same reasoning as the chips and Gasto/Ingreso toggle.
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(iconTint), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        }
        Column {
            Text(label, style = FiniaText.SecondarySmall, color = FiniaColors.TextSecondary)
            Text(fmt(animatedAmount.toDouble()), style = FiniaText.RowTitleBold.copy(fontSize = 15.sp), color = FiniaColors.TextPrimary)
        }
    }
}
