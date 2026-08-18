package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SalmonSample
import java.text.DecimalFormat

enum class SortOrder(val label: String) {
    NEWEST("Más recientes primero"),
    OLDEST("Más antiguos primero"),
    WEIGHT_DESC("Mayor peso a menor"),
    WEIGHT_ASC("Menor peso a mayor"),
    LENGTH_DESC("Mayor longitud")
}

@Composable
fun SampleTableSection(
    samples: List<SalmonSample>,
    onUpdateSample: (SalmonSample) -> Unit,
    onDeleteSample: (SalmonSample) -> Unit,
    onClearAll: () -> Unit,
    cagesList: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val df1 = remember { DecimalFormat("#,##0.0") }
    val df2 = remember { DecimalFormat("#,##0.00") }

    var filterCage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.NEWEST) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Dialog states
    var sampleToEdit by remember { mutableStateOf<SalmonSample?>(null) }
    var sampleToDelete by remember { mutableStateOf<SalmonSample?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    // Filter & sort logic
    val filteredSamples = remember(samples, filterCage, searchQuery, sortOrder) {
        samples
            .filter { sample ->
                val matchesCage = filterCage == null || sample.cageNumber == filterCage
                val matchesQuery = searchQuery.isBlank() ||
                        sample.cageNumber.contains(searchQuery, ignoreCase = true) ||
                        sample.samplingDate.contains(searchQuery, ignoreCase = true) ||
                        sample.notes.contains(searchQuery, ignoreCase = true) ||
                        sample.weightGrams.toString().contains(searchQuery)
                matchesCage && matchesQuery
            }
            .let { list ->
                when (sortOrder) {
                    SortOrder.NEWEST -> list.sortedByDescending { it.timestamp }
                    SortOrder.OLDEST -> list.sortedBy { it.timestamp }
                    SortOrder.WEIGHT_DESC -> list.sortedByDescending { it.weightGrams }
                    SortOrder.WEIGHT_ASC -> list.sortedBy { it.weightGrams }
                    SortOrder.LENGTH_DESC -> list.sortedByDescending { it.lengthCm ?: 0.0 }
                }
            }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sample_table_section"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Table Title & Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Tabla de Mediciones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${filteredSamples.size} registros encontrados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sort Menu
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Ordenar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            SortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label) },
                                    onClick = {
                                        sortOrder = order
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Clear All button (if not empty)
                    if (samples.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Borrar todo",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Search and Filter Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por jaula, peso, fecha o notas...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("table_search_input")
            )

            // Cage Filter Chips
            if (cagesList.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = filterCage == null,
                        onClick = { filterCage = null },
                        label = { Text("Todas las jaulas") }
                    )
                    cagesList.forEach { cage ->
                        FilterChip(
                            selected = filterCage == cage,
                            onClick = { filterCage = if (filterCage == cage) null else cage },
                            label = { Text("Jaula $cage") }
                        )
                    }
                }
            }

            // Visual Data Table
            if (filteredSamples.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (samples.isEmpty()) "No hay muestras registradas aún." else "Ningún registro coincide con los filtros.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Horizontally scrollable high contrast table
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        // Table Header Row
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "N°",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.width(36.dp)
                            )
                            Text(
                                text = "Jaula",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.width(75.dp)
                            )
                            Text(
                                text = "Peso (g)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.width(85.dp)
                            )
                            Text(
                                text = "Peso (kg)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.width(75.dp)
                            )
                            Text(
                                text = "Talla (cm)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.width(75.dp)
                            )
                            Text(
                                text = "Factor K",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                text = "Condición",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.width(100.dp)
                            )
                            Text(
                                text = "Hora",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.width(70.dp)
                            )
                            Text(
                                text = "Acciones",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.width(80.dp)
                            )
                        }

                        // Table Body Rows
                        filteredSamples.forEachIndexed { index, sample ->
                            val isEven = index % 2 == 0
                            Row(
                                modifier = Modifier
                                    .background(
                                        if (isEven) MaterialTheme.colorScheme.surface
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Index
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(36.dp)
                                )

                                // Cage
                                Text(
                                    text = sample.cageNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.width(75.dp)
                                )

                                // Weight (g)
                                Text(
                                    text = "${df1.format(sample.weightGrams)} g",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(85.dp)
                                )

                                // Weight (kg)
                                Text(
                                    text = "${df2.format(sample.weightKg)} kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.width(75.dp)
                                )

                                // Length (cm)
                                Text(
                                    text = sample.lengthCm?.let { "${df1.format(it)} cm" } ?: "-",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (sample.lengthCm != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (sample.lengthCm != null) FontWeight.Medium else FontWeight.Normal,
                                    modifier = Modifier.width(75.dp)
                                )

                                // Factor K
                                Text(
                                    text = sample.fultonK?.let { df2.format(it) } ?: "-",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when {
                                        sample.fultonK == null -> MaterialTheme.colorScheme.onSurfaceVariant
                                        sample.fultonK!! < 0.95 -> Color(0xFFF59E0B)
                                        sample.fultonK!! in 0.95..1.25 -> Color(0xFF10B981)
                                        else -> Color(0xFF3B82F6)
                                    },
                                    modifier = Modifier.width(80.dp)
                                )

                                // Condición badge
                                Box(modifier = Modifier.width(100.dp)) {
                                    val (badgeBg, badgeFg) = when {
                                        sample.fultonK == null -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                                        sample.fultonK!! < 0.95 -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
                                        sample.fultonK!! in 0.95..1.25 -> Pair(Color(0xFFD1FAE5), Color(0xFF047857))
                                        else -> Pair(Color(0xFFDBEAFE), Color(0xFF1D4ED8))
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = badgeBg
                                    ) {
                                        Text(
                                            text = when {
                                                sample.fultonK == null -> "Sin talla"
                                                sample.fultonK!! < 0.95 -> "Delgado"
                                                sample.fultonK!! in 0.95..1.25 -> "Óptimo"
                                                else -> "Robusto"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeFg,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Hora
                                Text(
                                    text = sample.formattedTime,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(70.dp)
                                )

                                // Action Buttons
                                Row(
                                    modifier = Modifier.width(80.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    IconButton(
                                        onClick = { sampleToEdit = sample },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { sampleToDelete = sample },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Sample Dialog
    if (sampleToEdit != null) {
        val sample = sampleToEdit!!
        var editWeight by remember { mutableStateOf(sample.weightGrams.toString()) }
        var editLength by remember { mutableStateOf(sample.lengthCm?.toString() ?: "") }
        var editCage by remember { mutableStateOf(sample.cageNumber) }
        var editNotes by remember { mutableStateOf(sample.notes) }
        var editError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { sampleToEdit = null },
            title = { Text("Editar Medición") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editCage,
                        onValueChange = { editCage = it },
                        label = { Text("N° Jaula") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editWeight,
                        onValueChange = { editWeight = it },
                        label = { Text("Peso en gramos (g) *") },
                        singleLine = true,
                        isError = editError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editLength,
                        onValueChange = { editLength = it },
                        label = { Text("Longitud en cm (Opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Notas / Observaciones") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (editError != null) {
                        Text(
                            text = editError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = editWeight.toDoubleOrNull()
                        if (w == null || w <= 0) {
                            editError = "Por favor ingresa un peso válido mayor a 0"
                            return@Button
                        }
                        val l = editLength.toDoubleOrNull()
                        onUpdateSample(
                            sample.copy(
                                cageNumber = editCage.ifBlank { "Jaula 1" },
                                weightGrams = w,
                                lengthCm = if (l != null && l > 0) l else null,
                                notes = editNotes
                            )
                        )
                        sampleToEdit = null
                    }
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { sampleToEdit = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete Sample Confirmation Dialog
    if (sampleToDelete != null) {
        val sample = sampleToDelete!!
        AlertDialog(
            onDismissRequest = { sampleToDelete = null },
            title = { Text("Eliminar Registro") },
            text = {
                Text("¿Estás seguro de eliminar el registro de ${df1.format(sample.weightGrams)} g de la ${sample.cageNumber}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSample(sample)
                        sampleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { sampleToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Limpiar Todas las Muestras") },
            text = {
                Text("¿Deseas eliminar todas las ${samples.size} mediciones registradas? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Borrar Todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
