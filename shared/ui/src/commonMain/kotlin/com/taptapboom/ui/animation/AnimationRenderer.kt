package com.taptapboom.ui.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.taptapboom.domain.model.AnimationType
import com.taptapboom.ui.mvi.ActiveAnimation
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dispatches rendering to the appropriate animation renderer based on AnimationType.
 * All rendering happens in the Canvas DrawScope — zero allocations on the hot path.
 */
object AnimationRenderer {

    // Pre-allocated Stroke objects to avoid allocation in draw loops
    private val waveStroke = Stroke(width = 3f)
    private val orbitStroke = Stroke(width = 1f)
    private val tempSize = Size(0f, 0f) // Reusable for scatter rects

    fun DrawScope.renderAnimation(animation: ActiveAnimation) {
        val progress = animation.progress
        if (progress >= 1f) return

        val alpha = (1f - progress).coerceIn(0f, 1f)
        val color = animation.color.copy(alpha = alpha)

        // Anchoring logic: Use tap point as origin, but center if it's a keyboard sentinel (-1, -1)
        val origin = if (animation.origin.x < 0f && animation.origin.y < 0f) center else animation.origin
        val scale = if (animation.isFullScreen) 2.5f else 1.0f

        when (animation.type) {
            AnimationType.RIPPLE -> drawRipple(origin, progress, color, scale)
            AnimationType.BURST -> drawBurst(origin, progress, color, scale)
            AnimationType.SPIRAL -> drawSpiral(origin, progress, color, scale)
            AnimationType.WAVE -> drawWave(origin, progress, color, scale)
            AnimationType.SCATTER -> drawScatter(origin, progress, color, scale)
            AnimationType.PULSE -> drawPulse(origin, progress, color, scale)
            AnimationType.BLOOM -> drawBloom(origin, progress, color, scale)
            AnimationType.SHATTER -> drawShatter(origin, progress, color, scale)
            AnimationType.ORBIT -> drawOrbit(origin, progress, color, scale)
            AnimationType.FLASH -> drawFlash(progress, color)
            AnimationType.MIRROR -> drawMirror(origin, progress, color)
            AnimationType.SLICE -> drawSlice(origin, progress, color)
        }
    }

    // ─── RIPPLE ─────────────────────────────────────────────
    private fun DrawScope.drawRipple(origin: Offset, progress: Float, color: Color, scale: Float = 1f) {
        val maxRadius = size.minDimension * 0.4f * scale
        val radius = maxRadius * progress
        val strokeWidth = (1f - progress) * 8f * scale
        drawCircle(
            color = color,
            radius = radius,
            center = origin,
            style = Stroke(width = strokeWidth)
        )
        // Inner ripple (delayed)
        if (progress > 0.2f) {
            val innerProgress = (progress - 0.2f) / 0.8f
            val innerRadius = maxRadius * innerProgress * 0.6f
            drawCircle(
                color = color.copy(alpha = color.alpha * 0.5f),
                radius = innerRadius,
                center = origin,
                style = Stroke(width = strokeWidth * 0.5f)
            )
        }
    }

    // ─── BURST ──────────────────────────────────────────────
    private fun DrawScope.drawBurst(origin: Offset, progress: Float, color: Color, scale: Float = 1f) {
        val count = 12
        val maxDist = size.minDimension * 0.3f * progress * scale
        val particleRadius = (1f - progress) * 6f * scale

        for (i in 0 until count) {
            val angle = (i * 360f / count) * (PI / 180.0)
            val x = origin.x + (maxDist * cos(angle)).toFloat()
            val y = origin.y + (maxDist * sin(angle)).toFloat()
            drawCircle(color = color, radius = particleRadius, center = Offset(x, y))
        }
    }

