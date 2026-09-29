package com.example.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import com.example.game.ActivePowerUp
import com.example.game.Asteroid
import com.example.game.AsteroidType
import com.example.game.CollectiblePowerUp
import com.example.game.CollectibleStar
import com.example.game.GameEngine
import com.example.game.LaserBeam
import com.example.game.Particle
import com.example.game.PowerUpType
import com.example.game.ShipInfo
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameCanvas(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val normX = offset.x / size.width
                        engine.setShipTarget(normX)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val deltaNormX = dragAmount.x / size.width
                        engine.moveShipBy(deltaNormX)
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height

        // Apply screen shake if active
        val shakeOffset = if (engine.screenShake > 0f) {
            Offset(
                (Random.nextFloat() - 0.5f) * engine.screenShake * 24f,
                (Random.nextFloat() - 0.5f) * engine.screenShake * 24f
            )
        } else {
            Offset.Zero
        }

        // Draw deep space background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF060913),
                    Color(0xFF0C1022),
                    Color(0xFF080B16)
                )
            )
        )

        // Draw Parallax Stars
        for (bgStar in engine.bgStars) {
            val starX = bgStar.x * width + shakeOffset.x
            val starY = bgStar.y * height + shakeOffset.y
            drawCircle(
                color = Color.White.copy(alpha = bgStar.alpha),
                radius = bgStar.size,
                center = Offset(starX, starY)
            )
        }

        // Draw Active Particles (exhaust, star sparkles, explosions)
        for (p in engine.particles) {
            val px = p.x * width + shakeOffset.x
            val py = p.y * height + shakeOffset.y
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(px, py)
            )
        }

        // Draw Lasers
        for (laser in engine.lasers) {
            val lx = laser.x * width + shakeOffset.x
            val ly = laser.y * height + shakeOffset.y
            val laserLen = height * 0.035f

            // Outer glow
            drawLine(
                color = Color(0x6600E5FF),
                start = Offset(lx, ly),
                end = Offset(lx, ly - laserLen),
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )
            // Core beam
            drawLine(
                color = Color.White,
                start = Offset(lx, ly),
                end = Offset(lx, ly - laserLen),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }

        // Draw Collectible Stars & Crystals
        for (star in engine.stars) {
            val sx = star.x * width + shakeOffset.x
            val sy = star.y * height + shakeOffset.y
            val starRadius = star.radius * width
            drawStar(
                center = Offset(sx, sy),
                radius = starRadius,
                isCrystal = star.isCrystal,
                glowPhase = star.glowPhase
            )
        }

        // Draw Collectible Power-Ups
        for (pu in engine.powerUps) {
            val pux = pu.x * width + shakeOffset.x
            val puy = pu.y * height + shakeOffset.y
            val puRadius = pu.radius * width
            drawPowerUpOrb(
                center = Offset(pux, puy),
                radius = puRadius,
                type = pu.type,
                pulse = pu.pulsePhase
            )
        }

        // Draw Asteroids
        for (asteroid in engine.asteroids) {
            val ax = asteroid.x * width + shakeOffset.x
            val ay = asteroid.y * height + shakeOffset.y
            val aRadius = asteroid.radius * width

            drawAsteroid(
                center = Offset(ax, ay),
                radius = aRadius,
                asteroid = asteroid
            )
        }

        // Draw Player Ship (if not game over)
        if (!engine.isGameOver) {
            val shipPixelX = engine.shipX * width + shakeOffset.x
            val shipPixelY = engine.shipY * height + shakeOffset.y
            val shipRadius = width * 0.065f

            val isFlashing = engine.isInvulnerable && ((engine.invulnerableTimer * 20).toInt() % 2 == 0)
            val shipAlpha = if (isFlashing) 0.35f else 1.0f

            rotate(
                degrees = engine.shipTilt,
                pivot = Offset(shipPixelX, shipPixelY)
            ) {
                drawShip(
                    center = Offset(shipPixelX, shipPixelY),
                    radius = shipRadius,
                    ship = engine.activeShip,
                    alpha = shipAlpha
                )
            }

            // Draw Active Shield Force-field Bubble
            val hasShield = engine.shields > 0 || engine.activePowerUps.any { it.type == PowerUpType.SHIELD }
            if (hasShield) {
                val shieldRadius = shipRadius * 1.5f
                drawCircle(
                    color = Color(0x3300E5FF),
                    radius = shieldRadius,
                    center = Offset(shipPixelX, shipPixelY)
                )
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = shieldRadius,
                    center = Offset(shipPixelX, shipPixelY),
                    style = Stroke(width = 3.5f)
                )
            }

            // Draw Magnet Aura if active
            val hasMagnet = engine.activePowerUps.any { it.type == PowerUpType.MAGNET }
            if (hasMagnet) {
                val magnetRadius = width * 0.35f
                drawCircle(
                    color = Color(0x22FFD700),
                    radius = magnetRadius,
                    center = Offset(shipPixelX, shipPixelY),
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

private fun DrawScope.drawShip(
    center: Offset,
    radius: Float,
    ship: ShipInfo,
    alpha: Float
) {
    val cx = center.x
    val cy = center.y

    // Main fuselage path
    val shipPath = Path().apply {
        // Nose cone
        moveTo(cx, cy - radius * 1.25f)
        // Right wing tip
        lineTo(cx + radius * 0.95f, cy + radius * 0.75f)
        // Right rear engine bay
        lineTo(cx + radius * 0.35f, cy + radius * 0.60f)
        // Engine center notch
        lineTo(cx, cy + radius * 0.45f)
        // Left rear engine bay
        lineTo(cx - radius * 0.35f, cy + radius * 0.60f)
        // Left wing tip
        lineTo(cx - radius * 0.95f, cy + radius * 0.75f)
        close()
    }

    // Ship Hull Fill
    drawPath(
        path = shipPath,
        color = ship.primaryColor.copy(alpha = alpha),
        style = Fill
    )

    // Ship Hull Outline
    drawPath(
        path = shipPath,
        color = Color.White.copy(alpha = alpha * 0.9f),
        style = Stroke(width = 2.5f)
    )

    // Cockpit canopy (Glass cyan glow)
    val canopyPath = Path().apply {
        moveTo(cx, cy - radius * 0.75f)
        lineTo(cx + radius * 0.22f, cy - radius * 0.05f)
        lineTo(cx, cy + radius * 0.18f)
        lineTo(cx - radius * 0.22f, cy - radius * 0.05f)
        close()
    }
    drawPath(
        path = canopyPath,
        color = Color(0xFFE0F7FA).copy(alpha = alpha * 0.95f),
        style = Fill
    )

    // Wing accent stripes
    drawLine(
        color = ship.accentColor.copy(alpha = alpha),
        start = Offset(cx + radius * 0.28f, cy - radius * 0.1f),
        end = Offset(cx + radius * 0.85f, cy + radius * 0.55f),
        strokeWidth = 3f
    )
    drawLine(
        color = ship.accentColor.copy(alpha = alpha),
        start = Offset(cx - radius * 0.28f, cy - radius * 0.1f),
        end = Offset(cx - radius * 0.85f, cy + radius * 0.55f),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawAsteroid(
    center: Offset,
    radius: Float,
    asteroid: Asteroid
) {
    val cx = center.x
    val cy = center.y

    // If comet, draw trailing tail
    if (asteroid.type == AsteroidType.COMET) {
        val tailLen = radius * 3f
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x0000E5FF), Color(0xAA00E5FF)),
                startY = cy - tailLen,
                endY = cy
            ),
            start = Offset(cx, cy - tailLen),
            end = Offset(cx, cy),
            strokeWidth = radius * 1.6f,
            cap = StrokeCap.Round
        )
    }

    // Outer glow for fast/comet
    if (asteroid.type == AsteroidType.FAST) {
        drawCircle(
            color = Color(0x44FF3366),
            radius = radius * 1.35f,
            center = center
        )
    }

    // Irregular rocky shape using rotation
    rotate(degrees = asteroid.rotation, pivot = center) {
        val numPoints = 8
        val rockPath = Path()
        for (i in 0 until numPoints) {
            val angle = (i.toFloat() / numPoints) * 2f * PI.toFloat()
            // Add deterministic irregularity based on asteroid ID and point index
            val irregularity = 0.85f + (((asteroid.id * 17 + i * 31) % 25).toFloat() / 100f)
            val r = radius * irregularity
            val px = cx + cos(angle) * r
            val py = cy + sin(angle) * r
            if (i == 0) rockPath.moveTo(px, py) else rockPath.lineTo(px, py)
        }
        rockPath.close()

        // Fill rock
        drawPath(
            path = rockPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    asteroid.color.copy(alpha = 1f),
                    asteroid.color.copy(alpha = 0.7f),
                    Color(0xFF1E2235)
                ),
                center = Offset(cx - radius * 0.25f, cy - radius * 0.25f),
                radius = radius
            )
        )

        // Outline rock
        drawPath(
            path = rockPath,
            color = Color(0x66FFFFFF),
            style = Stroke(width = 2.5f)
        )

        // Draw small craters
        drawCircle(
            color = Color(0x44000000),
            radius = radius * 0.22f,
            center = Offset(cx - radius * 0.25f, cy - radius * 0.15f)
        )
        drawCircle(
            color = Color(0x33000000),
            radius = radius * 0.18f,
            center = Offset(cx + radius * 0.22f, cy + radius * 0.2f)
        )
    }
}

