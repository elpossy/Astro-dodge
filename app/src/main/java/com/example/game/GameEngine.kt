package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.data.UserStats
import com.example.util.SoundManager
import com.example.util.VibrationHelper
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class GameEngine(
    private val soundManager: SoundManager,
    private val vibrationHelper: VibrationHelper,
    val onGameOver: (score: Int, stars: Int, wave: Int, duration: Int, dodged: Int) -> Unit
) {
    var shipX = 0.5f
    var shipY = 0.82f
    var targetShipX = 0.5f
    var shipTilt = 0f

    var activeShip: ShipInfo = ShipCatalog.SHIPS.first()
    var currentStats: UserStats = UserStats()

    var score = 0
    var bestScore = 0
    var wave = 1
    var starsInRun = 0
    var asteroidsDodged = 0
    var combo = 1
    var comboTimer = 0f
    val maxComboTimer = 3.2f

    var shields = 1
    var isInvulnerable = false
    var invulnerableTimer = 0f

    var isGameOver = false
    var isPaused = false
    var newRecordBeaten = false
    var runTimeSeconds = 0f

    var screenShake = 0f
    var waveAnnouncement: String? = null
    var waveAnnouncementTimer = 0f

    val asteroids = mutableListOf<Asteroid>()
    val stars = mutableListOf<CollectibleStar>()
    val powerUps = mutableListOf<CollectiblePowerUp>()
    val lasers = mutableListOf<LaserBeam>()
    val particles = mutableListOf<Particle>()
    val activePowerUps = mutableListOf<ActivePowerUp>()

    val bgStars = mutableListOf<BackgroundStar>()

    private var nextEntityId = 1L
    private var asteroidSpawnTimer = 0f
    private var starSpawnTimer = 0f
    private var powerUpSpawnTimer = 0f
    private var laserAutoFireTimer = 0f
    private var thrusterParticleTimer = 0f

    init {
        initBackgroundStars()
    }

    private fun initBackgroundStars() {
        bgStars.clear()
        for (i in 0 until 70) {
            bgStars.add(
                BackgroundStar(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    speed = Random.nextFloat() * 0.15f + 0.05f,
                    size = Random.nextFloat() * 3.5f + 1f,
                    alpha = Random.nextFloat() * 0.6f + 0.4f
                )
            )
        }
    }

    fun startNewGame(ship: ShipInfo, stats: UserStats, highscore: Int) {
        activeShip = ship
        currentStats = stats
        bestScore = highscore
        score = 0
        wave = 1
        starsInRun = 0
        asteroidsDodged = 0
        combo = 1
        comboTimer = 0f
        shields = ship.initialShields
        isInvulnerable = false
        invulnerableTimer = 0f
        isGameOver = false
        isPaused = false
        newRecordBeaten = false
        runTimeSeconds = 0f
        screenShake = 0f
        waveAnnouncement = "VAGUE 1"
        waveAnnouncementTimer = 2.0f

        shipX = 0.5f
        targetShipX = 0.5f
        shipTilt = 0f

        asteroids.clear()
        stars.clear()
        powerUps.clear()
        lasers.clear()
        particles.clear()
        activePowerUps.clear()

        asteroidSpawnTimer = 0.8f
        starSpawnTimer = 0.5f
        powerUpSpawnTimer = 10.0f
    }

    fun setShipTarget(normX: Float) {
        targetShipX = normX.coerceIn(0.06f, 0.94f)
    }

    fun moveShipBy(deltaNormX: Float) {
        targetShipX = (targetShipX + deltaNormX).coerceIn(0.06f, 0.94f)
    }

    fun togglePause() {
        if (!isGameOver) {
            isPaused = !isPaused
        }
    }

    fun update(dtSec: Float) {
        if (isGameOver || isPaused) return

        val dt = dtSec.coerceAtMost(0.05f)
        runTimeSeconds += dt

        // Screen shake decay
        if (screenShake > 0f) {
            screenShake = max(0f, screenShake - dt * 2.5f)
        }

        // Wave announcement timer
        if (waveAnnouncementTimer > 0f) {
            waveAnnouncementTimer -= dt
            if (waveAnnouncementTimer <= 0f) {
                waveAnnouncement = null
            }
        }

        // Difficulty scaling
        val difficultyMultiplier = when (currentStats.difficulty) {
            "EASY" -> 0.8f
            "HARD" -> 1.35f
            else -> 1.0f
        }

        // Background stars scroll
        val isSlowMo = activePowerUps.any { it.type == PowerUpType.SLOWMO }
        val speedFactor = (if (isSlowMo) 0.5f else 1.0f) * (1f + (wave - 1) * 0.08f)

        for (star in bgStars) {
            star.y += star.speed * speedFactor * dt
            if (star.y > 1.0f) {
                star.y = -0.02f
            }
        }

        // Smooth ship motion and banking tilt
        val prevX = shipX
        val moveSpeed = 12f * activeShip.speedMultiplier
        shipX += (targetShipX - shipX) * min(1f, dt * moveSpeed)
        val deltaX = shipX - prevX
        shipTilt = (deltaX * 300f).coerceIn(-25f, 25f)

        // Thruster particles
        thrusterParticleTimer += dt
        if (thrusterParticleTimer >= 0.035f) {
            thrusterParticleTimer = 0f
            emitThrusterParticles()
        }

        // Invulnerability timer
        if (isInvulnerable) {
            invulnerableTimer -= dt
            if (invulnerableTimer <= 0f) {
                isInvulnerable = false
            }
        }

        // Combo timer
        if (combo > 1) {
            comboTimer -= dt
            if (comboTimer <= 0f) {
                combo = 1
                comboTimer = 0f
            }
        }

        // Active power ups countdown
        val iterator = activePowerUps.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            val newTime = item.timeRemainingSec - dt
            if (newTime <= 0f) {
                iterator.remove()
            } else {
                val index = activePowerUps.indexOf(item)
                if (index != -1) {
                    activePowerUps[index] = item.copy(timeRemainingSec = newTime)
                }
            }
        }

        // Auto Laser Fire if LASER power-up is active
        val hasLaser = activePowerUps.any { it.type == PowerUpType.LASER }
        if (hasLaser) {
            laserAutoFireTimer += dt
            if (laserAutoFireTimer >= 0.20f) {
                laserAutoFireTimer = 0f
                fireLasers()
            }
        }

        // Wave updates based on score
        val calculatedWave = max(1, (score / 250) + 1)
        if (calculatedWave > wave) {
            wave = calculatedWave
            waveAnnouncement = "VAGUE $wave !"
            waveAnnouncementTimer = 2.2f
            soundManager.playPowerUpSound()
            vibrationHelper.powerUp()
        }

        // Spawn Asteroids
        asteroidSpawnTimer -= dt
        val baseSpawnInterval = max(0.45f, (1.6f - (wave * 0.10f)) / difficultyMultiplier)
        if (asteroidSpawnTimer <= 0f) {
            asteroidSpawnTimer = baseSpawnInterval + Random.nextFloat() * 0.3f
            spawnAsteroid(difficultyMultiplier, isSlowMo)
        }

        // Spawn Stars
        starSpawnTimer -= dt
        if (starSpawnTimer <= 0f) {
            starSpawnTimer = Random.nextFloat() * 0.8f + 0.6f
            spawnStar()
        }

        // Spawn PowerUp occasionally
        powerUpSpawnTimer -= dt
        if (powerUpSpawnTimer <= 0f) {
            powerUpSpawnTimer = Random.nextFloat() * 8f + 14f
            spawnPowerUp()
        }

        // Update Lasers
        val laserIter = lasers.iterator()
        while (laserIter.hasNext()) {
            val laser = laserIter.next()
            laser.y -= laser.speedY * dt
            if (laser.y < -0.05f) {
                laserIter.remove()
            }
        }

        // Update Stars & Magnet pull
        val hasMagnet = activePowerUps.any { it.type == PowerUpType.MAGNET }
        val effectiveMagnetRadius = if (hasMagnet) 0.35f else activeShip.magnetRadius

        val starIter = stars.iterator()
        while (starIter.hasNext()) {
            val star = starIter.next()
            star.glowPhase += dt * 5f

            // Magnet pulling logic
            val dx = shipX - star.x
            val dy = shipY - star.y
            val dist = hypot(dx, dy)

            if (dist < effectiveMagnetRadius) {
                val pullSpeed = if (hasMagnet) 0.8f else 0.45f
                star.x += (dx / dist) * pullSpeed * dt
                star.y += (dy / dist) * pullSpeed * dt
            } else {
                star.y += star.speedY * (if (isSlowMo) 0.7f else 1.0f) * dt
            }

            // Check pickup by ship
            val shipRadius = 0.045f
            if (dist < (shipRadius + star.radius)) {
                // Picked up!
                collectStar(star)
                starIter.remove()
                continue
            }

            if (star.y > 1.05f) {
                starIter.remove()
            }
        }

        // Update Power-ups
        val puIter = powerUps.iterator()
        while (puIter.hasNext()) {
            val pu = puIter.next()
            pu.pulsePhase += dt * 6f

            val dx = shipX - pu.x
            val dy = shipY - pu.y
            val dist = hypot(dx, dy)

            if (dist < effectiveMagnetRadius * 0.7f) {
                pu.x += (dx / dist) * 0.4f * dt
                pu.y += (dy / dist) * 0.4f * dt
            } else {
                pu.y += pu.speedY * dt
            }

            if (dist < (0.045f + pu.radius)) {
                activatePowerUp(pu.type)
                puIter.remove()
                continue
            }

            if (pu.y > 1.05f) {
                puIter.remove()
            }
        }

        // Update Asteroids & check Laser collisions
        val astIter = asteroids.iterator()
        val shipRadius = 0.042f

        while (astIter.hasNext()) {
            val ast = astIter.next()
            val slowMultiplier = if (isSlowMo) 0.4f else 1.0f
            ast.y += ast.speedY * slowMultiplier * dt
            ast.x += ast.speedX * slowMultiplier * dt
            ast.rotation += ast.rotationSpeed * dt

            // Laser hit collision
            var destroyedByLaser = false
            val lIter = lasers.iterator()
            while (lIter.hasNext()) {
                val laser = lIter.next()
                val distLaser = hypot(laser.x - ast.x, laser.y - ast.y)
                if (distLaser < ast.radius + 0.02f) {
                    lIter.remove()
                    destroyedByLaser = true
                    break
                }
            }

            if (destroyedByLaser) {
                // Destroy asteroid
                soundManager.playExplosionSound()
                vibrationHelper.tick()
                emitAsteroidExplosion(ast.x, ast.y, ast.color, ast.radius)
                score += ast.points * combo
                checkHighScore()
                // Drop a star occasionally
                if (ast.type == AsteroidType.GIANT || Random.nextFloat() < 0.4f) {
                    stars.add(
                        CollectibleStar(
                            id = nextEntityId++,
                            x = ast.x,
                            y = ast.y,
                            radius = 0.022f,
                            speedY = 0.18f,
                            isCrystal = (ast.type == AsteroidType.GIANT)
                        )
                    )
                }
                astIter.remove()
                continue
            }

            // Ship collision
            val distToShip = hypot(shipX - ast.x, shipY - ast.y)
            if (distToShip < (shipRadius + ast.radius * 0.85f)) {
                if (!isInvulnerable) {
                    // Collision handling
                    handleShipCollision(ast)
                    astIter.remove()
                    continue
                }
            }

            // Asteroid passed screen
            if (ast.y > 1.05f) {
                asteroidsDodged++
                score += 10
                checkHighScore()
                astIter.remove()
            }
        }

        // Update Particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt
            p.alpha = max(0f, p.life / p.maxLife)
            if (p.life <= 0f) {
                pIter.remove()
            }
        }
    }

    private fun handleShipCollision(asteroid: Asteroid) {
        val hasShieldPowerUp = activePowerUps.any { it.type == PowerUpType.SHIELD }

        if (hasShieldPowerUp) {
            // Remove shield power-up
            activePowerUps.removeAll { it.type == PowerUpType.SHIELD }
            soundManager.playShieldSound()
            vibrationHelper.powerUp()
            triggerInvulnerability(1.4f)
            screenShake = 0.4f
            emitShieldShatterParticles()
            emitAsteroidExplosion(asteroid.x, asteroid.y, asteroid.color, asteroid.radius)
            return
        }

        if (shields > 0) {
            shields--
            soundManager.playShieldSound()
            vibrationHelper.explosion()
            triggerInvulnerability(1.6f)
            screenShake = 0.5f
            emitShieldShatterParticles()
            emitAsteroidExplosion(asteroid.x, asteroid.y, asteroid.color, asteroid.radius)
            return
        }

        // Game Over!
        isGameOver = true
        soundManager.playGameOverSound()
        vibrationHelper.explosion()
        screenShake = 1.0f
        emitShipExplosion()

        checkHighScore()
        onGameOver(score, starsInRun, wave, runTimeSeconds.toInt(), asteroidsDodged)
    }

    private fun checkHighScore() {
        if (score > bestScore) {
            bestScore = score
            if (!newRecordBeaten && score > 50) {
                newRecordBeaten = true
            }
        }
    }

    private fun triggerInvulnerability(duration: Float) {
        isInvulnerable = true
        invulnerableTimer = duration
    }

    private fun collectStar(star: CollectibleStar) {
        val basePoints = if (star.isCrystal) 150 else 40
        val starCount = if (star.isCrystal) 3 else 1
        starsInRun += starCount
        score += basePoints * combo
        combo = min(6, combo + 1)
        comboTimer = maxComboTimer

        soundManager.playStarSound()
        vibrationHelper.tick()

        emitStarPickupParticles(star.x, star.y, if (star.isCrystal) Color(0xFFC084FC) else Color(0xFFFFD700))
        checkHighScore()
    }

    private fun activatePowerUp(type: PowerUpType) {
        soundManager.playPowerUpSound()
        vibrationHelper.powerUp()

        when (type) {
            PowerUpType.SHIELD -> {
                shields = min(activeShip.initialShields + 1, shields + 1)
            }
            else -> {}
        }

        // Add or refresh active power up
        val existingIndex = activePowerUps.indexOfFirst { it.type == type }
        val active = ActivePowerUp(type, type.durationSec, type.durationSec)
        if (existingIndex >= 0) {
            activePowerUps[existingIndex] = active
        } else {
            activePowerUps.add(active)
        }

        emitPowerUpSparkles(shipX, shipY, type.color)
    }

    private fun fireLasers() {
        soundManager.playLaserSound()
        vibrationHelper.tick()
        // Dual laser cannons
        lasers.add(LaserBeam(id = nextEntityId++, x = shipX - 0.025f, y = shipY - 0.03f, speedY = 1.2f))
        lasers.add(LaserBeam(id = nextEntityId++, x = shipX + 0.025f, y = shipY - 0.03f, speedY = 1.2f))
    }

    private fun spawnAsteroid(difficultyMultiplier: Float, isSlowMo: Boolean) {
        val rand = Random.nextFloat()
        val type = when {
            rand < 0.55f -> AsteroidType.NORMAL
            rand < 0.78f -> AsteroidType.FAST
            rand < 0.90f -> AsteroidType.GIANT
            else -> AsteroidType.COMET
        }

        val baseSpeed = (0.22f + wave * 0.025f) * difficultyMultiplier
        val (radius, speedY, speedX, color, pts) = when (type) {
            AsteroidType.NORMAL -> {
                val r = Random.nextFloat() * 0.015f + 0.035f
                Tuple5(r, baseSpeed * (Random.nextFloat() * 0.2f + 0.9f), (Random.nextFloat() - 0.5f) * 0.06f, Color(0xFF8D99AE), 25)
            }
            AsteroidType.FAST -> {
                val r = 0.028f
                Tuple5(r, baseSpeed * 1.55f, (Random.nextFloat() - 0.5f) * 0.08f, Color(0xFFFF5252), 40)
            }
            AsteroidType.GIANT -> {
                val r = Random.nextFloat() * 0.015f + 0.065f
                Tuple5(r, baseSpeed * 0.65f, (Random.nextFloat() - 0.5f) * 0.03f, Color(0xFF6B7280), 60)
            }
            AsteroidType.COMET -> {
                val r = 0.032f
                val drift = if (Random.nextBoolean()) 0.12f else -0.12f
                Tuple5(r, baseSpeed * 1.3f, drift, Color(0xFF00E5FF), 50)
            }
        }

        val spawnX = Random.nextFloat() * 0.88f + 0.06f
        asteroids.add(
            Asteroid(
                id = nextEntityId++,
                x = spawnX,
                y = -0.06f,
                radius = radius,
                speedX = speedX,
                speedY = speedY,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 4f,
                type = type,
                color = color,
                points = pts
            )
        )
    }

    private fun spawnStar() {
        val isCrystal = Random.nextFloat() < 0.22f
        stars.add(
            CollectibleStar(
                id = nextEntityId++,
                x = Random.nextFloat() * 0.86f + 0.07f,
                y = -0.04f,
                radius = if (isCrystal) 0.026f else 0.020f,
                speedY = Random.nextFloat() * 0.06f + 0.22f,
                isCrystal = isCrystal
            )
        )
    }

    private fun spawnPowerUp() {
        val types = PowerUpType.entries
        val chosen = types.random()
        powerUps.add(
            CollectiblePowerUp(
                id = nextEntityId++,
                x = Random.nextFloat() * 0.80f + 0.10f,
                y = -0.05f,
                radius = 0.028f,
                speedY = 0.18f,
                type = chosen
            )
        )
    }

    private fun emitThrusterParticles() {
        val count = 2
        for (i in 0 until count) {
            val offsetX = (Random.nextFloat() - 0.5f) * 0.02f
            particles.add(
                Particle(
                    x = shipX + offsetX,
                    y = shipY + 0.032f,
                    vx = (Random.nextFloat() - 0.5f) * 0.05f,
                    vy = Random.nextFloat() * 0.25f + 0.18f,
                    color = if (Random.nextBoolean()) activeShip.primaryColor else activeShip.accentColor,
                    size = Random.nextFloat() * 4f + 2f,
                    alpha = 0.9f,
                    life = 0.25f,
                    maxLife = 0.25f
                )
            )
        }
    }

    private fun emitStarPickupParticles(x: Float, y: Float, color: Color) {
        for (i in 0 until 12) {
            val angle = (i.toFloat() / 12f) * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 0.2f + 0.08f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    size = Random.nextFloat() * 3.5f + 2f,
                    alpha = 1.0f,
                    life = 0.35f,
                    maxLife = 0.35f
                )
            )
        }
    }

    private fun emitPowerUpSparkles(x: Float, y: Float, color: Color) {
        for (i in 0 until 18) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 0.35f + 0.1f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    size = Random.nextFloat() * 5f + 3f,
                    alpha = 1.0f,
                    life = 0.45f,
                    maxLife = 0.45f
                )
            )
        }
    }

    private fun emitShieldShatterParticles() {
        for (i in 0 until 24) {
            val angle = (i.toFloat() / 24f) * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 0.4f + 0.15f
            particles.add(
                Particle(
                    x = shipX,
                    y = shipY,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = Color(0xFF00E5FF),
                    size = Random.nextFloat() * 4f + 2f,
                    alpha = 1.0f,
                    life = 0.4f,
                    maxLife = 0.4f
                )
            )
        }
    }

    private fun emitAsteroidExplosion(x: Float, y: Float, color: Color, radius: Float) {
        val count = (radius * 300).toInt().coerceIn(10, 26)
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 0.35f + 0.05f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = if (Random.nextBoolean()) color else Color(0xFFFF9100),
                    size = Random.nextFloat() * 4.5f + 2f,
                    alpha = 1.0f,
                    life = 0.4f,
                    maxLife = 0.4f
                )
            )
        }
    }

    private fun emitShipExplosion() {
        for (i in 0 until 40) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 0.55f + 0.1f
            particles.add(
                Particle(
                    x = shipX,
                    y = shipY,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = when (Random.nextInt(4)) {
                        0 -> Color(0xFFFF3366)
                        1 -> Color(0xFFFF9100)
                        2 -> activeShip.primaryColor
                        else -> Color(0xFFFFD700)
                    },
                    size = Random.nextFloat() * 6f + 3f,
                    alpha = 1.0f,
                    life = 0.7f,
                    maxLife = 0.7f
                )
            )
        }
    }

    fun getUiState(): GameUiState {
        return GameUiState(
            score = score,
            bestScore = bestScore,
            wave = wave,
            starsInRun = starsInRun,
            combo = combo,
            comboProgress = if (combo > 1) (comboTimer / maxComboTimer) else 0f,
            shields = shields,
            maxShields = activeShip.initialShields,
            activePowerUps = activePowerUps.toList(),
            isGameOver = isGameOver,
            isPaused = isPaused,
            newRecordBeaten = newRecordBeaten,
            runDurationSec = runTimeSeconds.toInt(),
            asteroidsDodged = asteroidsDodged,
            waveAnnouncement = waveAnnouncement,
            screenShake = screenShake
        )
    }

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
}
