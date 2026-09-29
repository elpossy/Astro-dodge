package com.example.ui.achievements

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UserStats
import com.example.game.AppScreen
import com.example.game.GameViewModel
import com.example.ui.theme.CosmicBlack
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicCardBorder
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.min

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val current: Int,
    val target: Int,
    val color: Color
) {
    val isUnlocked: Boolean get() = current >= target
    val progress: Float get() = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
}

@Composable
fun AchievementsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.userStats.collectAsStateWithLifecycle()

    BackHandler {
        viewModel.navigateTo(AppScreen.MENU)
    }

    val unlockedShipsCount = stats.unlockedShips.split(",").size

    val achievements = listOf(
        AchievementItem("first_flight", "Premier Vol", "Effectuer votre premier vol spatial", min(stats.totalGamesPlayed, 1), 1, NeonCyan),
        AchievementItem("score_300", "Pilote Émérite", "Atteindre un score de 300 points", stats.bestScore, 300, NeonGold),
        AchievementItem("score_1000", "As Stellaire", "Atteindre un score de 1000 points", stats.bestScore, 1000, NeonPurple),
        AchievementItem("score_2500", "Légende Cosmique", "Atteindre un score exceptionnel de 2500 points", stats.bestScore, 2500, NeonGreen),
        AchievementItem("stars_50", "Chasseur d'Étoiles", "Récolter 50 étoiles cosmiques", stats.totalStars, 50, NeonGold),
        AchievementItem("stars_200", "Magnat Cosmique", "Récolter 200 étoiles cosmiques", stats.totalStars, 200, NeonGold),
        AchievementItem("wave_4", "Survivant Spatial", "Atteindre la Vague 4 en une seule partie", stats.highestWave, 4, NeonPurple),
        AchievementItem("dodge_50", "Maître de l'Esquive", "Esquiver 50 astéroïdes avec succès", stats.totalAsteroidsDodged, 50, NeonCyan),
        AchievementItem("dodge_200", "Centurion Stellaire", "Esquiver 200 astéroïdes au total", stats.totalAsteroidsDodged, 200, NeonCyan),
        AchievementItem("fleet_2", "Commandant de Flotte", "Débloquer au moins 2 vaisseaux dans le Hangar", unlockedShipsCount, 2, NeonGreen)
    )

    val unlockedCount = achievements.count { it.isUnlocked }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.MENU) },
                    modifier = Modifier.testTag("achievements_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Trophées",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                color = CosmicSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
            ) {
                Text(
                    text = "$unlockedCount / ${achievements.size}",
                    color = NeonGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Achievements List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(achievements) { item ->
                AchievementCard(item = item)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AchievementCard(item: AchievementItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isUnlocked) CosmicSurface else CosmicDark
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (item.isUnlocked) 1.5.dp else 1.dp,
            color = if (item.isUnlocked) item.color.copy(alpha = 0.6f) else CosmicCardBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = if (item.isUnlocked) item.color.copy(alpha = 0.2f) else CosmicCard,
                        shape = CircleShape,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (item.isUnlocked) Icons.Default.MilitaryTech else Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = if (item.isUnlocked) item.color else TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.description,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                if (item.isUnlocked) {
                    Surface(
                        color = item.color,
                        shape = CircleShape,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Validé",
                                tint = CosmicBlack,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = item.color,
                    trackColor = CosmicCardBorder
                )
                Text(
                    text = "${item.current} / ${item.target}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
