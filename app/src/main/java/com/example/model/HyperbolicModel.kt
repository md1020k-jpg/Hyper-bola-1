package com.example.model

import androidx.compose.ui.graphics.Color
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cosh
import kotlin.math.ln
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tanh

enum class FunctionCategory {
    PRIMARY,
    RECIPROCAL,
    INVERSE
}

enum class HyperbolicFunc(
    val displayName: String,
    val shortName: String,
    val color: Color,
    val formulaText: String,
    val derivativeText: String,
    val category: FunctionCategory
) {
    SINH(
        displayName = "sinh(x)",
        shortName = "sinh",
        color = Color(0xFF2563EB), // Electric blue
        formulaText = "(eˣ - e⁻ˣ) / 2",
        derivativeText = "cosh(x)",
        category = FunctionCategory.PRIMARY
    ),
    COSH(
        displayName = "cosh(x)",
        shortName = "cosh",
        color = Color(0xFFDC2626), // Crimson red (Catenary)
        formulaText = "(eˣ + e⁻ˣ) / 2",
        derivativeText = "sinh(x)",
        category = FunctionCategory.PRIMARY
    ),
    TANH(
        displayName = "tanh(x)",
        shortName = "tanh",
        color = Color(0xFF16A34A), // Emerald green
        formulaText = "sinh(x) / cosh(x)",
        derivativeText = "sech²(x)",
        category = FunctionCategory.PRIMARY
    ),
    SECH(
        displayName = "sech(x)",
        shortName = "sech",
        color = Color(0xFF8B5CF6), // Violet
        formulaText = "1 / cosh(x)",
        derivativeText = "-sech(x) · tanh(x)",
        category = FunctionCategory.RECIPROCAL
    ),
    CSCH(
        displayName = "csch(x)",
        shortName = "csch",
        color = Color(0xFF06B6D4), // Cyan
        formulaText = "1 / sinh(x)",
        derivativeText = "-csch(x) · coth(x)",
        category = FunctionCategory.RECIPROCAL
    ),
    COTH(
        displayName = "coth(x)",
        shortName = "coth",
        color = Color(0xFFEA580C), // Orange
        formulaText = "1 / tanh(x)",
        derivativeText = "-csch²(x)",
        category = FunctionCategory.RECIPROCAL
    ),
    ARSINH(
        displayName = "arcsinh(x)",
        shortName = "arcsinh",
        color = Color(0xFF9333EA), // Purple dashed
        formulaText = "ln(x + √(x² + 1))",
        derivativeText = "1 / √(x² + 1)",
        category = FunctionCategory.INVERSE
    ),
    ARCOSH(
        displayName = "arccosh(x)",
        shortName = "arccosh",
        color = Color(0xFFD97706), // Amber dashed
        formulaText = "ln(x + √(x² - 1)) [x ≥ 1]",
        derivativeText = "1 / √(x² - 1)",
        category = FunctionCategory.INVERSE
    ),
    ARTANH(
        displayName = "arctanh(x)",
        shortName = "arctanh",
        color = Color(0xFF0D9488), // Teal dashed
        formulaText = "½ ln((1+x) / (1-x)) [|x| < 1]",
        derivativeText = "1 / (1 - x²)",
        category = FunctionCategory.INVERSE
    );

    fun evaluate(x: Double, paramA: Double = 2.0, shiftC: Double = 0.0): Double? {
        val innerArg = (x - shiftC) / paramA
        return try {
            when (this) {
                SINH -> paramA * sinh(innerArg)
                COSH -> paramA * cosh(innerArg)
                TANH -> paramA * tanh(innerArg)
                SECH -> {
                    val c = cosh(innerArg)
                    if (abs(c) < 1e-12) null else paramA * (1.0 / c)
                }
                CSCH -> {
                    val s = sinh(innerArg)
                    if (abs(s) < 1e-12) null else paramA * (1.0 / s)
                }
                COTH -> {
                    val t = tanh(innerArg)
                    if (abs(t) < 1e-12) null else paramA * (1.0 / t)
                }
                ARSINH -> {
                    paramA * ln(innerArg + sqrt(innerArg * innerArg + 1.0))
                }
                ARCOSH -> {
                    if (innerArg < 1.0) null
                    else paramA * ln(innerArg + sqrt(innerArg * innerArg - 1.0))
                }
                ARTANH -> {
                    if (abs(innerArg) >= 1.0) null
                    else paramA * (0.5 * ln((1.0 + innerArg) / (1.0 - innerArg)))
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun evaluateDerivative(x: Double, paramA: Double = 2.0, shiftC: Double = 0.0): Double? {
        val innerArg = (x - shiftC) / paramA
        return try {
            when (this) {
                SINH -> cosh(innerArg)
                COSH -> sinh(innerArg)
                TANH -> {
                    val t = tanh(innerArg)
                    1.0 - t * t
                }
                SECH -> {
                    val c = cosh(innerArg)
                    if (abs(c) < 1e-12) null else -(1.0 / c) * tanh(innerArg)
                }
                CSCH -> {
                    val s = sinh(innerArg)
                    val t = tanh(innerArg)
                    if (abs(s) < 1e-12 || abs(t) < 1e-12) null else -(1.0 / s) * (1.0 / t)
                }
                COTH -> {
                    val s = sinh(innerArg)
                    if (abs(s) < 1e-12) null else -1.0 / (s * s)
                }
                ARSINH -> {
                    1.0 / sqrt(innerArg * innerArg + 1.0)
                }
                ARCOSH -> {
                    if (innerArg <= 1.0) null
                    else 1.0 / sqrt(innerArg * innerArg - 1.0)
                }
                ARTANH -> {
                    if (abs(innerArg) >= 1.0) null
                    else 1.0 / (1.0 - innerArg * innerArg)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun transformedName(paramA: Double = 2.0, shiftC: Double = 0.0): String {
        val shiftStr = if (abs(shiftC) > 0.01) {
            if (shiftC >= 0) " - ${String.format(Locale.US, "%.1f", shiftC)}"
            else " + ${String.format(Locale.US, "%.1f", abs(shiftC))}"
        } else ""
        val aFormatted = String.format(Locale.US, "%.1f", paramA)
        val innerStr = if (abs(shiftC) > 0.01) "(x$shiftStr)/$aFormatted" else "x/$aFormatted"
        return when (this) {
            COSH -> "$aFormatted cosh($innerStr) [Catenary]"
            else -> "$aFormatted $shortName($innerStr)"
        }
    }
}

data class GraphBounds(
    val xMin: Float = -7.0f,
    val xMax: Float = 7.0f,
    val yMin: Float = -3.0f,
    val yMax: Float = 9.0f
) {
    val xSpan: Float get() = xMax - xMin
    val ySpan: Float get() = yMax - yMin
}

enum class GraphPreset(
    val title: String,
    val bounds: GraphBounds,
    val defaultFunctions: Set<HyperbolicFunc>
) {
    SHOW_ALL(
        title = "Show All Functions",
        bounds = GraphBounds(xMin = -7.0f, xMax = 7.0f, yMin = -3.0f, yMax = 9.0f),
        defaultFunctions = setOf(
            HyperbolicFunc.SINH,
            HyperbolicFunc.COSH,
            HyperbolicFunc.TANH,
            HyperbolicFunc.ARSINH,
            HyperbolicFunc.ARCOSH,
            HyperbolicFunc.ARTANH
        )
    ),
    STANDARD(
        title = "Standard Only",
        bounds = GraphBounds(xMin = -7.0f, xMax = 7.0f, yMin = -3.0f, yMax = 9.0f),
        defaultFunctions = setOf(HyperbolicFunc.SINH, HyperbolicFunc.COSH, HyperbolicFunc.TANH)
    ),
    INVERSE(
        title = "Inverse Only",
        bounds = GraphBounds(xMin = -7.0f, xMax = 7.0f, yMin = -3.0f, yMax = 9.0f),
        defaultFunctions = setOf(HyperbolicFunc.ARSINH, HyperbolicFunc.ARCOSH, HyperbolicFunc.ARTANH)
    ),
    RECIPROCALS(
        title = "Reciprocals",
        bounds = GraphBounds(xMin = -7.0f, xMax = 7.0f, yMin = -3.0f, yMax = 9.0f),
        defaultFunctions = setOf(HyperbolicFunc.SECH, HyperbolicFunc.CSCH, HyperbolicFunc.COTH)
    ),
    WIDE(
        title = "Wide [-10, 10]",
        bounds = GraphBounds(xMin = -10.0f, xMax = 10.0f, yMin = -5.0f, yMax = 20.0f),
        defaultFunctions = setOf(HyperbolicFunc.SINH, HyperbolicFunc.COSH, HyperbolicFunc.TANH)
    )
}

enum class ParabolaMode(val title: String, val formula: String) {
    STANDARD_X_SQUARED(
        title = "Standard y = x²",
        formula = "y = x²"
    ),
    TAYLOR_SERIES(
        title = "Taylor Series y = 1 + x²/2",
        formula = "y = 1 + x²/2"
    ),
    MATCHED_CATENARY_PARABOLA(
        title = "Matched Vertex y = A + (x-c)²/(2A)",
        formula = "y = A + (x-c)²/(2A)"
    )
}

