package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import kotlin.math.exp

/**
 * An interactive educational and comparison card contrasting the Hyperbolic Cosine (cosh/catenary)
 * with the standard parabola y = x² and its Taylor series approximations.
 */
@Composable
fun ParabolaComparisonCard(
    showParabolaComparison: Boolean,
    parabolaMode: ParabolaMode,
    scrubX: Double,
    paramA: Double,
    shiftC: Double,
    onToggleComparison: () -> Unit,
    onSelectMode: (ParabolaMode) -> Unit,
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
            .testTag("parabola_comparison_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Switch Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = "Comparison",
                                tint = Color(0xFFD97706)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Hyperbolic Cosine vs. Parabola",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "cosh(x) vs. standard y = x²",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = showParabolaComparison,
                    onCheckedChange = { onToggleComparison() },
                    modifier = Modifier.testTag("parabola_compare_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFD97706),
                        checkedTrackColor = Color(0xFFF59E0B).copy(alpha = 0.5f)
                    )
                )
            }

            Text(
                text = "Visually and mathematically compare the catenary shape (cosh) against the parabolic curve (y = x²). Though both are U-shaped and symmetric, cosh grows exponentially while parabolas grow quadratically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Mode Selector Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Parabola Comparison Model:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ParabolaMode.values().forEach { mode ->
                        val isSelected = parabolaMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectMode(mode) },
                            label = { Text(mode.formula, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFFB45309)
                            ),
                            border = if (isSelected) BorderStroke(1.dp, Color(0xFFD97706)) else null,
                            modifier = Modifier.testTag("parabola_mode_${mode.name.lowercase(Locale.ROOT)}")
                        )
                    }
                }
            }

            // Live Difference Probe at scrubX
            val coshVal = HyperbolicFunc.COSH.evaluate(scrubX, paramA, shiftC) ?: 1.0
            val parabolaVal = when (parabolaMode) {
                ParabolaMode.STANDARD_X_SQUARED -> scrubX * scrubX
                ParabolaMode.TAYLOR_SERIES -> 1.0 + (scrubX * scrubX) / 2.0
                ParabolaMode.MATCHED_CATENARY_PARABOLA -> {
                    val dx = scrubX - shiftC
                    paramA + (dx * dx) / (2.0 * paramA)
                }
            }
            val delta = coshVal - parabolaVal
            val percentDiff = if (abs(coshVal) > 1e-6) (abs(delta) / coshVal) * 100.0 else 0.0

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Probe at x = ${String.format(Locale.US, "%.2f", scrubX)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (abs(delta) < 0.1) Color(0xFF16A34A).copy(alpha = 0.15f) else Color(0xFFDC2626).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Divergence: ${String.format(Locale.US, "%.1f", percentDiff)}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (abs(delta) < 0.1) Color(0xFF15803D) else Color(0xFFB91C1C),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Cosh readout
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDC2626))
                            )
                            Column {
                                Text("cosh(x)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                Text(String.format(Locale.US, "%.4f", coshVal), style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Parabola readout
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B))
                            )
                            Column {
                                Text(parabolaMode.formula, style = MaterialTheme.typography.labelSmall, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                                Text(String.format(Locale.US, "%.4f", parabolaVal), style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Delta readout
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Δy (cosh - para)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${if (delta >= 0) "+" else ""}${String.format(Locale.US, "%.4f", delta)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (abs(delta) < 0.1) Color(0xFF16A34A) else Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Mathematical & Historical Breakdown
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = "Historical note",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Why cosh(x) is not a Parabola",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Taylor Series Expansion of cosh(x):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "cosh(x) = 1 + x²/2! + x⁴/4! + x⁶/6! + ... = 1 + x²/2 + x⁴/24 + ...",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• For small |x| ≪ 1: The higher order terms (x⁴/24) are tiny, so cosh(x) ≈ 1 + x²/2 behaves closely like a parabola.\n• For larger |x|: cosh(x) ~ ½eˣ explodes exponentially, pulling far steeper than the polynomial x².",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "The Galileo Galilei Mystery (1638 vs 1691):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Galileo initially conjectured that a hanging flexible chain formed a parabola. In 1691, Christiaan Huygens, Gottfried Leibniz, and Johann Bernoulli solved the static equilibrium differential equation y'' = (w/T₀)√(1 + y'²), proving conclusively that the hanging curve is the catenary y = a cosh(x/a), not a parabola.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
