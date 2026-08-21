package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.FunctionCategory
import com.example.model.GraphBounds
import com.example.model.HyperbolicFunc
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun HyperbolicPlotCanvas(
    bounds: GraphBounds,
    activeFunctions: Set<HyperbolicFunc>,
    paramA: Double = 2.0,
    spanL: Double = 6.0,
    shiftC: Double = 0.0,
    scrubX: Double?,
    onScrubChange: (Double?) -> Unit,
    onBoundsChange: (GraphBounds) -> Unit,
    showGrid: Boolean = true,
    showAsymptotes: Boolean = true,
    showYEqualsX: Boolean = false,
    showTowers: Boolean = true,
    isPanZoomMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val zeroLineColor = Color(0xFF475569)
    val gridLineColor = Color(0xFFE2E8F0).copy(alpha = 0.8f)
    val darkGridLineColor = Color(0xFF334155).copy(alpha = 0.5f)
    val asymptoteColor = Color(0xFF94A3B8)
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

    val textPaint = remember(isDark) {
        Paint().apply {
            color = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.DKGRAY
            textSize = 28f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    val yTextPaint = remember(isDark) {
        Paint().apply {
            color = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.DKGRAY
            textSize = 26f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
    }

    val effectiveGridColor = if (isDark) darkGridLineColor else gridLineColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF))
            .testTag("hyperbolic_plot_canvas")
            .then(
                if (isPanZoomMode) {
                    Modifier.pointerInput(bounds) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val currentXSpan = bounds.xSpan
                            val currentYSpan = bounds.ySpan
                            val newXSpan = (currentXSpan / zoom).coerceIn(1.0f, 40.0f)
                            val newYSpan = (currentYSpan / zoom).coerceIn(1.5f, 60.0f)
                            val xMid = (bounds.xMin + bounds.xMax) / 2f - (pan.x / size.width) * newXSpan
                            val yMid = (bounds.yMin + bounds.yMax) / 2f + (pan.y / size.height) * newYSpan
                            val newBounds = GraphBounds(
                                xMin = xMid - newXSpan / 2f,
                                xMax = xMid + newXSpan / 2f,
                                yMin = yMid - newYSpan / 2f,
                                yMax = yMid + newYSpan / 2f
                            )
                            onBoundsChange(newBounds)
                        }
                    }
                } else {
                    Modifier
                        .pointerInput(bounds) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val mappedX = bounds.xMin + (offset.x / size.width) * bounds.xSpan
                                    onScrubChange(mappedX.toDouble())
                                }
                            )
                        }
                        .pointerInput(bounds) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val mappedX = bounds.xMin + (offset.x / size.width) * bounds.xSpan
                                    onScrubChange(mappedX.toDouble())
                                },
                                onDrag = { change, _ ->
                                    val mappedX = bounds.xMin + (change.position.x / size.width) * bounds.xSpan
                                    onScrubChange(mappedX.toDouble())
                                }
                            )
                        }
                }
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp)) {
            val width = size.width
            val height = size.height
            if (width <= 0 || height <= 0) return@Canvas

            fun mapX(x: Double): Float = ((x - bounds.xMin) / bounds.xSpan * width).toFloat()
            fun mapY(y: Double): Float = ((bounds.yMax - y) / bounds.ySpan * height).toFloat()

            // 1. Draw Grid Lines and Numeric Ticks
            if (showGrid) {
                drawGridAndAxes(
                    bounds = bounds,
                    width = width,
                    height = height,
                    gridColor = effectiveGridColor,
                    zeroLineColor = zeroLineColor,
                    textPaint = textPaint,
                    yTextPaint = yTextPaint,
                    mapX = ::mapX,
                    mapY = ::mapY
                )
            }

            // 2. Draw Asymptotes (e.g. tanh y = A and y = -A)
            if (showAsymptotes && (activeFunctions.contains(HyperbolicFunc.TANH) || activeFunctions.contains(HyperbolicFunc.SECH))) {
                val y1 = mapY(paramA)
                val yMinus1 = mapY(-paramA)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                if (y1 in 0f..height) {
                    drawLine(
                        color = asymptoteColor,
                        start = Offset(0f, y1),
                        end = Offset(width, y1),
                        strokeWidth = 2f,
                        pathEffect = dashEffect
                    )
                }
                if (yMinus1 in 0f..height) {
                    drawLine(
                        color = asymptoteColor,
                        start = Offset(0f, yMinus1),
                        end = Offset(width, yMinus1),
                        strokeWidth = 2f,
                        pathEffect = dashEffect
                    )
                }
            }

            // 3. Draw y = x (Identity line) if active
            if (showYEqualsX) {
                val x1 = bounds.xMin.toDouble()
                val y1 = x1
                val x2 = bounds.xMax.toDouble()
                val y2 = x2
                drawLine(
                    color = Color(0xFF64748B),
                    start = Offset(mapX(x1), mapY(y1)),
                    end = Offset(mapX(x2), mapY(y2)),
                    strokeWidth = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                )
            }

            // 4. Draw Support Towers for Catenary
            if (showTowers) {
                val leftTowerX = -spanL / 2.0 + shiftC
                val rightTowerX = spanL / 2.0 + shiftC
                val towerTopY = paramA * kotlin.math.cosh(spanL / (2.0 * paramA))
                val leftPx = mapX(leftTowerX)
                val rightPx = mapX(rightTowerX)
                val basePy = mapY(0.0)
                val topPy = mapY(towerTopY)
                val effectiveTowerColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A)

                // Left Tower
                if (leftPx in -20f..(width + 20f)) {
                    drawLine(
                        color = effectiveTowerColor,
                        start = Offset(leftPx, basePy),
                        end = Offset(leftPx, topPy),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = effectiveTowerColor,
                        radius = 12f,
                        center = Offset(leftPx, topPy)
                    )
                    drawCircle(
                        color = Color(0xFFDC2626),
                        radius = 6f,
                        center = Offset(leftPx, topPy)
                    )
                }

                // Right Tower
                if (rightPx in -20f..(width + 20f)) {
                    drawLine(
                        color = effectiveTowerColor,
                        start = Offset(rightPx, basePy),
                        end = Offset(rightPx, topPy),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = effectiveTowerColor,
                        radius = 12f,
                        center = Offset(rightPx, topPy)
                    )
                    drawCircle(
                        color = Color(0xFFDC2626),
                        radius = 6f,
                        center = Offset(rightPx, topPy)
                    )
                }
            }

            // 5. Draw Hyperbolic Curves
            val numSteps = 400
            val xStep = bounds.xSpan.toDouble() / numSteps

            for (func in activeFunctions) {
                val path = Path()
                var isFirstPoint = true
                var prevY: Double? = null

                for (i in 0..numSteps) {
                    val x = bounds.xMin + i * xStep
                    val y = func.evaluate(x, paramA, shiftC)
                    if (y == null || y.isNaN() || y.isInfinite()) {
                        isFirstPoint = true
                        prevY = null
                        continue
                    }
                    if (prevY != null && abs(y - prevY) > bounds.ySpan * 1.8) {
                        isFirstPoint = true
                    }
                    val px = mapX(x)
                    val py = mapY(y)
                    val clampedPy = py.coerceIn(-height * 0.5f, height * 1.5f)

                    if (isFirstPoint) {
                        path.moveTo(px, clampedPy)
                        isFirstPoint = false
                    } else {
                        path.lineTo(px, clampedPy)
                    }
                    prevY = y
                }

                val strokeStyle = if (func.category == FunctionCategory.INVERSE) {
                    Stroke(
                        width = 6f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                    )
                } else if (func == HyperbolicFunc.COSH) {
                    Stroke(
                        width = 8f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                } else {
                    Stroke(
                        width = 6f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                }

                drawPath(
                    path = path,
                    color = func.color,
                    style = strokeStyle
                )
            }

            // 6. Draw Scrubber Indicator
            scrubX?.let { xVal ->
                if (xVal in bounds.xMin.toDouble()..bounds.xMax.toDouble()) {
                    val scrubPx = mapX(xVal)
                    // Vertical guideline
                    drawLine(
                        color = Color(0xFF6366F1),
                        start = Offset(scrubPx, 0f),
                        end = Offset(scrubPx, height),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )

                    // Intersection dots
                    for (func in activeFunctions) {
                        val yVal = func.evaluate(xVal, paramA, shiftC)
                        if (yVal != null && !yVal.isNaN() && !yVal.isInfinite()) {
                            val dotPy = mapY(yVal)
                            if (dotPy in -10f..(height + 10f)) {
                                drawCircle(
                                    color = func.color.copy(alpha = 0.35f),
                                    radius = 16f,
                                    center = Offset(scrubPx, dotPy)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 10f,
                                    center = Offset(scrubPx, dotPy)
                                )
                                drawCircle(
                                    color = func.color,
                                    radius = 7f,
                                    center = Offset(scrubPx, dotPy)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawGridAndAxes(
    bounds: GraphBounds,
    width: Float,
    height: Float,
    gridColor: Color,
    zeroLineColor: Color,
    textPaint: Paint,
    yTextPaint: Paint,
    mapX: (Double) -> Float,
    mapY: (Double) -> Float
) {
    val xStep = computeNiceStep(bounds.xSpan.toDouble() / 6.0)
    val yStep = computeNiceStep(bounds.ySpan.toDouble() / 6.0)

    val xStart = floor(bounds.xMin / xStep) * xStep
    val xEnd = ceil(bounds.xMax / xStep) * xStep

    var currX = xStart
    while (currX <= xEnd) {
        val px = mapX(currX)
        if (px in 0f..width) {
            val isZero = abs(currX) < 1e-6
            drawLine(
                color = if (isZero) zeroLineColor else gridColor,
                start = Offset(px, 0f),
                end = Offset(px, height),
                strokeWidth = if (isZero) 3.5f else 1.5f
            )
            val labelText = formatNumber(currX)
            val labelY = (mapY(0.0) + 38f).coerceIn(40f, height - 10f)
            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                px,
                labelY,
                textPaint
            )
        }
        currX += xStep
    }

    val yStart = floor(bounds.yMin / yStep) * yStep
    val yEnd = ceil(bounds.yMax / yStep) * yStep

    var currY = yStart
    while (currY <= yEnd) {
        val py = mapY(currY)
        if (py in 0f..height) {
            val isZero = abs(currY) < 1e-6
            drawLine(
                color = if (isZero) zeroLineColor else gridColor,
                start = Offset(0f, py),
                end = Offset(width, py),
                strokeWidth = if (isZero) 3.5f else 1.5f
            )
            if (!isZero) {
                val labelText = formatNumber(currY)
                val labelX = (mapX(0.0) - 12f).coerceIn(50f, width - 15f)
                drawContext.canvas.nativeCanvas.drawText(
                    labelText,
                    labelX,
                    py + 10f,
                    yTextPaint
                )
            }
        }
        currY += yStep
    }
}

private fun computeNiceStep(rawStep: Double): Double {
    val exponent = floor(log10(rawStep))
    val fraction = rawStep / 10.0.pow(exponent)
    val niceFraction = when {
        fraction < 1.5 -> 1.0
        fraction < 3.0 -> 2.0
        fraction < 7.0 -> 5.0
        else -> 10.0
    }
    return niceFraction * 10.0.pow(exponent)
}

private fun formatNumber(value: Double): String {
    return if (abs(value) < 1e-5) "0"
    else if (value == floor(value)) value.toInt().toString()
    else String.format(Locale.US, "%.1f", value)
}
