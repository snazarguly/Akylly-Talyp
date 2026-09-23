package com.example.features.offline_ai.engine

import com.example.core.util.CampusCalendar
import com.example.features.offline_ai.knowledge.GeneralHnguKnowledge
import com.example.features.offline_ai.model.OfflineStudentData
import com.example.features.offline_ai.model.OfflineSubjectRecord
import java.util.Calendar
import java.util.Locale

/**
 * Deterministic on-device answer engine used while no local LLM is loaded.
 *
 * Answers only from the current student's own record ([OfflineStudentData]) and the shared
 * [GeneralHnguKnowledge] base, so it never invents facts. Anything it cannot answer gets the
 * fixed fallback from [unknownFallback]. Access control happens before this is called
 * (see OfflineSecurityGate).
 */
object OfflineAnswerEngine {

    const val DEMO_WATERMARK = "DEMO DATA — NOT REAL UNIVERSITY DATA"

    private val UNKNOWN_FALLBACKS = mapOf(
        "tk" to "Bagyşlaň, bu soraga offline režimde jogap berip bilemok. Internet birikmesi bolanda Online AI-dan soraň.",
        "ru" to "Извините, я не могу ответить на этот вопрос в offline режиме. Спросите Online AI, когда появится интернет.",
        "en" to "Sorry, I can't answer this question in offline mode. Please ask the Online AI when you are connected."
    )

    fun isUnknownFallback(text: String): Boolean = text in UNKNOWN_FALLBACKS.values

    fun answer(
        query: String,
        student: OfflineStudentData,
        languageCode: String,
        now: Calendar = Calendar.getInstance()
    ): String {
        val q = query.lowercase(Locale.ROOT).trim()
        val lang = when {
            q.any { it in 'Ѐ'..'ӿ' } -> "ru"
            languageCode == "ru" || languageCode == "en" -> languageCode
            else -> "tk"
        }
        val t = Labels(lang)
        val searchText = expandSynonyms(q)

        // Rule/policy questions ("How does the GPA system work?") belong to the general knowledge base,
        // even though they share keywords with personal questions ("What is my GPA?").
        if (RULE_MARKERS.any { q.contains(it) }) {
            GeneralHnguKnowledge.findMatch(query, lang)?.let { return it.formattedContent }
        }

        val personal = when {
            isGreeting(q) -> return t.greeting(student.fullName)
            MISSED_MOST_MARKERS.any { q.contains(it) } -> mostMissedAnswer(student, t)
            ATTENDANCE_MARKERS.any { q.contains(it) } -> attendanceAnswer(student, t)
            GRADE_MARKERS.any { q.contains(it) } -> gradesAnswer(searchText, student, t)
            EXAM_MARKERS.any { q.contains(it) } -> examsAnswer(student, t, now)
            SCHEDULE_MARKERS.any { q.contains(it) } -> scheduleAnswer(q, student, t, now)
            TEACHER_MARKERS.any { q.contains(it) } -> teachersAnswer(student, t)
            PROFILE_MARKERS.any { q.contains(it) } -> profileAnswer(student, t)
            else -> null
        }
        if (personal != null) return withWatermark(personal, student)

        GeneralHnguKnowledge.findMatch(query, lang)?.let { return it.formattedContent }

        // A bare subject name ("Kiberhowpsuzlyk esaslary") is treated as a question about that subject.
        val subjects = findSubjects(searchText, student)
        if (subjects.isNotEmpty()) return withWatermark(subjectDetails(subjects, t), student)

        return UNKNOWN_FALLBACKS.getValue(lang)
    }

    // ---------------------------------------------------------------------------------------------
    // Intents
    // ---------------------------------------------------------------------------------------------

    private fun gradesAnswer(searchText: String, student: OfflineStudentData, t: Labels): String {
        val requestedSemester = parseSemester(searchText)
        if (requestedSemester != null) {
            if (requestedSemester > student.currentSemester) {
                return t.futureSemester(requestedSemester, student.currentSemester)
            }
            val semester = student.allSemesters.firstOrNull { it.semesterNumber == requestedSemester }
                ?: return t.futureSemester(requestedSemester, student.currentSemester)
            val matchedInSemester = findSubjects(searchText, student).filter { it.semesterNumber == requestedSemester }
            if (matchedInSemester.isNotEmpty()) return subjectDetails(matchedInSemester, t)
            return semesterGrades(semester.semesterNumber, student, t)
        }

        val subjects = findSubjects(searchText, student)
        if (subjects.isNotEmpty()) return subjectDetails(subjects, t)

        return semesterGrades(student.currentSemester, student, t) +
            "\n\n${t.cumulativeGpa}: ${student.cumulativeGpa}% • ${t.credits}: ${student.totalCreditsEarned} / ${student.totalCreditsRequired}"
    }

