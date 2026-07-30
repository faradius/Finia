package com.devmastercrack.finia.presentation.finia.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.components.ScrimSheetHost
import com.devmastercrack.finia.presentation.finia.components.SheetHandle
import com.devmastercrack.finia.presentation.finia.components.dashedBorder

@Composable
fun AccountPickerSheet(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    ScrimSheetHost(onDismiss = vm::toggleAccountPicker, modifier = modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { SheetHandle() }
            Text("Cuenta", style = FiniaText.SheetTitle, color = FiniaColors.TextPrimary, modifier = Modifier.padding(bottom = 10.dp))

            state.accounts.forEach { acc ->
                val selected = state.form.cuenta == acc.nombre
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selected) FiniaColors.AccentSoft else Color.White)
                        .clickable { vm.selectAccountForForm(acc.nombre) }
                        .padding(horizontal = 18.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(acc.emoji, fontSize = 18.sp)
                    Text(acc.nombre, style = FiniaText.RowTitle.copy(fontSize = 15.sp), color = FiniaColors.TextPrimary)
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .dashedBorder(Color(0xFFD6D4CD), 16.dp)
                    .clickable(onClick = vm::openNewAccountFromPicker)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(Modifier.size(32.dp).clip(CircleShape).background(FiniaColors.AccentSoft), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = FiniaColors.Accent, modifier = Modifier.size(14.dp))
                }
                Text("Nueva cuenta", style = FiniaText.RowTitle.copy(fontSize = 14.sp), color = FiniaColors.TextPrimary)
            }
        }
    }
}
