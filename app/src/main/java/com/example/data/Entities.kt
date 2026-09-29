package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class GameScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Int,
    val starsCollected: Int,
    val wave: Int,
    val durationSeconds: Int,
    val shipId: String,
    val difficulty: String = "NORMAL",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey
    val id: Int = 1,
    val totalStars: Int = 0,
    val totalGamesPlayed: Int = 0,
    val bestScore: Int = 0,
    val highestWave: Int = 1,
    val totalAsteroidsDodged: Int = 0,
    val unlockedShips: String = "scout", // Comma-separated ship IDs
    val selectedShipId: String = "scout",
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val difficulty: String = "NORMAL" // EASY, NORMAL, HARD
)
