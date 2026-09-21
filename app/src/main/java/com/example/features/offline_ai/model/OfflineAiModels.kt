package com.example.features.offline_ai.model

/**
 * Highly detailed, structured academic data models for offline reasoning.
 */

data class OfflineGradeComponents(
    val midtermScore: Double,
    val finalExamScore: Double,
    val assignmentScore: Double? = null,
    val courseworkScore: Double? = null,
    val labScore: Double? = null,
    val participationScore: Double? = null,
    val finalScore: Double,
    val letterGrade: String,
    val isPassed: Boolean = true
)

data class OfflineSubjectAttendance(
    val totalClasses: Int,
    val attendedClasses: Int,
    val absences: Int,
    val excusedAbsences: Int = 0,
    val unexcusedAbsences: Int = 0,
    val lateArrivals: Int = 0,
    val percentage: Double
)

data class OfflineSubjectExamRecord(
    val id: String,
    val type: String, // "Midterm", "Final", "Synag"
    val date: String,
    val time: String,
    val classroom: String,
    val status: String, // "Completed", "Upcoming"
    val score: Double? = null
)

data class OfflineSubjectRecord(
    val id: String,
    val code: String,
    val name: String,
    val credits: Int,
    val semesterNumber: Int,
    val academicYear: String,
    val teacherName: String,
    val teacherTitle: String,
    val teacherDepartment: String,
    val grades: OfflineGradeComponents,
    val attendance: OfflineSubjectAttendance,
    val exams: List<OfflineSubjectExamRecord> = emptyList(),
    val description: String = ""
)

data class OfflineSemesterRecord(
    val semesterId: String,
    val semesterNumber: Int,
    val academicYear: String,
    val yearNumber: Int,
    val season: String,
    val startDate: String,
    val endDate: String,
    val status: String,
    val totalSubjects: Int,
    val totalCredits: Int,
    val completedCredits: Int,
    val semesterGpa: Double,
    val semesterAverage: Double,
    val totalClassesHeld: Int,
    val totalAttended: Int,
    val totalAbsences: Int,
    val excusedAbsences: Int,
    val unexcusedAbsences: Int,
    val lateArrivals: Int,
    val subjects: List<OfflineSubjectRecord>,
    val weeklySchedule: List<OfflineScheduleRecord> = emptyList()
)

data class OfflineAcademicYearRecord(
    val yearNumber: Int,
    val academicYear: String,
    val semesters: List<OfflineSemesterRecord>
)

data class OfflineTeacherRecord(
    val id: String,
    val fullName: String,
    val department: String,
    val academicTitle: String,
    val subjectsTaught: List<String>,
    val semestersTaught: List<Int>,
    val isCurrentTeacher: Boolean
)

data class OfflineScheduleRecord(
    val id: String,
    val semesterNumber: Int = 0,
    val dayOfWeek: Int,
    val dayName: String,
    val startTime: String,
    val endTime: String,
    val subjectName: String,
    val subjectCode: String = "",
    val classroom: String,
    val teacherName: String,
    val lessonType: String
)

data class OfflineExamRecord(
    val id: String,
    val subjectName: String,
    val semesterNumber: Int,
    val dateIso: String,
    val displayDate: String,
    val time: String,
    val classroom: String,
    val type: String,
    val daysLeft: Int,
    val teacherName: String,
    val isCompleted: Boolean = false,
    val score: Double? = null
)

data class OfflineAnnouncementRecord(
    val id: String,
    val title: String,
    val date: String,
    val category: String,
    val content: String,
    val isImportant: Boolean
)

data class OfflineOverallAttendance(
    val totalClassesHeld: Int,
    val totalAttended: Int,
    val totalAbsences: Int,
    val excusedAbsences: Int,
    val unexcusedAbsences: Int,
    val lateArrivals: Int,
    val overallPercentage: Double
)

data class OfflineChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String,
    val tokensPerSec: Double = 0.0,
    val retrievedContextPreview: String? = null
)

enum class ModelInferenceState {
    IDLE,
    LOADING_WEIGHTS,
    RETRIEVING_CONTEXT,
    GENERATING,
    READY
}

data class OfflineStudentData(
    val userId: String,
    val fullName: String,
    val username: String,
    val studentCardNumber: String,
    val faculty: String,
    val department: String,
    val major: String,
    val degree: String,
    val group: String,
    val academicYear: String,
    val currentYear: Int,
    val currentSemester: Int,
    val enrollmentDate: String,
    val academicStatus: String,
    val email: String,
    val language: String,
    val academicYears: List<OfflineAcademicYearRecord>,
    val allSemesters: List<OfflineSemesterRecord>,
    val cumulativeGpa: Double,
    val totalCreditsEarned: Int,
    val totalCreditsRequired: Int,
    val currentSemesterCredits: Int,
    val completedCoursesCount: Int,
    val attendanceSummary: OfflineOverallAttendance,
    val thesisTopic: String?,
    val academicAdvisor: String,
    val honorsAchievements: List<String>,
    val schedule: List<OfflineScheduleRecord>,
    val teacherHistory: List<OfflineTeacherRecord>,
    val exams: List<OfflineExamRecord>,
    val announcements: List<OfflineAnnouncementRecord>,
    val isDemoData: Boolean = true
)
