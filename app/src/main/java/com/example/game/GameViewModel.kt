package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.data.GameScore
import com.example.data.UserStats
import com.example.util.SoundManager
import com.example.util.VibrationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max

enum class AppScreen {
    MENU,
    GAME,
    HANGAR,
    LEADERBOARD,
    ACHIEVEMENTS,
    SETTINGS
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = GameRepository(database.gameDao())

    val soundManager = SoundManager()
    val vibrationHelper = VibrationHelper(application)

    val topScores: StateFlow<List<GameScore>> = repository.topScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bestScore: StateFlow<Int> = repository.bestScore
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userStats: StateFlow<UserStats> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats())

    private val _currentScreen = MutableStateFlow(AppScreen.MENU)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _gameUiState = MutableStateFlow(GameUiState())
    val gameUiState: StateFlow<GameUiState> = _gameUiState.asStateFlow()

    val engine = GameEngine(
        soundManager = soundManager,
        vibrationHelper = vibrationHelper,
        onGameOver = { score, stars, wave, duration, dodged ->
            handleGameOver(score, stars, wave, duration, dodged)
        }
    )

    init {
        viewModelScope.launch {
            userStats.collect { stats ->
                soundManager.soundEnabled = stats.soundEnabled
                vibrationHelper.vibrateEnabled = stats.vibrateEnabled
                engine.currentStats = stats
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun startGame() {
        val currentShip = ShipCatalog.getShip(userStats.value.selectedShipId)
        val highscore = bestScore.value
        engine.startNewGame(currentShip, userStats.value, highscore)
        _gameUiState.value = engine.getUiState()
        _currentScreen.value = AppScreen.GAME
    }

    fun updateGameFrame(dtSec: Float) {
        engine.update(dtSec)
        _gameUiState.value = engine.getUiState()
    }

    fun pauseGame() {
        if (!engine.isGameOver && !engine.isPaused) {
            engine.togglePause()
            _gameUiState.value = engine.getUiState()
        }
    }

    fun resumeGame() {
        if (engine.isPaused) {
            engine.togglePause()
            _gameUiState.value = engine.getUiState()
        }
    }

    fun restartGame() {
        startGame()
    }

    fun returnToMenu() {
        _currentScreen.value = AppScreen.MENU
    }

    private fun handleGameOver(score: Int, stars: Int, wave: Int, duration: Int, dodged: Int) {
        viewModelScope.launch {
            val stats = userStats.value
            val newTotalStars = stats.totalStars + stars
            val newTotalGames = stats.totalGamesPlayed + 1
            val newBestScore = max(stats.bestScore, score)
            val newHighestWave = max(stats.highestWave, wave)
            val newDodged = stats.totalAsteroidsDodged + dodged

            val updatedStats = stats.copy(
                totalStars = newTotalStars,
                totalGamesPlayed = newTotalGames,
                bestScore = newBestScore,
                highestWave = newHighestWave,
                totalAsteroidsDodged = newDodged
            )
            repository.saveStats(updatedStats)

            repository.recordGameResult(
                score = score,
                starsCollected = stars,
                wave = wave,
                durationSeconds = duration,
                shipId = stats.selectedShipId,
                difficulty = stats.difficulty,
                asteroidsDodged = dodged
            )
        }
    }

    fun selectShip(shipId: String) {
        val stats = userStats.value
        if (stats.unlockedShips.split(",").contains(shipId)) {
            viewModelScope.launch {
                repository.saveStats(stats.copy(selectedShipId = shipId))
            }
        }
    }

    fun unlockShip(ship: ShipInfo): Boolean {
        val stats = userStats.value
        if (stats.totalStars >= ship.priceStars) {
            val unlockedList = stats.unlockedShips.split(",").toMutableList()
            if (!unlockedList.contains(ship.id)) {
                unlockedList.add(ship.id)
                viewModelScope.launch {
                    val updated = stats.copy(
                        totalStars = stats.totalStars - ship.priceStars,
                        unlockedShips = unlockedList.joinToString(","),
                        selectedShipId = ship.id
                    )
                    repository.saveStats(updated)
                }
                soundManager.playPowerUpSound()
                vibrationHelper.powerUp()
                return true
            }
        }
        return false
    }

    fun updateSettings(sound: Boolean, vibrate: Boolean, difficulty: String) {
        viewModelScope.launch {
            val updated = userStats.value.copy(
                soundEnabled = sound,
                vibrateEnabled = vibrate,
                difficulty = difficulty
            )
            repository.saveStats(updated)
        }
    }

    fun clearScoreHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
