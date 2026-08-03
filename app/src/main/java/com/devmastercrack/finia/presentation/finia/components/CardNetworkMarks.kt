package com.devmastercrack.finia.presentation.finia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText

/** No real brand assets — a simple italic-bold wordmark, per the design's icon policy. */
@Composable
fun VisaMark(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Text("VISA", style = FiniaText.VisaMark, color = color, modifier = modifier)
}

@Composable
fun MastercardMark(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 18.dp) {
    Box(modifier = modifier) {
        Box(Modifier.size(size).background(FiniaColors.MastercardRed, CircleShape))
        Box(
            Modifier.size(size)
                .offset(x = size * 0.6f)
                .background(FiniaColors.MastercardOrange.copy(alpha = 0.9f), CircleShape),
        )
    }
}
