package com.example.features.offline_ai.rag

/**
 * Interface for local embedding generation.
 * Converts text into numerical vectors for semantic search.
 */
interface EmbeddingEngine {

    /**
     * Dimension of the embedding vectors (e.g., 384 for small models).
     */
    val dimensions: Int

    /**
     * Generates an embedding for the given text.
     */
    suspend fun getEmbedding(text: String): FloatArray

    /**
     * Generates embeddings for a list of texts in batch.
     */
    suspend fun getEmbeddings(texts: List<String>): List<FloatArray>
}
