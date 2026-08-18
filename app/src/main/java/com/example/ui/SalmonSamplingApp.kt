package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.CageTargetsSection
import com.example.ui.components.DetailedHistorySection
import com.example.ui.components.ExportDialog
import com.example.ui.components.LiveStatsCards
import com.example.ui.components.SalmonChartSection
import com.example.ui.components.SampleEntryCard
import com.example.ui.components.SampleTableSection
import com.example.ui.components.TargetWeightDialog
import com.example.ui.components.VoiceInputDialog
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ENTRY("Muestreo", Icons.Default.Add),
    HISTORY("Historial", Icons.Default.History),
    CHARTS("Gráficos", Icons.Default.BarChart),
    TARGETS("Objetivos", Icons.Default.Flag)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalmonSamplingApp(
    viewModel: SalmonViewModel,
    modifier: Modifier = Modifier
) {
    val samples by viewModel.allSamples.collectAsStateWithLifecycle()
    val distinctCages by viewModel.distinctCages.collectAsStateWithLifecycle()
    val cageNumber by viewModel.cageNumber.collectAsStateWithLifecycle()
    val samplingDate by viewModel.samplingDate.collectAsStateWithLifecycle()
    val weightInput by viewModel.weightInput.collectAsStateWithLifecycle()
    val lengthInput by viewModel.lengthInput.collectAsStateWithLifecycle()
    val lastAddedSample by viewModel.lastAddedSample.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val showExportDialog by viewModel.showExportDialog.collectAsStateWithLifecycle()
    val showTargetWeightDialog by viewModel.showTargetWeightDialog.collectAsStateWithLifecycle()
    val showVoiceDialog by viewModel.showVoiceDialog.collectAsStateWithLifecycle()
    val isAutoSaveVoice by viewModel.isAutoSaveVoice.collectAsStateWithLifecycle()

    val currentSessionSamples by viewModel.currentSessionSamples.collectAsStateWithLifecycle()
    val currentSessionCount by viewModel.currentSessionCount.collectAsStateWithLifecycle()
    val cageTargetsMap by viewModel.cageTargetsMap.collectAsStateWithLifecycle()

    val historyCageFilter by viewModel.historyCageFilter.collectAsStateWithLifecycle()
    val historyDateFilter by viewModel.historyDateFilter.collectAsStateWithLifecycle()
    val historyStartDate by viewModel.historyDateRangeStart.collectAsStateWithLifecycle()
    val historyEndDate by viewModel.historyDateRangeEnd.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val weightFocusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Seed initial demo data on launch if empty so the user can explore graphs and history immediately
    LaunchedEffect(Unit) {
        viewModel.seedDemoDataIfEmpty()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.salmon_logo),
                                contentDescription = "Logo Muestreo",
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Muestreo Salmones",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Biometría & Peso en Tiempo Real",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    // Voice Input Trigger Action (Hands-free sampling)
                    IconButton(
                        onClick = { viewModel.setShowVoiceDialog(true) },
                        modifier = Modifier.testTag("top_bar_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Muestreo por voz",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Target Weight Quick Action
                    IconButton(
                        onClick = { viewModel.setShowTargetWeightDialog(true) },
                        modifier = Modifier.testTag("top_bar_targets_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Pesos Objetivo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Export Action Button (Excel, PDF, CSV)
                    IconButton(
                        onClick = { viewModel.setShowExportDialog(true) },
                        modifier = Modifier.testTag("top_bar_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Exportar datos",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Night mode / Dark mode toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("dark_mode_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Cambiar tema",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                AppNavTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            coroutineScope.launch {
                                listState.scrollToItem(0)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { viewModel.setShowVoiceDialog(true) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_voice_sampling")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Dictar Muestra por Voz"
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    // TAB 0: MUESTREO (Ingreso rápido + Correlativo + Alerta 50 + KPI en vivo + Comparación Objetivo)
                    0 -> {
                        item {
                            SampleEntryCard(
                                cageNumber = cageNumber,
                                onCageNumberChange = { viewModel.updateCageNumber(it) },
                                samplingDate = samplingDate,
                                onSamplingDateChange = { viewModel.updateSamplingDate(it) },
                                weightInput = weightInput,
                                onWeightInputChange = { viewModel.updateWeightInput(it) },
                                lengthInput = lengthInput,
                                onLengthInputChange = { viewModel.updateLengthInput(it) },
                                onSaveSample = {
                                    val saved = viewModel.saveSample()
                                    if (saved) {
                                        coroutineScope.launch {
                                            weightFocusRequester.requestFocus()
                                        }
                                    }
                                },
                                onOpenVoiceInput = { viewModel.setShowVoiceDialog(true) },
                                onOpenTargetSettings = { viewModel.setShowTargetWeightDialog(true) },
                                targetWeightGrams = cageTargetsMap[cageNumber] ?: 3500.0,
                                currentSessionCount = currentSessionCount,
                                lastAddedSample = lastAddedSample,
                                errorMessage = errorMessage,
                                weightFocusRequester = weightFocusRequester
                            )
                        }

                        item {
                            LiveStatsCards(
                                samples = currentSessionSamples.ifEmpty { samples.filter { it.cageNumber == cageNumber } },
                                targetWeightGrams = cageTargetsMap[cageNumber] ?: 3500.0,
                                cageName = cageNumber,
                                onOpenTargetSettings = { viewModel.setShowTargetWeightDialog(true) }
                            )
                        }

                        // Quick Preview Table for active cage & date
                        item {
                            SampleTableSection(
                                samples = currentSessionSamples.ifEmpty { samples.filter { it.cageNumber == cageNumber } },
                                onUpdateSample = { viewModel.updateSample(it) },
                                onDeleteSample = { viewModel.deleteSample(it) },
                                onClearAll = { viewModel.clearAll() }
                            )
                        }
                    }

                    // TAB 1: HISTORIAL DETALLADO (Agrupado por Fecha y Jaula + Filtros completos)
                    1 -> {
                        item {
                            DetailedHistorySection(
                                allSamples = samples,
                                cageTargets = cageTargetsMap,
                                selectedCageFilter = historyCageFilter,
                                onCageFilterChange = { viewModel.setHistoryCageFilter(it) },
                                selectedDateFilter = historyDateFilter,
                                onDateFilterChange = { viewModel.setHistoryDateFilter(it) },
                                startDateFilter = historyStartDate,
                                endDateFilter = historyEndDate,
                                onDateRangeChange = { start, end -> viewModel.setHistoryDateRange(start, end) },
                                onClearFilters = { viewModel.clearHistoryFilters() },
                                onUpdateSample = { viewModel.updateSample(it) },
                                onDeleteSample = { viewModel.deleteSample(it) }
                            )
                        }
                    }

                    // TAB 2: GRÁFICOS & DISTRIBUCIÓN ESTADÍSTICA
                    2 -> {
                        item {
                            SalmonChartSection(
                                samples = samples
                            )
                        }
                    }

                    // TAB 3: PESOS OBJETIVO POR JAULA (J-101 A J-110)
                    3 -> {
                        item {
                            CageTargetsSection(
                                allSamples = samples,
                                targetsMap = cageTargetsMap,
                                onSaveTarget = { cage, target -> viewModel.setCageTarget(cage, target) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    // Export Dialog (PDF, Excel, CSV)
    if (showExportDialog) {
        ExportDialog(
            samples = samples,
            selectedCage = cageNumber,
            onDismiss = { viewModel.setShowExportDialog(false) }
        )
    }

    // Target Weight Dialog
    if (showTargetWeightDialog) {
        TargetWeightDialog(
            onDismiss = { viewModel.setShowTargetWeightDialog(false) },
            targetsMap = cageTargetsMap,
            onSaveTarget = { cage, target -> viewModel.setCageTarget(cage, target) }
        )
    }

    // Voice Input Dialog (Hands-free with wet hands)
    if (showVoiceDialog) {
        VoiceInputDialog(
            onDismiss = { viewModel.setShowVoiceDialog(false) },
            onVoiceInputProcessed = { text -> viewModel.processVoiceInput(text) },
            isAutoSaveEnabled = isAutoSaveVoice,
            onToggleAutoSave = { viewModel.toggleAutoSaveVoice() },
            activeCage = cageNumber,
            correlativeNumber = currentSessionCount + 1
        )
    }
}
