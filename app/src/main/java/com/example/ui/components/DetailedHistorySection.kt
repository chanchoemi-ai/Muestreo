package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SalmonSample
import com.example.ui.SalmonViewModel
import com.example.ui.theme.ConditionAlert
import com.example.ui.theme.ConditionLow
import com.example.ui.theme.ConditionOptimum
import com.example.ui.theme.ConditionRobust
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DetailedHistorySection(
    allSamples: List<SalmonSample>,
    cageTargets: Map<String, Double>,
    selectedCageFilter: String,
    onCageFilterChange: (String) -> Unit,
    selectedDateFilter: String,
    onDateFilterChange: (String) -> Unit,
    startDateFilter: String?,
    endDateFilter: String?,
    onDateRangeChange: (String?, String?) -> Unit,
    onClearFilters: () -> Unit,
    onUpdateSample: (SalmonSample) -> Unit,
    onDeleteSample: (SalmonSample) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cages = remember { listOf("TODAS") + SalmonViewModel.STANDARD_CAGES }

    // Dialog state for edit/delete
    var sampleToEdit by remember { mutableStateOf<SalmonSample?>(null) }
    var sampleToDelete by remember { mutableStateOf<SalmonSample?>(null) }

    // Expanded states for session groups
    val expandedSessions = remember { mutableStateMapOf<String, Boolean>() }

    // Filter samples
    val filteredSamples = remember(
        allSamples, selectedCageFilter, selectedDateFilter, startDateFilter, endDateFilter
    ) {
        allSamples.filter { sample ->
            val matchCage = (selectedCageFilter == "TODAS" || sample.cageNumber.equals(selectedCageFilter, ignoreCase = true))
            val matchDate = (selectedDateFilter == "TODAS" || sample.samplingDate == selectedDateFilter)
            val matchRange = (startDateFilter == null || sample.samplingDate >= startDateFilter) &&
                    (endDateFilter == null || sample.samplingDate <= endDateFilter)
            matchCage && matchDate && matchRange
        }
    }

    // Group filtered samples by Session (Fecha + Jaula)
    // Key: "samplingDate|cageNumber"
    val sessions = remember(filteredSamples) {
        filteredSamples
            .groupBy { "${it.samplingDate}|${it.cageNumber}" }
            .map { (key, list) ->
                val parts = key.split("|")
                val date = parts[0]
                val cage = parts.getOrElse(1) { "J-101" }
                // Sort ascending by timestamp to calculate true correlative numbers within this session
                val sortedList = list.sortedBy { it.timestamp }
                SessionGroup(
                    sessionKey = key,
                    date = date,
                    cage = cage,
                    samples = sortedList
                )
            }
            .sortedWith(compareByDescending<SessionGroup> { it.date }.thenBy { it.cage })
    }

    // Date picker helpers
    val calendar = remember { Calendar.getInstance() }
    val startDatePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                onDateRangeChange(sdf.format(cal.time), endDateFilter)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val endDatePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                onDateRangeChange(startDateFilter, sdf.format(cal.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val df0 = remember { DecimalFormat("#,##0") }
    val df1 = remember { DecimalFormat("#,##0.0") }
    val df2 = remember { DecimalFormat("#,##0.00") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("detailed_history_section"),
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Historial Detallado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Agrupado por Fecha y Jaula • ${filteredSamples.size} registros",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (selectedCageFilter != "TODAS" || selectedDateFilter != "TODAS" || startDateFilter != null || endDateFilter != null) {
                    TextButton(
                        onClick = onClearFilters,
                        modifier = Modifier.testTag("clear_filters_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Filter 1: Cage Selection Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "FILTRAR POR JAULA:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cages.forEach { cage ->
                        val isSelected = selectedCageFilter.equals(cage, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCageFilterChange(cage) },
                            label = {
                                Text(
                                    text = cage,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("filter_chip_$cage")
                        )
                    }
                }
            }

            // Filter 2: Date Range Controls
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "FILTRAR POR FECHA / RANGO:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start Date Button
                    OutlinedButton(
                        onClick = { startDatePicker.show() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = startDateFilter ?: "Desde",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }

                    // End Date Button
                    OutlinedButton(
                        onClick = { endDatePicker.show() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = endDateFilter ?: "Hasta",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }

                    // Quick Today Button
                    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
                    FilterChip(
                        selected = selectedDateFilter == today,
                        onClick = {
                            if (selectedDateFilter == today) {
                                onDateFilterChange("TODAS")
                            } else {
                                onDateFilterChange(today)
                                onDateRangeChange(null, null)
                            }
                        },
                        label = { Text("Hoy") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Summary Bar
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SESIONES ENCONTRADAS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${sessions.size} sesiones de muestreo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TOTAL PECES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${filteredSamples.size} ejemplares",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sessions List
            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No se encontraron muestras para los filtros seleccionados",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sessions.forEach { session ->
                        val isExpanded = expandedSessions[session.sessionKey] ?: true
                        SessionCard(
                            session = session,
                            targetWeightGrams = cageTargets[session.cage] ?: 3500.0,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedSessions[session.sessionKey] = !isExpanded
                            },
                            onEditSample = { sampleToEdit = it },
                            onDeleteSample = { sampleToDelete = it }
                        )
                    }
                }
            }
        }
    }

    // Edit Sample Dialog
    sampleToEdit?.let { sample ->
        EditSampleDialog(
            sample = sample,
            onDismiss = { sampleToEdit = null },
            onSave = { updated ->
                onUpdateSample(updated)
                sampleToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    sampleToDelete?.let { sample ->
        AlertDialog(
            onDismissRequest = { sampleToDelete = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("¿Eliminar registro?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar la muestra de ${sample.weightGrams.toInt()} g de la ${sample.cageNumber} (${sample.samplingDate})?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSample(sample)
                        sampleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
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
}

data class SessionGroup(
    val sessionKey: String,
    val date: String,
    val cage: String,
    val samples: List<SalmonSample>
) {
    val totalCount: Int get() = samples.size
    val isQuotaMet: Boolean get() = totalCount >= 50

    val avgWeight: Double get() = if (samples.isNotEmpty()) samples.map { it.weightGrams }.average() else 0.0

    val lengthSamples = samples.filter { it.lengthCm != null && it.lengthCm > 0 }
    val avgLength: Double? get() = if (lengthSamples.isNotEmpty()) lengthSamples.mapNotNull { it.lengthCm }.average() else null

    val kFactors = samples.mapNotNull { it.fultonK }
    val avgK: Double? get() = if (kFactors.isNotEmpty()) kFactors.average() else null
}

@Composable
fun SessionCard(
    session: SessionGroup,
    targetWeightGrams: Double,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEditSample: (SalmonSample) -> Unit,
    onDeleteSample: (SalmonSample) -> Unit
) {
    val df0 = remember { DecimalFormat("#,##0") }
    val df1 = remember { DecimalFormat("#,##0.0") }
    val df2 = remember { DecimalFormat("#,##0.00") }

    val diffGrams = session.avgWeight - targetWeightGrams
    val diffPercent = if (targetWeightGrams > 0) (diffGrams / targetWeightGrams) * 100.0 else 0.0

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Session Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = session.cage,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = session.date,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        // 50-fish quota status badge
                        if (session.isQuotaMet) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ConditionOptimum.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ConditionOptimum)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ConditionOptimum,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Cuota 50 OK (${session.totalCount})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ConditionOptimum,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ConditionLow.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ConditionLow)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ConditionLow,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Incompleto: ${session.totalCount}/50 (Faltan ${50 - session.totalCount})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ConditionLow,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // Stats line: Average weight and target diff
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Promedio: ${df0.format(session.avgWeight)} g (${df2.format(session.avgWeight / 1000.0)} kg)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Target comparison
                        val diffSign = if (diffGrams >= 0) "+" else ""
                        val diffColor = if (diffGrams >= 0) ConditionOptimum else ConditionAlert
                        Text(
                            text = "vs Objetivo: $diffSign${df0.format(diffGrams)} g ($diffSign${df1.format(diffPercent)}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = diffColor
                        )
                    }
                }

                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Colapsar" else "Expandir"
                    )
                }
            }

            // Expanded Samples List
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Table Column Headers
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "N°",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(36.dp)
                            )
                            Text(
                                text = "HORA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp)
                            )
                            Text(
                                text = "PESO (g)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "TALLA (cm)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "FACTOR K",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "ACCIONES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(64.dp)
                            )
                        }
                    }

                    // Individual Sample Rows with Correlative Number
                    session.samples.forEachIndexed { index, sample ->
                        val correlative = index + 1
                        SampleHistoryRow(
                            correlative = correlative,
                            sample = sample,
                            onEdit = { onEditSample(sample) },
                            onDelete = { onDeleteSample(sample) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun SampleHistoryRow(
    correlative: Int,
    sample: SalmonSample,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val df0 = remember { DecimalFormat("#,##0") }
    val df1 = remember { DecimalFormat("#,##0.0") }
    val df2 = remember { DecimalFormat("#,##0.00") }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Correlative number (Pez #N)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.width(36.dp)
            ) {
                Text(
                    text = "#$correlative",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Time
            Text(
                text = sample.formattedTime,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(60.dp)
            )

            // Weight (Mandatory)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${df0.format(sample.weightGrams)} g",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${df2.format(sample.weightKg)} kg",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Length (Optional)
            Box(modifier = Modifier.weight(1f)) {
                if (sample.lengthCm != null && sample.lengthCm > 0) {
                    Text(
                        text = "${df1.format(sample.lengthCm)} cm",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Fulton K
            Box(modifier = Modifier.weight(1f)) {
                if (sample.fultonK != null) {
                    val kVal = sample.fultonK!!
                    val kColor = when {
                        kVal < 0.95 -> ConditionLow
                        kVal in 0.95..1.25 -> ConditionOptimum
                        else -> ConditionRobust
                    }
                    Text(
                        text = df2.format(kVal),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = kColor
                    )
                } else {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Actions (Edit & Delete)
            Row(
                modifier = Modifier.width(64.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditSampleDialog(
    sample: SalmonSample,
    onDismiss: () -> Unit,
    onSave: (SalmonSample) -> Unit
) {
    var weightText by remember { mutableStateOf(sample.weightGrams.toInt().toString()) }
    var lengthText by remember { mutableStateOf(sample.lengthCm?.toString() ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Editar Muestra (${sample.cageNumber})",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = {
                        weightText = it
                        errorMessage = null
                    },
                    label = { Text("Peso en gramos (g) *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lengthText,
                    onValueChange = { lengthText = it },
                    label = { Text("Longitud en cm (Opcional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightText.trim().replace(",", ".").toDoubleOrNull()
                    if (w == null || w <= 0) {
                        errorMessage = "Ingresa un peso válido mayor a 0"
                        return@Button
                    }
                    val l = lengthText.trim().replace(",", ".").toDoubleOrNull()
                    onSave(
                        sample.copy(
                            weightGrams = w,
                            lengthCm = if (l != null && l > 0) l else null
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
