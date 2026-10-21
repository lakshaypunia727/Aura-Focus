package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.AmbientSoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundSynthesizer(private val context: Context) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private var ambientLoopJob: Job? = null

    fun playSessionCompleteSound() {
        CoroutineScope(Dispatchers.Default).launch {
            playZenBell()
        }
    }

    fun playTickSound() {
        CoroutineScope(Dispatchers.Default).launch {
            playWoodClick()
        }
    }

    fun triggerVibration(pattern: LongArray = longArrayOf(0, 80, 80, 150)) {
        try {
            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, -1)
                    v.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            // Ignore vibration exceptions
        }
    }

    fun startAmbientSound(soundType: AmbientSoundType) {
        stopAmbientSound()
        if (soundType == AmbientSoundType.MUTE) return

        ambientLoopJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                when (soundType) {
                    AmbientSoundType.CLOCK_TICK -> {
                        playWoodClick()
                        delay(1000)
                    }
                    AmbientSoundType.ZEN_BELL -> {
                        // Periodic gentle reminder chime every 5 minutes or stay calm
                        delay(60000)
                    }
                    AmbientSoundType.RAIN_WHISPER -> {
                        playRainBurst()
                        delay(800)
                    }
                    AmbientSoundType.WHITE_NOISE -> {
                        playNoiseBurst()
                        delay(1000)
                    }
                    AmbientSoundType.MUTE -> break
                }
            }
        }
    }

    fun stopAmbientSound() {
        ambientLoopJob?.cancel()
        ambientLoopJob = null
    }

    /**
     * Synthesizes a calming Buddhist / Zen singing bowl / bell chime.
     */
    private fun playZenBell() {
        try {
            val sampleRate = 44100
            val durationSeconds = 2.5
            val numSamples = (durationSeconds * sampleRate).toInt()
            val buffer = ShortArray(numSamples)
            val baseFreq = 528.0 // 528 Hz - Solfeggio "Miracle / Transformation" frequency
            val harmonic1 = 1056.0
            val harmonic2 = 1584.0

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                // Exponential decay envelope
                val envelope = exp(-1.8 * time)
                val sample = (
                    0.6 * sin(2 * Math.PI * baseFreq * time) +
                    0.25 * sin(2 * Math.PI * harmonic1 * time) +
                    0.15 * sin(2 * Math.PI * harmonic2 * time)
                ) * envelope

                buffer[i] = (sample * Short.MAX_VALUE * 0.7).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            playBuffer(buffer, sampleRate)
        } catch (e: Exception) {
            // Ignore audio generation failures
        }
    }

    /**
     * Synthesizes a subtle, crisp wooden clock tick.
     */
    private fun playWoodClick() {
        try {
            val sampleRate = 44100
            val durationSeconds = 0.04
            val numSamples = (durationSeconds * sampleRate).toInt()
            val buffer = ShortArray(numSamples)
            val freq = 1200.0

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                val envelope = exp(-120.0 * time)
                val sample = sin(2 * Math.PI * freq * time) * envelope
                buffer[i] = (sample * Short.MAX_VALUE * 0.4).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            playBuffer(buffer, sampleRate)
        } catch (e: Exception) {
            // Ignore audio generation failures
        }
    }

    private fun playRainBurst() {
        try {
            val sampleRate = 22050
            val durationSeconds = 0.8
            val numSamples = (durationSeconds * sampleRate).toInt()
            val buffer = ShortArray(numSamples)

            var lastSample = 0.0
            for (i in 0 until numSamples) {
                val white = (Random.nextDouble() * 2.0 - 1.0)
                // Low pass filtered for soft rain sound
                lastSample = (lastSample + (0.04 * white)) / 1.04
                val time = i.toDouble() / sampleRate
                val env = sin(Math.PI * (time / durationSeconds))
                buffer[i] = (lastSample * Short.MAX_VALUE * 0.25 * env).toInt().toShort()
            }

            playBuffer(buffer, sampleRate)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun playNoiseBurst() {
        try {
            val sampleRate = 22050
            val durationSeconds = 1.0
            val numSamples = (durationSeconds * sampleRate).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val white = (Random.nextDouble() * 2.0 - 1.0)
                buffer[i] = (white * Short.MAX_VALUE * 0.08).toInt().toShort()
            }

            playBuffer(buffer, sampleRate)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
        CoroutineScope(Dispatchers.Default).launch {
            val sleepDuration = (buffer.size.toDouble() / sampleRate * 1000).toLong() + 200
            delay(sleepDuration)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