private fun DrawScope.drawStar(
    center: Offset,
    radius: Float,
    isCrystal: Boolean,
    glowPhase: Float
) {
    val cx = center.x
    val cy = center.y
    val pulse = 1f + sin(glowPhase) * 0.15f
    val r = radius * pulse

    val coreColor = if (isCrystal) Color(0xFFC084FC) else Color(0xFFFFD700)
    val glowColor = if (isCrystal) Color(0x66C084FC) else Color(0x66FFD700)

    // Halo
    drawCircle(
        color = glowColor,
        radius = r * 1.8f,
        center = center
    )

    if (isCrystal) {
        // Faceted diamond shape
        val diamondPath = Path().apply {
            moveTo(cx, cy - r * 1.3f)
            lineTo(cx + r * 1.1f, cy)
            lineTo(cx, cy + r * 1.3f)
            lineTo(cx - r * 1.1f, cy)
            close()
        }
        drawPath(path = diamondPath, color = coreColor, style = Fill)
        drawPath(path = diamondPath, color = Color.White, style = Stroke(width = 2f))
    } else {
        // 4-pointed radiant star
        val starPath = Path().apply {
            val innerR = r * 0.42f
            for (i in 0 until 8) {
                val currentR = if (i % 2 == 0) r else innerR
                val angle = (i.toFloat() / 8f) * 2f * PI.toFloat() - (PI.toFloat() / 2f)
                val px = cx + cos(angle) * currentR
                val py = cy + sin(angle) * currentR
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(path = starPath, color = coreColor, style = Fill)
        drawPath(path = starPath, color = Color.White, style = Stroke(width = 1.5f))
    }
}

private fun DrawScope.drawPowerUpOrb(
    center: Offset,
    radius: Float,
    type: PowerUpType,
    pulse: Float
) {
    val cx = center.x
    val cy = center.y
    val pulseScale = 1f + sin(pulse) * 0.18f
    val r = radius * pulseScale

    // Pulsing outer ring
    drawCircle(
        color = type.color.copy(alpha = 0.35f),
        radius = r * 1.7f,
        center = center
    )
    drawCircle(
        color = type.color,
        radius = r * 1.2f,
        center = center,
        style = Stroke(width = 3f)
    )
    drawCircle(
        color = type.color.copy(alpha = 0.85f),
        radius = r * 0.85f,
        center = center
    )

    // Inner icon / letter representation
    // Inner white core
    drawCircle(
        color = Color.White,
        radius = r * 0.45f,
        center = center
    )
}
