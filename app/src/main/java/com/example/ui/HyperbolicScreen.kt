package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.GraphPreset
import com.example.ui.components.CatenaryDemoCard
import com.example.ui.components.FunctionSelectorSection
import com.example.ui.components.HyperbolicCalculusDerivationCard
import com.example.ui.components.HyperbolicPlotCanvas
import com.example.ui.components.IdentitiesDialog
import com.example.ui.components.ParabolaComparisonCard
import com.example.ui.components.PointInspectorCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HyperbolicScreen(
    viewModel: HyperbolicViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.showTheoryDialog) {
        IdentitiesDialog(
            onDismissRequest = { viewModel.setTheoryDialogVisible(false) }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.catenary_logo_1787308787072),
                            contentDescription = "App Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                        )
                        Text(
                            text = "Hyperbolic & Catenary Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (uiState.isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = if (uiState.isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.togglePanZoomMode() },
                        modifier = Modifier.testTag("toggle_pan_zoom_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PanTool,
                            contentDescription = "Pan/Zoom Mode",
                            tint = if (uiState.isPanZoomMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.resetBounds() },
                        modifier = Modifier.testTag("reset_zoom_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Zoom",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setTheoryDialogVisible(true) },
                        modifier = Modifier.testTag("open_theory_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Theory & Formulas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // View Mode Tabs
            PrimaryTabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                AppViewTab.values().forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.setSelectedTab(tab) },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            when (tab) {
                                AppViewTab.PLOT -> Icon(Icons.Default.ShowChart, contentDescription = null)
                                AppViewTab.PHYSICS -> Icon(Icons.Default.Architecture, contentDescription = null)
                                AppViewTab.IDENTITIES -> Icon(Icons.Default.Functions, contentDescription = null)
                            }
                        },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase(Locale.ROOT)}")
                    )
                }
            }

            // Tab Content
            when (uiState.selectedTab) {
                AppViewTab.PLOT -> {
                    PlotTabView(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
                AppViewTab.PHYSICS -> {
                    PhysicsTabView(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
                AppViewTab.IDENTITIES -> {
                    IdentitiesTabView()
                }
            }
        }
    }
}

@Composable
private fun PlotTabView(
    uiState: HyperbolicUiState,
    viewModel: HyperbolicViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Description Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.catenary_logo_1787308787072),
                    contentDescription = "Catenary App Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hyperbolic Grapher & Catenary Calculator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Interactive plot of sinh, cosh, tanh and inverses with catenary arc length S = 2A sinh(L / 2A) and sag h = A(cosh(L / 2A) - 1).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Engineering Calculations (Suspended Cable) Stat Panel
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().testTag("engineering_calc_panel")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Engineering Calculations (Suspended Cable)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EngineeringStatBox(
                        label = "TOWER SPAN (L)",
                        value = "${String.format(Locale.US, "%.2f", uiState.spanL)} m",
                        modifier = Modifier.weight(1f)
                    )
                    EngineeringStatBox(
                        label = "CABLE ARC LENGTH (S)",
                        value = "${String.format(Locale.US, "%.2f", uiState.arcLength)} m",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EngineeringStatBox(
                        label = "MAX CABLE SAG (h)",
                        value = "${String.format(Locale.US, "%.2f", uiState.maxSag)} m",
                        modifier = Modifier.weight(1f)
                    )
                    EngineeringStatBox(
                        label = "CABLE SLACK (%)",
                        value = "+${String.format(Locale.US, "%.1f", uiState.slackPercent)}%",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Sliders Card: Span L, Parameter A, Shift c
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Parameters & Transformations",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetTransformations() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp).testTag("reset_sliders_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset Sliders",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Span (L) slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tower Span (L):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", uiState.spanL),
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Slider(
                        value = uiState.spanL.toFloat(),
                        onValueChange = {
                            val rounded = (Math.round(it * 10.0) / 10.0)
                            viewModel.setSpanL(rounded)
                        },
                        valueRange = 1.0f..10.0f,
                        steps = 44,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("span_l_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Amplitude / Parameter (A) slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Amplitude / Parameter (A):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", uiState.paramA),
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Slider(
                        value = uiState.paramA.toFloat(),
                        onValueChange = {
                            val rounded = (Math.round(it * 10.0) / 10.0)
                            viewModel.setParamA(rounded)
                        },
                        valueRange = 0.5f..5.0f,
                        steps = 44,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("param_a_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Shift (c) slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Horizontal Shift (c):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", uiState.shiftC),
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Slider(
                        value = uiState.shiftC.toFloat(),
                        onValueChange = {
                            val rounded = (Math.round(it * 10.0) / 10.0)
                            viewModel.setShiftC(rounded)
                        },
                        valueRange = -3.0f..3.0f,
                        steps = 59,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shift_c_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }
        }

        // View Mode & Toggle Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GraphPreset.values().forEach { preset ->
                val isSelected = uiState.selectedPreset == preset
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectPreset(preset) },
                    label = { Text(preset.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("preset_${preset.name.lowercase(Locale.ROOT)}")
                )
            }

            // Toggle y = x Identity Line
            FilterChip(
                selected = uiState.showYEqualsX,
                onClick = { viewModel.toggleYEqualsX() },
                label = { Text("Toggle y = x Line") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                modifier = Modifier.testTag("toggle_y_equals_x_chip")
            )

            // Toggle Tangent Line Slope (dy/dx)
            FilterChip(
                selected = uiState.showTangentLine,
                onClick = { viewModel.toggleTangentLine() },
                label = { Text("Tangent Slope (dy/dx)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (uiState.showTangentLine) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFF43F5E).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFFE11D48)
                ),
                modifier = Modifier.testTag("toggle_tangent_line_chip")
            )

            // Toggle Parabola Comparison (cosh vs y = x²)
            FilterChip(
                selected = uiState.showParabolaComparison,
                onClick = { viewModel.toggleParabolaComparison() },
                label = { Text("Compare Parabola (y = x²)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (uiState.showParabolaComparison) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFFB45309)
                ),
                modifier = Modifier.testTag("toggle_parabola_comparison_chip")
            )
        }

        // Main Graph Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Interactive Mode Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isPanZoomMode) Icons.Default.PanTool else Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (uiState.isPanZoomMode) "Pan & Pinch Zoom Mode" else "Drag on canvas to inspect coordinates",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "x: [${String.format(Locale.US, "%.1f", uiState.bounds.xMin)}, ${String.format(Locale.US, "%.1f", uiState.bounds.xMax)}]",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // The Canvas
                HyperbolicPlotCanvas(
                    bounds = uiState.bounds,
                    activeFunctions = uiState.activeFunctions,
                    paramA = uiState.paramA,
                    spanL = uiState.spanL,
                    shiftC = uiState.shiftC,
                    scrubX = uiState.scrubX,
                    onScrubChange = { viewModel.setScrubX(it) },
                    onBoundsChange = { viewModel.updateBounds(it) },
                    showGrid = uiState.showGrid,
                    showAsymptotes = uiState.showAsymptotes,
                    showYEqualsX = uiState.showYEqualsX,
                    showTangentLine = uiState.showTangentLine,
                    showParabolaComparison = uiState.showParabolaComparison,
                    parabolaMode = uiState.parabolaMode,
                    morphBlend = uiState.morphBlend,
                    isAutoMorphing = uiState.isAutoMorphing,
                    showTowers = true,
                    isPanZoomMode = uiState.isPanZoomMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                )
            }
        }

        // Functions Toggle Section
        FunctionSelectorSection(
            activeFunctions = uiState.activeFunctions,
            onToggleFunction = { viewModel.toggleFunction(it) }
        )

        // Live Point Inspector Card
        PointInspectorCard(
            scrubX = uiState.scrubX,
            paramA = uiState.paramA,
            shiftC = uiState.shiftC,
            activeFunctions = uiState.activeFunctions,
            onScrubChange = { viewModel.setScrubX(it) },
            boundsMinX = uiState.bounds.xMin,
            boundsMaxX = uiState.bounds.xMax,
            showTangentLine = uiState.showTangentLine,
            onToggleTangentLine = { viewModel.toggleTangentLine() },
            showParabolaComparison = uiState.showParabolaComparison,
            parabolaMode = uiState.parabolaMode
        )

        // Interactive Parabola vs Hyperbolic Cosine Comparison Card
        ParabolaComparisonCard(
            showParabolaComparison = uiState.showParabolaComparison,
            parabolaMode = uiState.parabolaMode,
            scrubX = uiState.scrubX,
            paramA = uiState.paramA,
            shiftC = uiState.shiftC,
            morphBlend = uiState.morphBlend,
            isAutoMorphing = uiState.isAutoMorphing,
            onToggleComparison = { viewModel.toggleParabolaComparison() },
            onSelectMode = { viewModel.setParabolaMode(it) },
            onMorphBlendChange = { viewModel.setMorphBlend(it) },
            onToggleAutoMorph = { viewModel.toggleAutoMorph() }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun EngineeringStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PhysicsTabView(
    uiState: HyperbolicUiState,
    viewModel: HyperbolicViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CatenaryDemoCard(
            calculation = uiState.catenaryCalculation,
            selectedPreset = uiState.selectedCablePreset,
            onTensionChange = { viewModel.updateCableTension(it) },
            onSpanChange = { viewModel.updateCableSpan(it) },
            onMassChange = { viewModel.updateCableMassDensity(it) },
            onPresetSelect = { viewModel.applyCablePreset(it) }
        )

        // Additional physics explanations
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Why Hyperbolic Cosine in Physics?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "1. Hanging Cables (Catenary):\n" +
                            "When an idealized chain or cable hangs freely under uniform gravity, the balance of horizontal tension and vertical distributed weight results in the differential equation:\n" +
                            "y'' = (w / T₀) √(1 + (y')²)\n" +
                            "Solving this yields the catenary: y = a · cosh(x/a).\n\n" +
                            "2. Gateway Arch in St. Louis:\n" +
                            "The monumental arch is a weighted inverted catenary arch described by y = -a · cosh(x/b) + C, which transfers all compressive load directly along the curve without bending moments!\n\n" +
                            "3. Special Relativity Rapidity:\n" +
                            "In Einstein's special relativity, Lorentz boosts use hyperbolic rotations: velocity addition corresponds to summing rapidities θ where β = v/c = tanh(θ).",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun IdentitiesTabView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Interactive Calculus Properties and Mathematical Derivation Card
        HyperbolicCalculusDerivationCard()

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Hyperbolic Identities & Formulas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IdentityGroup(
                    groupName = "Exponential Definitions",
                    rows = listOf(
                        "sinh(x) = (eˣ - e⁻ˣ) / 2",
                        "cosh(x) = (eˣ + e⁻ˣ) / 2",
                        "tanh(x) = (eˣ - e⁻ˣ) / (eˣ + e⁻ˣ)",
                        "sech(x) = 1 / cosh(x)",
                        "csch(x) = 1 / sinh(x)",
                        "coth(x) = 1 / tanh(x)"
                    )
                )

                IdentityGroup(
                    groupName = "Pythagorean Identities",
                    rows = listOf(
                        "cosh²(x) - sinh²(x) = 1",
                        "1 - tanh²(x) = sech²(x)",
                        "coth²(x) - 1 = csch²(x)"
                    )
                )

                IdentityGroup(
                    groupName = "Addition & Double-Angle Formulas",
                    rows = listOf(
                        "sinh(x ± y) = sinh(x)cosh(y) ± cosh(x)sinh(y)",
                        "cosh(x ± y) = cosh(x)cosh(y) ± sinh(x)sinh(y)",
                        "sinh(2x) = 2 · sinh(x) · cosh(x)",
                        "cosh(2x) = cosh²(x) + sinh²(x) = 2cosh²(x) - 1"
                    )
                )

                IdentityGroup(
                    groupName = "Derivatives",
                    rows = listOf(
                        "d/dx [sinh(x)] = cosh(x)",
                        "d/dx [cosh(x)] = sinh(x)",
                        "d/dx [tanh(x)] = sech²(x)",
                        "d/dx [sech(x)] = -sech(x) · tanh(x)",
                        "d/dx [csch(x)] = -csch(x) · coth(x)",
                        "d/dx [coth(x)] = -csch²(x)"
                    )
                )

                IdentityGroup(
                    groupName = "Taylor Series Expansions",
                    rows = listOf(
                        "sinh(x) = x + x³/3! + x⁵/5! + ...",
                        "cosh(x) = 1 + x²/2! + x⁴/4! + ...",
                        "tanh(x) = x - x³/3 + 2x⁵/15 - ..."
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun IdentityGroup(
    groupName: String,
    rows: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = groupName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rows.forEach { row ->
                    Text(
                        text = row,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
