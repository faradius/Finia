package com.devmastercrack.finia.presentation.finia.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.model.Transaction
import com.devmastercrack.finia.presentation.finia.util.fmt

@Composable
fun TransactionRow(
    tx: Transaction,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowIconCircle(emoji = tx.emoji, bg = FiniaColors.PastelNeutral)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(tx.concepto, style = FiniaText.RowTitle, color = FiniaColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = FiniaText.Secondary, color = FiniaColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        val amountColor = if (tx.monto > 0) FiniaColors.Accent else FiniaColors.TextPrimary
        val sign = if (tx.monto > 0) "+ " else "- "
        Text(
            sign + fmt(kotlin.math.abs(tx.monto)),
            style = FiniaText.RowTitleBold,
            color = amountColor,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
