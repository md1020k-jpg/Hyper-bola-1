package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.window.Dialog

@Composable
fun IdentitiesDialog(
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("identities_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
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
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Functions,
                                    contentDescription = "Formulas",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Mathematical Reference",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Hyperbolic Trigonometry & Identities",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_identities_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ReferenceCard(
                            title = "Exponential Definitions",
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            titleColor = MaterialTheme.colorScheme.primary,
                            items = listOf(
                                "sinh(x) = (eˣ - e⁻ˣ) / 2" to "Odd function: sinh(-x) = -sinh(x)",
                                "cosh(x) = (eˣ + e⁻ˣ) / 2" to "Even function: cosh(-x) = cosh(x)",
                                "tanh(x) = (eˣ - e⁻ˣ) / (eˣ + e⁻ˣ)" to "Range: (-1, 1), Asymptotes at ±1",
                                "sech(x) = 2 / (eˣ + e⁻ˣ)" to "Range: (0, 1]",
                                "coth(x) = (eˣ + e⁻ˣ) / (eˣ - e⁻ˣ)" to "Undefined at x = 0"
                            )
                        )
                    }

                    item {
                        ReferenceCard(
                            title = "Pythagorean & Fundamental Identities",
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            titleColor = MaterialTheme.colorScheme.secondary,
                            items = listOf(
                                "cosh²(x) - sinh²(x) = 1" to "Fundamental unit hyperbola identity",
                                "1 - tanh²(x) = sech²(x)" to "Analog to 1 + tan²θ = sec²θ",
                                "coth²(x) - 1 = csch²(x)" to "Analog to cot²θ + 1 = csc²θ"
                            )
                        )
                    }

                    item {
                        ReferenceCard(
                            title = "Sum & Double Angle Formulas",
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            titleColor = MaterialTheme.colorScheme.tertiary,
                            items = listOf(
                                "sinh(x ± y) = sinh(x)cosh(y) ± cosh(x)sinh(y)" to "Addition theorem",
                                "cosh(x ± y) = cosh(x)cosh(y) ± sinh(x)sinh(y)" to "Note the positive sign!",
                                "sinh(2x) = 2 sinh(x) cosh(x)" to "Double angle sinh",
                                "cosh(2x) = cosh²(x) + sinh²(x)" to "= 2cosh²(x) - 1 = 1 + 2sinh²(x)"
                            )
                        )
                    }

                    item {
                        ReferenceCard(
                            title = "Calculus (Derivatives & Integrals)",
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            titleColor = MaterialTheme.colorScheme.onSurface,
                            items = listOf(
                                "d/dx [sinh(x)] = cosh(x)" to "∫ cosh(x) dx = sinh(x) + C",
                                "d/dx [cosh(x)] = sinh(x)" to "∫ sinh(x) dx = cosh(x) + C (No negative sign!)",
                                "d/dx [tanh(x)] = sech²(x)" to "∫ sech²(x) dx = tanh(x) + C",
                                "d/dx [sech(x)] = -sech(x)tanh(x)" to "∫ tanh(x) dx = ln(cosh(x)) + C"
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceCard(
    title: String,
    containerColor: Color,
    titleColor: Color,
    items: List<Pair<String, String>>
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )

            items.forEach { (formula, note) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = formula,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
