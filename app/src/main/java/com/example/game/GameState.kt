package com.example.game

data class GameUiState(
    val score: Int = 0,
    val bestScore: Int = 0,
    val wave: Int = 1,
    val starsInRun: Int = 0,
    val combo: Int = 1,
    val comboProgress: Float = 0f, // 1.0f down to 0f
    val shields: Int = 1,
    val maxShields: Int = 1,
    val activePowerUps: List<ActivePowerUp> = emptyList(),
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val newRecordBeaten: Boolean = false,
    val runDurationSec: Int = 0,
    val asteroidsDodged: Int = 0,
    val waveAnnouncement: String? = null,
    val screenShake: Float = 0f
)
