package com.example.features.offline_ai.rag

import kotlin.math.sqrt

/**
 * Simple in-memory implementation of VectorStore using Cosine Similarity.
 */
class SimpleVectorStore : VectorStore {
    private val chunks = mutableListOf<VectorStore.Chunk>()

    override suspend fun addChunk(chunk: VectorStore.Chunk) {
        chunks.add(chunk)
    }

    override suspend fun search(queryEmbedding: FloatArray, limit: Int): List<VectorStore.Chunk> {
        if (queryEmbedding.isEmpty() || chunks.isEmpty()) return emptyList()

        return chunks.map { chunk ->
            val score = if (chunk.embedding != null) {
                cosineSimilarity(queryEmbedding, chunk.embedding)
            } else 0f
            chunk to score
        }
            .sortedByDescending { it.second }
            .take(limit)
            .filter { it.second > 0.3f } // Threshold
            .map { it.first }
    }

    override suspend fun clear() {
        chunks.clear()
    }

    private fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            normA += v1[i] * v1[i]
            normB += v2[i] * v2[i]
        }
        val result = dotProduct / (sqrt(normA) * sqrt(normB))
        return if (result.isNaN()) 0f else result
    }
}
