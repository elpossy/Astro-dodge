package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.AppScreen
import com.example.game.GameViewModel
import com.example.ui.achievements.AchievementsScreen
import com.example.ui.game.GameScreen
import com.example.ui.hangar.HangarScreen
import com.example.ui.menu.MainMenuScreen
import com.example.ui.scores.LeaderboardScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.CosmicBlack
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CosmicBlack
                ) {
                    val viewModel: GameViewModel = viewModel()
                    AstroDodgeApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AstroDodgeApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            AppScreen.MENU -> MainMenuScreen(viewModel = viewModel)
            AppScreen.GAME -> GameScreen(viewModel = viewModel)
            AppScreen.HANGAR -> HangarScreen(viewModel = viewModel)
            AppScreen.LEADERBOARD -> LeaderboardScreen(viewModel = viewModel)
            AppScreen.ACHIEVEMENTS -> AchievementsScreen(viewModel = viewModel)
            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
        }
    }
}
