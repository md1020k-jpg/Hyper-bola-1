package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.model.CablePreset
import com.example.model.CatenaryCalculation
import com.example.model.CatenaryCurvePoint
import com.example.model.GraphBounds
import com.example.model.GraphPreset
import com.example.model.HyperbolicFunc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.cosh
import kotlin.math.sinh

enum class AppViewTab(val title: String) {
    PLOT("Interactive Plot"),
    PHYSICS("Catenary Physics"),
    IDENTITIES("Identities & Theory")
}

data class HyperbolicUiState(
    val activeFunctions: Set<HyperbolicFunc> = GraphPreset.SHOW_ALL.defaultFunctions,
    val bounds: GraphBounds = GraphPreset.SHOW_ALL.bounds,
    val spanL: Double = 6.0,   // Tower Span (L)
    val paramA: Double = 2.0,  // Parameter / Amplitude (A)
    val shiftC: Double = 0.0,  // Horizontal Shift (c)
    val scrubX: Double = 0.0,  // Default inspect coordinate
    val selectedPreset: GraphPreset = GraphPreset.SHOW_ALL,
    val selectedTab: AppViewTab = AppViewTab.PLOT,
    val showGrid: Boolean = true,
    val showAsymptotes: Boolean = true,
    val showYEqualsX: Boolean = false,
    val isPanZoomMode: Boolean = false,
    val showTheoryDialog: Boolean = false,
    val isDarkTheme: Boolean = false,
    // Physics Catenary Simulation State (y = a * cosh(x / a) where a = T₀ / (μ * g))
    val catenaryCalculation: CatenaryCalculation = CatenaryCalculation(
        horizontalTensionN = 25000.0,
        spanM = 200.0,
        linearMassDensityKgPerM = 1.5
    ),
    val selectedCablePreset: CablePreset? = CablePreset.HIGH_VOLTAGE_POWER_LINE
) {
    // S = 2 * A * sinh(L / (2 * A))
    val arcLength: Double get() = 2.0 * paramA * sinh(spanL / (2.0 * paramA))
    
    // h = A * (cosh(L / (2 * A)) - 1)
    val maxSag: Double get() = paramA * (cosh(spanL / (2.0 * paramA)) - 1.0)
    
    // Slack % = ((S - L) / L) * 100
    val slackPercent: Double get() = ((arcLength - spanL) / spanL) * 100.0
    
    // Tower coordinate properties
    val towerLeftX: Double get() = -spanL / 2.0 + shiftC
    val towerRightX: Double get() = spanL / 2.0 + shiftC
    val towerY: Double get() = paramA * cosh(spanL / (2.0 * paramA))
}

class HyperbolicViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HyperbolicUiState())
    val uiState: StateFlow<HyperbolicUiState> = _uiState.asStateFlow()

    fun toggleFunction(func: HyperbolicFunc) {
        _uiState.update { current ->
            val updated = if (current.activeFunctions.contains(func)) {
                if (current.activeFunctions.size > 1) current.activeFunctions - func else current.activeFunctions
            } else {
                current.activeFunctions + func
            }
            current.copy(activeFunctions = updated)
        }
    }

    fun setScrubX(x: Double?) {
        if (x != null) {
            _uiState.update { it.copy(scrubX = x) }
        }
    }

    fun updateBounds(newBounds: GraphBounds) {
        _uiState.update { it.copy(bounds = newBounds) }
    }

    fun selectPreset(preset: GraphPreset) {
        _uiState.update {
            it.copy(
                selectedPreset = preset,
                bounds = preset.bounds,
                activeFunctions = preset.defaultFunctions,
                scrubX = 0.0
            )
        }
    }

    fun resetBounds() {
        selectPreset(GraphPreset.SHOW_ALL)
    }

    fun setSpanL(newL: Double) {
        _uiState.update { it.copy(spanL = newL) }
    }

    fun setParamA(newA: Double) {
        _uiState.update { it.copy(paramA = newA) }
    }

    fun setShiftC(newShift: Double) {
        _uiState.update { it.copy(shiftC = newShift) }
    }

    fun resetTransformations() {
        _uiState.update { it.copy(spanL = 6.0, paramA = 2.0, shiftC = 0.0) }
    }

    fun toggleYEqualsX() {
        _uiState.update { it.copy(showYEqualsX = !it.showYEqualsX) }
    }

    fun setSelectedTab(tab: AppViewTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun togglePanZoomMode() {
        _uiState.update { it.copy(isPanZoomMode = !it.isPanZoomMode) }
    }

    fun toggleGrid() {
        _uiState.update { it.copy(showGrid = !it.showGrid) }
    }

    fun toggleAsymptotes() {
        _uiState.update { it.copy(showAsymptotes = !it.showAsymptotes) }
    }

    fun setTheoryDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showTheoryDialog = visible) }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun setDarkTheme(isDark: Boolean) {
        _uiState.update { it.copy(isDarkTheme = isDark) }
    }

    // =========================================================================
    // Catenary Cable Physics Calculation Engine (y = a * cosh(x / a))
    // =========================================================================

    /**
     * Updates the horizontal tension T₀ (in Newtons).
     * Recomputes catenary scaling parameter a = T₀ / (μ * g).
     */
    fun updateCableTension(tensionN: Double) {
        _uiState.update { current ->
            val updatedCalc = current.catenaryCalculation.copy(
                horizontalTensionN = tensionN.coerceAtLeast(10.0)
            )
            current.copy(
                catenaryCalculation = updatedCalc,
                selectedCablePreset = null
            )
        }
    }

    /**
     * Updates the span L between cable supports/pylons (in meters).
     */
    fun updateCableSpan(spanM: Double) {
        _uiState.update { current ->
            val updatedCalc = current.catenaryCalculation.copy(
                spanM = spanM.coerceAtLeast(1.0)
            )
            current.copy(
                catenaryCalculation = updatedCalc,
                selectedCablePreset = null
            )
        }
    }

    /**
     * Updates the cable linear mass density μ (in kg/m).
     * Recomputes linear weight density w = μ * g and a = T₀ / w.
     */
    fun updateCableMassDensity(massKgPerM: Double) {
        _uiState.update { current ->
            val updatedCalc = current.catenaryCalculation.copy(
                linearMassDensityKgPerM = massKgPerM.coerceAtLeast(0.01)
            )
            current.copy(
                catenaryCalculation = updatedCalc,
                selectedCablePreset = null
            )
        }
    }

    /**
     * Applies a predefined real-world engineering cable preset.
     */
    fun applyCablePreset(preset: CablePreset) {
        _uiState.update { current ->
            val updatedCalc = CatenaryCalculation(
                horizontalTensionN = preset.tensionN,
                spanM = preset.spanM,
                linearMassDensityKgPerM = preset.massKgPerM
            )
            current.copy(
                catenaryCalculation = updatedCalc,
                selectedCablePreset = preset
            )
        }
    }

    /**
     * Calculates the catenary elevation y, relative sag, slope, and local tension at position x.
     * Uses equation: y(x) = a * cosh(x / a).
     */
    fun calculateCatenaryPoint(x: Double): CatenaryCurvePoint {
        val calc = _uiState.value.catenaryCalculation
        val a = calc.parameterA
        val y = calc.evaluateY(x)
        val sag = y - a
        val slope = calc.evaluateSlope(x)
        val tension = calc.evaluateTensionN(x)
        val halfSpan = calc.halfSpanM
        val fraction = if (calc.spanM > 0.0) ((x + halfSpan) / calc.spanM).toFloat().coerceIn(0f, 1f) else 0.5f
        val normSag = if (calc.maxSagM > 0.0) (sag / calc.maxSagM).toFloat() else 0f
        return CatenaryCurvePoint(
            x = x,
            y = y,
            sag = sag,
            slope = slope,
            tensionN = tension,
            normalizedX = fraction,
            normalizedSag = normSag
        )
    }

    /**
     * Generates evenly sampled points along the catenary curve between x = -L/2 and x = +L/2.
     */
    fun getSampledCatenaryCurve(stepCount: Int = 100): List<CatenaryCurvePoint> {
        return _uiState.value.catenaryCalculation.sampleCurvePoints(stepCount)
    }
}

