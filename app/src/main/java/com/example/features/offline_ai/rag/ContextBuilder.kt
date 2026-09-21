package com.example.features.offline_ai.rag

/**
 * Component responsible for merging retrieved chunks, student records, 
 * and conversation history into a single structured prompt.
 */
object ContextBuilder {

    fun build(
        systemPrompt: String,
        retrievedChunks: List<VectorStore.Chunk>,
        studentContext: String,
        conversationHistory: String,
        userQuery: String
    ): String {
        val sb = StringBuilder()
        
        sb.append("### SYSTEM INSTRUCTION\n")
        sb.append(systemPrompt)
        sb.append("\n\n")

        if (retrievedChunks.isNotEmpty()) {
            sb.append("### UNIVERSITY KNOWLEDGE BASE (Verified Facts)\n")
            retrievedChunks.forEach { chunk ->
                sb.append("- ${chunk.text}\n")
            }
            sb.append("\n")
        }

        if (studentContext.isNotEmpty()) {
            sb.append("### CURRENT STUDENT ACADEMIC DATA (Authorized)\n")
            sb.append(studentContext)
            sb.append("\n")
        }

        if (conversationHistory.isNotEmpty()) {
            sb.append("### RECENT CONVERSATION HISTORY\n")
            sb.append(conversationHistory)
            sb.append("\n")
        }

        sb.append("### USER QUESTION\n")
        sb.append(userQuery)
        sb.append("\n\n")
        sb.append("### OFFLINE AI ANSWER:\n")

        return sb.toString()
    }
}
