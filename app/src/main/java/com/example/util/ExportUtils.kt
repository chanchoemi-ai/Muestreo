package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.SalmonSample
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

object ExportUtils {

    private val df1 = DecimalFormat("#,##0.0")
    private val df2 = DecimalFormat("#,##0.00")
    private val dfInt = DecimalFormat("#,##0")

    /**
     * Exports samples to CSV with UTF-8 BOM for flawless Excel compatibility.
     */
    fun exportToCsv(
        context: Context,
        samples: List<SalmonSample>,
        cageFilter: String? = null,
        dateFilter: String? = null
    ): File {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Muestreo_Salmones_$timeStamp.csv"
        val file = File(exportsDir, fileName)

        FileOutputStream(file).use { fos ->
            // Write UTF-8 BOM so Excel opens accents properly
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Summary block
                writer.append("REPORTE DE MUESTREO DE SALMONES\n")
                writer.append("Fecha de Exportación;${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}\n")
                if (!cageFilter.isNullOrBlank()) writer.append("Filtro Jaula;$cageFilter\n")
                if (!dateFilter.isNullOrBlank()) writer.append("Filtro Fecha;$dateFilter\n")
                writer.append("Total Peces Muestreados;${samples.size}\n")

                if (samples.isNotEmpty()) {
                    val avgWeight = samples.map { it.weightGrams }.average()
                    val minWeight = samples.minOf { it.weightGrams }
                    val maxWeight = samples.maxOf { it.weightGrams }
                    val lengthSamples = samples.filter { it.lengthCm != null && it.lengthCm > 0 }
                    val avgLength = if (lengthSamples.isNotEmpty()) lengthSamples.mapNotNull { it.lengthCm }.average() else null
                    val kSamples = samples.mapNotNull { it.fultonK }
                    val avgK = if (kSamples.isNotEmpty()) kSamples.average() else null

                    writer.append("Peso Promedio (g);${df1.format(avgWeight)}\n")
                    writer.append("Peso Promedio (kg);${df2.format(avgWeight / 1000.0)}\n")
                    writer.append("Peso Mínimo (g);${df1.format(minWeight)}\n")
                    writer.append("Peso Máximo (g);${df1.format(maxWeight)}\n")
                    if (avgLength != null) writer.append("Longitud Promedio (cm);${df1.format(avgLength)}\n")
                    if (avgK != null) writer.append("Factor de Condición K Promedio;${df2.format(avgK)}\n")
                }
                writer.append("\n")

                // Table headers
                writer.append("N°;Jaula;Fecha;Hora;Peso (g);Peso (kg);Longitud (cm);Factor K Fulton;Clasificación Condición;Notas\n")

                // Rows
                samples.forEachIndexed { index, sample ->
                    val kStr = sample.fultonK?.let { df2.format(it) } ?: "N/A"
                    val lengthStr = sample.lengthCm?.let { df1.format(it) } ?: "N/A"
                    writer.append("${index + 1};")
                    writer.append("${sample.cageNumber};")
                    writer.append("${sample.samplingDate};")
                    writer.append("${sample.formattedTime};")
                    writer.append("${df1.format(sample.weightGrams)};")
                    writer.append("${df2.format(sample.weightKg)};")
                    writer.append("$lengthStr;")
                    writer.append("$kStr;")
                    writer.append("${sample.conditionCategory};")
                    writer.append("${sample.notes.replace(";", ",")}\n")
                }
            }
        }
        return file
    }

    /**
     * Exports samples as an Excel XML Spreadsheet (.xls) with rich styling, colors, and headers.
     */
    fun exportToExcelXml(
        context: Context,
        samples: List<SalmonSample>,
        cageFilter: String? = null,
        dateFilter: String? = null
    ): File {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Muestreo_Salmones_$timeStamp.xls"
        val file = File(exportsDir, fileName)

        val avgWeight = if (samples.isNotEmpty()) samples.map { it.weightGrams }.average() else 0.0
        val minWeight = if (samples.isNotEmpty()) samples.minOf { it.weightGrams } else 0.0
        val maxWeight = if (samples.isNotEmpty()) samples.maxOf { it.weightGrams } else 0.0
        val lengthSamples = samples.filter { it.lengthCm != null && it.lengthCm > 0 }
        val avgLength = if (lengthSamples.isNotEmpty()) lengthSamples.mapNotNull { it.lengthCm }.average() else 0.0
        val kSamples = samples.mapNotNull { it.fultonK }
        val avgK = if (kSamples.isNotEmpty()) kSamples.average() else 0.0

        val xmlBuilder = StringBuilder()
        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        xmlBuilder.append("<?mso-application progid=\"Excel.Sheet\"?>\n")
        xmlBuilder.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
        xmlBuilder.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n")
        xmlBuilder.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n")
        xmlBuilder.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
        xmlBuilder.append(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">\n")

        // Styles
        xmlBuilder.append("<Styles>\n")
        xmlBuilder.append("<Style ss:ID=\"Default\" ss:Name=\"Normal\"><Alignment ss:Vertical=\"Center\"/><Font ss:FontName=\"Calibri\" ss:Size=\"11\"/></Style>\n")
        xmlBuilder.append("<Style ss:ID=\"Title\"><Font ss:FontName=\"Calibri\" ss:Size=\"16\" ss:Bold=\"1\" ss:Color=\"#0A3663\"/><Alignment ss:Horizontal=\"Left\" ss:Vertical=\"Center\"/></Style>\n")
        xmlBuilder.append("<Style ss:ID=\"SubTitle\"><Font ss:FontName=\"Calibri\" ss:Size=\"11\" ss:Italic=\"1\" ss:Color=\"#555555\"/></Style>\n")
        xmlBuilder.append("<Style ss:ID=\"KpiHeader\"><Font ss:FontName=\"Calibri\" ss:Size=\"11\" ss:Bold=\"1\" ss:Color=\"#FFFFFF\"/><Interior ss:Color=\"#0D5C75\" ss:Pattern=\"Solid\"/><Alignment ss:Horizontal=\"Center\"/></Style>\n")
        xmlBuilder.append("<Style ss:ID=\"KpiValue\"><Font ss:FontName=\"Calibri\" ss:Size=\"12\" ss:Bold=\"1\" ss:Color=\"#0A3663\"/><Interior ss:Color=\"#E6F4F8\" ss:Pattern=\"Solid\"/><Alignment ss:Horizontal=\"Center\"/></Style>\n")
        xmlBuilder.append("<Style ss:ID=\"TableHeader\"><Font ss:FontName=\"Calibri\" ss:Size=\"11\" ss:Bold=\"1\" ss:Color=\"#FFFFFF\"/><Interior ss:Color=\"#0A3663\" ss:Pattern=\"Solid\"/><Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/></Style>\n")
        xmlBuilder.append("<Style ss:ID=\"CellEven\"><Interior ss:Color=\"#F8FAFC\" ss:Pattern=\"Solid\"/><Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/></Style>\n")
        xmlBuilder.append("<Style ss:ID=\"CellOdd\"><Interior ss:Color=\"#FFFFFF\" ss:Pattern=\"Solid\"/><Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/></Style>\n")
        xmlBuilder.append("<Style ss:ID=\"NumberCell\"><Alignment ss:Horizontal=\"Right\" ss:Vertical=\"Center\"/></Style>\n")
        xmlBuilder.append("</Styles>\n")

        // Worksheet
        xmlBuilder.append("<Worksheet ss:Name=\"Muestreo Salmones\">\n")
        xmlBuilder.append("<Table ss:ExpandedColumnCount=\"10\" ss:DefaultRowHeight=\"20\">\n")
        xmlBuilder.append("<Column ss:Width=\"40\"/>\n")
        xmlBuilder.append("<Column ss:Width=\"90\"/>\n")
        xmlBuilder.append("<Column ss:Width=\"85\"/>\n")
        xmlBuilder.append("<Column ss:Width=\"70\"/>\n")
        xmlBuilder.append("<Column ss:Width=\"90\"/>\n")
        xmlBuilder.append("<Column ss:Width=\"90\"/>\n")
        xmlBuilder.append("<Column ss:Width=\"95\"/>\n")
        xmlBuilder.append("<Column ss:Width=\"95\"/>\n")
        xmlBuilder.append("<Column ss:Width=\"140\"/>\n")
        xmlBuilder.append("<Column ss:Width=\"150\"/>\n")

        // Title
        xmlBuilder.append("<Row ss:Height=\"28\"><Cell ss:StyleID=\"Title\"><Data ss:Type=\"String\">MUESTREO BIOMÉTRICO DE SALMONES</Data></Cell></Row>\n")
        xmlBuilder.append("<Row><Cell ss:StyleID=\"SubTitle\"><Data ss:Type=\"String\">Generado: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}</Data></Cell></Row>\n")
        if (!cageFilter.isNullOrBlank()) xmlBuilder.append("<Row><Cell ss:StyleID=\"SubTitle\"><Data ss:Type=\"String\">Jaula Seleccionada: $cageFilter</Data></Cell></Row>\n")
        xmlBuilder.append("<Row></Row>\n")

        // KPI Summary Block
        xmlBuilder.append("<Row ss:Height=\"22\">\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiHeader\"><Data ss:Type=\"String\">Muestras</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiHeader\"><Data ss:Type=\"String\">Peso Promedio (g)</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiHeader\"><Data ss:Type=\"String\">Peso Promedio (kg)</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiHeader\"><Data ss:Type=\"String\">Peso Mín (g)</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiHeader\"><Data ss:Type=\"String\">Peso Máx (g)</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiHeader\"><Data ss:Type=\"String\">Longitud Prom (cm)</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiHeader\"><Data ss:Type=\"String\">Factor K Fulton</Data></Cell>\n")
        xmlBuilder.append("</Row>\n")

        xmlBuilder.append("<Row ss:Height=\"24\">\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiValue\"><Data ss:Type=\"Number\">${samples.size}</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiValue\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", avgWeight)}</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiValue\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.2f", avgWeight / 1000.0)}</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiValue\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", minWeight)}</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiValue\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", maxWeight)}</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiValue\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", avgLength)}</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"KpiValue\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.2f", avgK)}</Data></Cell>\n")
        xmlBuilder.append("</Row>\n")
        xmlBuilder.append("<Row></Row>\n")

        // Table Header
        xmlBuilder.append("<Row ss:Height=\"24\">\n")
        xmlBuilder.append("<Cell ss:StyleID=\"TableHeader\"><Data ss:Type=\"String\">N°</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"TableHeader\"><Data ss:Type=\"String\">Jaula</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"TableHeader\"><Data ss:Type=\"String\">Fecha</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"TableHeader\"><Data ss:Type=\"String\">Hora</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"TableHeader\"><Data ss:Type=\"String\">Peso (g)</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"TableHeader\"><Data ss:Type=\"String\">Peso (kg)</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"TableHeader\"><Data ss:Type=\"String\">Longitud (cm)</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"TableHeader\"><Data ss:Type=\"String\">Factor K</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"TableHeader\"><Data ss:Type=\"String\">Condición</Data></Cell>\n")
        xmlBuilder.append("<Cell ss:StyleID=\"TableHeader\"><Data ss:Type=\"String\">Notas</Data></Cell>\n")
        xmlBuilder.append("</Row>\n")

        // Rows
        samples.forEachIndexed { idx, item ->
            val style = if (idx % 2 == 0) "CellEven" else "CellOdd"
            xmlBuilder.append("<Row>\n")
            xmlBuilder.append("<Cell ss:StyleID=\"$style\"><Data ss:Type=\"Number\">${idx + 1}</Data></Cell>\n")
            xmlBuilder.append("<Cell ss:StyleID=\"$style\"><Data ss:Type=\"String\">${item.cageNumber}</Data></Cell>\n")
            xmlBuilder.append("<Cell ss:StyleID=\"$style\"><Data ss:Type=\"String\">${item.samplingDate}</Data></Cell>\n")
            xmlBuilder.append("<Cell ss:StyleID=\"$style\"><Data ss:Type=\"String\">${item.formattedTime}</Data></Cell>\n")
            xmlBuilder.append("<Cell ss:StyleID=\"$style\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.weightGrams)}</Data></Cell>\n")
            xmlBuilder.append("<Cell ss:StyleID=\"$style\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.3f", item.weightKg)}</Data></Cell>\n")
            if (item.lengthCm != null) {
                xmlBuilder.append("<Cell ss:StyleID=\"$style\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.lengthCm)}</Data></Cell>\n")
            } else {
                xmlBuilder.append("<Cell ss:StyleID=\"$style\"><Data ss:Type=\"String\">-</Data></Cell>\n")
            }
            if (item.fultonK != null) {
                xmlBuilder.append("<Cell ss:StyleID=\"$style\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.2f", item.fultonK)}</Data></Cell>\n")
            } else {
                xmlBuilder.append("<Cell ss:StyleID=\"$style\"><Data ss:Type=\"String\">-</Data></Cell>\n")
            }
            xmlBuilder.append("<Cell ss:StyleID=\"$style\"><Data ss:Type=\"String\">${item.conditionCategory}</Data></Cell>\n")
            xmlBuilder.append("<Cell ss:StyleID=\"$style\"><Data ss:Type=\"String\">${item.notes}</Data></Cell>\n")
            xmlBuilder.append("</Row>\n")
        }

        xmlBuilder.append("</Table>\n")
        xmlBuilder.append("</Worksheet>\n")
        xmlBuilder.append("</Workbook>\n")

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.write(xmlBuilder.toString())
            }
        }
        return file
    }

    /**
     * Generates a high-quality printable PDF report.
     */
    fun exportToPdf(
        context: Context,
        samples: List<SalmonSample>,
        cageFilter: String? = null,
        dateFilter: String? = null
    ): File {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Reporte_Muestreo_Salmones_$timeStamp.pdf"
        val file = File(exportsDir, fileName)

        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width (pt)
        val pageHeight = 842 // A4 standard height (pt)

        val paint = Paint().apply { isAntiAlias = true }
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 10f
            color = Color.DKGRAY
        }
        val headerPaint = Paint().apply {
            isAntiAlias = true
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.rgb(10, 54, 99)
        }
        val titleSubPaint = Paint().apply {
            isAntiAlias = true
            textSize = 9f
            color = Color.GRAY
        }
        val thPaint = Paint().apply {
            isAntiAlias = true
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.WHITE
        }

        val rowsPerPage = 28
        val totalPages = if (samples.isEmpty()) 1 else ((samples.size - 1) / rowsPerPage) + 1

        val avgWeight = if (samples.isNotEmpty()) samples.map { it.weightGrams }.average() else 0.0
        val minWeight = if (samples.isNotEmpty()) samples.minOf { it.weightGrams } else 0.0
        val maxWeight = if (samples.isNotEmpty()) samples.maxOf { it.weightGrams } else 0.0
        val lengthSamples = samples.filter { it.lengthCm != null && it.lengthCm > 0 }
        val avgLength = if (lengthSamples.isNotEmpty()) lengthSamples.mapNotNull { it.lengthCm }.average() else null
        val kSamples = samples.mapNotNull { it.fultonK }
        val avgK = if (kSamples.isNotEmpty()) kSamples.average() else null

        // Standard Deviation
        val stdDev = if (samples.size > 1) {
            val variance = samples.map { (it.weightGrams - avgWeight).pow(2) }.sum() / (samples.size - 1)
            sqrt(variance)
        } else 0.0
        val cvPercent = if (avgWeight > 0) (stdDev / avgWeight) * 100.0 else 0.0

        var currentSampleIndex = 0

        for (pageIndex in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var y = 40f

            // Top Header (on first page)
            if (pageIndex == 1) {
                // Top decorative bar
                paint.color = Color.rgb(10, 54, 99)
                canvas.drawRect(30f, y, 565f, y + 4f, paint)
                y += 24f

                canvas.drawText("REPORTE DE MUESTREO DE SALMONES", 30f, y, headerPaint)
                y += 14f

                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                canvas.drawText("Generado el: $dateStr | Página $pageIndex de $totalPages", 30f, y, titleSubPaint)
                if (!cageFilter.isNullOrBlank()) {
                    canvas.drawText("Jaula: $cageFilter", 380f, y, titleSubPaint)
                }
                y += 20f

                // KPI Stats Card Box
                paint.color = Color.rgb(235, 245, 252)
                canvas.drawRoundRect(30f, y, 565f, y + 65f, 8f, 8f, paint)

                paint.color = Color.rgb(15, 80, 140)
                paint.strokeWidth = 1f
                paint.style = Paint.Style.STROKE
                canvas.drawRoundRect(30f, y, 565f, y + 65f, 8f, 8f, paint)
                paint.style = Paint.Style.FILL

                val kpiTitlePaint = Paint().apply {
                    textSize = 8.5f
                    color = Color.rgb(60, 90, 120)
                    isAntiAlias = true
                }
                val kpiValPaint = Paint().apply {
                    textSize = 12f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    color = Color.rgb(10, 45, 80)
                    isAntiAlias = true
                }

                val row1Y = y + 22f
                val row2Y = y + 38f
                val row3Y = y + 54f

                // Col 1: Total & Avg Weight
                canvas.drawText("Total Muestras:", 45f, row1Y, kpiTitlePaint)
                canvas.drawText("${samples.size} peces", 115f, row1Y, kpiValPaint)

                canvas.drawText("Peso Promedio:", 45f, row2Y, kpiTitlePaint)
                canvas.drawText("${df1.format(avgWeight)} g (${df2.format(avgWeight / 1000.0)} kg)", 115f, row2Y, kpiValPaint)

                canvas.drawText("Desv. Est / CV%:", 45f, row3Y, kpiTitlePaint)
                canvas.drawText("±${df1.format(stdDev)} g (${df1.format(cvPercent)}%)", 115f, row3Y, kpiTitlePaint)

                // Col 2: Min/Max & Length & K
                canvas.drawText("Rango Peso:", 320f, row1Y, kpiTitlePaint)
                canvas.drawText("${df1.format(minWeight)} g - ${df1.format(maxWeight)} g", 380f, row1Y, kpiValPaint)

                canvas.drawText("Longitud Prom:", 320f, row2Y, kpiTitlePaint)
                val lenStr = if (avgLength != null) "${df1.format(avgLength)} cm" else "N/A"
                canvas.drawText(lenStr, 380f, row2Y, kpiValPaint)

                canvas.drawText("Factor K Prom:", 320f, row3Y, kpiTitlePaint)
                val kStr = if (avgK != null) "${df2.format(avgK)}" else "N/A"
                canvas.drawText(kStr, 380f, row3Y, kpiValPaint)

                y += 80f
            } else {
                // Secondary Page Header
                paint.color = Color.rgb(10, 54, 99)
                canvas.drawText("Reporte de Muestreo de Salmones (Continuación) - Pág $pageIndex / $totalPages", 30f, y, headerPaint.apply { textSize = 12f })
                y += 20f
            }

            // Table Header Bar
            paint.color = Color.rgb(10, 54, 99)
            canvas.drawRoundRect(30f, y, 565f, y + 20f, 4f, 4f, paint)

            canvas.drawText("N°", 40f, y + 14f, thPaint)
            canvas.drawText("Jaula", 70f, y + 14f, thPaint)
            canvas.drawText("Hora", 130f, y + 14f, thPaint)
            canvas.drawText("Peso (g)", 185f, y + 14f, thPaint)
            canvas.drawText("Peso (kg)", 245f, y + 14f, thPaint)
            canvas.drawText("Long. (cm)", 305f, y + 14f, thPaint)
            canvas.drawText("Factor K", 370f, y + 14f, thPaint)
            canvas.drawText("Condición", 435f, y + 14f, thPaint)
            canvas.drawText("Notas", 515f, y + 14f, thPaint)

            y += 22f

            val rowPaint = Paint().apply {
                textSize = 9f
                isAntiAlias = true
                color = Color.DKGRAY
            }

            var rowsDrawn = 0
            while (currentSampleIndex < samples.size && rowsDrawn < rowsPerPage) {
                val item = samples[currentSampleIndex]
                val isEven = (rowsDrawn % 2 == 0)

                // Background
                paint.color = if (isEven) Color.rgb(248, 250, 252) else Color.WHITE
                canvas.drawRect(30f, y, 565f, y + 18f, paint)

                canvas.drawText("${currentSampleIndex + 1}", 40f, y + 13f, rowPaint)
                canvas.drawText(item.cageNumber, 70f, y + 13f, rowPaint)
                canvas.drawText(item.formattedTime, 130f, y + 13f, rowPaint)
                canvas.drawText(df1.format(item.weightGrams), 185f, y + 13f, rowPaint)
                canvas.drawText(df2.format(item.weightKg), 245f, y + 13f, rowPaint)

                val lStr = item.lengthCm?.let { df1.format(it) } ?: "-"
                canvas.drawText(lStr, 305f, y + 13f, rowPaint)

                val fkStr = item.fultonK?.let { df2.format(it) } ?: "-"
                canvas.drawText(fkStr, 370f, y + 13f, rowPaint)

                val cond = when {
                    item.fultonK == null -> "-"
                    item.fultonK!! < 0.95 -> "Delgado"
                    item.fultonK!! in 0.95..1.25 -> "Óptimo"
                    item.fultonK!! in 1.25..1.45 -> "Robusto"
                    else -> "Muy Robusto"
                }
                canvas.drawText(cond, 435f, y + 13f, rowPaint)

                val shortNotes = if (item.notes.length > 8) item.notes.substring(0, 8) + "…" else item.notes
                canvas.drawText(shortNotes, 515f, y + 13f, rowPaint)

                y += 18f
                currentSampleIndex++
                rowsDrawn++
            }

            // Footer
            val footerPaint = Paint().apply {
                textSize = 8f
                color = Color.GRAY
                isAntiAlias = true
            }
            canvas.drawText("Sistema de Muestreo de Salmones - Confidencial Acuícola", 30f, 815f, footerPaint)
            canvas.drawText("Página $pageIndex de $totalPages", 500f, 815f, footerPaint)

            pdfDocument.finishPage(page)
        }

        FileOutputStream(file).use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()
        return file
    }

    /**
     * Shares the generated file using Android's Intent.ACTION_SEND.
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Adjunto reporte de muestreo de salmones (${file.name})")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
