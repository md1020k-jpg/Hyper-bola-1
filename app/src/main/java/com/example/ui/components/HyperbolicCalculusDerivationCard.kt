package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Sections for exploring hyperbolic and catenary calculus derivations.
 */
enum class CalculusTopic(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    CATENARY_DERIVATION(
        title = "Catenary Derivation",
        subtitle = "Solving y'' = 1/a √(1 + y'²)",
        icon = Icons.Default.Calculate
    ),
    DERIVATIVES_INTEGRALS(
        title = "Derivatives & Integrals",
        subtitle = "Standard calculus rules & tables",
        icon = Icons.Default.IntegrationInstructions
    ),
    ARC_LENGTH_SAG(
        title = "Arc Length & Sag Proof",
        subtitle = "Evaluating S = ∫ √(1 + y'²) dx",
        icon = Icons.Default.AutoAwesome
    ),
    INVERSE_CALCULUS(
        title = "Inverse Functions",
        subtitle = "Derivatives of arsinh, arcosh, artanh",
        icon = Icons.Default.Functions
    )
}

/**
 * A comprehensive Jetpack Compose component that presents formal mathematical
 * derivations, step-by-step proofs, and calculus properties (derivatives, integrals,
 * differential equations, and series) for hyperbolic trigonometry and the catenary curve.
 */
@Composable
fun HyperbolicCalculusDerivationCard(
    modifier: Modifier = Modifier,
    initialTopic: CalculusTopic = CalculusTopic.CATENARY_DERIVATION
) {
    var selectedTopic by remember { mutableStateOf(initialTopic) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("hyperbolic_calculus_derivation_card")
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
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Calculus Derivations",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Calculus & Mathematical Derivations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Rigorous proofs, differential mechanics, and integrals",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Topic Selector FilterChips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculusTopic.values().forEach { topic ->
                    val isSelected = selectedTopic == topic
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTopic = topic },
                        leadingIcon = {
                            Icon(
                                imageVector = topic.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(topic.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("topic_chip_${topic.name.lowercase()}")
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // Dynamic Content based on selected topic
            when (selectedTopic) {
                CalculusTopic.CATENARY_DERIVATION -> CatenaryDerivationContent()
                CalculusTopic.DERIVATIVES_INTEGRALS -> CalculusRulesContent()
                CalculusTopic.ARC_LENGTH_SAG -> ArcLengthSagContent()
                CalculusTopic.INVERSE_CALCULUS -> InverseCalculusContent()
            }
        }
    }
}

/**
 * Step-by-step physical and differential derivation of the Catenary curve y = a * cosh(x / a).
 */
@Composable
private fun CatenaryDerivationContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DerivationStepCard(
            stepNumber = "1",
            title = "Static Equilibrium of Cable Element",
            description = "Consider a small suspended cable element of length ds between x and x + dx with linear weight density w (N/m). Let T(x) be the tension tangent to the curve making angle θ with the horizontal.",
            equations = listOf(
                "Horizontal Equilibrium: T · cos(θ) = T₀ (constant horizontal tension)",
                "Vertical Equilibrium: d(T · sin(θ)) = w · ds = w · √(dx² + dy²)"
            )
        )

        DerivationStepCard(
            stepNumber = "2",
            title = "Formulating the Non-Linear ODE",
            description = "Since slope y' = tan(θ) = (T · sin θ) / (T · cos θ) = T_v / T₀, differentiate with respect to x:",
            equations = listOf(
                "y' = tan(θ)",
                "y'' = d/dx [tan(θ)] = (1 / T₀) · d(T_v)/dx",
                "Since ds/dx = √(1 + (y')²):",
                "y'' = (w / T₀) · √(1 + (y')²)"
            )
        )

        DerivationStepCard(
            stepNumber = "3",
            title = "Solving via Separation of Variables",
            description = "Let a = T₀ / w (scaling length parameter) and substitute slope variable u = y', so u' = du/dx:",
            equations = listOf(
                "du / dx = (1 / a) · √(1 + u²)",
                "∫ du / √(1 + u²) = (1 / a) ∫ dx",
                "arsinh(u) = (x / a) + C₁"
            )
        )

        DerivationStepCard(
            stepNumber = "4",
            title = "Boundary Conditions & Integration",
            description = "Place the coordinate vertex at the lowest point of the cable where at x = 0 the slope u = y'(0) = 0. Therefore C₁ = 0.",
            equations = listOf(
                "u = y'(x) = sinh(x / a)",
                "y(x) = ∫ sinh(x / a) dx = a · cosh(x / a) + C₂",
                "Choosing y(0) = a (or ground datum y_vertex = a) gives C₂ = 0:",
                "★ y(x) = a · cosh(x / a)"
            ),
            isFinalResult = true
        )
    }
}

/**
 * Tabulated calculus properties: Derivatives, Integrals, and Series for all hyperbolic functions.
 */
