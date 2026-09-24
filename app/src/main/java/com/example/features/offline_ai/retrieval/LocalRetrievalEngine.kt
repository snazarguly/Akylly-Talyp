package com.example.features.offline_ai.retrieval

import com.example.features.offline_ai.analyzer.OfflineQuestionAnalyzer
import com.example.features.offline_ai.data.db.PersonalKnowledgeEntity
import com.example.features.offline_ai.intent.AiIntent
import com.example.features.offline_ai.knowledge.GeneralHnguKnowledge
import com.example.features.offline_ai.model.OfflineStudentData
import java.util.Locale

/**
 * Enhanced Intelligent Retrieval Engine.
 * Instead of raw text matching, it uses the detected [AiIntent] to gather specific, 
 * high-granularity data from the student's records and the university knowledge base.
 */
class LocalRetrievalEngine {

    data class RetrievalResult(
        val intent: AiIntent,
        val contextSummary: String,
        val retrievedItemsCount: Int,
        val personalKnowledgeMatches: List<PersonalKnowledgeEntity> = emptyList(),
        val generalKnowledgeMatch: GeneralHnguKnowledge.GeneralKnowledgeMatch? = null
    )

    /**
     * Orchestrates the retrieval based on the detected intent.
     */
    fun retrieve(
        analyzed: OfflineQuestionAnalyzer.AnalyzedQuery,
        intent: AiIntent,
        studentData: OfflineStudentData,
        personalKnowledgeList: List<PersonalKnowledgeEntity> = emptyList(),
        generalKnowledgeMatch: GeneralHnguKnowledge.GeneralKnowledgeMatch? = null
    ): RetrievalResult {
        val sb = StringBuilder()
        var itemsCount = 0

        // Always include basic profile for any academic intent
        sb.append("[TALYP: ${studentData.fullName} | ID: ${studentData.studentCardNumber}]\n")
        sb.append("[HÄZIRKI ÝAGDAÝ: ${studentData.currentYear}-nji ýyl, ${studentData.currentSemester}-nji semestr]\n\n")

        when (intent) {
            AiIntent.GET_CUMULATIVE_GPA -> {
                sb.append("GPA MAGLUMATY:\n")
                sb.append("- Umumy kumulýatiw GPA: ${studentData.cumulativeGpa}%\n")
                studentData.allSemesters.forEach { sem ->
                    sb.append("- Semestr ${sem.semesterNumber}: ${sem.semesterGpa}%\n")
                }
                itemsCount++
            }
            
            AiIntent.GET_TODAY_SCHEDULE, AiIntent.GET_TOMORROW_SCHEDULE, AiIntent.GET_SCHEDULE -> {
                sb.append("SAPAK TERTIBI:\n")
                studentData.schedule.forEach { s ->
                    sb.append("- ${s.dayName} (${s.startTime}-${s.endTime}): ${s.subjectName} [${s.lessonType}], Otag: ${s.classroom}, Mugallym: ${s.teacherName}\n")
                }
                itemsCount += studentData.schedule.size
            }

            AiIntent.GET_SUBJECT_GRADE -> {
                sb.append("DERS BAHALARY:\n")
                studentData.allSemesters.flatMap { it.subjects }.forEach { sub ->
                    sb.append("- ${sub.name}: ${sub.grades.finalScore}% [${sub.grades.letterGrade}]\n")
                }
                itemsCount += 5
            }

            AiIntent.GET_ATTENDANCE -> {
                val att = studentData.attendanceSummary
                sb.append("GATNAŞYK JEMLEMESI:\n")
                sb.append("- Umumy gatnaşyk: ${att.overallPercentage}%\n")
                sb.append("- Jemi gelmedik sagatlar: ${att.totalAbsences} (Sebäpli: ${att.excusedAbsences}, Sebäpsiz: ${att.unexcusedAbsences})\n")
                itemsCount++
            }

            AiIntent.GET_EXAMS, AiIntent.GET_NEXT_EXAM -> {
                sb.append("EKZAMENLER:\n")
                studentData.exams.forEach { e ->
                    sb.append("- ${e.subjectName}: ${e.displayDate}, ${e.time}, Otag: ${e.classroom}, Mugallym: ${e.teacherName}\n")
                }
                itemsCount += studentData.exams.size
            }

            AiIntent.STUDENT_PROFILE -> {
                sb.append("DOLY PROFIL:\n")
                sb.append("- Fakultet: ${studentData.faculty}\n")
                sb.append("- Hünär: ${studentData.major}\n")
                sb.append("- Topar: ${studentData.group}\n")
                sb.append("- Toplanan kreditler: ${studentData.totalCreditsEarned} / ${studentData.totalCreditsRequired}\n")
                itemsCount++
            }

            AiIntent.HNGU_KNOWLEDGE -> {
                if (generalKnowledgeMatch != null) {
                    sb.append("UNIWERSITET MAGLUMATY:\n")
                    sb.append(generalKnowledgeMatch.formattedContent)
                    itemsCount++
                }
            }

            else -> {
                // For UNKNOWN or others, include a bit of everything to allow some reasoning
                sb.append("UMUMY AKADEMIKI KONTEKST:\n")
                sb.append("- GPA: ${studentData.cumulativeGpa}%\n")
                sb.append("- Gatnaşyk: ${studentData.attendanceSummary.overallPercentage}%\n")
            }
        }

        // Add personal learned knowledge if any
        if (personalKnowledgeList.isNotEmpty()) {
            sb.append("\nÝERLI ÝATDA SAKLANANLAR:\n")
            personalKnowledgeList.forEach { pk ->
                sb.append("- Sorag: ${pk.question}\n  Jogap: ${pk.answer}\n")
            }
        }

        return RetrievalResult(
            intent = intent,
            contextSummary = sb.toString().trim(),
            retrievedItemsCount = itemsCount,
            personalKnowledgeMatches = personalKnowledgeList,
            generalKnowledgeMatch = generalKnowledgeMatch
        )
    }
}