    private fun semesterGrades(semesterNumber: Int, student: OfflineStudentData, t: Labels): String {
        val semester = student.allSemesters.first { it.semesterNumber == semesterNumber }
        val rows = semester.subjects.joinToString("\n") {
            "• ${it.name} (${it.code}): ${fmt(it.grades.finalScore)}% [${it.grades.letterGrade}] • ${it.credits} ${t.creditsShort}"
        }
        return "📊 ${t.semester} $semesterNumber (${semester.academicYear}, ${semester.season})\n\n$rows\n\n" +
            "Semestr GPA: ${semester.semesterGpa}% • ${t.credits}: ${semester.completedCredits} / ${semester.totalCredits}"
    }

    private fun subjectDetails(subjects: List<OfflineSubjectRecord>, t: Labels): String =
        subjects.sortedBy { it.semesterNumber }.joinToString("\n\n") { s ->
            val g = s.grades
            "📘 ${t.semester} ${s.semesterNumber} — ${s.name} (${s.code})\n" +
                "• ${t.finalGrade}: ${fmt(g.finalScore)}% [${g.letterGrade}]\n" +
                "• ${t.midterm}: ${fmt(g.midtermScore)} • ${t.exam}: ${fmt(g.finalExamScore)}\n" +
                "• ${t.teacher}: ${s.teacherName} (${s.teacherTitle})\n" +
                "• ${t.attendance}: ${s.attendance.attendedClasses}/${s.attendance.totalClasses} (${fmt(s.attendance.percentage)}%)"
        }

    private fun mostMissedAnswer(student: OfflineStudentData, t: Labels): String {
        val top = student.allSemesters.flatMap { it.subjects }
            .filter { it.attendance.absences > 0 }
            .sortedByDescending { it.attendance.absences }
            .take(3)
        if (top.isEmpty()) return t.noAbsences
        val rows = top.joinToString("\n") {
            "• ${it.name} (${t.semester} ${it.semesterNumber}): ${it.attendance.absences} ${t.absencesShort} " +
                "(${it.attendance.excusedAbsences} ${t.excused}, ${it.attendance.unexcusedAbsences} ${t.unexcused})"
        }
        return "📉 ${t.mostMissed}\n\n$rows"
    }

    private fun attendanceAnswer(student: OfflineStudentData, t: Labels): String {
        val a = student.attendanceSummary
        val current = student.allSemesters.firstOrNull { it.semesterNumber == student.currentSemester }
        val currentLine = current?.let {
            "\n• ${t.semester} ${it.semesterNumber}: ${it.totalAttended}/${it.totalClassesHeld}, ${it.totalAbsences} ${t.absencesShort}"
        } ?: ""
        return "✅ ${t.attendance}\n\n" +
            "• ${t.overall}: ${fmt(a.overallPercentage)}% (${a.totalAttended}/${a.totalClassesHeld})\n" +
            "• ${t.absencesShort}: ${a.totalAbsences} (${a.excusedAbsences} ${t.excused}, ${a.unexcusedAbsences} ${t.unexcused})" +
            currentLine
    }

    private fun examsAnswer(student: OfflineStudentData, t: Labels, now: Calendar): String {
        val upcoming = student.exams
            .mapNotNull { exam -> CampusCalendar.daysUntil(exam.dateIso, now)?.let { exam to it } }
            .filter { (exam, days) -> !exam.isCompleted && days >= 0 }
            .sortedBy { (_, days) -> days }
        if (upcoming.isEmpty()) return t.noUpcomingExams
        val rows = upcoming.joinToString("\n") { (e, days) ->
            "• ${e.subjectName} — ${e.displayDate}, ${e.time}, ${t.room} ${e.classroom} (${t.daysLeft(days)})"
        }
        return "📝 ${t.upcomingExams}\n\n$rows"
    }

    private fun scheduleAnswer(q: String, student: OfflineStudentData, t: Labels, now: Calendar): String {
        val today = CampusCalendar.todayDayOfWeek(now)
        val targetDay = when {
            TOMORROW_MARKERS.any { q.contains(it) } -> today % 7 + 1
            TODAY_MARKERS.any { q.contains(it) } -> today
            else -> null
        }
        val lessons = student.schedule
            .filter { targetDay == null || it.dayOfWeek == targetDay }
            .sortedWith(compareBy({ it.dayOfWeek }, { CampusCalendar.minutesOfDay(it.startTime) ?: 0 }))
        if (lessons.isEmpty()) return t.noClasses
        val rows = lessons.joinToString("\n") {
            "• ${it.dayName} ${it.startTime}-${it.endTime} — ${it.subjectName} (${it.lessonType}, ${t.room} ${it.classroom}, ${it.teacherName})"
        }
        return "📅 ${t.schedule}\n\n$rows"
    }