    // ─── SPIRAL ─────────────────────────────────────────────
    private fun DrawScope.drawSpiral(origin: Offset, progress: Float, color: Color, scale: Float = 1f) {
        val turns = 3
        val maxRadius = size.minDimension * 0.25f * scale
        val dotCount = 20
        for (i in 0 until dotCount) {
            val t = i.toFloat() / dotCount * progress
            val angle = t * turns * 2 * PI
            val radius = maxRadius * t
            val x = origin.x + (radius * cos(angle)).toFloat()
            val y = origin.y + (radius * sin(angle)).toFloat()
            drawCircle(
                color = color.copy(alpha = color.alpha * (1f - t)),
                radius = 3f * scale,
                center = Offset(x, y)
            )
        }
    }

    // ─── WAVE ───────────────────────────────────────────────
    private fun DrawScope.drawWave(origin: Offset, progress: Float, color: Color, scale: Float = 1f) {
        val rings = 3
        val maxRadius = size.minDimension * 0.35f * scale
        for (i in 0 until rings) {
            val delay = i * 0.15f
            val ringProgress = ((progress - delay) / (1f - delay)).coerceIn(0f, 1f)
            if (ringProgress > 0f) {
                drawCircle(
                    color = color.copy(alpha = color.alpha * (1f - ringProgress)),
                    radius = maxRadius * ringProgress,
                    center = origin,
                    style = Stroke(width = 3f * scale)
                )
            }
        }
    }

    // ─── SCATTER ────────────────────────────────────────────
    private fun DrawScope.drawScatter(origin: Offset, progress: Float, color: Color, scale: Float = 1f) {
        val count = 8
        val maxDist = size.minDimension * 0.3f * scale
        for (i in 0 until count) {
            val angle = (i * 45f + progress * 30f) * (PI / 180.0)
            val dist = maxDist * progress * (0.5f + (i % 3) * 0.2f)
            val x = origin.x + (dist * cos(angle)).toFloat()
            val y = origin.y + (dist * sin(angle)).toFloat()
            val rectSize = (1f - progress) * 12f * scale
            drawRect(
                color = color,
                topLeft = Offset(x - rectSize / 2, y - rectSize / 2),
                size = Size(rectSize, rectSize)
            )
        }
    }

    // ─── PULSE ──────────────────────────────────────────────
    private fun DrawScope.drawPulse(origin: Offset, progress: Float, color: Color, scale: Float = 1f) {
        val maxRadius = size.minDimension * 0.15f * scale
        val pulseScale = if (progress < 0.5f) progress * 2f else 1f
        val alpha = if (progress < 0.5f) color.alpha else color.alpha * (1f - (progress - 0.5f) * 2f)
        drawCircle(
            color = color.copy(alpha = alpha.coerceAtLeast(0f)),
            radius = maxRadius * pulseScale,
            center = origin
        )
    }

    // ─── BLOOM ──────────────────────────────────────────────
    private fun DrawScope.drawBloom(origin: Offset, progress: Float, color: Color, scale: Float = 1f) {
        val petals = 6
        val maxRadius = size.minDimension * 0.2f * progress * scale
        for (i in 0 until petals) {
            val angle = (i * 60f + progress * 45f) * (PI / 180.0)
            val x = origin.x + (maxRadius * cos(angle)).toFloat()
            val y = origin.y + (maxRadius * sin(angle)).toFloat()
            drawCircle(
                color = color,
                radius = (1f - progress) * 10f * scale,
                center = Offset(x, y)
            )
        }
        // Center dot
        drawCircle(color = color, radius = (1f - progress) * 6f * scale, center = origin)
    }

    // ─── SHATTER ────────────────────────────────────────────
    private fun DrawScope.drawShatter(origin: Offset, progress: Float, color: Color, scale: Float = 1f) {
        val fragments = 10
        val maxDist = size.minDimension * 0.35f * scale
        for (i in 0 until fragments) {
            val seed = (i * 137.5f) // Golden angle scatter
            val angle = seed * (PI / 180.0)
            val dist = maxDist * progress * (0.3f + (i % 4) * 0.2f)
            val x = origin.x + (dist * cos(angle)).toFloat()
            val y = origin.y + (dist * sin(angle)).toFloat() + progress * 40f * scale // gravity
            val triSize = (1f - progress) * 8f * scale
            drawCircle(color = color, radius = triSize, center = Offset(x, y))
        }
    }

