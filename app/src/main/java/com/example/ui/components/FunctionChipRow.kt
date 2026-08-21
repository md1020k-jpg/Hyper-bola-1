package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.FunctionCategory
import com.example.model.HyperbolicFunc

@Composable
fun FunctionSelectorSection(
    activeFunctions: Set<HyperbolicFunc>,
    onToggleFunction: (HyperbolicFunc) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category 1: Primary (sinh, cosh, tanh)
        Text(
            text = "Primary Functions",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HyperbolicFunc.values()
                .filter { it.category == FunctionCategory.PRIMARY }
                .forEach { func ->
                    val isSelected = activeFunctions.contains(func)
                    FunctionTogglePill(
                        func = func,
                        isSelected = isSelected,
                        onToggle = { onToggleFunction(func) }
                    )
                }
        }

        // Category 2: Inverse Functions (arcsinh, arccosh, arctanh)
        Text(
            text = "Inverse Functions",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HyperbolicFunc.values()
                .filter { it.category == FunctionCategory.INVERSE }
                .forEach { func ->
                    val isSelected = activeFunctions.contains(func)
                    FunctionTogglePill(
                        func = func,
                        isSelected = isSelected,
                        onToggle = { onToggleFunction(func) }
                    )
                }
        }

        // Category 3: Reciprocal Functions (sech, csch, coth)
        Text(
            text = "Reciprocal Functions",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HyperbolicFunc.values()
                .filter { it.category == FunctionCategory.RECIPROCAL }
                .forEach { func ->
                    val isSelected = activeFunctions.contains(func)
                    FunctionTogglePill(
                        func = func,
                        isSelected = isSelected,
                        onToggle = { onToggleFunction(func) }
                    )
                }
        }
    }
}

@Composable
fun FunctionTogglePill(
    func: HyperbolicFunc,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        func.color.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val borderColor = if (isSelected) func.color else MaterialTheme.colorScheme.outlineVariant

    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        modifier = Modifier.testTag("function_toggle_${func.shortName}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(func.color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = func.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) func.color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