@Composable
private fun CalculusRulesContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Fundamental Hyperbolic Derivatives & Integrals",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        CalculusTableRow(
            func = "sinh(x)",
            derivative = "cosh(x)",
            integral = "cosh(x) + C",
            definition = "(eˣ - e⁻ˣ) / 2"
        )

        CalculusTableRow(
            func = "cosh(x)",
            derivative = "sinh(x)",
            integral = "sinh(x) + C",
            definition = "(eˣ + e⁻ˣ) / 2"
        )

        CalculusTableRow(
            func = "tanh(x)",
            derivative = "sech²(x)",
            integral = "ln(cosh(x)) + C",
            definition = "sinh(x) / cosh(x)"
        )

        CalculusTableRow(
            func = "sech(x)",
            derivative = "-sech(x) · tanh(x)",
            integral = "2 · arctan(eˣ) + C",
            definition = "1 / cosh(x)"
        )

        CalculusTableRow(
            func = "csch(x)",
            derivative = "-csch(x) · coth(x)",
            integral = "ln|tanh(x/2)| + C",
            definition = "1 / sinh(x)"
        )

        CalculusTableRow(
            func = "coth(x)",
            derivative = "-csch²(x)",
            integral = "ln|sinh(x)| + C",
            definition = "cosh(x) / sinh(x)"
        )

        // Calculus Chain Rule Application
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Chain Rule for Catenary: y(x) = a · cosh(x / a)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "• First Derivative (Slope): y'(x) = a · sinh(x/a) · (1/a) = sinh(x/a)\n" +
                            "• Second Derivative (Curvature): y''(x) = (1/a) · cosh(x/a)\n" +
                            "• Local Radius of Curvature: R = [1 + (y')²]^(3/2) / |y''| = a · cosh²(x/a)",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Proof and derivation for Arc Length S and Cable Sag h using hyperbolic integrals.
 */
@Composable
private fun ArcLengthSagContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DerivationStepCard(
            stepNumber = "1",
            title = "Arc Length Integral Setup",
            description = "The differential arc length ds along any differentiable plane curve is given by ds = √(1 + (dy/dx)²) dx.",
            equations = listOf(
                "y(x) = a · cosh(x / a)",
                "y'(x) = sinh(x / a)",
                "1 + (y')² = 1 + sinh²(x / a) = cosh²(x / a)",
                "ds = √(cosh²(x / a)) dx = cosh(x / a) dx"
            )
        )

        DerivationStepCard(
            stepNumber = "2",
            title = "Integrating Arc Length across Span L",
            description = "Integrate symmetrically from x = -L/2 to x = +L/2 between the two support towers:",
            equations = listOf(
                "S = ∫_{-L/2}^{+L/2} cosh(x / a) dx = 2 ∫₀^{L/2} cosh(x / a) dx",
                "S = 2 [ a · sinh(x / a) ]₀^{L/2}",
                "★ S = 2a · sinh(L / 2a)"
            ),
            isFinalResult = true
        )

        DerivationStepCard(
            stepNumber = "3",
            title = "Maximum Sag Proof",
            description = "Maximum sag h is the vertical drop from the tower supports at x = ±L/2 to the vertex at x = 0:",
            equations = listOf(
                "h = y(L / 2) - y(0)",
                "h = a · cosh((L/2) / a) - a · cosh(0)",
                "Since cosh(0) = 1:",
                "★ h = a · [ cosh(L / 2a) - 1 ]"
            ),
            isFinalResult = true
        )

        DerivationStepCard(
            stepNumber = "4",
            title = "Exact Relation between Tension, Sag, and Arc Length",
            description = "Using the hyperbolic identity cosh²(θ) - sinh²(θ) = 1, we obtain the exact algebraic invariant:",
            equations = listOf(
                "S² = 4a² sinh²(L/2a) = 4a² [ cosh²(L/2a) - 1 ]",
                "Since h = a[cosh(L/2a) - 1] ⟹ cosh(L/2a) = 1 + h/a",
                "S² = 4a² [ (1 + h/a)² - 1 ] = 4a² [ 2(h/a) + (h/a)² ] = 8ah + 4h²",
                "★ S = 2 · √( 2ah + h² )"
            )
        )
    }
}

/**
 * Calculus properties and logarithmic identities for inverse hyperbolic functions.
 */
@Composable
private fun InverseCalculusContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Inverse Hyperbolic Derivatives & Logarithmic Formulas",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        InverseCalculusCard(
            func = "arsinh(x)",
            logForm = "ln(x + √(x² + 1))",
            domain = "x ∈ ℝ",
            derivative = "1 / √(x² + 1)",
            integral = "x · arsinh(x) - √(x² + 1) + C"
        )

        InverseCalculusCard(
            func = "arcosh(x)",
            logForm = "ln(x + √(x² - 1))",
            domain = "x ≥ 1",
            derivative = "1 / √(x² - 1)",
            integral = "x · arcosh(x) - √(x² - 1) + C"
        )

        InverseCalculusCard(
            func = "artanh(x)",
            logForm = "(1/2) · ln((1 + x) / (1 - x))",
            domain = "-1 < x < 1",
            derivative = "1 / (1 - x²)",
            integral = "x · artanh(x) + (1/2) · ln(1 - x²) + C"
        )
    }
}

/**
 * Reusable card for a step in a mathematical derivation.
 */
@Composable
private fun DerivationStepCard(
    stepNumber: String,
    title: String,
    description: String,
    equations: List<String>,
    isFinalResult: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isFinalResult) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            1.dp,
            if (isFinalResult) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                        shape = CircleShape,
                        color = if (isFinalResult) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stepNumber,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            equations.forEach { eq ->
                                val isHighlight = eq.startsWith("★")
                                Text(
                                    text = eq,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formatted row for calculus reference table.
 */
@Composable
private fun CalculusTableRow(
    func: String,
    derivative: String,
    integral: String,
    definition: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "f(x) = $func",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "= $definition",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "d/dx [f(x)]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = derivative,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "∫ f(x) dx",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = integral,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Card for inverse hyperbolic function properties.
 */
@Composable
private fun InverseCalculusCard(
    func: String,
    logForm: String,
    domain: String,
    derivative: String,
    integral: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = func,
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = "Domain: $domain",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Logarithmic Form: $logForm",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "d/dx [$func]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = derivative,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                }

                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = "∫ $func dx",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = integral,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }
    }
}
