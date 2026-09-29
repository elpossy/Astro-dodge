package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    var soundEnabled: Boolean = true

    private val sampleRate = 22050

    fun playStarSound() {
        if (!soundEnabled) return
        scope.launch {
            val durationMs = 120
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                // Frequency jumps from 660Hz to 990Hz halfway through
                val freq = if (i < numSamples / 2) 660.0 else 990.0
                val progress = i.toDouble() / numSamples
                val envelope = 1.0 - progress
                val sample = sin(2.0 * Math.PI * freq * t) * envelope * 0.7
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playPowerUpSound() {
        if (!soundEnabled) return
        scope.launch {
            val durationMs = 240
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            val samplesPerNote = numSamples / notes.size

            for (i in 0 until numSamples) {
                val noteIndex = (i / samplesPerNote).coerceIn(0, notes.lastIndex)
                val freq = notes[noteIndex]
                val t = i.toDouble() / sampleRate
                val noteProgress = (i % samplesPerNote).toDouble() / samplesPerNote
                val envelope = (1.0 - noteProgress * 0.8)
                val sample = sin(2.0 * Math.PI * freq * t) * envelope * 0.7
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playLaserSound() {
        if (!soundEnabled) return
        scope.launch {
            val durationMs = 90
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                // Frequency sweeps down from 1200Hz to 300Hz
                val freq = 1200.0 - (progress * 900.0)
                val t = i.toDouble() / sampleRate
                val envelope = (1.0 - progress) * (1.0 - progress)
                // Add slight square wave harmonic for retro synth feel
                val sine = sin(2.0 * Math.PI * freq * t)
                val retroSquare = if (sine > 0) 0.6 else -0.6
                val sample = (sine * 0.5 + retroSquare * 0.5) * envelope * 0.65
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playExplosionSound() {
        if (!soundEnabled) return
        scope.launch {
            val durationMs = 280
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var lastVal = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val envelope = (1.0 - progress) * (1.0 - progress)
                // Filtered white noise with low rumble
                val whiteNoise = (Random.nextDouble() * 2.0 - 1.0)
                // Low-pass filtering
                lastVal = (lastVal * 0.8) + (whiteNoise * 0.2)
                val sample = lastVal * envelope * 0.8
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playShieldSound() {
        if (!soundEnabled) return
        scope.launch {
            val durationMs = 150
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val envelope = 1.0 - progress
                val sample = (sin(2.0 * Math.PI * 440.0 * t) + sin(2.0 * Math.PI * 880.0 * t)) * 0.4 * envelope
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playGameOverSound() {
        if (!soundEnabled) return
        scope.launch {
            val durationMs = 350
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            val notes = doubleArrayOf(440.0, 392.0, 349.2, 311.1)
            val samplesPerNote = numSamples / notes.size

            for (i in 0 until numSamples) {
                val noteIndex = (i / samplesPerNote).coerceIn(0, notes.lastIndex)
                val freq = notes[noteIndex]
                val t = i.toDouble() / sampleRate
                val envelope = 1.0 - (i.toDouble() / numSamples)
                val sample = sin(2.0 * Math.PI * freq * t) * envelope * 0.7
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    private fun playPcm(buffer: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            // Release after playback finishes
            val waitMs = ((buffer.size.toDouble() / sampleRate) * 1000).toLong() + 50
            Thread.sleep(waitMs)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio glitches safely
        }
    }
}
