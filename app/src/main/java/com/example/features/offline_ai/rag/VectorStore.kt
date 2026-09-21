package com.example.features.offline_ai.rag

/**
 * Lightweight local vector database.
 * Stores text chunks and their corresponding embeddings.
 */
interface VectorStore {

    data class Chunk(
        val id: String,
        val text: String,
        val metadata: Map<String, String> = emptyMap(),
        val embedding: FloatArray? = null
    )

    /**
     * Adds a chunk to the store.
     */
    suspend fun addChunk(chunk: Chunk)

    /**
     * Performs a semantic search using cosine similarity.
     * @param queryEmbedding Vector of the user's question.
     * @param limit Number of results to return.
     */
    suspend fun search(queryEmbedding: FloatArray, limit: Int = 3): List<Chunk>

    /**
     * Clears all chunks from the store.
     */
    suspend fun clear()
}
