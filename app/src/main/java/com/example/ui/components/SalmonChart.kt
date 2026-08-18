package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ScatterPlot
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SalmonSample
import com.example.ui.theme.ConditionOptimum
import com.example.ui.theme.ConditionRobust
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.min

enum class ChartType(val title: String) {
    HISTOGRAM("Distribución (g)"),
    TREND("Evolución Muestreo"),
    DISPERSION("Peso vs Talla")
}

@Composable
fun SalmonChartSection(
    samples: List<SalmonSample>,
    modifier: Modifier = Modifier
) {
    var selectedChart by remember { mutableStateOf(ChartType.HISTOGRAM) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("salmon_chart_card"),
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
                .padding(16.dp)
        ) {
            // Chart Header & Selector
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
                            imageVector = when (selectedChart) {
                                ChartType.HISTOGRAM -> Icons.Default.BarChart
                                ChartType.TREND -> Icons.Default.ShowChart
                                ChartType.DISPERSION -> Icons.Default.ScatterPlot
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Historial Gráfico",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (selectedChart) {
                                ChartType.HISTOGRAM -> "Frecuencia por rangos de peso"
                                ChartType.TREND -> "Secuencia temporal del peso"
                                ChartType.DISPERSION -> "Relación alométrica peso-talla"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Type Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ChartType.values().forEach { chart ->
                    val isSelected = selectedChart == chart
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { selectedChart = chart }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chart.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (samples.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Registra muestras para visualizar los gráficos estadísticos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                when (selectedChart) {
                    ChartType.HISTOGRAM -> WeightHistogramChart(samples = samples)
                    ChartType.TREND -> SamplingTrendLineChart(samples = samples)
                    ChartType.DISPERSION -> WeightLengthScatterChart(samples = samples)
                }
            }
        }
    }
}

/**
 * 1. Weight Histogram (Bar chart grouped into sensible weight intervals)
 */
@Composable
fun WeightHistogramChart(
    samples: List<SalmonSample>,
    modifier: Modifier = Modifier
) {
    val weights = samples.map { it.weightGrams }
    val minW = weights.minOrNull() ?: 0.0
    val maxW = weights.maxOrNull() ?: 5000.0

    // Compute dynamic or standard bins
    val binSize = when {
        maxW <= 1500 -> 250.0
        maxW <= 3500 -> 500.0
        maxW <= 6000 -> 750.0
        else -> 1000.0
    }

    val minBin = (minW / binSize).toInt() * binSize
    val maxBin = ((maxW / binSize).toInt() + 1) * binSize
    val binCount = max(3, min(8, ((maxBin - minBin) / binSize).toInt()))

    val bins = remember(samples) {
        val list = mutableListOf<Triple<String, Int, Double>>() // Label, count, minVal
        for (i in 0 until binCount) {
            val start = minBin + (i * binSize)
            val end = start + binSize
            val count = weights.count { it >= start && (if (i == binCount - 1) it <= end else it < end) }
            val label = "${(start / 1000).toInt()}.${((start % 1000) / 100).toInt()}-${(end / 1000).toInt()}.${((end % 1000) / 100).toInt()}k"
            list.add(Triple(label, count, start))
        }
        list
    }

    val maxCount = max(1, bins.maxOfOrNull { it.second } ?: 1)
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val paddingLeft = 32.dp.toPx()
                val paddingRight = 16.dp.toPx()
                val paddingTop = 24.dp.toPx()
                val paddingBottom = 28.dp.toPx()

                val chartWidth = size.width - paddingLeft - paddingRight
                val chartHeight = size.height - paddingTop - paddingBottom

                // Grid lines (horizontal)
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = paddingTop + (chartHeight * i / gridLines)
                    val countVal = (maxCount * (gridLines - i) / gridLines)
                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, y),
                        end = Offset(size.width - paddingRight, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        countVal.toString(),
                        paddingLeft - 8.dp.toPx(),
                        y + 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 24f
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isAntiAlias = true
                        }
                    )
                }

                // Bars
                val barGap = 8.dp.toPx()
                val totalBars = bins.size
                val barWidth = (chartWidth - (barGap * (totalBars + 1))) / totalBars

                bins.forEachIndexed { index, (label, count, _) ->
                    val barHeight = (count.toFloat() / maxCount) * chartHeight
                    val x = paddingLeft + barGap + (index * (barWidth + barGap))
                    val y = paddingTop + chartHeight - barHeight

                    // Bar with gradient
                    if (count > 0) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryColor, primaryColor.copy(alpha = 0.75f)),
                                startY = y,
                                endY = paddingTop + chartHeight
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Count on top of bar
                        drawContext.canvas.nativeCanvas.drawText(
                            count.toString(),
                            x + (barWidth / 2),
                            y - 4.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#0284C7")
                                textSize = 24f
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                        )
                    }

                    // X-axis label
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        x + (barWidth / 2),
                        size.height - 6.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 20f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }
            }
        }

        // Mini legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Cantidad de salmones por rango de peso (kg)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 2. Sampling Trend Line Chart (Chronological progression of weights & moving average)
 */
