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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.devmastercrack.finia.presentation.finia.model.DefaultCategories
import com.devmastercrack.finia.presentation.finia.model.NewCategoryEmojiOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerSheet(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val allCats = DefaultCategories + state.customCategories
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // Apply the selection first so its highlight/checkmark is visible for a beat, then let the
    // sheet animate closed — selecting and closing in the same instant felt like tapping did
    // nothing before the sheet just vanished.
    val selectAndClose: (String) -> Unit = { nombre ->
        vm.selectCategoryManually(nombre)
        scope.launch {
            delay(180)
            sheetState.hide()
        }.invokeOnCompletion { if (!sheetState.isVisible) vm.toggleCategoryPicker() }
    }

    ModalBottomSheet(
        onDismissRequest = vm::toggleCategoryPicker,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), contentAlignment = Alignment.Center) { SheetHandle() }
            Text("Categoría", style = FiniaText.SheetTitle, color = FiniaColors.TextPrimary, modifier = Modifier.padding(bottom = 10.dp))

            // Fixed to ~6 rows (58dp each) so the list scrolls internally instead of growing
            // the sheet — "Nueva categoría" below stays pinned and always visible.
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 348.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                allCats.forEach { c ->
                    val selected = state.form.categoria == c.nombre
                    val rowBg by animateColorAsState(
                        targetValue = if (selected) FiniaColors.AccentSoft else Color.White,
                        animationSpec = tween(150),
                        label = "categoryRowBg",
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(rowBg)
                            .clickable { selectAndClose(c.nombre) }
                            .padding(horizontal = 18.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(Modifier.size(32.dp).clip(CircleShape).background(FiniaColors.SurfaceNeutral2), contentAlignment = Alignment.Center) {
                            Text(c.emoji, fontSize = 16.sp)
                        }
                        Text(c.nombre, style = FiniaText.RowTitle.copy(fontSize = 15.sp), color = FiniaColors.TextPrimary, modifier = Modifier.weight(1f))
                        if (selected) {
                            Box(Modifier.size(18.dp).clip(CircleShape).background(FiniaColors.Accent), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                }
            }

            Box(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(FiniaColors.BorderSubtle2))
            }

            if (state.newCategoryOpen) {
                // No second divider here — the one above (before the if/else) already separates
                // the list from whatever comes next, collapsed or expanded.
                Column(Modifier.fillMaxWidth().padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(FiniaColors.SurfaceNeutral)
                                .clickable(onClick = vm::toggleNewCategoryEmojiPicker),
                            contentAlignment = Alignment.Center,
                        ) { Text(state.newCategoryEmoji, fontSize = 16.sp) }
                        // No background box — plain text on the sheet itself, same treatment as
                        // Monto/Descripción in Nuevo movimiento.
                        BasicTextField(
                            value = state.newCategoryName,
                            onValueChange = vm::onNewCategoryNameChange,
                            singleLine = true,
                            // FiniaText.RowTitle, not a bare TextStyle — a plain TextStyle
                            // defaults to Normal weight instead of the Medium weight every other
                            // row label in this list uses, which read as a different typeface.
                            textStyle = FiniaText.RowTitle.copy(color = FiniaColors.TextPrimary),
                            decorationBox = { inner ->
                                Box(Modifier.padding(vertical = 12.dp)) {
                                    if (state.newCategoryName.isEmpty()) Text("Nombre de categoría", style = FiniaText.RowTitle, color = FiniaColors.TextSecondary)
                                    inner()
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (state.newCategoryEmojiPickerOpen) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NewCategoryEmojiOptions.take(6).forEach { emoji ->
                                Box(
                                    Modifier.size(34.dp).clip(CircleShape).background(FiniaColors.SurfaceNeutral).clickable { vm.setNewCategoryEmoji(emoji) },
                                    contentAlignment = Alignment.Center,
                                ) { Text(emoji, fontSize = 15.sp) }
                            }
                        }
                    }
                    // Cancelar stays a plain text link (dismissing back to the collapsed
                    // trigger), but the confirming/data-creating action gets the same filled-
                    // button weight every other "Guardar"-type action in the app uses — Cancelar/
                    // Mes actual (both neutral, non-data actions) isn't the right precedent here.
                    val enabled = state.newCategoryName.isNotBlank()
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            "Cancelar", style = FiniaText.RowTitleSemibold.copy(fontSize = 13.sp), color = FiniaColors.TextSecondary,
                            modifier = Modifier
                                .clickable(onClick = vm::toggleNewCategory)
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
                        androidx.compose.material3.Button(
                            onClick = vm::addNewCategory,
                            enabled = enabled,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = FiniaColors.Accent,
                                disabledContainerColor = FiniaColors.Accent.copy(alpha = 0.4f),
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(40.dp),
                        ) {
                            Text("Crear", style = FiniaText.ButtonSmall, color = Color.White)
                        }
                    }
                }
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .dashedBorder(Color(0xFFD6D4CD), 16.dp)
                        .clickable(onClick = vm::toggleNewCategory)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(FiniaColors.AccentSoft), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = FiniaColors.Accent, modifier = Modifier.size(14.dp))
                    }
                    Text("Nueva categoría", style = FiniaText.RowTitle.copy(fontSize = 14.sp), color = FiniaColors.TextPrimary)
                }
            }
        }
    }
}
