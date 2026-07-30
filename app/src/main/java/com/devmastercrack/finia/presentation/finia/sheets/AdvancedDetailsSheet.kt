package com.devmastercrack.finia.presentation.finia.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
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
import com.devmastercrack.finia.presentation.finia.components.FiniaChip
import com.devmastercrack.finia.presentation.finia.components.FiniaSwitch
import com.devmastercrack.finia.presentation.finia.model.PeopleOptions
import com.devmastercrack.finia.presentation.finia.util.fmt

@Composable
fun AdvancedDetailsSheet(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(Color.White)) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = FiniaColors.TextPrimary,
                modifier = Modifier.size(20.dp).clickable(onClick = vm::closeAdvanced),
            )
            Text("Detalles avanzados", style = FiniaText.SheetTitle.copy(fontSize = 17.sp), color = FiniaColors.TextPrimary, modifier = Modifier.padding(start = 12.dp))
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            AdvancedField("Fecha", state.form.fecha, { vm.updateFormField { f -> f.copy(fecha = it) } })
            AdvancedField("Hora", state.form.hora, { vm.updateFormField { f -> f.copy(hora = it) } })
            AdvancedField("Etiquetas", state.form.etiquetas, { vm.updateFormField { f -> f.copy(etiquetas = it) } }, "Ej. viaje, familia")
            AdvancedField("Ubicación", state.form.ubicacion, { vm.updateFormField { f -> f.copy(ubicacion = it) } }, "Ej. Ciudad de México")
            AdvancedField("Persona", state.form.persona, { vm.updateFormField { f -> f.copy(persona = it) } }, "Ej. Carlos")
            AdvancedField("Proyecto", state.form.proyecto, { vm.updateFormField { f -> f.copy(proyecto = it) } }, "Ej. Remodelación")

            Text("Método de pago", style = FiniaText.Label.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary, modifier = Modifier.padding(top = 4.dp, bottom = 6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 14.dp)) {
                listOf("Tarjeta", "Efectivo", "Transferencia").forEach { m ->
                    FiniaChip(
                        m, selected = state.form.metodoPago == m,
                        onClick = { vm.updateFormField { f -> f.copy(metodoPago = m) } },
                        selectedBg = FiniaColors.SurfaceNeutral2, selectedFg = FiniaColors.TextPrimary,
                    )
                }
            }

            ToggleRow("Dividir entre varias personas", state.splitOn, vm::toggleSplit)
            ToggleRow("A meses sin intereses", state.cuotasOn, vm::toggleCuotas)
            ToggleRow("Recordatorio", state.recordatorioOn, vm::toggleRecordatorio)
            ToggleRow("Movimiento recurrente", state.recurrenteOn, vm::toggleRecurrente)

            if (state.splitOn) {
                Text("Dividir con...", style = FiniaText.Label.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary, modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PeopleOptions.forEach { name ->
                        FiniaChip(
                            name, selected = name in state.selectedPeople, onClick = { vm.togglePerson(name) },
                            selectedBg = FiniaColors.AccentSoft, selectedFg = FiniaColors.TextPrimary,
                        )
                    }
                }
                if (state.selectedPeople.isNotEmpty()) {
                    val monto = state.form.monto.toDoubleOrNull() ?: 0.0
                    val n = state.selectedPeople.size + 1
                    Text(
                        "Cada quien paga ${fmt(monto / n)} (incluyéndote)", style = FiniaText.SecondarySmall, color = FiniaColors.TextSecondary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            if (state.cuotasOn) {
                Text("Número de meses", style = FiniaText.Label.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary, modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 6, 9, 12).forEach { c ->
                        FiniaChip(
                            "${c}x", selected = state.form.cuotas == c, onClick = { vm.setCuotas(c) },
                            selectedBg = FiniaColors.AccentSoft, selectedFg = FiniaColors.TextPrimary,
                        )
                    }
                }
                val monto = state.form.monto.toDoubleOrNull() ?: 0.0
                if (monto > 0) {
                    Text(
                        "${state.form.cuotas} pagos de ${fmt(monto / state.form.cuotas)}", style = FiniaText.SecondarySmall, color = FiniaColors.TextSecondary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            Text("Notas largas", style = FiniaText.Label.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary, modifier = Modifier.padding(top = 14.dp, bottom = 6.dp))
            BasicTextField(
                value = state.form.notaLarga,
                onValueChange = { vm.updateFormField { f -> f.copy(notaLarga = it) } },
                textStyle = TextStyle(fontSize = 14.sp, color = FiniaColors.TextPrimary),
                decorationBox = { inner ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .border(1.dp, FiniaColors.BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        if (state.form.notaLarga.isEmpty()) Text("Añade contexto adicional...", style = TextStyle(fontSize = 14.sp), color = FiniaColors.TextSecondary)
                        inner()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashedActionButton("📷 Adjuntar ticket", Modifier.weight(1f))
                DashedActionButton("📎 Adjuntar archivo", Modifier.weight(1f))
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .border(1.dp, FiniaColors.BorderSubtle, RoundedCornerShape(16.dp))
                    .clickable(onClick = vm::toggleDestAccountPicker)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(state.accounts.find { it.nombre == state.form.cuentaDestino }?.emoji ?: "➕", fontSize = 16.sp)
                Text(
                    "Cuenta destino (opcional)", style = FiniaText.SecondarySmall.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary,
                    modifier = Modifier.padding(start = 10.dp),
                )
                Text(
                    state.form.cuentaDestino.ifBlank { "Ninguna" }, style = FiniaText.RowTitleBold.copy(fontSize = 14.sp), color = FiniaColors.TextPrimary,
                    modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End,
                )
            }
            Box(Modifier.height(100.dp))
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
            androidx.compose.material3.Button(
                onClick = vm::closeAdvanced,
                colors = ButtonDefaults.buttonColors(containerColor = FiniaColors.TextPrimary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Listo", style = FiniaText.Button.copy(fontSize = 15.sp), color = Color.White)
            }
        }
    }
}

@Composable
private fun AdvancedField(label: String, value: String, onChange: (String) -> Unit, placeholder: String = "") {
    Column(Modifier.padding(bottom = 12.dp)) {
        Text(label, style = FiniaText.Label.copy(fontSize = 12.sp), color = FiniaColors.TextSecondary)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = FiniaColors.TextPrimary),
            decorationBox = { inner ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .border(1.dp, FiniaColors.BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(placeholder, style = TextStyle(fontSize = 14.sp), color = FiniaColors.TextSecondary)
                    }
                    inner()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = FiniaText.RowTitle.copy(fontSize = 14.sp), color = FiniaColors.TextPrimary)
        FiniaSwitch(checked = checked, onCheckedChange = onToggle, activeColor = FiniaColors.TextPrimary)
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(FiniaColors.BorderSubtle2))
}

@Composable
private fun DashedActionButton(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFC9C7C0), RoundedCornerShape(14.dp))
            .clickable {}
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = FiniaText.SecondarySmall.copy(fontSize = 13.sp), color = FiniaColors.TextMuted)
    }
}
