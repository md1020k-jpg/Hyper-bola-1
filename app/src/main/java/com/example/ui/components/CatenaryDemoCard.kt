package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CablePreset
import com.example.model.CatenaryCalculation
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CatenaryDemoCard(
    calculation: CatenaryCalculation,
    selectedPreset: CablePreset?,
    onTensionChange: (Double) -> Unit,
    onSpanChange: (Double) -> Unit,
    onMassChange: (Double) -> Unit,
    onPresetSelect: (CablePreset) -> Unit,
    modifier: Modifier = Modifier
) {
    var probeX by remember { mutableDoubleStateOf(0.0) } // Probe x position along span (-L/2 to +L/2)

    val halfSpan = calculation.halfSpanM
    val a = calculation.parameterA
    val sag = calculation.maxSagM
    val cableLength = calculation.arcLengthM
    val tensionMax = calculation.maxTensionN
    val vertReaction = calculation.verticalReactionN
    val totalMass = calculation.totalCableMassKg
    val slackPct = calculation.slackPercent

    // Probe point evaluations
    val probeY = calculation.evaluateY(probeX.coerceIn(-halfSpan, halfSpan))
    val probeSag = probeY - a
    val probeTension = calculation.evaluateTensionN(probeX.coerceIn(-halfSpan, halfSpan))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("catenary_demo_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Architecture,
                    contentDescription = "Catenary Physics",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "Catenary Cable Physics Simulator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Equation: y = a · cosh(x / a)  where  a = T₀ / (μ · g)",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Real-world Presets Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Engineering Presets",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CablePreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { onPresetSelect(preset) },
                            label = { Text(preset.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("cable_preset_${preset.name.lowercase(Locale.ROOT)}")
                        )
                    }
                }
            }

            // Visual Simulation Canvas with Touch Probe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B1120))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .pointerInput(calculation.spanM) {
                            detectTapGestures { offset ->
                                val w = size.width
                                val pylonLeft = w * 0.12f
                                val pylonRight = w * 0.88f
                                val spanPx = pylonRight - pylonLeft
                                val norm = ((offset.x - pylonLeft) / spanPx).coerceIn(0f, 1f)
                                probeX = -halfSpan + norm * calculation.spanM
                            }
                        }
                        .pointerInput(calculation.spanM) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val w = size.width
                                val pylonLeft = w * 0.12f
                                val pylonRight = w * 0.88f
                                val spanPx = pylonRight - pylonLeft
                                val norm = ((change.position.x - pylonLeft) / spanPx).coerceIn(0f, 1f)
                                probeX = -halfSpan + norm * calculation.spanM
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val groundY = h - 18f
                    val topY = 22f

                    // Draw Ground Level
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(0f, groundY),
                        end = Offset(w, groundY),
                        strokeWidth = 3f
                    )

                    // Pylon coordinates
                    val pylonLeftX = w * 0.12f
                    val pylonRightX = w * 0.88f
                    val pylonTopY = topY + 12f

                    // Draw Left Pylon
                    drawLine(
                        color = Color(0xFF94A3B8),
                        start = Offset(pylonLeftX, groundY),
                        end = Offset(pylonLeftX, pylonTopY),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )

                    // Draw Right Pylon
                    drawLine(
                        color = Color(0xFF94A3B8),
                        start = Offset(pylonRightX, groundY),
                        end = Offset(pylonRightX, pylonTopY),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )

                    // Pylon Support tops
                    drawCircle(Color(0xFFCBD5E1), radius = 5f, center = Offset(pylonLeftX, pylonTopY))
                    drawCircle(Color(0xFFCBD5E1), radius = 5f, center = Offset(pylonRightX, pylonTopY))

                    // Draw hanging Catenary Cable using y = a * cosh(x / a)
                    val curvePoints = calculation.sampleCurvePoints(120)
                    val path = Path()
                    val cableColor = Color(0xFF38BDF8) // Bright Cyan Blue

                    val availableCurveHeight = (groundY - pylonTopY - 24f).coerceAtLeast(20f)

                    curvePoints.forEachIndexed { index, pt ->
                        val screenX = pylonLeftX + pt.normalizedX * (pylonRightX - pylonLeftX)
                        // Sag is 0 at vertex (lowest point) and maxSag at ends
                        // Lowest point on canvas is closer to ground
                        val screenY = pylonTopY + (1f - pt.normalizedSag) * availableCurveHeight

                        if (index == 0) {
                            path.moveTo(screenX, screenY)
                        } else {
                            path.lineTo(screenX, screenY)
                        }
                    }

                    // Stroke the hanging cable
                    drawPath(
                        path = path,
                        color = cableColor,
                        style = Stroke(
                            width = 4.5f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw Sag dimension dashed line at mid-span
                    val midX = (pylonLeftX + pylonRightX) / 2f
                    val vertexY = pylonTopY + availableCurveHeight
                    drawLine(
                        color = Color(0xFFF59E0B),
                        start = Offset(midX, pylonTopY),
                        end = Offset(midX, vertexY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // Draw Vertex Indicator
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 4.5f,
                        center = Offset(midX, vertexY)
                    )

                    // Draw Probe / Inspection marker
                    val probeNormX = ((probeX + halfSpan) / calculation.spanM).toFloat().coerceIn(0f, 1f)
                    val probeNormSag = if (sag > 0.0) (probeSag / sag).toFloat().coerceIn(0f, 1f) else 0f
                    val probeScreenX = pylonLeftX + probeNormX * (pylonRightX - pylonLeftX)
                    val probeScreenY = pylonTopY + (1f - probeNormSag) * availableCurveHeight

                    // Probe vertical guide line
                    drawLine(
                        color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                        start = Offset(probeScreenX, topY),
                        end = Offset(probeScreenX, groundY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // Probe dot
                    drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = 6f,
                        center = Offset(probeScreenX, probeScreenY)
                    )
                    drawCircle(
                        color = Color(0xFF6750A4),
                        radius = 3.5f,
                        center = Offset(probeScreenX, probeScreenY)
                    )
                }

                // Interactive Hint Banner
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .background(Color(0x99000000), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Touch/drag to probe x = ${String.format(Locale.US, "%.1f", probeX)} m",
                        color = Color(0xFFE2E8F0),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Probe telemetry strip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Local Probe @ x=${String.format(Locale.US, "%.1f", probeX)}m",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sag: ${String.format(Locale.US, "%.2f", probeSag)}m | Tension: ${String.format(Locale.US, "%.1f", probeTension / 1000.0)} kN",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Interactive Physics Parameter Sliders
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Cable Physics Parameters",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // 1. Horizontal Tension Slider (T₀)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Horizontal Tension (T₀):",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", calculation.horizontalTensionN / 1000.0)} kN  (${calculation.horizontalTensionN.roundToInt()} N)",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Slider(
                        value = (calculation.horizontalTensionN / 1000.0).toFloat(),
                        onValueChange = { onTensionChange(it.toDouble() * 1000.0) },
                        valueRange = 1.0f..200.0f,
                        modifier = Modifier.fillMaxWidth().testTag("cable_tension_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // 2. Span Slider (L)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Straighten, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Support Span (L):",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", calculation.spanM)} m",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Slider(
                        value = calculation.spanM.toFloat(),
                        onValueChange = { onSpanChange(it.toDouble()) },
                        valueRange = 20.0f..600.0f,
                        modifier = Modifier.fillMaxWidth().testTag("cable_span_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // 3. Linear Mass Density Slider (μ)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Linear Mass Density (μ):",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.2f", calculation.linearMassDensityKgPerM)} kg/m",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Slider(
                        value = calculation.linearMassDensityKgPerM.toFloat(),
                        onValueChange = { onMassChange(it.toDouble()) },
                        valueRange = 0.1f..15.0f,
                        modifier = Modifier.fillMaxWidth().testTag("cable_mass_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Derived Catenary Parameter Highlight
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Derived Catenary Scale (a = T₀ / w)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "a = ${String.format(Locale.US, "%.2f", a)} m",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Distributed Weight (w = μ·g)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.2f", calculation.linearWeightDensityNpm)} N/m",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Comprehensive Engineering Output Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Computed Catenary Mechanics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CatenaryStatCard(
                        title = "MAX SAG (h)",
                        formula = "a(cosh(L/2a) - 1)",
                        value = "${String.format(Locale.US, "%.2f", sag)} m",
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    CatenaryStatCard(
                        title = "ARC LENGTH (S)",
                        formula = "2a sinh(L/2a)",
                        value = "${String.format(Locale.US, "%.2f", cableLength)} m",
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CatenaryStatCard(
                        title = "MAX TENSION (T_max)",
                        formula = "T₀ cosh(L/2a)",
                        value = "${String.format(Locale.US, "%.1f", tensionMax / 1000.0)} kN",
                        color = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )
                    CatenaryStatCard(
                        title = "SUPPORT REACTION (V)",
                        formula = "T₀ sinh(L/2a)",
                        value = "${String.format(Locale.US, "%.1f", vertReaction / 1000.0)} kN",
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CatenaryStatCard(
                        title = "TOTAL CABLE MASS",
                        formula = "μ · S",
                        value = "${String.format(Locale.US, "%.1f", totalMass)} kg",
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    CatenaryStatCard(
                        title = "SLACK ELONGATION",
                        formula = "((S - L)/L)·100%",
                        value = "+${String.format(Locale.US, "%.2f", slackPct)}%",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CatenaryStatCard(
    title: String,
    formula: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formula,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
