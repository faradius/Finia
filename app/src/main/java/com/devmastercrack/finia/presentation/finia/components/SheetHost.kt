package com.devmastercrack.finia.presentation.finia.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Dimmed bottom sheet: tapping the scrim dismisses it. Slides up from the bottom on
 * appearance, with the scrim fading in alongside it.
 *
 * [onDismiss] fires only once the exit animation actually finishes — [content] gets a
 * `requestClose` callback to trigger that animated exit (from an "X" button, a
 * swipe-down gesture, etc.) instead of calling into the ViewModel directly, which would
 * rip the sheet out of composition before it has a chance to animate away.
 */
@Composable
fun ScrimSheetHost(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    content: @Composable (requestClose: () -> Unit) -> Unit,
) {
    val visibleState = remember { MutableTransitionState(false) }.apply { targetState = true }
    LaunchedEffect(visibleState.currentState, visibleState.targetState) {
        if (!visibleState.currentState && !visibleState.targetState) onDismiss()
    }
    val requestClose: () -> Unit = { visibleState.targetState = false }

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(180)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(indication = null, interactionSource = remember(), onClick = requestClose),
            )
        }
        AnimatedVisibility(
            visibleState = visibleState,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(320, easing = FastOutSlowInEasing),
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(220, easing = FastOutSlowInEasing),
            ),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius), clip = false)
                    .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                    .background(Color.White)
                    .clickable(indication = null, interactionSource = remember(), onClick = {}),
            ) {
                content(requestClose)
            }
        }
    }
}

/** Centered dialog with a dimmed scrim (calendar pickers). Fades and scales in. */
@Composable
fun ScrimDialogHost(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val visibleState = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) { visibleState.targetState = true }

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(160)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(indication = null, interactionSource = remember(), onClick = onDismiss),
            )
        }
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.9f, animationSpec = tween(200)),
            exit = fadeOut(tween(160)) + scaleOut(targetScale = 0.9f, animationSpec = tween(160)),
        ) {
            Box(
                Modifier
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), clip = false)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(indication = null, interactionSource = remember(), onClick = {}),
            ) {
                content()
            }
        }
    }
}

/**
 * No-scrim bottom sheet: the region above [passthroughUntil] stays fully click-through to
 * whatever is behind it (the accounts carousel), matching the design's transparent-scrim
 * account sheets. Tapping between [passthroughUntil] and the sheet's top still dismisses it.
 * The sheet itself still slides up on appearance, matching [ScrimSheetHost].
 */
@Composable
fun NoScrimSheetHost(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    passthroughUntil: androidx.compose.ui.unit.Dp = 230.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    content: @Composable (requestClose: () -> Unit) -> Unit,
) {
    val visibleState = remember { MutableTransitionState(false) }.apply { targetState = true }
    LaunchedEffect(visibleState.currentState, visibleState.targetState) {
        if (!visibleState.currentState && !visibleState.targetState) onDismiss()
    }
    val requestClose: () -> Unit = { visibleState.targetState = false }

    Box(modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = passthroughUntil)
                .clickable(indication = null, interactionSource = remember(), onClick = requestClose),
        )
        AnimatedVisibility(
            visibleState = visibleState,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(320, easing = FastOutSlowInEasing),
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(220, easing = FastOutSlowInEasing),
            ),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius), clip = false)
                    .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                    .background(Color.White)
                    .clickable(indication = null, interactionSource = remember(), onClick = {}),
            ) {
                content(requestClose)
            }
        }
    }
}

@Composable
private fun remember(): MutableInteractionSource = androidx.compose.runtime.remember { MutableInteractionSource() }
