package com.example.features.offline_ai.rag

/**
 * Placeholder implementation of EmbeddingEngine.
 * In a real scenario, this would load a small TFLite or ONNX model.
 */
class SimpleEmbeddingEngine : EmbeddingEngine {
    override val dimensions: Int = 384

    override suspend fun getEmbedding(text: String): FloatArray {
        // Deterministic pseudo-embedding based on string hash for demo/testing
        val result = FloatArray(dimensions)
        val hash = text.hashCode().toLong()
        for (i in 0 until dimensions) {
            result[i] = ((hash + i) % 1000) / 1000f
        }
        return result
    }

    override suspend fun getEmbeddings(texts: List<String>): List<FloatArray> {
        return texts.map { getEmbedding(it) }
    }
}
