package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "salmon_samples")
data class SalmonSample(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cageNumber: String,
    val samplingDate: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val weightGrams: Double, // Mandatory
    val lengthCm: Double? = null, // Optional
    val notes: String = ""
) {
    val weightKg: Double
        get() = weightGrams / 1000.0

    /**
     * Fulton's Condition Factor (K):
     * K = (Weight in grams / (Length in cm)^3) * 100
     * Standard biological metric for salmon body condition.
     */
    val fultonK: Double?
        get() = if (lengthCm != null && lengthCm > 0) {
            (weightGrams / (lengthCm * lengthCm * lengthCm)) * 100.0
        } else {
            null
        }

    val conditionCategory: String
        get() {
            val k = fultonK ?: return "Sin datos de talla"
            return when {
                k < 0.95 -> "Bajo peso / Delgado"
                k in 0.95..1.25 -> "Óptimo / Normal"
                k in 1.25..1.45 -> "Robusto"
                else -> "Muy robusto / Alto"
            }
        }

    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedDateTime: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
