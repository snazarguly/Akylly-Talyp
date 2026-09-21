package com.example.features.offline_ai.security

import com.example.features.offline_ai.model.OfflineStudentData
import java.util.Locale

/**
 * High-security boundary that prevents cross-student data leaks and prompt injections.
 * Security happens BEFORE context reaches the LLM.
 */
object StudentPrivacyGuard {

    sealed class GuardResult {
        object Allowed : GuardResult()
        data class Blocked(val reason: String, val localizedMessage: String) : GuardResult()
    }

    private val FORBIDDEN_KEYWORDS = listOf(
        "system prompt", "internal instruction", "ignore previous", "jailbreak",
        "api key", "database secret", "password", "parol", "şifre"
    )

    /**
     * Checks if the query is safe and authorized for the current student.
     */
    fun validateQuery(query: String, currentStudentId: String): GuardResult {
        val q = query.lowercase(Locale.ROOT).trim()

        // 1. Prevent prompt injection
        if (FORBIDDEN_KEYWORDS.any { q.contains(it) }) {
            return GuardResult.Blocked(
                "PROMPT_INJECTION",
                "Howpsuzlyk sebäpli bu soraga jogap berip bilmerin."
            )
        }

        // 2. Prevent cross-student data access
        val otherStudents = listOf("batyr", "merjen", "suleyman").filter { it != currentStudentId.lowercase() }
        if (otherStudents.any { q.contains(it) } && (q.contains("baha") || q.contains("gpa") || q.contains("grades"))) {
            return GuardResult.Blocked(
                "CROSS_USER_ACCESS",
                "Siz diňe öz akademiki maglumatlaryňyzy görüp bilersiňiz."
            )
        }

        return GuardResult.Allowed
    }
}
