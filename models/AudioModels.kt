package com.voiceai.models

import kotlinx.serialization.Serializable

@Serializable
enum class AudioEffect {
    NOISE_REDUCTION,
    NORMALIZE,
    COMPRESS
}

@Serializable
data class EffectRequest(
    val inputPath: String,
    val outputFileName: String,
    val effect: AudioEffect
)

@Serializable
data class ProcessingResult(
    val success: Boolean,
    val outputPath: String
)
