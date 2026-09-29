package com.example.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
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
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.isActive

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.gameUiState.collectAsStateWithLifecycle()

    BackHandler {
        if (!uiState.isGameOver) {
            viewModel.pauseGame()
        } else {
            viewModel.returnToMenu()
        }
    }

    // High frequency game update frame loop
    var lastNanoTime by remember { mutableLongStateOf(0L) }
    LaunchedEffect(uiState.isGameOver, uiState.isPaused) {
        lastNanoTime = System.nanoTime()
        while (isActive && !uiState.isGameOver && !uiState.isPaused) {
            withFrameNanos { now ->
                if (lastNanoTime != 0L) {
                    val dt = (now - lastNanoTime) / 1_000_000_000f
                    viewModel.updateGameFrame(dt)
                }
                lastNanoTime = now
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBlack)
    ) {
        // Compose Canvas with Game Engine Objects
        GameCanvas(
            engine = viewModel.engine,
            modifier = Modifier.fillMaxSize()
        )

        // Top Heads-Up Display (HUD)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score & Record
                Column {
                    Text(
                        text = "${uiState.score}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "RECORD : ${uiState.bestScore}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Middle Badges: Stars & Shields
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stars counter
                    Surface(
                        color = CosmicSurface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Étoiles",
                                tint = NeonGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${uiState.starsInRun}",
                                color = NeonGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Shields indicators
                    Surface(
                        color = CosmicSurface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Bouclier",
                                tint = if (uiState.shields > 0) NeonCyan else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${uiState.shields}",
                                color = if (uiState.shields > 0) NeonCyan else Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Wave Pill
                    Surface(
                        color = NeonPurple.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "VAGUE ${uiState.wave}",
                            color = NeonPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Pause Button
                IconButton(
                    onClick = { viewModel.pauseGame() },
                    modifier = Modifier
                        .testTag("pause_button")
                        .size(44.dp)
                        .background(CosmicSurface.copy(alpha = 0.85f), CircleShape)
                        .border(1.dp, CosmicCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = TextPrimary
                    )
                }
            }

            // Combo multiplier bar (if combo > 1)
            AnimatedVisibility(
                visible = uiState.combo > 1,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = NeonGold,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "COMBO x${uiState.combo}",
                                color = CosmicBlack,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { uiState.comboProgress },
                            modifier = Modifier
                                .width(80.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NeonGold,
                            trackColor = CosmicCardBorder
                        )
                    }
                }
            }

            // Active Power-ups pills
            if (uiState.activePowerUps.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (pu in uiState.activePowerUps) {
                        Surface(
                            color = pu.type.color.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, pu.type.color.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = pu.type.title.uppercase(),
                                    color = pu.type.color,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${pu.timeRemainingSec.toInt()}s",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Wave Announcement Overlay Banner
        AnimatedVisibility(
            visible = uiState.waveAnnouncement != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            uiState.waveAnnouncement?.let { text ->
                Surface(
                    color = CosmicCard.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, NeonCyan)
                ) {
                    Text(
                        text = text,
                        color = NeonCyan,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                    )
                }
            }
        }

        // Bottom Controls Dock: Directional Touch Pads for easy two-thumb play
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val leftInteraction = remember { MutableInteractionSource() }
            val isLeftPressed by leftInteraction.collectIsPressedAsState()

            val rightInteraction = remember { MutableInteractionSource() }
            val isRightPressed by rightInteraction.collectIsPressedAsState()

            LaunchedEffect(isLeftPressed) {
                while (isLeftPressed) {
                    viewModel.engine.moveShipBy(-0.015f)
                    withFrameNanos { }
                }
            }

            LaunchedEffect(isRightPressed) {
                while (isRightPressed) {
                    viewModel.engine.moveShipBy(0.015f)
                    withFrameNanos { }
                }
            }

            // Left Touch Button
            Surface(
                modifier = Modifier
                    .testTag("left_control_button")
                    .size(64.dp)
                    .clickable(
                        interactionSource = leftInteraction,
                        indication = null,
                        onClick = {}
                    ),
                shape = CircleShape,
                color = if (isLeftPressed) NeonCyan.copy(alpha = 0.35f) else CosmicSurface.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = if (isLeftPressed) NeonCyan else CosmicCardBorder
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Gauche",
                        tint = if (isLeftPressed) NeonCyan else TextSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = "Glissez ou touchez",
                color = TextSecondary.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            // Right Touch Button
            Surface(
                modifier = Modifier
                    .testTag("right_control_button")
                    .size(64.dp)
                    .clickable(
                        interactionSource = rightInteraction,
                        indication = null,
                        onClick = {}
                    ),
                shape = CircleShape,
                color = if (isRightPressed) NeonCyan.copy(alpha = 0.35f) else CosmicSurface.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = if (isRightPressed) NeonCyan else CosmicCardBorder
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Droite",
                        tint = if (isRightPressed) NeonCyan else TextSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Pause Modal
        if (uiState.isPaused && !uiState.isGameOver) {
            Dialog(onDismissRequest = { viewModel.resumeGame() }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicDark),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CosmicCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.paused_title),
                            color = NeonCyan,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Score actuel : ${uiState.score}",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.resumeGame() },
                            modifier = Modifier
                                .testTag("resume_button")
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CosmicBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.btn_resume),
                                color = CosmicBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { viewModel.restartGame() },
                            modifier = Modifier
                                .testTag("restart_pause_button")
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_restart))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { viewModel.returnToMenu() },
                            modifier = Modifier
                                .testTag("menu_pause_button")
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f))
                        ) {
                            Text(stringResource(R.string.btn_menu))
                        }
                    }
                }
            }
        }

        // Game Over Modal
        if (uiState.isGameOver) {
            Dialog(onDismissRequest = { /* Require button click */ }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicDark),
                    border = androidx.compose.foundation.BorderStroke(2.dp, if (uiState.newRecordBeaten) NeonGold else CosmicCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState.newRecordBeaten) {
                            Surface(
                                color = NeonGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = NeonGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.new_record_badge),
                                        color = NeonGold,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Text(
                            text = stringResource(R.string.game_over_title),
                            color = NeonRed,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Score Display
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CosmicSurface,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.score_label),
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${uiState.score}",
                                    color = Color.White,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatBox(
                                title = "Étoiles",
                                value = "+${uiState.starsInRun}",
                                color = NeonGold,
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                title = "Vague",
                                value = "${uiState.wave}",
                                color = NeonPurple,
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                title = "Esquives",
                                value = "${uiState.asteroidsDodged}",
                                color = NeonGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Play Again Button
                        Button(
                            onClick = { viewModel.restartGame() },
                            modifier = Modifier
                                .testTag("restart_game_over_button")
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = CosmicBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.btn_restart),
                                color = CosmicBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Menu Button
                        OutlinedButton(
                            onClick = { viewModel.returnToMenu() },
                            modifier = Modifier
                                .testTag("menu_game_over_button")
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_menu),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = CosmicSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
