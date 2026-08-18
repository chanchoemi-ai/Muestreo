package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun LiveStatsCards(
    samples: List<SalmonSample>,
    targetWeightGrams: Double? = 3500.0,
    cageName: String = "J-101",
    onOpenTargetSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val df1 = remember { DecimalFormat("#,##0.0") }
    val df2 = remember { DecimalFormat("#,##0.00") }
    val dfInt = remember { DecimalFormat("#,##0") }

    val count = samples.size
    val avgWeightGrams = if (count > 0) samples.map { it.weightGrams }.average() else 0.0
    val avgWeightKg = avgWeightGrams / 1000.0
    val minWeight = if (count > 0) samples.minOf { it.weightGrams } else 0.0
    val maxWeight = if (count > 0) samples.maxOf { it.weightGrams } else 0.0

    val withLength = samples.filter { it.lengthCm != null && it.lengthCm > 0 }
    val avgLength = if (withLength.isNotEmpty()) withLength.mapNotNull { it.lengthCm }.average() else null

    val kFactors = samples.mapNotNull { it.fultonK }
    val avgK = if (kFactors.isNotEmpty()) kFactors.average() else null

    val stdDev = if (count > 1) {
        val variance = samples.map { (it.weightGrams - avgWeightGrams).pow(2) }.sum() / (count - 1)
        sqrt(variance)
    } else 0.0
    val cvPercent = if (avgWeightGrams > 0) (stdDev / avgWeightGrams) * 100.0 else 0.0

    val target = targetWeightGrams ?: 3500.0
    val diffGrams = if (count > 0) avgWeightGrams - target else 0.0
    val diffPercent = if (target > 0) (diffGrams / target) * 100.0 else 0.0
    val isPositiveDiff = diffGrams >= 0

    val isQuotaMet = count >= SalmonViewModel.MIN_REQUIRED_SAMPLES_PER_CAGE
    val quotaProgress = (count / 50f).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_stats_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Hero Real-time Average Weight Banner with Target Comparison
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hero_average_weight_card"),
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header line with Cage and Quota status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = cageName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "BIOMETRÍA EN VIVO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Quota badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isQuotaMet) ConditionOptimum.copy(alpha = 0.15f) else ConditionLow.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isQuotaMet) ConditionOptimum else ConditionLow
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isQuotaMet) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isQuotaMet) ConditionOptimum else ConditionLow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isQuotaMet) "$count / 50 (Completo)" else "$count / 50 (Faltan ${50 - count})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isQuotaMet) ConditionOptimum else ConditionLow,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Big Average Weight Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "PESO PROMEDIO DEL LOTE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        AnimatedContent(
                            targetState = avgWeightGrams,
                            transitionSpec = {
                                (slideInVertically { height -> height } + fadeIn()) togetherWith
                                        (slideOutVertically { height -> -height } + fadeOut())
                            },
                            label = "avg_weight_anim"
                        ) { targetWeight ->
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (count > 0) dfInt.format(targetWeight) else "—",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "g",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                if (count > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${df2.format(avgWeightKg)} kg)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Min / Max range
                    if (count > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Rango Min - Max",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${dfInt.format(minWeight)} - ${dfInt.format(maxWeight)} g",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // TARGET WEIGHT & REAL-TIME DIFFERENCE BADGE
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (count == 0) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    } else if (isPositiveDiff) {
                        ConditionOptimum.copy(alpha = 0.12f)
                    } else {
                        ConditionAlert.copy(alpha = 0.12f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (count == 0) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        else if (isPositiveDiff) ConditionOptimum.copy(alpha = 0.6f)
                        else ConditionAlert.copy(alpha = 0.6f)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Peso Objetivo ($cageName)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${dfInt.format(target)} g (${String.format("%.2f", target / 1000.0)} kg)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (count > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPositiveDiff) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isPositiveDiff) ConditionOptimum else ConditionAlert,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    val sign = if (isPositiveDiff) "+" else ""
                                    Text(
                                        text = "$sign${dfInt.format(diffGrams)} g",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (isPositiveDiff) ConditionOptimum else ConditionAlert
                                    )
                                    Text(
                                        text = "$sign${df1.format(diffPercent)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPositiveDiff) ConditionOptimum else ConditionAlert
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Sin muestras",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Secondary Metrics Grid: CV%, Length, Fulton K
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // CV% Card (Uniformity)
            val cvQuality = when {
                count < 2 -> "Pocos datos"
                cvPercent < 10.0 -> "Excelente (<10%)"
                cvPercent < 15.0 -> "Bueno (10-15%)"
                cvPercent < 20.0 -> "Aceptable"
                else -> "Alta dispersión"
            }
            val cvColor = when {
                count < 2 -> MaterialTheme.colorScheme.onSurfaceVariant
                cvPercent < 12.0 -> ConditionOptimum
                cvPercent < 18.0 -> MaterialTheme.colorScheme.primary
                else -> ConditionAlert
            }

            MetricCard(
                modifier = Modifier.weight(1f),
                title = "HOMOGENEIDAD (CV%)",
                mainValue = if (count > 1) "${df1.format(cvPercent)}%" else "—",
                subValue = cvQuality,
                subValueColor = cvColor,
                icon = Icons.Default.AutoGraph
            )

            // Length Card
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "TALLA PROMEDIO",
                mainValue = if (avgLength != null) "${df1.format(avgLength)} cm" else "—",
                subValue = if (withLength.isNotEmpty()) "${withLength.size} con talla" else "Sin talla",
                subValueColor = MaterialTheme.colorScheme.onSurfaceVariant,
                icon = Icons.Default.Straighten
            )

            // Fulton K Card
            val kCategory = when {
                avgK == null -> "—"
                avgK < 0.95 -> "Bajo peso"
                avgK in 0.95..1.25 -> "Óptimo"
                else -> "Robusto"
            }
            val kColor = when {
                avgK == null -> MaterialTheme.colorScheme.onSurfaceVariant
                avgK < 0.95 -> ConditionLow
                avgK in 0.95..1.25 -> ConditionOptimum
                else -> ConditionRobust
            }

            MetricCard(
                modifier = Modifier.weight(1f),
                title = "FACTOR K FULTON",
                mainValue = if (avgK != null) df2.format(avgK) else "—",
                subValue = kCategory,
                subValueColor = kColor,
                icon = Icons.Default.Scale
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    mainValue: String,
    subValue: String,
    subValueColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = mainValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )

            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = subValueColor,
                maxLines = 1
            )
        }
    }
}
