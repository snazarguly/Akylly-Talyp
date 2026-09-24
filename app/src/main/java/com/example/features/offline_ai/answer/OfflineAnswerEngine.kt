package com.example.features.offline_ai.answer

import com.example.features.offline_ai.analyzer.OfflineQuestionAnalyzer
import com.example.features.offline_ai.intent.AiIntent
import com.example.features.offline_ai.retrieval.LocalRetrievalEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale

/**
 * Component responsible for synthesizing a final human-friendly response
 * based on the retrieved structured data and detected intent.
 */
object OfflineAnswerEngine {

    fun generateResponseStream(
        analyzed: OfflineQuestionAnalyzer.AnalyzedQuery,
        retrieval: LocalRetrievalEngine.RetrievalResult
    ): Flow<String> = flow {
        val response = synthesize(analyzed, retrieval)
        
        // Simulating a fast-typing effect for better UX
        val tokens = response.split(Regex("(?<=\\s)|(?<=[.,!?])"))
        val buffer = StringBuilder()
        for (token in tokens) {
            buffer.append(token)
            emit(buffer.toString())
            delay(10) // Fast 10ms delay
        }
    }

    private fun synthesize(
        analyzed: OfflineQuestionAnalyzer.AnalyzedQuery,
        retrieval: LocalRetrievalEngine.RetrievalResult
    ): String {
        val lang = analyzed.languageCode
        val intent = retrieval.intent
        val context = retrieval.contextSummary

        // 1. Blocked or Restricted by Security (Logic moved to Service, but safety check here)
        if (context.contains("SecurityCheckResult.Blocked")) {
            return context // Return the block message directly
        }

        // 2. Intent-Based Synthesis
        return when (intent) {
            AiIntent.GREETING -> {
                when (lang) {
                    "ru" -> "Здравствуйте! Я ваш офлайн-помощник в ХНГУ. Чем могу помочь?"
                    "en" -> "Hello! I am your offline assistant at IOGU. How can I help you?"
                    else -> "Salam! Men Ýagşygeldi Kakaýew adyndaky HNGU-nyň awtonom kömekçisisiňiz. Size näme kömek edip bilerin?"
                }
            }

            AiIntent.GET_CUMULATIVE_GPA -> {
                val gpaMatch = Regex("Umumy kumulýatiw GPA: (\\d+\\.?\\d*)%").find(context)
                val gpa = gpaMatch?.groupValues?.get(1) ?: "0.0"
                when (lang) {
                    "ru" -> "Ваш общий кумулятивный GPA составляет $gpa%."
                    "en" -> "Your overall cumulative GPA is $gpa%."
                    else -> "Siziň umumy kumulýatiw GPA görkezijiňiz $gpa%."
                }
            }

            AiIntent.GET_TODAY_SCHEDULE -> {
                if (!context.contains("SAPAK TERTIBI")) return getUnknownMessage(lang)
                when (lang) {
                    "ru" -> "Вот ваше расписание на сегодня:\n\n" + formatList(context)
                    "en" -> "Here is your schedule for today:\n\n" + formatList(context)
                    else -> "Siziň şu günki sapak tertibiňiz:\n\n" + formatList(context)
                }
            }

            AiIntent.HNGU_KNOWLEDGE -> {
                if (retrieval.generalKnowledgeMatch != null) {
                    retrieval.generalKnowledgeMatch.formattedContent
                } else {
                    getUnknownMessage(lang)
                }
            }

            AiIntent.UNKNOWN -> getUnknownMessage(lang)

            else -> {
                // Generic handler for other intents
                if (retrieval.retrievedItemsCount > 0) {
                    when (lang) {
                        "ru" -> "Согласно вашим данным:\n\n$context"
                        "en" -> "Based on your academic records:\n\n$context"
                        else -> "Siziň akademiki maglumatlaryňyza görä:\n\n$context"
                    }
                } else {
                    getUnknownMessage(lang)
                }
            }
        }
    }

    private fun formatList(context: String): String {
        return context.lines()
            .filter { it.startsWith("- ") }
            .joinToString("\n")
    }

    private fun getUnknownMessage(lang: String): String {
        return when (lang) {
            "ru" -> "К сожалению, я не нашел точной информации по этому вопросу в вашей офлайн-базе."
            "en" -> "I'm sorry, I couldn't find exact information for this question in your offline database."
            else -> "Bagyşlaň, bu sorag boýunça häzirki offline maglumatlarymdan takyk jogap tapyp bilmedim."
        }
    }
}
