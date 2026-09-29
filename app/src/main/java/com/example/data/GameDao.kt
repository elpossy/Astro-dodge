package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_scores ORDER BY score DESC, timestamp DESC LIMIT :limit")
    fun getTopScores(limit: Int = 20): Flow<List<GameScore>>

    @Query("SELECT MAX(score) FROM game_scores")
    fun getBestScore(): Flow<Int?>

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: GameScore): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: UserStats)

    @Query("DELETE FROM game_scores")
    suspend fun clearAllScores()
}
