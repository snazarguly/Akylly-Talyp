package com.example.features.offline_ai.response

import java.util.Locale

/**
 * Validates the LLM-generated response before showing it to the user.
 * Prevents leaks of internal instructions or obviously incorrect data.
 */
object ResponseGuard {

    private val SYSTEM_MARKERS = listOf("### SYSTEM", "### USER", "### CONTEXT", "Assistant:", "Instruction:")

    /**
     * Cleans and validates generated text.
     */
    fun process(generatedText: String): String {
        var cleaned = generatedText.trim()
        
        // 1. Remove accidental internal instruction leakage
        for (marker in SYSTEM_MARKERS) {
            if (cleaned.contains(marker)) {
                cleaned = cleaned.substringBefore(marker).trim()
            }
        }

        // 2. Prevent "As an AI..." prefix if it's annoying in offline mode
        if (cleaned.startsWith("As a language model") || cleaned.startsWith("As an AI")) {
            cleaned = cleaned.substringAfter("\n").trim()
        }

        return if (cleaned.isBlank()) "Bagyşlaň, jogap döredip bilmedim." else cleaned
    }
}
