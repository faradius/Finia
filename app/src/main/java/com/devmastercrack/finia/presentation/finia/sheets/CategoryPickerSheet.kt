package com.devmastercrack.finia.presentation.finia.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.components.ScrimSheetHost
import com.devmastercrack.finia.presentation.finia.components.SheetHandle
import com.devmastercrack.finia.presentation.finia.components.dashedBorder
import com.devmastercrack.finia.presentation.finia.model.DefaultCategories
import com.devmastercrack.finia.presentation.finia.model.NewCategoryEmojiOptions

@Composable
fun CategoryPickerSheet(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val allCats = DefaultCategories + state.customCategories
    ScrimSheetHost(onDismiss = vm::toggleCategoryPicker, modifier = modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { SheetHandle() }
            Text("Categoría", style = FiniaText.SheetTitle, color = FiniaColors.TextPrimary, modifier = Modifier.padding(bottom = 10.dp))

            allCats.forEach { c ->
                val selected = state.form.categoria == c.nombre
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selected) FiniaColors.AccentSoft else Color.White)
                        .clickable { vm.selectCategoryManually(c.nombre) }
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

            Box(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(FiniaColors.BorderSubtle2))
            }

            if (state.newCategoryOpen) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(FiniaColors.SurfaceNeutral)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(Color.White).border(1.dp, FiniaColors.BorderSubtle, CircleShape)
                                .clickable(onClick = vm::toggleNewCategoryEmojiPicker),
                            contentAlignment = Alignment.Center,
                        ) { Text(state.newCategoryEmoji, fontSize = 16.sp) }
                        BasicTextField(
                            value = state.newCategoryName,
                            onValueChange = vm::onNewCategoryNameChange,
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = FiniaColors.TextPrimary),
                            decorationBox = { inner ->
                                Box(
                                    Modifier.background(Color.White, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
                                ) {
                                    if (state.newCategoryName.isEmpty()) Text("Nombre de categoría", style = TextStyle(fontSize = 14.sp), color = FiniaColors.TextSecondary)
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
                                    Modifier.size(34.dp).clip(CircleShape).background(Color.White).clickable { vm.setNewCategoryEmoji(emoji) },
                                    contentAlignment = Alignment.Center,
                                ) { Text(emoji, fontSize = 15.sp) }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(
                            "Cancelar", style = FiniaText.ButtonSmall, color = Color(0xFF5F6359),
                            modifier = Modifier.clickable(onClick = vm::toggleNewCategory).padding(horizontal = 10.dp, vertical = 10.dp),
                        )
                        val enabled = state.newCategoryName.isNotBlank()
                        Text(
                            "Agregar", style = FiniaText.ButtonSmall, color = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(FiniaColors.Accent.copy(alpha = if (enabled) 1f else 0.5f))
                                .clickable(enabled = enabled, onClick = vm::addNewCategory)
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                        )
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
