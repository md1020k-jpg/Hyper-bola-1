package com.example.model

import androidx.compose.ui.geometry.Offset
import java.util.Locale
import kotlin.math.atan
import kotlin.math.cosh
import kotlin.math.sinh
import kotlin.math.sqrt

/**
 * Standard physical constants for catenary mechanics.
 */
object CatenaryPhysicsConstants {
    const val STANDARD_GRAVITY = 9.80665 // m/s²
}

/**
 * Real-world cable presets for engineering and physics simulations.
 */
enum class CablePreset(
    val title: String,
    val description: String,
    val tensionN: Double,
    val spanM: Double,
    val massKgPerM: Double
) {
    HIGH_VOLTAGE_POWER_LINE(
        title = "HV Power Transmission Line",
        description = "Aluminum Conductor Steel Reinforced (ACSR) between pylons",
        tensionN = 28000.0,  // 28 kN
        spanM = 300.0,       // 300 m
        massKgPerM = 1.65    // 1.65 kg/m
    ),
    SUSPENSION_BRIDGE(
        title = "Suspension Bridge Cable",
        description = "Heavy parallel wire main stay cable",
        tensionN = 150000.0, // 150 kN
        spanM = 500.0,       // 500 m
        massKgPerM = 12.5    // 12.5 kg/m
    ),
    ZIPLINE(
        title = "Mountain Zipline Cable",
        description = "Galvanized steel wire rope under high tension",
        tensionN = 16000.0,  // 16 kN
        spanM = 150.0,       // 150 m
        massKgPerM = 1.2     // 1.2 kg/m
    ),
    TELECOM_FIBER(
        title = "Fiber Optic Aerial Drop",
        description = "Lightweight aerial self-supporting dielectric cable",
        tensionN = 2500.0,   // 2.5 kN
        spanM = 60.0,        // 60 m
        massKgPerM = 0.22    // 0.22 kg/m
    )
}

/**
 * Encapsulates the physics cable inputs and mathematical calculations for y = a * cosh(x / a).
 */
data class CatenaryCalculation(
    val horizontalTensionN: Double = 25000.0, // T₀ (Newtons)
    val spanM: Double = 200.0,                // L (Meters)
    val linearMassDensityKgPerM: Double = 1.5,// μ (kg/m)
    val gravity: Double = CatenaryPhysicsConstants.STANDARD_GRAVITY
) {
    // Linear weight density w = μ * g (N/m)
    val linearWeightDensityNpm: Double
        get() = linearMassDensityKgPerM * gravity

    // Catenary scaling parameter a = T₀ / w (meters)
    val parameterA: Double
        get() = if (linearWeightDensityNpm > 0.0) horizontalTensionN / linearWeightDensityNpm else 1.0

    // Half span (L / 2)
    val halfSpanM: Double
        get() = spanM / 2.0

    // Catenary curve function: y = a * cosh(x / a)
    fun evaluateY(x: Double): Double {
        val a = parameterA
        return a * cosh(x / a)
    }

    // Relative sag from lowest point: y_rel(x) = a * (cosh(x / a) - 1)
    fun evaluateSagAt(x: Double): Double {
        val a = parameterA
        return a * (cosh(x / a) - 1.0)
    }

    // First derivative / slope: y'(x) = sinh(x / a)
    fun evaluateSlope(x: Double): Double {
        val a = parameterA
        return sinh(x / a)
    }

    // Local tension at horizontal distance x: T(x) = T₀ * cosh(x / a) = T₀ + w * h(x)
    fun evaluateTensionN(x: Double): Double {
        val a = parameterA
        return horizontalTensionN * cosh(x / a)
    }

    // Maximum Cable Sag: h = a * (cosh(L / 2a) - 1)
    val maxSagM: Double
        get() = parameterA * (cosh(halfSpanM / parameterA) - 1.0)

    // Total Cable Arc Length: S = 2 * a * sinh(L / 2a)
    val arcLengthM: Double
        get() = 2.0 * parameterA * sinh(halfSpanM / parameterA)

    // Slack / Elongation: ΔS = S - L
    val elongationM: Double
        get() = arcLengthM - spanM

    // Slack Percentage: ((S - L) / L) * 100%
    val slackPercent: Double
        get() = if (spanM > 0.0) (elongationM / spanM) * 100.0 else 0.0

    // Support elevation at x = ±L/2: y(L/2) = a * cosh(L / 2a)
    val supportElevationM: Double
        get() = evaluateY(halfSpanM)

    // Maximum Tension at Support Pylons: T_max = T₀ * cosh(L / 2a)
    val maxTensionN: Double
        get() = evaluateTensionN(halfSpanM)

    // Vertical Reaction Force at each support: V = T₀ * sinh(L / 2a) = (w * S) / 2
    val verticalReactionN: Double
        get() = horizontalTensionN * sinh(halfSpanM / parameterA)

    // Angle of inclination at supports: θ = arctan(sinh(L / 2a))
    val supportAngleDeg: Double
        get() = Math.toDegrees(atan(evaluateSlope(halfSpanM)))

    // Total Cable Mass: M = μ * S (kg)
    val totalCableMassKg: Double
        get() = linearMassDensityKgPerM * arcLengthM

    // Total Suspended Weight: W = w * S (N)
    val totalCableWeightN: Double
        get() = linearWeightDensityNpm * arcLengthM

    /**
     * Samples the catenary curve between x = -L/2 and x = +L/2 into normalized coordinates
     * or real (x, y) coordinates for canvas rendering and visualization.
     */
    fun sampleCurvePoints(stepCount: Int = 100): List<CatenaryCurvePoint> {
        val count = stepCount.coerceAtLeast(10)
        val a = parameterA
        val l = spanM
        val hSpan = l / 2.0

        return (0..count).map { i ->
            val fraction = i.toDouble() / count.toDouble()
            val x = -hSpan + fraction * l
            val y = a * cosh(x / a)
            val sag = y - a
            val slope = sinh(x / a)
            val tension = horizontalTensionN * cosh(x / a)
            CatenaryCurvePoint(
                x = x,
                y = y,
                sag = sag,
                slope = slope,
                tensionN = tension,
                normalizedX = fraction.toFloat(),
                normalizedSag = if (maxSagM > 0.0) (sag / maxSagM).toFloat() else 0f
            )
        }
    }
}

/**
 * Data representation for a point along the computed catenary curve.
 */
data class CatenaryCurvePoint(
    val x: Double,
    val y: Double,
    val sag: Double,
    val slope: Double,
    val tensionN: Double,
    val normalizedX: Float,
    val normalizedSag: Float
)
