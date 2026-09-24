package com.example.features.offline_ai.model

/**
 * Highly detailed, structured academic data models for offline reasoning.
 * Preserves strict relational hierarchies:
 * Student -> Academic Year -> Semester -> Subject -> Teacher + Grade Components + Attendance + Exams
 *
 * All semester GPAs, cumulative GPAs, and attendance totals are mathematically calculated
 * from the underlying subject-level records.
 *
 * NOTE: Marked as DEMO DATA — NOT REAL UNIVERSITY DATA.
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
) {
    // Convenience backward-compatibility properties
    val currentGrade: Double get() = grades.finalScore
    val midtermScore: Double get() = grades.midtermScore
    val finalScore: Double get() = grades.finalExamScore
    val examScore: Double get() = grades.finalExamScore
    val isPassed: Boolean get() = grades.isPassed
    val attendedClasses: Int get() = attendance.attendedClasses
    val absences: Int get() = attendance.absences
    val lateArrivals: Int get() = attendance.lateArrivals
    val totalHours: Int get() = attendance.totalClasses
    val semester: Int get() = semesterNumber
}

data class OfflineSemesterRecord(
    val semesterId: String,
    val semesterNumber: Int,
    val academicYear: String,
    val yearNumber: Int,
    val season: String, // "Güýz" (Fall) or "Bahar" (Spring)
    val startDate: String,
    val endDate: String,
    val status: String, // "Completed", "In Progress", "Upcoming"
    val totalSubjects: Int,
    val totalCredits: Int,
    val completedCredits: Int,
    val semesterGpa: Double, // Credit-weighted calculation
    val semesterAverage: Double, // Simple average
    val totalClassesHeld: Int,
    val totalAttended: Int,
    val totalAbsences: Int,
    val excusedAbsences: Int,
    val unexcusedAbsences: Int,
    val lateArrivals: Int,
    val subjects: List<OfflineSubjectRecord>,
    val weeklySchedule: List<OfflineScheduleRecord> = emptyList()
) {
    val semesterAbsences: Int get() = totalAbsences
}

data class OfflineAcademicYearRecord(
    val yearNumber: Int,
    val academicYear: String,
    val semesters: List<OfflineSemesterRecord>
)

data class OfflineTeacherRecord(
    val id: String,
    val fullName: String,
    val department: String,
    val academicTitle: String, // Docent, Professor, Uly Mugallym
    val subjectsTaught: List<String>,
    val semestersTaught: List<Int>,
    val isCurrentTeacher: Boolean
)

data class OfflineScheduleRecord(
    val id: String,
    val semesterNumber: Int = 0,
    val dayOfWeek: Int, // 1=Duşenbe, 2=Sişenbe, 3=Çarşenbe, 4=Penşenbe, 5=Anna, 6=Şenbe
    val dayName: String,
    val startTime: String,
    val endTime: String,
    val subjectName: String,
    val subjectCode: String = "",
    val classroom: String,
    val teacherName: String,
    val lessonType: String // Leksiýa, Tejribe, Laboratoriýa, Amaly
)

data class OfflineExamRecord(
    val id: String,
    val subjectName: String,
    val semesterNumber: Int,
    val dateIso: String,
    val displayDate: String,
    val time: String,
    val classroom: String,
    val type: String, // Final, Midterm, Synag
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

/**
 * Root student data container containing the complete multi-year hierarchical academic tree.
 */
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
) {
    val currentSemesterSubjects: List<OfflineSubjectRecord>
        get() = allSemesters.find { it.semesterNumber == currentSemester }?.subjects
            ?: allSemesters.lastOrNull()?.subjects ?: emptyList()

    val pastSemesters: List<OfflineSemesterRecord>
        get() = allSemesters.filter { it.status == "Completed" }
}
