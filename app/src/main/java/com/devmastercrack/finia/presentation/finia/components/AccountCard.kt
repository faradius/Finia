package com.devmastercrack.finia.presentation.finia.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.AccountView
import com.devmastercrack.finia.presentation.finia.model.CardBackground
import com.devmastercrack.finia.presentation.finia.model.CardNetwork
import com.devmastercrack.finia.presentation.finia.util.fmt

@Composable
fun AccountCard(
    view: AccountView,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val acc = view.account
    // Fixed width — changing width (animated or not) based on which card is "active" makes
    // every neighboring card's position shift as you scroll, which is what made the carousel's
    // snap-to-center feel broken/glitchy. Active vs inactive is conveyed by opacity alone, which
    // doesn't affect layout at all.
    val width = 296.dp
    val alphaVal by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isActive) 1f else 0.55f,
        animationSpec = androidx.compose.animation.core.tween(110),
        label = "accountCardAlpha",
    )
    val bgModifier = when (val bg = acc.cardBg) {
        is CardBackground.Solid -> Modifier.background(bg.color)
        is CardBackground.Gradient -> Modifier.background(bg.brush)
    }
    Column(
        modifier = modifier
            .width(width)
            .height(190.dp)
            .alpha(alphaVal)
            .clip(RoundedCornerShape(24.dp))
            .then(bgModifier)
            // A ripple sweeping across the card while it's simultaneously sliding into the
            // center (the tap-to-navigate case, isActive == false at tap time) reads as a messy
            // double effect once it arrives. Only the already-centered card — where a tap just
            // opens its detail sheet in place, nothing moves — keeps the ripple.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = if (isActive) LocalIndication.current else null,
                onClick = onClick,
            )
            .padding(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
            Column {
                Text(acc.nombre, style = FiniaText.RowTitleBold.copy(fontSize = 15.sp), color = acc.cardFg)
                Text(acc.tipo.label, style = FiniaText.SecondarySmall, color = acc.cardFg.copy(alpha = 0.65f))
            }
            Box(
                Modifier.size(30.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(acc.emoji, fontSize = 15.sp)
            }
        }
        Text(view.saldoLabel, style = FiniaText.Secondary, color = acc.cardFg.copy(alpha = 0.65f), modifier = Modifier.padding(top = 18.dp))
        Text(fmt(view.available), style = FiniaText.AmountLarge, color = acc.cardFg, modifier = Modifier.padding(top = 2.dp))
        Box(Modifier.weight(1f))
        if (acc.isCredit) {
            Box(
                Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(5.dp)).background(Color.White.copy(alpha = 0.2f)),
            ) {
                Box(
                    Modifier.fillMaxWidth(view.pct / 100f).height(9.dp).clip(RoundedCornerShape(5.dp)).background(view.pctColor),
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            ) {
                Text(view.pctLabel, style = FiniaText.SecondarySmall, color = acc.cardFg.copy(alpha = 0.75f))
                Text("${view.denomLabel} ${fmt(view.denomAmount)}", style = FiniaText.SecondarySmall, color = acc.cardFg.copy(alpha = 0.75f))
                if (acc.network == CardNetwork.MASTERCARD) {
                    MastercardMark(size = 18.dp, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
        if (acc.network == CardNetwork.VISA) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
                VisaMark(color = acc.cardFg)
            }
        }
    }
}

@Composable
fun AddAccountCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(296.dp)
            .height(190.dp)
            .clip(RoundedCornerShape(24.dp))
            .dashedBorder(Color(0xFFC9C6BC), 24.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(FiniaColors.SegmentedTrack),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                androidx.compose.material.icons.Icons.Filled.Add, contentDescription = "Nueva cuenta",
                tint = FiniaColors.Accent, modifier = Modifier.size(20.dp),
            )
        }
        Text("Agregar cuenta", style = FiniaText.RowTitleSemibold, color = Color(0xFF5F6359), modifier = Modifier.padding(top = 10.dp))
    }
}
