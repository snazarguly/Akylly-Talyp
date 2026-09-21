package com.example.features.offline_ai.analyzer

import java.util.Locale

/**
 * Component responsible for query normalization and basic language detection.
 * Ensures that the intent detector and retrieval engine work with clean, consistent text.
 */
object OfflineQuestionAnalyzer {

    data class AnalyzedQuery(
        val original: String,
        val normalized: String,
        val tokens: Set<String>,
        val languageCode: String
    )

    /**
     * Normalizes the user question:
     * - Lowercase
     * - Strips punctuation (preserving Turkmen characters)
     * - Cleanup whitespace
     * - Detects language
     */
    fun analyze(query: String, hintLanguage: String = "tk"): AnalyzedQuery {
        val q = query.lowercase(Locale.ROOT).trim()
        
        // Normalize: remove punctuation but keep Turkmen and Russian alphabets
        val normalized = q.replace(Regex("[^a-zа-ý0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            
        val tokens = normalized.split(" ").filter { it.length > 1 }.toSet()
        val detectedLang = detectLanguage(q, hintLanguage)
        
        return AnalyzedQuery(
            original = query,
            normalized = normalized,
            tokens = tokens,
            languageCode = detectedLang
        )
    }

    private fun detectLanguage(q: String, hint: String): String {
        // Cyrillic check
        if (q.any { it in '\u0400'..'\u04FF' }) return "ru"
        
        // Specific Turkmen markers
        val tkKeywords = listOf("näme", "haçan", "nirede", "nähili", "näçe", "barmy", "ýok", "sapak", "baham")
        if (tkKeywords.any { q.contains(it) }) return "tk"
        
        // English markers
        val enKeywords = listOf("what", "when", "where", "how", "many", "show", "tell", "about")
        if (enKeywords.any { q.contains(it) }) return "en"
        
        return hint
    }
}
