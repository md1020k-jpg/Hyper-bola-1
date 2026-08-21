package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.HyperbolicFunc
import com.example.model.ParabolaMode
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cosh
import kotlin.math.sinh

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PointInspectorCard(
    scrubX: Double,
    paramA: Double = 2.0,
    shiftC: Double = 0.0,
    activeFunctions: Set<HyperbolicFunc>,
    onScrubChange: (Double) -> Unit,
    boundsMinX: Float,
    boundsMaxX: Float,
    showParabolaComparison: Boolean = false,
    parabolaMode: ParabolaMode = ParabolaMode.STANDARD_X_SQUARED,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("point_inspector_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Current X coordinate & Live evaluation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Coordinate Inspector",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Coordinate Inspector",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.testTag("current_x_badge")
                ) {
                    Text(
                        text = "x = ${String.format(Locale.US, "%.3f", scrubX)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Interactive X Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Scrub X Domain",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "[${String.format(Locale.US, "%.1f", boundsMinX)} to ${String.format(Locale.US, "%.1f", boundsMaxX)}]",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = scrubX.toFloat().coerceIn(boundsMinX, boundsMaxX),
                    onValueChange = { onScrubChange(it.toDouble()) },
                    valueRange = boundsMinX..boundsMaxX,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scrub_x_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Values grid for active functions
            if (activeFunctions.isEmpty()) {
                Text(
                    text = "Select one or more functions above to inspect values.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeFunctions.forEach { func ->
                        val yVal = func.evaluate(scrubX, paramA, shiftC)
                        val derivVal = func.evaluateDerivative(scrubX, paramA, shiftC)
                        FunctionValueChip(
                            func = func,
                            paramA = paramA,
                            shiftC = shiftC,
                            yVal = yVal,
                            derivVal = derivVal
                        )
                    }

                    // Parabola Comparison Chip
                    if (showParabolaComparison) {
                        val paraY = when (parabolaMode) {
                            ParabolaMode.STANDARD_X_SQUARED -> scrubX * scrubX
                            ParabolaMode.TAYLOR_SERIES -> 1.0 + (scrubX * scrubX) / 2.0
                            ParabolaMode.MATCHED_CATENARY_PARABOLA -> {
                                val dx = scrubX - shiftC
                                paramA + (dx * dx) / (2.0 * paramA)
                            }
                        }
                        val paraDeriv = when (parabolaMode) {
                            ParabolaMode.STANDARD_X_SQUARED -> 2.0 * scrubX
                            ParabolaMode.TAYLOR_SERIES -> scrubX
                            ParabolaMode.MATCHED_CATENARY_PARABOLA -> (scrubX - shiftC) / paramA
                        }
                        val coshY = HyperbolicFunc.COSH.evaluate(scrubX, paramA, shiftC) ?: 0.0
                        val deltaY = coshY - paraY

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.10f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("value_chip_parabola")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B))
                                    )
                                    Text(
                                        text = "Parabola (${parabolaMode.formula})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "y = ${String.format(Locale.US, "%.4f", paraY)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "dy/dx = ${String.format(Locale.US, "%.4f", paraDeriv)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Δ (cosh - parabola) = ${if (deltaY >= 0) "+" else ""}${String.format(Locale.US, "%.4f", deltaY)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (abs(deltaY) < 0.1) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }

            // Live Fundamental Identity Check: cosh²(u) - sinh²(u) = 1
            val innerArg = (scrubX - shiftC) / paramA
            val sinhVal = sinh(innerArg)
            val coshVal = cosh(innerArg)
            val identityResult = (coshVal * coshVal) - (sinhVal * sinhVal)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Identity",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Hyperbolic Pythagorean Identity (cosh²(u) - sinh²(u) = 1):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "cosh²(u) - sinh²(u) = ${String.format(Locale.US, "%.4f", coshVal * coshVal)} - ${String.format(Locale.US, "%.4f", sinhVal * sinhVal)} = ${String.format(Locale.US, "%.6f", identityResult)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FunctionValueChip(
    func: HyperbolicFunc,
    paramA: Double,
    shiftC: Double,
    yVal: Double?,
    derivVal: Double?
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = func.color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, func.color.copy(alpha = 0.4f)),
        modifier = Modifier.testTag("value_chip_${func.shortName}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(func.color)
                )
                Text(
                    text = func.transformedName(paramA, shiftC),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = func.color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (yVal == null) "Undefined (out of domain)" else "y = ${String.format(Locale.US, "%.4f", yVal)}",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            derivVal?.let {
                Text(
                    text = "dy/dx = ${String.format(Locale.US, "%.4f", it)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