    private fun teachersAnswer(student: OfflineStudentData, t: Labels): String {
        val rows = student.teacherHistory.filter { it.isCurrentTeacher }.joinToString("\n") {
            "• ${it.fullName} (${it.academicTitle}, ${it.department})"
        }
        return "👨‍🏫 ${t.teachers}\n\n$rows"
    }

    private fun profileAnswer(s: OfflineStudentData, t: Labels): String =
        "👤 ${s.fullName}\n\n" +
            "• ${t.studentCard}: ${s.studentCardNumber}\n" +
            "• ${t.faculty}: ${s.faculty}\n" +
            "• ${t.major}: ${s.major}\n" +
            "• ${t.group}: ${s.group} • ${t.year}: ${s.currentYear} • ${t.semester}: ${s.currentSemester}\n" +
            "• ${t.cumulativeGpa}: ${s.cumulativeGpa}%\n" +
            "• ${t.advisor}: ${s.academicAdvisor}"

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

    private fun withWatermark(text: String, student: OfflineStudentData): String =
        if (student.isDemoData) "$text\n\n⚠️ $DEMO_WATERMARK" else text

    private fun isGreeting(q: String): Boolean =
        q.length < 25 && GREETINGS.any { q == it || q.startsWith("$it ") || q.startsWith("$it,") || q.startsWith("$it!") }

    private val SEMESTER_BEFORE = Regex("(\\d{1,2})\\s*-?\\s*(?:nji|njy|inji|ynji|unji|ünji|st|nd|rd|th|й|ой|ый)?\\s*(?:semest|семест)")
    private val SEMESTER_AFTER = Regex("(?:semestr|semester|семестр)\\w*\\s*(\\d{1,2})")

    private fun parseSemester(q: String): Int? =
        (SEMESTER_BEFORE.find(q) ?: SEMESTER_AFTER.find(q))?.groupValues?.get(1)?.toIntOrNull()

    /** Subjects whose distinctive name words appear in the query, across all available semesters. */
    private fun findSubjects(q: String, student: OfflineStudentData): List<OfflineSubjectRecord> =
        student.allSemesters.flatMap { it.subjects }.filter { subject ->
            subject.code.lowercase(Locale.ROOT) in q ||
                subject.name.lowercase(Locale.ROOT)
                    .split(' ', '(', ')', '/', '-', ',')
                    .filter { it.length >= 6 && it !in SUBJECT_STOP_WORDS }
                    .any { word -> q.contains(word.take(8)) }
        }

    private fun expandSynonyms(q: String): String =
        SYNONYMS.entries.fold(q) { acc, (foreign, turkmen) -> if (acc.contains(foreign)) "$acc $turkmen" else acc }

    private fun fmt(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.ROOT, "%.1f", value)

    private val GREETINGS = listOf("salam", "salom", "hello", "hi", "hey", "привет", "здравствуйте")
    private val RULE_MARKERS = listOf("düzgün", "ulgam", "nähili işleýär", "rule", "policy", "system", "правил", "систем")
    private val MISSED_MOST_MARKERS = listOf("iň köp gatnaşmadyk", "iň köp gelmedik", "iň köp propusk", "miss the most", "missed the most", "most missed", "больше всего пропуст")
    private val ATTENDANCE_MARKERS = listOf("gatnaşyk", "gelmedik", "propusk", "attendance", "absence", "посещаем", "пропуск")
    private val GRADE_MARKERS = listOf("baha", "balym", "ballar", "gpa", "aldym", "grade", "score", "did i get", "mark", "оценк", "балл", "получил")
    private val EXAM_MARKERS = listOf("ekzamen", "synag", "exam", "экзамен", "зачет", "сесси")
    private val SCHEDULE_MARKERS = listOf("sapak", "tertip", "raspisan", "schedule", "timetable", "class", "lesson", "расписан", "заняти", "пара")
    private val TEACHER_MARKERS = listOf("mugallym", "teacher", "professor", "instructor", "преподавател", "учител")
    private val PROFILE_MARKERS = listOf("men kim", "profil", "maglumatym", "who am i", "profile", "кто я", "профиль")
    private val TODAY_MARKERS = listOf("şu gün", "şugün", "bu gün", "today", "сегодня")
    private val TOMORROW_MARKERS = listOf("ertir", "tomorrow", "завтра")
    private val SUBJECT_STOP_WORDS = setOf("esaslary", "esaslar", "boýunça", "tejribeligi", "programmalar")
    private val SYNONYMS = mapOf(
        "mathemat" to "matematika", "math" to "matematika", "математик" to "matematika",
        "physics" to "fizika", "физик" to "fizika",
        "chemistry" to "himiki", "химия" to "himiki",
        "english" to "iňlis", "английск" to "iňlis",
        "programming" to "programmirleme", "программир" to "programmirleme",
        "cybersecurity" to "kiberhowpsuzlyk", "кибербезопас" to "kiberhowpsuzlyk",
        "network" to "torlary", "сети" to "torlary"
    )

