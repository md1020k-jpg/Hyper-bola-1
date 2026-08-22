package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
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
import com.example.model.ParabolaMode
import com.example.ui.HyperbolicUiState
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Convenient overload of HyperbolicPlotCanvas accepting the full HyperbolicUiState.
 */
@Composable
fun HyperbolicPlotCanvas(
    uiState: HyperbolicUiState,
    onScrubChange: (Double?) -> Unit,
    onBoundsChange: (GraphBounds) -> Unit,
    modifier: Modifier = Modifier
) {
    HyperbolicPlotCanvas(
        bounds = uiState.bounds,
        activeFunctions = uiState.activeFunctions,
        paramA = uiState.paramA,
        spanL = uiState.spanL,
        shiftC = uiState.shiftC,
        scrubX = uiState.scrubX,
        onScrubChange = onScrubChange,
        onBoundsChange = onBoundsChange,
        showGrid = uiState.showGrid,
        showAsymptotes = uiState.showAsymptotes,
        showYEqualsX = uiState.showYEqualsX,
        showParabolaComparison = uiState.showParabolaComparison,
        parabolaMode = uiState.parabolaMode,
        morphBlend = uiState.morphBlend,
        isAutoMorphing = uiState.isAutoMorphing,
        showTowers = true,
        isPanZoomMode = uiState.isPanZoomMode,
        modifier = modifier
    )
}

