package com.example.core.model

data class StudentProfile(
    val id: String = "std-2022-0481",
    val firstName: String = "Süleýman",
    val lastName: String = "Nazargulyýew",
    val patronymic: String = "Saparmyradowiç",
    val fullName: String = "Süleýman Nazargulyýew Saparmyradowiç",
    val faculty: String = "Sanly tehnologiýalar we kiberhowpsuzlyk fakulteti",
    val major: String = "Maglumat ulgamlary we tehnologiýalary",
    val degree: String = "Bakalawr",
    val year: Int = 4,
    val group: String = "B topary",
    val studentCardNumber: String = "HNGU-2022-8419",
    val email: String = "s.nazargulyyew@iogu.edu.tm",
    val cumulativeGpa: Double = 92.4,
    val totalCreditsEarned: Int = 184,
    val currentSemesterCredits: Int = 28,
    val completedCoursesCount: Int = 34,
    val isDemo: Boolean = true
)

enum class LessonType {
    LECTURE,
    PRACTICE,
    LAB
}

data class ScheduleItem(
    val id: String,
    val dayOfWeek: Int, // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val startTime: String,
    val endTime: String,
    val subjectName: String,
    val classroom: String,
    val teacherName: String,
    val type: LessonType,
    val group: String = "B topary",
    val faculty: String = "Sanly tehnologiýalar we kiberhowpsuzlyk fakulteti",
    val year: Int = 4,
    val isDemo: Boolean = true
)

data class SubjectItem(
    val id: String,
    val code: String,
    val name: String,
    val teacherName: String,
    val credits: Int,
    val totalHours: Int,
    val semester: Int,
    val currentGrade: Double,
    val midtermScore: Double,
    val finalScore: Double,
    val examScore: Double,
    val isPassed: Boolean = true,
    val description: String = "",
    val isDemo: Boolean = true
)

enum class ExamType {
    MIDTERM,
    FINAL
}

data class ExamItem(
    val id: String,
    val subjectName: String,
    val dateIso: String, // YYYY-MM-DD e.g. 2026-09-20
    val displayDate: String, // 20 Sentýabr, 2026
    val time: String, // 10:00
    val classroom: String, // 205-nji otag
    val type: ExamType,
    val daysLeft: Int,
    val teacherName: String,
    val isPast: Boolean = false,
    val isDemo: Boolean = true
)

data class AnnouncementItem(
    val id: String,
    val title: String,
    val date: String,
    val category: String, // Uniwersitet, Fakultet, Okuw, Ekzamen, Çäreler, Möhüm
    val shortDescription: String,
    val fullContent: String,
    val isImportant: Boolean = false,
    val author: String = "HNGU Rektoraty",
    val isDemo: Boolean = true
)

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: String,
    val targetScreen: String, // "schedule", "exams", "announcements", "grades"
    val isRead: Boolean = false,
    val iconType: String = "academic"
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String,
    val relatedCards: List<String> = emptyList()
)

data class SemesterProgress(
    val semesterNumber: Int,
    val semesterLabel: String,
    val gpa: Double
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String = ""
)

data class QuizSet(
    val id: String,
    val subjectName: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val questions: List<QuizQuestion>
)