    /** Localized labels; Turkmen is the default. */
    private class Labels(val lang: String) {
        private fun l(tk: String, en: String, ru: String) = when (lang) {
            "en" -> en
            "ru" -> ru
            else -> tk
        }

        val semester = l("Semestr", "Semester", "Семестр")
        val credits = l("Kreditler", "Credits", "Кредиты")
        val creditsShort = l("kredit", "credits", "кред.")
        val cumulativeGpa = l("Umumy GPA", "Cumulative GPA", "Общий GPA")
        val finalGrade = l("Jemleýji baha", "Final grade", "Итоговая оценка")
        val midterm = l("Aralyk", "Midterm", "Промежуточный")
        val exam = l("Ekzamen", "Exam", "Экзамен")
        val teacher = l("Mugallym", "Teacher", "Преподаватель")
        val teachers = l("Häzirki mugallymlaryňyz:", "Your current teachers:", "Ваши текущие преподаватели:")
        val attendance = l("Gatnaşyk", "Attendance", "Посещаемость")
        val overall = l("Jemi", "Overall", "Всего")
        val absencesShort = l("gelmedik", "absences", "пропусков")
        val excused = l("sebäpli", "excused", "по уваж. причине")
        val unexcused = l("sebäpsiz", "unexcused", "без причины")
        val mostMissed = l("Iň köp gatnaşmadyk dersleriňiz:", "Subjects you missed the most:", "Больше всего пропусков:")
        val noAbsences = l("Siziň hiç bir dersden gelmedik sapagyňyz ýok. 👏", "You have no absences in any subject. 👏", "У вас нет пропусков ни по одному предмету. 👏")
        val upcomingExams = l("Ýakynlaşýan ekzamenler:", "Upcoming exams:", "Ближайшие экзамены:")
        val noUpcomingExams = l("Häzirki wagtda ýakynlaşýan ekzamen ýok.", "There are no upcoming exams right now.", "Ближайших экзаменов сейчас нет.")
        val schedule = l("Sapak tertibi:", "Class schedule:", "Расписание занятий:")
        val noClasses = l("Bu gün üçin sapak ýok.", "There are no classes on that day.", "На этот день занятий нет.")
        val room = l("otag", "room", "ауд.")
        val studentCard = l("Talyp belgisi", "Student ID", "Студенческий билет")
        val faculty = l("Fakultet", "Faculty", "Факультет")
        val major = l("Hünär", "Major", "Специальность")
        val group = l("Topar", "Group", "Группа")
        val year = l("Kurs", "Year", "Курс")
        val advisor = l("Ylmy maslahatçy", "Academic advisor", "Научный руководитель")

        fun daysLeft(days: Int) = if (days == 0) l("şu gün", "today", "сегодня") else l("$days gün galdy", "in $days days", "через $days дн.")

        fun futureSemester(requested: Int, current: Int) = l(
            "$requested-nji semestr entek başlamady. Häzir siz $current-nji semestrde okaýarsyňyz.",
            "Semester $requested has not started yet. You are currently in semester $current.",
            "Семестр $requested ещё не начался. Сейчас вы учитесь в семестре $current."
        )

        fun greeting(name: String) = l(
            "Salam, $name! Men Akylly Talyp Offline AI. Bahalaryňyz, gatnaşygyňyz, sapak tertibiňiz, ekzamenleriňiz we HNGU düzgünleri barada sorap bilersiňiz.",
            "Hello, $name! I'm Akylly Talyp Offline AI. Ask me about your grades, attendance, schedule, exams or HNGU rules.",
            "Здравствуйте, $name! Я Akylly Talyp Offline AI. Спросите об оценках, посещаемости, расписании, экзаменах или правилах ХНГУ."
        )
    }
}
