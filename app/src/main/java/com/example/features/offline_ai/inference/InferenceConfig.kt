package com.example.features.offline_ai.inference

/**
 * Configuration for local LLM inference parameters.
 */
data class InferenceConfig(
    val modelAssetPath: String = "models/gemma3.task",
    val contextLength: Int = 4096,
    val maxOutputTokens: Int = 1024,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val threads: Int = Runtime.getRuntime().availableProcessors().coerceAtMost(4),
    val useGpu: Boolean = true,
    val repeatPenalty: Float = 1.1f,
    val quantization: String = "Q4_K_M"
)
