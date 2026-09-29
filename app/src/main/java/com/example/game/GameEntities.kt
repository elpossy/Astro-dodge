package com.example.game

import androidx.compose.ui.graphics.Color

enum class AsteroidType {
    NORMAL,
    FAST,
    GIANT,
    COMET
}

enum class PowerUpType(val title: String, val durationSec: Float, val color: Color) {
    SHIELD("Bouclier", 8f, Color(0xFF00E5FF)),
    MAGNET("Aimant", 9f, Color(0xFFFFD700)),
    LASER("Tir Laser", 7f, Color(0xFFFF3366)),
    SLOWMO("Ralenti", 6f, Color(0xFFC084FC))
}

data class ActivePowerUp(
    val type: PowerUpType,
    val timeRemainingSec: Float,
    val totalDurationSec: Float
)

data class Asteroid(
    val id: Long,
    var x: Float,
    var y: Float,
    val radius: Float,
    val speedX: Float,
    var speedY: Float,
    var rotation: Float = 0f,
    val rotationSpeed: Float,
    val type: AsteroidType,
    val color: Color,
    val points: Int
)

data class CollectibleStar(
    val id: Long,
    var x: Float,
    var y: Float,
    val radius: Float,
    val speedY: Float,
    val isCrystal: Boolean,
    var glowPhase: Float = 0f
)

data class CollectiblePowerUp(
    val id: Long,
    var x: Float,
    var y: Float,
    val radius: Float,
    val speedY: Float,
    val type: PowerUpType,
    var pulsePhase: Float = 0f
)

data class LaserBeam(
    val id: Long,
    var x: Float,
    var y: Float,
    val speedY: Float
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var size: Float,
    var alpha: Float,
    var life: Float,
    val maxLife: Float
)

data class BackgroundStar(
    val x: Float,
    var y: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float
)