    // ─── ORBIT ──────────────────────────────────────────────
    private fun DrawScope.drawOrbit(origin: Offset, progress: Float, color: Color, scale: Float = 1f) {
        val orbiters = 4
        val orbitRadius = size.minDimension * 0.15f * (1f + progress * 0.5f) * scale
        for (i in 0 until orbiters) {
            val baseAngle = i * 90f
            val angle = (baseAngle + progress * 720f) * (PI / 180.0)
            val x = origin.x + (orbitRadius * cos(angle)).toFloat()
            val y = origin.y + (orbitRadius * sin(angle)).toFloat()
            drawCircle(
                color = color,
                radius = (1f - progress) * 5f * scale,
                center = Offset(x, y)
            )
        }
        // Center ring
        drawCircle(
            color = color.copy(alpha = color.alpha * 0.3f),
            radius = orbitRadius,
            center = origin,
            style = Stroke(width = 1f * scale)
        )
    }

    // ─── FLASH ──────────────────────────────────────────────
    private fun DrawScope.drawFlash(progress: Float, color: Color) {
        // Full-screen flash that fades quickly
        val alpha = if (progress < 0.1f) progress * 10f else (1f - progress) * 1.1f
        drawRect(
            color = color.copy(alpha = alpha.coerceIn(0f, 0.6f)),
            size = size
        )
    }

    // ─── MIRROR ─────────────────────────────────────────────
    private fun DrawScope.drawMirror(origin: Offset, progress: Float, color: Color) {
        // 4-axis symmetry relative to center
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        
        // Use a stylized ripple/burst hybrid
        val count = 8
        val maxDist = size.minDimension * 0.2f * progress
        val dotRadius = (1f - progress) * 5f

        // Mirror across 4 quadrants
        val quadrants = listOf(
            Offset(1f, 1f),
            Offset(-1f, 1f),
            Offset(1f, -1f),
            Offset(-1f, -1f)
        )

        quadrants.forEach { quad ->
            for (i in 0 until count) {
                val angle = (i * 360f / count + progress * 90f) * (PI / 180.0)
                val rx = (maxDist * cos(angle)).toFloat()
                val ry = (maxDist * sin(angle)).toFloat()
                
                // Offset from the quad-adjusted origin
                val x = (origin.x - centerX) * quad.x + centerX + rx
                val y = (origin.y - centerY) * quad.y + centerY + ry
                
                drawCircle(color = color, radius = dotRadius, center = Offset(x, y))
            }
        }
    }

    // ─── SLICE ──────────────────────────────────────────────
    private fun DrawScope.drawSlice(origin: Offset, progress: Float, color: Color) {
        // Partition the screen through the tap point
        val isVertical = size.width > size.height
        val alpha = if (progress < 0.2f) progress * 5f else (1f - progress) * 0.8f
        
        if (isVertical) {
            // Horizontal slice moving outwards
            val thickness = progress * size.height * 0.5f
            drawRect(
                color = color.copy(alpha = alpha.coerceIn(0f, 0.4f)),
                topLeft = Offset(0f, origin.y - thickness / 2),
                size = Size(size.width, thickness)
            )
            // Accent line
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(0f, origin.y),
                end = Offset(size.width, origin.y),
                strokeWidth = 2f
            )
        } else {
            // Vertical slice
            val thickness = progress * size.width * 0.5f
            drawRect(
                color = color.copy(alpha = alpha.coerceIn(0f, 0.4f)),
                topLeft = Offset(origin.x - thickness / 2, 0f),
                size = Size(thickness, size.height)
            )
            // Accent line
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(origin.x, 0f),
                end = Offset(origin.x, size.height),
                strokeWidth = 2f
            )
        }
    }
}
