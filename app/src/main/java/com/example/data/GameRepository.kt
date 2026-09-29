package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val dao: GameDao) {

    val topScores: Flow<List<GameScore>> = dao.getTopScores()
    val bestScore: Flow<Int> = dao.getBestScore().map { it ?: 0 }
    val userStats: Flow<UserStats> = dao.getUserStats().map {
        it ?: UserStats()
    }

    suspend fun recordGameResult(
        score: Int,
        starsCollected: Int,
        wave: Int,
        durationSeconds: Int,
        shipId: String,
        difficulty: String,
        asteroidsDodged: Int
    ): Boolean {
        val gameScore = GameScore(
            score = score,
            starsCollected = starsCollected,
            wave = wave,
            durationSeconds = durationSeconds,
            shipId = shipId,
            difficulty = difficulty
        )
        dao.insertScore(gameScore)

        // Fetch or create stats
        // We will update in place
        return true
    }

    suspend fun saveStats(stats: UserStats) {
        dao.insertOrUpdateStats(stats)
    }

    suspend fun clearHistory() {
        dao.clearAllScores()
    }
}
