package com.example.features.offline_ai.intent

import com.example.features.offline_ai.analyzer.OfflineQuestionAnalyzer
import java.util.Locale

/**
 * Component responsible for mapping a normalized query to an [AiIntent].
 * Uses pattern matching and keyword density.
 */
object OfflineIntentDetector {

    fun detect(analyzed: OfflineQuestionAnalyzer.AnalyzedQuery, historyContext: String): AiIntent {
        val q = analyzed.normalized
        val tokens = analyzed.tokens

        // 1. Greetings
        if (isGreeting(q)) return AiIntent.GREETING

        // 2. GPA & Grades
        if (q.contains("gpa") || q.contains("ortaça") || q.contains("ortaca")) {
            if (q.contains("umumy") || tokens.size < 3) return AiIntent.GET_CUMULATIVE_GPA
            return AiIntent.GET_SEMESTER_GPA
        }
        
        if (q.contains("baha") || q.contains("bal") || q.contains("score") || q.contains("оценк")) {
            if (q.contains("iň ýokary") || q.contains("highest") || q.contains("iň gowy")) return AiIntent.GET_BEST_GRADE
            if (q.contains("iň pes") || q.contains("lowest")) return AiIntent.GET_WORST_GRADE
            if (q.contains("geçmedik") || q.contains("failed") || q.contains("bergä")) return AiIntent.GET_FAILED_SUBJECTS
            if (q.contains("geçen") || q.contains("passed")) return AiIntent.GET_PASSED_SUBJECTS
            return AiIntent.GET_SUBJECT_GRADE
        }

        // 3. Schedule
        if (q.contains("sapak") || q.contains("tertibi") || q.contains("schedule") || q.contains("timetable") || q.contains("расписан")) {
            if (q.contains("şu gün") || q.contains("today") || q.contains("sugun")) return AiIntent.GET_TODAY_SCHEDULE
            if (q.contains("ertir") || q.contains("tomorrow")) return AiIntent.GET_TOMORROW_SCHEDULE
            if (q.contains("indiki") || q.contains("next")) return AiIntent.GET_NEXT_CLASS
            return AiIntent.GET_SCHEDULE
        }

        // 4. Exams
        if (q.contains("synag") || q.contains("ekzamen") || q.contains("exam") || q.contains("экзамен")) {
            if (q.contains("indiki") || q.contains("ýakyn") || q.contains("next")) return AiIntent.GET_NEXT_EXAM
            return AiIntent.GET_EXAMS
        }

        // 5. Attendance
        if (q.contains("gatnaşyk") || q.contains("attendance") || q.contains("посещаем") || q.contains("gelmedik")) {
            if (tokens.size > 3) return AiIntent.GET_SUBJECT_ATTENDANCE
            return AiIntent.GET_ATTENDANCE
        }

        // 6. Profile & Identity
        if (q.contains("men kim") || q.contains("profil") || q.contains("maglumatym") || q.contains("who am i")) {
            return AiIntent.STUDENT_PROFILE
        }

        // 7. Teacher
        if (q.contains("mugallym") || q.contains("teacher") || q.contains("professor") || q.contains("prepodawatel")) {
            return AiIntent.GET_TEACHER
        }

        // 8. Knowledge Base (Fallback to HNGU info if general keywords are found)
        if (q.contains("näme") || q.contains("what is") || q.contains("how to") || q.contains("düzgüni") || q.contains("hngu")) {
            return AiIntent.HNGU_KNOWLEDGE
        }

        // 9. Context Inheritance (if ambiguous)
        if (isAmbiguous(analyzed)) {
            val hist = historyContext.lowercase(Locale.ROOT)
            return when {
                hist.contains("gpa") -> AiIntent.GET_CUMULATIVE_GPA
                hist.contains("baha") -> AiIntent.GET_SUBJECT_GRADE
                hist.contains("sapak") -> AiIntent.GET_TODAY_SCHEDULE
                else -> AiIntent.UNKNOWN
            }
        }

        return AiIntent.UNKNOWN
    }

    private fun isGreeting(q: String): Boolean {
        val greetings = listOf("salam", "salowmaleýkim", "hello", "hi", "привет", "здравствуй")
        return greetings.any { q.startsWith(it) || q == it }
    }

    private fun isAmbiguous(analyzed: OfflineQuestionAnalyzer.AnalyzedQuery): Boolean {
        val q = analyzed.normalized
        return q.length < 15 && (q.contains("nähili") || q.contains("näçe") || q.contains("haýsy") || q.contains("how"))
    }
}
