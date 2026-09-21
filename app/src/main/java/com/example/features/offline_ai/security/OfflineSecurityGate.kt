package com.example.features.offline_ai.security

import com.example.features.offline_ai.model.OfflineStudentData
import java.util.Locale

/**
 * Pre-LLM Programmatic Security & Privacy Enforcement Gate.
 *
 * CRITICAL ARCHITECTURE RULE:
 * This verification occurs BEFORE any student data or user queries are passed to the LLM.
 * Unauthorized requests (accessing other students' private data, credentials, system prompts, API keys)
 * are blocked programmatically and NEVER passed to the LLM or saved as unknown questions.
 */
object OfflineSecurityGate {

    sealed class SecurityCheckResult {
        object Allowed : SecurityCheckResult()
        data class Blocked(
            val message: String,
            val reason: BlockReason
        ) : SecurityCheckResult()
    }

    enum class BlockReason {
        OTHER_STUDENT_DATA_ACCESS,
        SYSTEM_PROMPT_OR_SECRETS_ACCESS,
        CREDENTIALS_OR_PASSWORDS_ACCESS
    }

    // Known student names in demo dataset that must not be cross-accessed
    private val KNOWN_OTHER_STUDENT_NAMES = listOf(
        "merjen", "merjen_m", "merjen myradowa",
        "batyr", "batyr_a", "batyr ataýew", "batyr atayew",
        "suleyman", "suleyman_05", "süleýman", "süleýman gurbanow", "suleyman gurbanow"
    )

    /**
     * Inspects the incoming user query against security and privacy policies.
     * Enforces strict user authorization programmatically.
     */
    fun checkQuery(
        userQuery: String,
        currentUserId: String,
        currentStudent: OfflineStudentData?,
        languageCode: String
    ): SecurityCheckResult {
        val q = userQuery.lowercase(Locale.ROOT).trim()

        val isRussian = languageCode == "ru" || q.contains("покажи") || q.contains("пароль") || q.contains("оценки") || q.contains("студент")
        val isEnglish = languageCode == "en" || q.contains("show") || q.contains("password") || q.contains("grades of") || q.contains("other student")
        val isTurkish = languageCode == "tr" || q.contains("şifre") || q.contains("göster") || q.contains("notları") || q.contains("başka öğrenci")

        // 1. Check for System Secrets, API Keys, Passwords, System Prompts, Database Dumps
        if (isSystemSecretsAttempt(q)) {
            val response = when {
                isRussian -> "Из соображений безопасности системы внутренние инструкции, конфигурации, ключи API и конфиденциальные данные защищены и недоступны."
                isEnglish -> "For system security reasons, internal system instructions, configuration, API keys, and database secrets cannot be disclosed."
                isTurkish -> "Sistem güvenliği nedeniyle dahili yönergeler, yapılandırmalar, API anahtarları ve veritabanı sırları erişime kapalıdır."
                else -> "Ulgam howpsuzlygy sebäpli içerki konfigurasiýalar, açarlar, parollar we howpsuzlyk maglumatlary elýeterli däl."
            }
            return SecurityCheckResult.Blocked(response, BlockReason.SYSTEM_PROMPT_OR_SECRETS_ACCESS)
        }

        // 2. Check for Unauthorized Access to Other Students' Private Information
        if (isOtherStudentDataAccessAttempt(q, currentUserId, currentStudent)) {
            val response = when {
                isRussian -> "Эта информация является конфиденциальной и недоступна для вашего аккаунта. Я не могу показывать или передавать личные данные других пользователей."
                isEnglish -> "This information is private and is not available to your account. I cannot reveal or share another user's personal information."
                isTurkish -> "Bu bilgi özel ve sizin erişiminize açık değil. Başka kullanıcıların kişisel bilgilerini gösteremem veya paylaşamam."
                else -> "Bu maglumat şahsy we size elýeterli däl. Men başga ulanyjylaryň şahsy maglumatlaryny görkezip ýa-da paýlaşyp bilmeýärin."
            }
            return SecurityCheckResult.Blocked(response, BlockReason.OTHER_STUDENT_DATA_ACCESS)
        }

        return SecurityCheckResult.Allowed
    }

    private fun isSystemSecretsAttempt(q: String): Boolean {
        val secretKeywords = listOf(
            "system prompt", "system instruction", "system instructions", "ignore previous instructions",
            "internal prompt", "developer mode", "jailbreak",
            "api key", "apikey", "api_key", "secret key", "secret_key",
            "password", "passwords", "parol", "parollar", "şifre", "şifreler", "пароль", "пароли",
            "token", "bearer token", "auth token", "credential", "credentials",
            "database dump", "drop table", "select * from users", "database contents",
            "internal security", "hidden instructions", "içerki instruksiýa"
        )
        return secretKeywords.any { q.contains(it) }
    }

    private fun isOtherStudentDataAccessAttempt(
        q: String,
        currentUserId: String,
        currentStudent: OfflineStudentData?
    ): Boolean {
        // Generic queries asking for other students' private data
        val genericOtherPatterns = listOf(
            "başga talyp", "başga biriniň", "özgäniň", "başga ulanyjy",
            "other student", "another student", "other students", "another user", "someone else",
            "другой студент", "другого студента", "чужие оценки", "другой пользователь",
            "başka öğrenci", "başka birinin", "diğer öğrenci"
        )

        val askingForPrivateField = listOf(
            "baha", "bahasy", "bahalary", "gpa", "bal", "gatnaşyk", "gelmedik", "tertibi", "karta", "belgi",
            "grade", "grades", "score", "attendance", "schedule", "profile",
            "оценк", "балл", "посещаемость", "расписание", "профиль",
            "not", "notlar", "puan", "devamsızlık", "program"
        )

        for (pattern in genericOtherPatterns) {
            if (q.contains(pattern)) {
                // If query is asking about another student's grades/attendance/profile
                if (askingForPrivateField.any { q.contains(it) }) {
                    return true
                }
            }
        }

        // Specific other student checks
        val currentUsername = currentUserId.lowercase(Locale.ROOT)
        val currentFullName = currentStudent?.fullName?.lowercase(Locale.ROOT) ?: ""

        // Check if query explicitly mentions another known student
        for (name in KNOWN_OTHER_STUDENT_NAMES) {
            if (q.contains(name)) {
                // If it is NOT the currently logged-in student
                val isSelf = currentUsername.contains(name) || currentFullName.contains(name)
                if (!isSelf) {
                    // Unauthorized attempt to see another named student's data
                    return true
                }
            }
        }

        // Check student card number patterns (e.g., student card of another student like 2022-...)
        if (q.contains("talyp kart") || q.contains("student card") || q.contains("студенческ") || q.contains("öğrenci kartı")) {
            if (q.contains("başga") || q.contains("other") || q.contains("друго") || q.contains("başka")) {
                return true
            }
        }

        return false
    }
}