/**
 * Custom Compose Canvas component that draws the 2D Cartesian coordinate grid,
 * axis numeric labels, asymptotes, and calculates/renders all active hyperbolic curves
 * and smoothly animated parabola comparison morph transitions.
 */
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
    showParabolaComparison: Boolean = false,
    parabolaMode: ParabolaMode = ParabolaMode.STANDARD_X_SQUARED,
    morphBlend: Float = 1.0f,
    isAutoMorphing: Boolean = false,
    showTowers: Boolean = true,
    isPanZoomMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val zeroLineColor = Color(0xFF64748B)
    val gridLineColor = Color(0xFFE2E8F0).copy(alpha = 0.85f)
    val darkGridLineColor = Color(0xFF334155).copy(alpha = 0.6f)
    val asymptoteColor = Color(0xFF94A3B8)
    val parabolaColor = Color(0xFFF59E0B) // Amber gold for parabola comparison
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

    // Smooth continuous auto-morph animation loop if enabled
    val infiniteTransition = rememberInfiniteTransition(label = "morphAnimationLoop")
    val autoMorphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "autoMorphValue"
    )

    val targetBlend = if (!showParabolaComparison) 0f else if (isAutoMorphing) autoMorphProgress else morphBlend

    // Animate morph transition smoothly between hyperbolic cosine and the parabola curve
    val animatedMorphProgress by animateFloatAsState(
        targetValue = targetBlend,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "morphProgress"
    )

    // Animate overall comparison visibility / alpha
    val animatedParabolaAlpha by animateFloatAsState(
        targetValue = if (showParabolaComparison) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "parabolaAlpha"
    )

    val textPaint = remember(isDark) {
        Paint().apply {
            color = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.DKGRAY
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    val yTextPaint = remember(isDark) {
        Paint().apply {
            color = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.DKGRAY
            textSize = 26f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
    }

    val hudHeaderPaint = remember {
        Paint().apply {
            color = android.graphics.Color.rgb(147, 197, 253) // light blue
            textSize = 22f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }
    }

    val hudTextPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 25f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }
    }

    val hudSubTextPaint = remember {
        Paint().apply {
            color = android.graphics.Color.rgb(253, 224, 71) // amber yellow
            textSize = 23f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }
    }

    val hudBgPaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(235, 15, 23, 42)
            isAntiAlias = true
        }
    }

    val hudBorderPaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(180, 99, 102, 241)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
    }

    val effectiveGridColor = if (isDark) darkGridLineColor else gridLineColor

    var isTouching by remember { mutableStateOf(false) }
    var touchPoint by remember { mutableStateOf<Offset?>(null) }

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
                    Modifier.pointerInput(bounds) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            touchPoint = down.position
                            isTouching = true
                            val mappedX = bounds.xMin + (down.position.x / size.width) * bounds.xSpan
                            onScrubChange(mappedX.toDouble().coerceIn(bounds.xMin.toDouble(), bounds.xMax.toDouble()))

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) {
                                    isTouching = false
                                    break
                                }
                                touchPoint = change.position
                                val newMappedX = bounds.xMin + (change.position.x / size.width) * bounds.xSpan
                                onScrubChange(newMappedX.toDouble().coerceIn(bounds.xMin.toDouble(), bounds.xMax.toDouble()))
                            }
                            isTouching = false
                        }
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

            // 1. Draw Grid Lines, Coordinate Axes and Numeric Ticks
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

            // 2. Draw Asymptotes (e.g. tanh y = ±A and sech y = 0)
            if (showAsymptotes && (activeFunctions.contains(HyperbolicFunc.TANH) || activeFunctions.contains(HyperbolicFunc.COTH))) {
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

            // 3. Draw y = x (Identity line) for inverse function symmetry comparison
            if (showYEqualsX) {
                val x1 = bounds.xMin.toDouble()
                val y1 = x1
                val x2 = bounds.xMax.toDouble()
                val y2 = x2
                drawLine(
                    color = Color(0xFF64748B),
                    start = Offset(mapX(x1), mapY(y1)),
                    end = Offset(mapX(x2), mapY(y2)),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                )
            }

            // 4. Draw Catenary Cable Support Towers
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
                        strokeWidth = 7f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = effectiveTowerColor,
                        radius = 10f,
                        center = Offset(leftPx, topPy)
                    )
                    drawCircle(
                        color = Color(0xFFDC2626),
                        radius = 5f,
                        center = Offset(leftPx, topPy)
                    )
                }

                // Right Tower
                if (rightPx in -20f..(width + 20f)) {
                    drawLine(
                        color = effectiveTowerColor,
                        start = Offset(rightPx, basePy),
                        end = Offset(rightPx, topPy),
                        strokeWidth = 7f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = effectiveTowerColor,
                        radius = 10f,
                        center = Offset(rightPx, topPy)
                    )
                    drawCircle(
                        color = Color(0xFFDC2626),
                        radius = 5f,
                        center = Offset(rightPx, topPy)
                    )
                }
            }

            val numSteps = 450
            val xStep = bounds.xSpan.toDouble() / numSteps

            // 5. Draw Calculated Hyperbolic Function Curves
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
                        width = 7.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                } else {
                    Stroke(
                        width = 5.5f,
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

            // 5b. Draw Animated Parabola Comparison Curve and Shaded Morph Divergence Ribbon
            val effectiveAlpha = animatedParabolaAlpha.coerceIn(0f, 1f)
            val effectiveBlend = animatedMorphProgress.coerceIn(0f, 1f)

            if (effectiveAlpha > 0.005f) {
                // Shaded Divergence Area between cosh(x) and the morphed comparison curve
                if (effectiveBlend > 0.02f) {
                    val divergencePath = Path()
                    var hasFirstDivPoint = false

                    // Trace cosh(x) forward
                    for (i in 0..numSteps) {
                        val x = bounds.xMin + i * xStep
                        val coshY = paramA * kotlin.math.cosh((x - shiftC) / paramA)
                        if (coshY.isNaN() || coshY.isInfinite()) continue
                        val px = mapX(x)
                        val py = mapY(coshY).coerceIn(-height * 0.5f, height * 1.5f)
                        if (!hasFirstDivPoint) {
                            divergencePath.moveTo(px, py)
                            hasFirstDivPoint = true
                        } else {
                            divergencePath.lineTo(px, py)
                        }
                    }

                    // Trace morphed parabola backward to close polygon
                    for (i in numSteps downTo 0) {
                        val x = bounds.xMin + i * xStep
                        val coshY = paramA * kotlin.math.cosh((x - shiftC) / paramA)
                        val paraY = when (parabolaMode) {
                            ParabolaMode.STANDARD_X_SQUARED -> x * x
                            ParabolaMode.TAYLOR_SERIES -> 1.0 + (x * x) / 2.0
                            ParabolaMode.MATCHED_CATENARY_PARABOLA -> {
                                val dx = x - shiftC
                                paramA + (dx * dx) / (2.0 * paramA)
                            }
                        }
                        val morphedY = (1.0 - effectiveBlend) * coshY + effectiveBlend * paraY
                        if (morphedY.isNaN() || morphedY.isInfinite()) continue
                        val px = mapX(x)
                        val py = mapY(morphedY).coerceIn(-height * 0.5f, height * 1.5f)
                        divergencePath.lineTo(px, py)
                    }

                    if (hasFirstDivPoint) {
                        divergencePath.close()
                        drawPath(
                            path = divergencePath,
                            color = parabolaColor.copy(alpha = 0.20f * effectiveAlpha * effectiveBlend)
                        )
                    }
                }

                // Render Morphed Parabola Curve Path
                val parabolaPath = Path()
                var isFirstParaPoint = true
                for (i in 0..numSteps) {
                    val x = bounds.xMin + i * xStep
                    val coshY = paramA * kotlin.math.cosh((x - shiftC) / paramA)
                    val targetParaY = when (parabolaMode) {
                        ParabolaMode.STANDARD_X_SQUARED -> x * x
                        ParabolaMode.TAYLOR_SERIES -> 1.0 + (x * x) / 2.0
                        ParabolaMode.MATCHED_CATENARY_PARABOLA -> {
                            val dx = x - shiftC
                            paramA + (dx * dx) / (2.0 * paramA)
                        }
                    }
                    val morphedY = (1.0 - effectiveBlend) * coshY + effectiveBlend * targetParaY

                    if (morphedY.isNaN() || morphedY.isInfinite()) {
                        isFirstParaPoint = true
                        continue
                    }
                    val px = mapX(x)
                    val py = mapY(morphedY)
                    val clampedPy = py.coerceIn(-height * 0.5f, height * 1.5f)

                    if (isFirstParaPoint) {
                        parabolaPath.moveTo(px, clampedPy)
                        isFirstParaPoint = false
                    } else {
                        parabolaPath.lineTo(px, clampedPy)
                    }
                }

                drawPath(
                    path = parabolaPath,
                    color = parabolaColor.copy(alpha = effectiveAlpha),
                    style = Stroke(
                        width = (6.5f * effectiveAlpha).coerceAtLeast(1.5f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                    )
                )
            }

            // 6. Draw Interactive Crosshair Cursor and Precision (x, y) Coordinate HUD
            scrubX?.let { xVal ->
                if (xVal in bounds.xMin.toDouble()..bounds.xMax.toDouble()) {
                    val scrubPx = mapX(xVal)

                    // Determine primary curve intersection for snap / reference
                    val primaryFunc = activeFunctions.firstOrNull { it == HyperbolicFunc.COSH }
                        ?: activeFunctions.firstOrNull()
                    val primaryY = primaryFunc?.evaluate(xVal, paramA, shiftC)
                    val primaryPy = if (primaryY != null && !primaryY.isNaN() && !primaryY.isInfinite()) {
                        mapY(primaryY)
                    } else null

                    // Crosshair Y-coordinate: follows finger when dragging or snaps to primary curve
                    val targetCrosshairY = if (isTouching && touchPoint != null) {
                        touchPoint!!.y.coerceIn(0f, height)
                    } else {
                        primaryPy?.coerceIn(0f, height) ?: (height * 0.5f)
                    }

                    val crosshairDash = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    val crosshairColor = Color(0xFF6366F1).copy(alpha = if (isTouching) 0.90f else 0.70f)

                    // 6a. Vertical Crosshair Guideline
                    drawLine(
                        color = crosshairColor,
                        start = Offset(scrubPx, 0f),
                        end = Offset(scrubPx, height),
                        strokeWidth = if (isTouching) 3.5f else 2.5f,
                        pathEffect = crosshairDash
                    )

                    // 6b. Horizontal Crosshair Guideline
                    drawLine(
                        color = crosshairColor,
                        start = Offset(0f, targetCrosshairY),
                        end = Offset(width, targetCrosshairY),
                        strokeWidth = if (isTouching) 3.5f else 2.5f,
                        pathEffect = crosshairDash
                    )

                    // 6c. Reticle Target at Finger / Cursor Intersection
                    val reticleRadius = if (isTouching) 18f else 14f
                    drawCircle(
                        color = Color(0xFF6366F1).copy(alpha = 0.25f),
                        radius = reticleRadius + 8f,
                        center = Offset(scrubPx, targetCrosshairY)
                    )
                    drawCircle(
                        color = Color(0xFF818CF8),
                        radius = reticleRadius,
                        center = Offset(scrubPx, targetCrosshairY),
                        style = Stroke(width = 2.5f)
                    )
                    // Reticle 4-cardinal ticks
                    val tickLen = 6f
                    drawLine(
                        color = Color(0xFF818CF8),
                        start = Offset(scrubPx, targetCrosshairY - reticleRadius - tickLen),
                        end = Offset(scrubPx, targetCrosshairY - reticleRadius + 2f),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = Color(0xFF818CF8),
                        start = Offset(scrubPx, targetCrosshairY + reticleRadius - 2f),
                        end = Offset(scrubPx, targetCrosshairY + reticleRadius + tickLen),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = Color(0xFF818CF8),
                        start = Offset(scrubPx - reticleRadius - tickLen, targetCrosshairY),
                        end = Offset(scrubPx - reticleRadius + 2f, targetCrosshairY),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = Color(0xFF818CF8),
                        start = Offset(scrubPx + reticleRadius - 2f, targetCrosshairY),
                        end = Offset(scrubPx + reticleRadius + tickLen, targetCrosshairY),
                        strokeWidth = 2.5f
                    )
                    // Center reticle bullseye dot
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(scrubPx, targetCrosshairY)
                    )

                    // 6d. Parabola Intersection Dot with Smooth Morph Transition
                    var morphedProbeY: Double? = null
                    var probeDelta: Double? = null
                    if (effectiveAlpha > 0.05f) {
                        val coshY = paramA * kotlin.math.cosh((xVal - shiftC) / paramA)
                        val targetParaY = when (parabolaMode) {
                            ParabolaMode.STANDARD_X_SQUARED -> xVal * xVal
                            ParabolaMode.TAYLOR_SERIES -> 1.0 + (xVal * xVal) / 2.0
                            ParabolaMode.MATCHED_CATENARY_PARABOLA -> {
                                val dx = xVal - shiftC
                                paramA + (dx * dx) / (2.0 * paramA)
                            }
                        }
                        val calcMorphedY = (1.0 - effectiveBlend) * coshY + effectiveBlend * targetParaY
                        morphedProbeY = calcMorphedY
                        probeDelta = coshY - calcMorphedY
                        val paraPy = mapY(calcMorphedY)

                        if (paraPy in -10f..(height + 10f)) {
                            drawCircle(
                                color = parabolaColor.copy(alpha = 0.35f * effectiveAlpha),
                                radius = 15f * effectiveAlpha,
                                center = Offset(scrubPx, paraPy)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = effectiveAlpha),
                                radius = 9f * effectiveAlpha,
                                center = Offset(scrubPx, paraPy)
                            )
                            drawCircle(
                                color = parabolaColor.copy(alpha = effectiveAlpha),
                                radius = 6.5f * effectiveAlpha,
                                center = Offset(scrubPx, paraPy)
                            )
                        }
                    }

                    // 6e. Intersection Dots on Active Curves
                    val curvePoints = mutableListOf<Pair<HyperbolicFunc, Double>>()
                    for (func in activeFunctions) {
                        val yVal = func.evaluate(xVal, paramA, shiftC)
                        if (yVal != null && !yVal.isNaN() && !yVal.isInfinite()) {
                            curvePoints.add(func to yVal)
                            val dotPy = mapY(yVal)
                            if (dotPy in -10f..(height + 10f)) {
                                drawCircle(
                                    color = func.color.copy(alpha = 0.35f),
                                    radius = 15f,
                                    center = Offset(scrubPx, dotPy)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 9f,
                                    center = Offset(scrubPx, dotPy)
                                )
                                drawCircle(
                                    color = func.color,
                                    radius = 6.5f,
                                    center = Offset(scrubPx, dotPy)
                                )
                            }
                        }
                    }

                    // 6f. Floating Precision Coordinate HUD Badge
                    val displayEntries = mutableListOf<String>()
                    val headerText = "CROSSHAIR (x, y)"
                    displayEntries.add("x = ${String.format(Locale.US, "%+.3f", xVal)}")

                    for ((func, yVal) in curvePoints.take(3)) {
                        displayEntries.add("${func.displayName} = ${String.format(Locale.US, "%+.3f", yVal)}")
                    }
                    if (effectiveAlpha > 0.05f && morphedProbeY != null) {
                        displayEntries.add("para = ${String.format(Locale.US, "%+.3f", morphedProbeY)}")
                        probeDelta?.let { d ->
                            displayEntries.add("Δy = ${if (d >= 0) "+" else ""}${String.format(Locale.US, "%.3f", d)}")
                        }
                    }

                    val hudLineHeight = 30f
                    val hudPaddingX = 20f
                    val hudPaddingY = 16f
                    val hudWidth = 270f
                    val hudHeight = hudPaddingY * 2f + displayEntries.size * hudLineHeight + 24f

                    // Smart HUD auto-positioning to prevent finger occlusion or screen clipping
                    val preferredLeft = scrubPx + 24f
                    val preferredTop = targetCrosshairY - hudHeight - 20f

                    val actualLeft = if (preferredLeft + hudWidth > width - 12f) {
                        (scrubPx - hudWidth - 24f).coerceAtLeast(12f)
                    } else {
                        preferredLeft.coerceAtLeast(12f)
                    }

                    val actualTop = if (preferredTop < 12f) {
                        (targetCrosshairY + 24f).coerceAtMost(height - hudHeight - 12f)
                    } else {
                        preferredTop.coerceAtMost(height - hudHeight - 12f)
                    }

                    val hudRight = actualLeft + hudWidth
                    val hudBottom = actualTop + hudHeight

                    // Render HUD container
                    drawContext.canvas.nativeCanvas.drawRoundRect(
                        actualLeft,
                        actualTop,
                        hudRight,
                        hudBottom,
                        16f,
                        16f,
                        hudBgPaint
                    )
                    drawContext.canvas.nativeCanvas.drawRoundRect(
                        actualLeft,
                        actualTop,
                        hudRight,
                        hudBottom,
                        16f,
                        16f,
                        hudBorderPaint
                    )

                    // Render HUD header
                    drawContext.canvas.nativeCanvas.drawText(
                        headerText,
                        actualLeft + hudPaddingX,
                        actualTop + hudPaddingY + 16f,
                        hudHeaderPaint
                    )

                    // Render HUD coordinate lines
                    var currentTextY = actualTop + hudPaddingY + 44f
                    for ((index, entry) in displayEntries.withIndex()) {
                        val paintToUse = if (entry.startsWith("Δy") || entry.startsWith("para")) {
                            hudSubTextPaint
                        } else {
                            hudTextPaint
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            entry,
                            actualLeft + hudPaddingX,
                            currentTextY,
                            paintToUse
                        )
                        currentTextY += hudLineHeight
                    }
                }
            }
        }
    }
}

/**
 * Draws coordinate axes, subtle grid lines, and aligned numeric labels.
 */
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

/**
 * Computes human-friendly step intervals (1, 2, 5, 10...) based on span.
 */
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

/**
 * Formats numbers neatly for Cartesian axes tick displays.
 */
private fun formatNumber(value: Double): String {
    return if (abs(value) < 1e-5) "0"
    else if (value == floor(value)) value.toInt().toString()
    else String.format(Locale.US, "%.1f", value)
}