@Composable
fun SamplingTrendLineChart(
    samples: List<SalmonSample>,
    modifier: Modifier = Modifier
) {
    // Reverse samples to chronological order (oldest to newest)
    val chronoSorted = remember(samples) { samples.reversed() }
    val weights = chronoSorted.map { it.weightGrams }
    val minW = max(0.0, (weights.minOrNull() ?: 0.0) * 0.85)
    val maxW = (weights.maxOrNull() ?: 5000.0) * 1.15
    val rangeW = max(1.0, maxW - minW)

    // Moving average (window of 5)
    val movingAverages = remember(weights) {
        val window = 5
        weights.mapIndexed { idx, _ ->
            val start = max(0, idx - window + 1)
            val sub = weights.subList(start, idx + 1)
            sub.average()
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val paddingLeft = 42.dp.toPx()
                val paddingRight = 16.dp.toPx()
                val paddingTop = 16.dp.toPx()
                val paddingBottom = 26.dp.toPx()

                val chartWidth = size.width - paddingLeft - paddingRight
                val chartHeight = size.height - paddingTop - paddingBottom

                // Grid lines (horizontal)
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = paddingTop + (chartHeight * i / gridLines)
                    val weightVal = maxW - (rangeW * i / gridLines)
                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, y),
                        end = Offset(size.width - paddingRight, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        "${(weightVal / 1000).toInt()}.${((weightVal % 1000) / 100).toInt()}k",
                        paddingLeft - 6.dp.toPx(),
                        y + 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 22f
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isAntiAlias = true
                        }
                    )
                }

                if (chronoSorted.size >= 2) {
                    val stepX = chartWidth / (chronoSorted.size - 1)

                    // Fill area under line
                    val fillPath = Path()
                    val linePath = Path()
                    val avgPath = Path()

                    chronoSorted.forEachIndexed { i, sample ->
                        val x = paddingLeft + (i * stepX)
                        val y = paddingTop + chartHeight - (((sample.weightGrams - minW) / rangeW).toFloat() * chartHeight)
                        val avgY = paddingTop + chartHeight - (((movingAverages[i] - minW) / rangeW).toFloat() * chartHeight)

                        if (i == 0) {
                            linePath.moveTo(x, y)
                            fillPath.moveTo(x, paddingTop + chartHeight)
                            fillPath.lineTo(x, y)
                            avgPath.moveTo(x, avgY)
                        } else {
                            linePath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                            avgPath.lineTo(x, avgY)
                        }
                    }

                    fillPath.lineTo(paddingLeft + ((chronoSorted.size - 1) * stepX), paddingTop + chartHeight)
                    fillPath.close()

                    // Draw Gradient Fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.35f), primaryColor.copy(alpha = 0.02f)),
                            startY = paddingTop,
                            endY = paddingTop + chartHeight
                        )
                    )

                    // Draw Moving Average (Orange dashed)
                    drawPath(
                        path = avgPath,
                        color = secondaryColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                        )
                    )

                    // Draw Main Line (Cyan/Blue solid)
                    drawPath(
                        path = linePath,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw Points
                    chronoSorted.forEachIndexed { i, sample ->
                        val x = paddingLeft + (i * stepX)
                        val y = paddingTop + chartHeight - (((sample.weightGrams - minW) / rangeW).toFloat() * chartHeight)
                        drawCircle(
                            color = primaryColor,
                            radius = 3.5.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 1.8.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                } else if (chronoSorted.size == 1) {
                    val x = paddingLeft + (chartWidth / 2)
                    val y = paddingTop + (chartHeight / 2)
                    drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = Offset(x, y))
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Peso por Muestra (g)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(3.dp)
                        .background(secondaryColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Media Móvil (n=5)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 3. Weight vs Length Scatter Plot (Alometric relationship & condition curve)
 */
@Composable
fun WeightLengthScatterChart(
    samples: List<SalmonSample>,
    modifier: Modifier = Modifier
) {
    val withLength = remember(samples) { samples.filter { it.lengthCm != null && it.lengthCm > 0 } }

    if (withLength.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ingresa la longitud (cm) en las muestras para ver la correlación peso-talla",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    val minL = max(10.0, (withLength.minOf { it.lengthCm!! } * 0.9))
    val maxL = (withLength.maxOf { it.lengthCm!! } * 1.1)
    val minW = max(0.0, (withLength.minOf { it.weightGrams } * 0.85))
    val maxW = (withLength.maxOf { it.weightGrams } * 1.15)

    val rangeL = max(1.0, maxL - minL)
    val rangeW = max(1.0, maxW - minW)

    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val paddingLeft = 42.dp.toPx()
                val paddingRight = 16.dp.toPx()
                val paddingTop = 16.dp.toPx()
                val paddingBottom = 26.dp.toPx()

                val chartWidth = size.width - paddingLeft - paddingRight
                val chartHeight = size.height - paddingTop - paddingBottom

                // Grid
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = paddingTop + (chartHeight * i / gridLines)
                    val weightVal = maxW - (rangeW * i / gridLines)
                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, y),
                        end = Offset(size.width - paddingRight, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        "${(weightVal / 1000).toInt()}.${((weightVal % 1000) / 100).toInt()}k",
                        paddingLeft - 6.dp.toPx(),
                        y + 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 22f
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isAntiAlias = true
                        }
                    )
                }

                // Scatter Points
                withLength.forEach { sample ->
                    val length = sample.lengthCm ?: return@forEach
                    val x = paddingLeft + (((length - minL) / rangeL).toFloat() * chartWidth)
                    val y = paddingTop + chartHeight - (((sample.weightGrams - minW) / rangeW).toFloat() * chartHeight)

                    val k = sample.fultonK ?: 1.0
                    val pointColor = when {
                        k < 0.95 -> Color(0xFFF59E0B) // Orange/Warning
                        k in 0.95..1.25 -> Color(0xFF10B981) // Green / Optimal
                        else -> Color(0xFF3B82F6) // Blue / Robust
                    }

                    drawCircle(
                        color = pointColor,
                        radius = 5.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // Scatter Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "K < 0.95 (Delgado)",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "K: 0.95-1.25 (Óptimo)",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "K > 1.25 (Robusto)",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
