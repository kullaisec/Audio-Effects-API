package com.voiceai.services

import com.voiceai.models.AudioEffect
import com.voiceai.models.ProcessingResult
import java.io.BufferedReader
import java.io.InputStreamReader

class AudioProcessingService {
    private val ffmpegPath = "/usr/bin/ffmpeg"
    private val outputDirectory = "/var/voiceai/processed"

    fun applyEffect(inputPath: String, outputFileName: String, effect: AudioEffect): ProcessingResult {
        val effectFilter = getEffectFilter(effect)
        val outputPath = "$outputDirectory/$outputFileName"
        
        // Build command for processing audio with selected effect
        val command = "$ffmpegPath -i $inputPath -af $effectFilter -y $outputPath"
        val process = Runtime.getRuntime().exec(arrayOf("/bin/sh", "-c", command))
        
        val exitCode = process.waitFor()
        return ProcessingResult(success = exitCode == 0, outputPath = outputPath)
    }

    private fun getEffectFilter(effect: AudioEffect): String {
        return when (effect) {
            AudioEffect.NOISE_REDUCTION -> "afftdn=nf=-25"
            AudioEffect.NORMALIZE -> "loudnorm=I=-16:LRA=11"
            AudioEffect.COMPRESS -> "acompressor=threshold=-20dB"
        }
    }
}
