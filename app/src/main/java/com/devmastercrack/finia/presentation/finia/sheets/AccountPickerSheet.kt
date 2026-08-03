package com.devmastercrack.finia.presentation.finia.sheets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.devmastercrack.finia.presentation.finia.components.SheetHandle
import com.devmastercrack.finia.presentation.finia.components.dashedBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPickerSheet(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val selectAndClose: (String) -> Unit = { nombre ->
        vm.selectAccountForForm(nombre)
        scope.launch {
            delay(180)
            sheetState.hide()
        }.invokeOnCompletion { if (!sheetState.isVisible) vm.toggleAccountPicker() }
    }

    ModalBottomSheet(
        onDismissRequest = vm::toggleAccountPicker,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), contentAlignment = Alignment.Center) { SheetHandle() }
            Text("Cuenta", style = FiniaText.SheetTitle, color = FiniaColors.TextPrimary, modifier = Modifier.padding(bottom = 10.dp))

            state.accounts.forEach { acc ->
                val selected = state.form.cuenta == acc.nombre
                val rowBg by animateColorAsState(
                    targetValue = if (selected) FiniaColors.AccentSoft else Color.White,
                    animationSpec = tween(150),
                    label = "accountRowBg",
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(rowBg)
                        .clickable { selectAndClose(acc.nombre) }
                        .padding(horizontal = 18.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(FiniaColors.SurfaceNeutral2), contentAlignment = Alignment.Center) {
                        Text(acc.emoji, fontSize = 16.sp)
                    }
                    Text(acc.nombre, style = FiniaText.RowTitle.copy(fontSize = 15.sp), color = FiniaColors.TextPrimary, modifier = Modifier.weight(1f))
                    if (selected) {
                        Box(Modifier.size(18.dp).clip(CircleShape).background(FiniaColors.Accent), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                        }
                    }
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
