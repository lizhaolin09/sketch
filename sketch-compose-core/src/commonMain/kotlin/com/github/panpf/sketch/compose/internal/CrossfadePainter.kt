/*
 * Copyright 2023 Coil Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.github.panpf.sketch.compose.internal

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.layout.times
import com.github.panpf.sketch.decode.internal.computeSizeMultiplier
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.TimeSource

/**
 * A [Painter] that crossfades from [start] to [end].
 *
 * NOTE: The animation can only be executed once as the [start] drawable is dereferenced at
 * the end of the transition.
 */
@Stable
internal class CrossfadePainter(
    var start: Painter?,
    val end: Painter?,
    private val fitScale: Boolean = true,
    private val durationMillis: Int,
    private val fadeStart: Boolean,
    private val preferExactIntrinsicSize: Boolean,
) : Painter() {

    private var invalidateTick by mutableIntStateOf(0)
    private var startTime: TimeSource.Monotonic.ValueTimeMark? = null
    private var isDone = false

    private var maxAlpha: Float by mutableFloatStateOf(DefaultAlpha)
    private var colorFilter: ColorFilter? by mutableStateOf(null)

    override val intrinsicSize get() = computeIntrinsicSize()

    override fun DrawScope.onDraw() {
        if (isDone) {
            drawPainter(end, maxAlpha)
            return
        }

        // Initialize startTime the first time we're drawn.
        val startTime = startTime ?: TimeSource.Monotonic.markNow().also { startTime = it }
        val percent = startTime.elapsedNow().inWholeMilliseconds / durationMillis.toFloat()
        val endAlpha = percent.coerceIn(0f, 1f) * maxAlpha
        val startAlpha = if (fadeStart) maxAlpha - endAlpha else maxAlpha
        isDone = percent >= 1f

        drawPainter(start, startAlpha)
        drawPainter(end, endAlpha)

        if (isDone) {
            start = null
        } else {
            // Increment this value to force the painter to be redrawn.
            invalidateTick++
        }
    }

    override fun applyAlpha(alpha: Float): Boolean {
        this.maxAlpha = alpha
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        this.colorFilter = colorFilter
        return true
    }

    private fun computeIntrinsicSize(): Size {
        if (start == null && end == null) return Size.Unspecified
        val startSize = start?.intrinsicSize ?: Size.Zero
        val endSize = end?.intrinsicSize ?: Size.Zero

        val isStartSpecified = startSize.isSpecified
        val isEndSpecified = endSize.isSpecified
        if (isStartSpecified && isEndSpecified) {
            return Size(
                width = max(startSize.width, endSize.width),
                height = max(startSize.height, endSize.height),
            )
        }
        if (preferExactIntrinsicSize) {
            if (isStartSpecified) return startSize
            if (isEndSpecified) return endSize
        }
        return Size.Unspecified
    }

    private fun DrawScope.drawPainter(painter: Painter?, alpha: Float) {
        if (painter == null || alpha <= 0) return

        with(painter) {
            val drawSize = this@drawPainter.size
            val painterScaledSize = computeScaledSize(this@with.intrinsicSize, drawSize)
            if (drawSize.isUnspecified || drawSize.isEmpty()) {
                draw(painterScaledSize, alpha, colorFilter)
            } else {
                inset(
                    horizontal = (drawSize.width - painterScaledSize.width) / 2,
                    vertical = (drawSize.height - painterScaledSize.height) / 2
                ) {
                    draw(painterScaledSize, alpha, colorFilter)
                }
            }
        }
    }

    private fun computeScaledSize(srcSize: Size, dstSize: Size): Size {
        if (srcSize.isUnspecified || srcSize.isEmpty()) return dstSize
        if (dstSize.isUnspecified || dstSize.isEmpty()) return dstSize
        val sizeMultiplier = computeSizeMultiplier(
            srcWidth = srcSize.width.roundToInt(),
            srcHeight = srcSize.height.roundToInt(),
            dstWidth = dstSize.width.roundToInt(),
            dstHeight = dstSize.height.roundToInt(),
            fitScale = fitScale
        )
        return srcSize * ScaleFactor(sizeMultiplier.toFloat(), sizeMultiplier.toFloat())
    }
}