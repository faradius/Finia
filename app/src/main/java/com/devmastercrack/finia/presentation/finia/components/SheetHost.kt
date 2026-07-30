package com.devmastercrack.finia.presentation.finia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Dimmed bottom sheet: tapping the scrim dismisses it. */
@Composable
fun ScrimSheetHost(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(indication = null, interactionSource = remember(), onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                .background(Color.White)
                .clickable(indication = null, interactionSource = remember(), onClick = {}),
        ) {
            content()
        }
    }
}

/** Centered dialog with a dimmed scrim (calendar pickers). */
@Composable
fun ScrimDialogHost(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(indication = null, interactionSource = remember(), onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.clickable(indication = null, interactionSource = remember(), onClick = {})) {
            content()
        }
    }
}

/**
 * No-scrim bottom sheet: the region above [passthroughUntil] stays fully click-through to
 * whatever is behind it (the accounts carousel), matching the design's transparent-scrim
 * account sheets. Tapping between [passthroughUntil] and the sheet's top still dismisses it.
 */
@Composable
fun NoScrimSheetHost(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    passthroughUntil: androidx.compose.ui.unit.Dp = 230.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    content: @Composable () -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = passthroughUntil)
                .clickable(indication = null, interactionSource = remember(), onClick = onDismiss),
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                .background(Color.White)
                .clickable(indication = null, interactionSource = remember(), onClick = {}),
        ) {
            content()
        }
    }
}

@Composable
private fun remember(): MutableInteractionSource = androidx.compose.runtime.remember { MutableInteractionSource() }
