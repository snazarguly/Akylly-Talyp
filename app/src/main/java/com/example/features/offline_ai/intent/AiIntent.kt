package com.example.features.offline_ai.intent

/**
 * Supported intent categories for the Offline AI.
 * Represents the primary goal of the user's query.
 */
enum class AiIntent {
    STUDENT_PROFILE,
    GET_CUMULATIVE_GPA,
    GET_SEMESTER_GPA,
    GET_SUBJECT_GRADE,
    GET_SUBJECT_DETAILS,
    GET_PASSED_SUBJECTS,
    GET_FAILED_SUBJECTS,
    GET_BEST_GRADE,
    GET_WORST_GRADE,
    GET_CREDITS,
    GET_ATTENDANCE,
    GET_SUBJECT_ATTENDANCE,
    GET_SCHEDULE,
    GET_TODAY_SCHEDULE,
    GET_TOMORROW_SCHEDULE,
    GET_NEXT_CLASS,
    GET_SCHEDULE_BY_DATE,
    GET_EXAMS,
    GET_NEXT_EXAM,
    GET_TEACHER,
    GET_SUBJECT_TEACHER,
    GET_ROOM,
    GET_SEMESTER_INFO,
    GET_ACADEMIC_YEAR,
    HNGU_KNOWLEDGE,
    GREETING,
    CLEAR_HISTORY,
    UNKNOWN
}
