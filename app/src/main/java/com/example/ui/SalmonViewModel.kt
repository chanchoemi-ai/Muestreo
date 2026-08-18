package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CageTarget
import com.example.data.model.SalmonSample
import com.example.data.repository.SalmonRepository
import com.example.util.ParsedVoiceInput
import com.example.util.VoiceTextParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalmonViewModel(private val repository: SalmonRepository) : ViewModel() {

    companion object {
        val STANDARD_CAGES = listOf(
            "J-101", "J-102", "J-103", "J-104", "J-105",
            "J-106", "J-107", "J-108", "J-109", "J-110"
        )
        const val MIN_REQUIRED_SAMPLES_PER_CAGE = 50
    }

    val allSamples: StateFlow<List<SalmonSample>> = repository.allSamples
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val distinctCages: StateFlow<List<String>> = repository.distinctCages
        .map { cages ->
            // Merge with standard cages to ensure J-101 to J-110 are always present
            (STANDARD_CAGES + cages).distinct().sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = STANDARD_CAGES
        )

    val allCageTargets: StateFlow<List<CageTarget>> = repository.allCageTargets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cageTargetsMap: StateFlow<Map<String, Double>> = allCageTargets
        .map { list -> list.associate { it.cageNumber to it.targetWeightGrams } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Form Entry States
    private val _cageNumber = MutableStateFlow(STANDARD_CAGES.first())
    val cageNumber: StateFlow<String> = _cageNumber.asStateFlow()

    private val _samplingDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val samplingDate: StateFlow<String> = _samplingDate.asStateFlow()

    private val _weightInput = MutableStateFlow("")
    val weightInput: StateFlow<String> = _weightInput.asStateFlow()

    private val _lengthInput = MutableStateFlow("")
    val lengthInput: StateFlow<String> = _lengthInput.asStateFlow()

    private val _lastAddedSample = MutableStateFlow<SalmonSample?>(null)
    val lastAddedSample: StateFlow<SalmonSample?> = _lastAddedSample.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // UI Theme and Dialog states
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _showTargetWeightDialog = MutableStateFlow(false)
    val showTargetWeightDialog: StateFlow<Boolean> = _showTargetWeightDialog.asStateFlow()

    private val _showVoiceDialog = MutableStateFlow(false)
    val showVoiceDialog: StateFlow<Boolean> = _showVoiceDialog.asStateFlow()

    private val _isAutoSaveVoice = MutableStateFlow(true)
    val isAutoSaveVoice: StateFlow<Boolean> = _isAutoSaveVoice.asStateFlow()

    private val _voiceStatusMessage = MutableStateFlow<String?>(null)
    val voiceStatusMessage: StateFlow<String?> = _voiceStatusMessage.asStateFlow()

    // History Filter States
    private val _historyCageFilter = MutableStateFlow("TODAS")
    val historyCageFilter: StateFlow<String> = _historyCageFilter.asStateFlow()

    private val _historyDateFilter = MutableStateFlow("TODAS")
    val historyDateFilter: StateFlow<String> = _historyDateFilter.asStateFlow()

    private val _historyDateRangeStart = MutableStateFlow<String?>(null)
    val historyDateRangeStart: StateFlow<String?> = _historyDateRangeStart.asStateFlow()

    private val _historyDateRangeEnd = MutableStateFlow<String?>(null)
    val historyDateRangeEnd: StateFlow<String?> = _historyDateRangeEnd.asStateFlow()

    // Current Session Samples (for active cage + active date)
    val currentSessionSamples: StateFlow<List<SalmonSample>> = combine(
        allSamples, _cageNumber, _samplingDate
    ) { samples, cage, date ->
        samples.filter { it.cageNumber == cage && it.samplingDate == date }
            .sortedBy { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Correlative next index for current session
    val currentSessionCount: StateFlow<Int> = currentSessionSamples
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun updateCageNumber(cage: String) {
        _cageNumber.value = cage
    }

    fun updateSamplingDate(date: String) {
        _samplingDate.value = date
    }

    fun updateWeightInput(weight: String) {
        _weightInput.value = weight
        if (_errorMessage.value != null) {
            _errorMessage.value = null
        }
    }

    fun updateLengthInput(length: String) {
        _lengthInput.value = length
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setShowExportDialog(show: Boolean) {
        _showExportDialog.value = show
    }

    fun setShowTargetWeightDialog(show: Boolean) {
        _showTargetWeightDialog.value = show
    }

    fun setShowVoiceDialog(show: Boolean) {
        _showVoiceDialog.value = show
    }

    fun toggleAutoSaveVoice() {
        _isAutoSaveVoice.value = !_isAutoSaveVoice.value
    }

    fun setHistoryCageFilter(cage: String) {
        _historyCageFilter.value = cage
    }

    fun setHistoryDateFilter(date: String) {
        _historyDateFilter.value = date
    }

    fun setHistoryDateRange(start: String?, end: String?) {
        _historyDateRangeStart.value = start
        _historyDateRangeEnd.value = end
    }

    fun clearHistoryFilters() {
        _historyCageFilter.value = "TODAS"
        _historyDateFilter.value = "TODAS"
        _historyDateRangeStart.value = null
        _historyDateRangeEnd.value = null
    }

    fun setCageTarget(cageNumber: String, targetGrams: Double) {
        viewModelScope.launch {
            repository.setCageTarget(cageNumber, targetGrams)
        }
    }

    fun saveSample(): Boolean {
        val rawWeight = _weightInput.value.trim().replace(",", ".")
        val weight = rawWeight.toDoubleOrNull()

        if (weight == null || weight <= 0) {
            _errorMessage.value = "Por favor ingresa un peso válido en gramos (mayor a 0)."
            return false
        }

        val rawLength = _lengthInput.value.trim().replace(",", ".")
        val length = if (rawLength.isNotBlank()) rawLength.toDoubleOrNull() else null

        val sample = SalmonSample(
            cageNumber = _cageNumber.value.trim().ifBlank { STANDARD_CAGES.first() },
            samplingDate = _samplingDate.value,
            timestamp = System.currentTimeMillis(),
            weightGrams = weight,
            lengthCm = if (length != null && length > 0) length else null
        )

        viewModelScope.launch {
            repository.insertSample(sample)
            _lastAddedSample.value = sample
            // Reset entry inputs for immediate next fish entry
            _weightInput.value = ""
            _lengthInput.value = ""
            _errorMessage.value = null
        }
        return true
    }

    fun processVoiceInput(spokenText: String): ParsedVoiceInput {
        val parsed = VoiceTextParser.parse(spokenText)

        // If target cage detected, update active cage
        if (parsed.targetCage != null && STANDARD_CAGES.contains(parsed.targetCage)) {
            _cageNumber.value = parsed.targetCage
        }

        if (parsed.weightGrams != null) {
            _weightInput.value = if (parsed.weightGrams % 1.0 == 0.0) {
                parsed.weightGrams.toLong().toString()
            } else {
                parsed.weightGrams.toString()
            }
            if (parsed.lengthCm != null) {
                _lengthInput.value = parsed.lengthCm.toString()
            }

            if (_isAutoSaveVoice.value) {
                // Auto-save directly
                saveSample()
                _voiceStatusMessage.value = "Pez registrado: ${parsed.weightGrams.toInt()} g" +
                        if (parsed.lengthCm != null) " (${parsed.lengthCm} cm)" else ""
            } else {
                _voiceStatusMessage.value = "Valores cargados. Di 'Guardar' o presiona Guardar."
            }
        } else if (parsed.isSaveCommand) {
            if (_weightInput.value.isNotBlank()) {
                saveSample()
                _voiceStatusMessage.value = "Muestra guardada correctamente"
            } else {
                _voiceStatusMessage.value = "No hay peso ingresado para guardar"
            }
        } else {
            _voiceStatusMessage.value = "No se detectó un número de peso válido. Prueba diciendo ej: '3450' o '3500 con 62'"
        }

        return parsed
    }

    fun updateSample(sample: SalmonSample) {
        viewModelScope.launch {
            repository.updateSample(sample)
        }
    }

    fun deleteSample(sample: SalmonSample) {
        viewModelScope.launch {
            repository.deleteSample(sample)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
            _lastAddedSample.value = null
        }
    }

    /**
     * Seeds realistic demo sampling data if database is empty to let the user see charts and history immediately.
     */
    fun seedDemoDataIfEmpty() {
        viewModelScope.launch {
            if (allSamples.value.isEmpty()) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val demoData = mutableListOf<SalmonSample>()
                // Seed 52 samples for J-101 to demonstrate a completed quota (>=50)
                val baseWeights = listOf(3200.0, 3450.0, 3600.0, 2950.0, 3800.0, 4100.0, 3300.0, 3520.0, 3700.0, 3900.0)
                for (i in 1..52) {
                    val weight = baseWeights[(i - 1) % baseWeights.size] + ((i * 37) % 300) - 150
                    val length = 55.0 + (weight / 150.0)
                    demoData.add(
                        SalmonSample(
                            cageNumber = "J-101",
                            samplingDate = today,
                            timestamp = System.currentTimeMillis() - ((53 - i) * 20000L),
                            weightGrams = weight,
                            lengthCm = String.format(Locale.US, "%.1f", length).toDoubleOrNull()
                        )
                    )
                }

                // Also seed a partial sample of 18 fish for J-102 (shows < 50 warning)
                for (i in 1..18) {
                    val weight = 2800.0 + ((i * 43) % 400)
                    val length = 52.0 + (weight / 160.0)
                    demoData.add(
                        SalmonSample(
                            cageNumber = "J-102",
                            samplingDate = today,
                            timestamp = System.currentTimeMillis() - ((20 - i) * 15000L),
                            weightGrams = weight,
                            lengthCm = String.format(Locale.US, "%.1f", length).toDoubleOrNull()
                        )
                    )
                }

                repository.insertAll(demoData)

                // Seed standard target weights
                val defaultTargets = listOf(
                    CageTarget("J-101", 3500.0),
                    CageTarget("J-102", 3100.0),
                    CageTarget("J-103", 3800.0),
                    CageTarget("J-104", 4200.0),
                    CageTarget("J-105", 3400.0),
                    CageTarget("J-106", 3600.0),
                    CageTarget("J-107", 3900.0),
                    CageTarget("J-108", 4000.0),
                    CageTarget("J-109", 3700.0),
                    CageTarget("J-110", 3500.0)
                )
                repository.setAllCageTargets(defaultTargets)
            }
        }
    }
}

class SalmonViewModelFactory(private val repository: SalmonRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalmonViewModel::class.java)) {
            return SalmonViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
