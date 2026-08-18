package com.example.util

import java.util.Locale

data class ParsedVoiceInput(
    val weightGrams: Double? = null,
    val lengthCm: Double? = null,
    val targetCage: String? = null,
    val isSaveCommand: Boolean = false,
    val rawText: String = ""
)

object VoiceTextParser {

    private val spanishNumbers = mapOf(
        "cero" to 0, "un" to 1, "uno" to 1, "una" to 1, "dos" to 2, "tres" to 3, "cuatro" to 4,
        "cinco" to 5, "seis" to 6, "siete" to 7, "ocho" to 8, "nueve" to 9, "diez" to 10,
        "once" to 11, "doce" to 12, "trece" to 13, "catorce" to 14, "quince" to 15,
        "dieciseis" to 16, "dieciséis" to 16, "diecisiete" to 17, "dieciocho" to 18, "diecinueve" to 19,
        "veinte" to 20, "veintiuno" to 21, "veintidos" to 22, "veintidós" to 22, "veintitres" to 23,
        "veintitrés" to 23, "veinticuatro" to 24, "veinticinco" to 25, "veintiseis" to 26,
        "veintiséis" to 26, "veintisiete" to 27, "veintiocho" to 28, "veintinueve" to 29,
        "treinta" to 30, "cuarenta" to 40, "cincuenta" to 50, "sesenta" to 60,
        "setenta" to 70, "ochenta" to 80, "noventa" to 90,
        "cien" to 100, "ciento" to 100, "doscientos" to 200, "doscientas" to 200,
        "trescientos" to 300, "trescientas" to 300, "cuatrocientos" to 400, "cuatrocientas" to 400,
        "quinientos" to 500, "quinientas" to 500, "seiscientos" to 600, "seiscientas" to 600,
        "setecientos" to 700, "setecientas" to 700, "ochocientos" to 800, "ochocientas" to 800,
        "novecientos" to 900, "novecientas" to 900, "mil" to 1000
    )

    fun parse(text: String): ParsedVoiceInput {
        val clean = text.lowercase(Locale.ROOT)
            .replace(",", ".")
            .replace("-", " ")
            .trim()

        var cage: String? = null
        val isSave = clean.contains("guardar") || clean.contains("registrar") || clean.contains("anotar")

        // 1. Detect cage command (e.g. "jaula 103", "jaula j 105", "j 102", "j108")
        val cageRegex = Regex("""(?:jaula|modulo|módulo)?\s*(?:j\s*)?(\b10[1-9]\b|\b110\b)""")
        val cageMatch = cageRegex.find(clean)
        if (cageMatch != null) {
            val num = cageMatch.groupValues[1]
            cage = "J-$num"
        }

        // 2. Extract digits and decimals directly
        val numberMatches = Regex("""\b\d+(?:\.\d+)?\b""").findAll(clean).map { it.value.toDoubleOrNull() }.filterNotNull().toList()

        var weight: Double? = null
        var length: Double? = null

        if (numberMatches.isNotEmpty()) {
            // Usually salmon weights in grams are in range 200 to 15000 grams
            // Salmon length in cm is in range 20 to 120 cm
            // If someone said "3.5 kilos" or "3.5 kg"
            if (clean.contains("kilo") || clean.contains("kg")) {
                val kiloRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:kilos?|kg)""")
                val km = kiloRegex.find(clean)
                if (km != null) {
                    val kVal = km.groupValues[1].toDoubleOrNull()
                    if (kVal != null) {
                        weight = kVal * 1000.0
                    }
                }
            }

            if (weight == null) {
                // Look for weight based on values
                val largeNumbers = numberMatches.filter { it >= 150.0 }
                val smallNumbers = numberMatches.filter { it in 15.0..130.0 }

                if (largeNumbers.isNotEmpty()) {
                    weight = largeNumbers.first()
                    if (smallNumbers.isNotEmpty()) {
                        length = smallNumbers.first()
                    } else if (numberMatches.size >= 2 && numberMatches[1] < 150.0) {
                        length = numberMatches[1]
                    }
                } else if (numberMatches.size == 1) {
                    // Single number: if > 150 it's weight in grams. If < 15, might be kg (e.g. 3.4)
                    val n = numberMatches[0]
                    if (n > 150.0) {
                        weight = n
                    } else if (n in 0.2..12.0) {
                        // Interpreted as kg
                        weight = n * 1000.0
                    } else {
                        weight = n
                    }
                } else if (numberMatches.size >= 2) {
                    // First number is weight, second is length
                    weight = numberMatches[0]
                    length = numberMatches[1]
                }
            }
        }

        // If no digit found, try words (e.g., "tres mil cuatrocientos")
        if (weight == null) {
            val words = clean.split(Regex("""\s+"""))
            var total = 0.0
            var current = 0.0
            for (w in words) {
                val num = spanishNumbers[w]
                if (num != null) {
                    if (num == 1000) {
                        if (current == 0.0) current = 1.0
                        total += current * 1000
                        current = 0.0
                    } else if (num == 100 && current > 0) {
                        current *= 100
                    } else {
                        current += num
                    }
                }
            }
            total += current
            if (total >= 100.0) {
                weight = total
            }
        }

        return ParsedVoiceInput(
            weightGrams = weight,
            lengthCm = length,
            targetCage = cage,
            isSaveCommand = isSave,
            rawText = text
        )
    }
}
