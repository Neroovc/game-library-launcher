package com.gamelauncher.engine.detector

interface EngineDetector {
    fun detect(filePath: String): EngineDetectionResult
}

data class EngineDetectionResult(
    val engine: String? = null,
    val confidence: Float = 0f
)
