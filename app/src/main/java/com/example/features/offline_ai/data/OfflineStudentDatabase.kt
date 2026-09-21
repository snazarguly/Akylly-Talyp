package com.example.features.offline_ai.data

import com.example.features.offline_ai.model.OfflineAcademicYearRecord
import com.example.features.offline_ai.model.OfflineAnnouncementRecord
import com.example.features.offline_ai.model.OfflineExamRecord
import com.example.features.offline_ai.model.OfflineGradeComponents
import com.example.features.offline_ai.model.OfflineOverallAttendance
import com.example.features.offline_ai.model.OfflineScheduleRecord
import com.example.features.offline_ai.model.OfflineSemesterRecord
import com.example.features.offline_ai.model.OfflineStudentData
import com.example.features.offline_ai.model.OfflineSubjectAttendance
import com.example.features.offline_ai.model.OfflineSubjectExamRecord
import com.example.features.offline_ai.model.OfflineSubjectRecord
import com.example.features.offline_ai.model.OfflineTeacherRecord
import kotlin.math.roundToInt

/**
 * Deterministic Generator & Local In-Memory Database for rich student academic datasets.
 *
 * All data is strictly organized by SEMESTER:
 * Semester -> Subjects -> Teachers -> Weekly Schedule -> Attendance -> Exams -> Grades -> Credits -> Semester GPA
 *
 * If the student is in Semester 6:
 * - Semesters 1, 2, 3, 4, 5, 6 are complete and available.
 * - Each semester has its own independent, realistic weekly schedule.
 * - Semester 7 and 8 DO NOT exist and are strictly unavailable.
 *
 * DEMO DATA — NOT REAL UNIVERSITY DATA.
 */
class OfflineStudentDatabase {

    private val usersMap: Map<String, OfflineStudentData> = mapOf(
        "Suleyman_05" to createSuleymanComprehensiveData(),
        "Merjen_M" to createMerjenComprehensiveData(),
        "Batyr_A" to createBatyrComprehensiveData()
    )

    fun getStudentData(userId: String): OfflineStudentData? =
        usersMap[userId] ?: usersMap.entries.firstOrNull { it.key.equals(userId, ignoreCase = true) }?.value ?: usersMap.values.firstOrNull()

    fun getAllUserIds(): List<String> = usersMap.keys.toList()

    companion object {

        private fun roundOneDecimal(value: Double): Double {
            return (value * 10.0).roundToInt() / 10.0
        }

        private fun calculateSemesterRecord(
            semesterId: String,
            semesterNumber: Int,
            academicYear: String,
            yearNumber: Int,
            season: String,
            startDate: String,
            endDate: String,
            status: String,
            subjects: List<OfflineSubjectRecord>,
            schedule: List<OfflineScheduleRecord>
        ): OfflineSemesterRecord {
            val totalSubjects = subjects.size
            val totalCredits = subjects.sumOf { it.credits }
            val completedCredits = subjects.filter { it.grades.isPassed }.sumOf { it.credits }

            val weightedGradeSum = subjects.sumOf { it.grades.finalScore * it.credits }
            val semesterGpa = if (totalCredits > 0) roundOneDecimal(weightedGradeSum / totalCredits.toDouble()) else 0.0
            val semesterAverage = if (totalSubjects > 0) roundOneDecimal(subjects.map { it.grades.finalScore }.average()) else 0.0

            val totalClassesHeld = subjects.sumOf { it.attendance.totalClasses }
            val totalAttended = subjects.sumOf { it.attendance.attendedClasses }
            val totalAbsences = subjects.sumOf { it.attendance.absences }
            val excused = subjects.sumOf { it.attendance.excusedAbsences }
            val unexcused = subjects.sumOf { it.attendance.unexcusedAbsences }
            val late = subjects.sumOf { it.attendance.lateArrivals }

            return OfflineSemesterRecord(
                semesterId = semesterId,
                semesterNumber = semesterNumber,
                academicYear = academicYear,
                yearNumber = yearNumber,
                season = season,
                startDate = startDate,
                endDate = endDate,
                status = status,
                totalSubjects = totalSubjects,
                totalCredits = totalCredits,
                completedCredits = completedCredits,
                semesterGpa = semesterGpa,
                semesterAverage = semesterAverage,
                totalClassesHeld = totalClassesHeld,
                totalAttended = totalAttended,
                totalAbsences = totalAbsences,
                excusedAbsences = excused,
                unexcusedAbsences = unexcused,
                lateArrivals = late,
                subjects = subjects,
                weeklySchedule = schedule
            )
        }

        // =========================================================================================
        // 1. SULEYMAN_05 (3rd Year, Information Systems & Cybersecurity, CURRENT SEMESTER = 6)
        // Semesters 1, 2, 3, 4, 5, 6 AVAILABLE. Semesters 7 & 8 DO NOT EXIST.
        // =========================================================================================
        private fun createSuleymanComprehensiveData(): OfflineStudentData {
            // -------------------------------------------------------------------------------------
            // YEAR 1 - SEMESTER 1 (Fall 2023)
            // -------------------------------------------------------------------------------------
            val s1Subjects = listOf(
                OfflineSubjectRecord(
                    id = "s1_math101", code = "MATH-101", name = "Ýokary matematika I", credits = 5,
                    semesterNumber = 1, academicYear = "2023-2024", teacherName = "T. Beglerow",
                    teacherTitle = "Dosent", teacherDepartment = "Ýokary matematika kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 84.0, finalExamScore = 89.0, assignmentScore = 88.0, courseworkScore = null, labScore = null, participationScore = 90.0, finalScore = 87.0, letterGrade = "B+", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 58, absences = 2, excusedAbsences = 2, unexcusedAbsences = 0, lateArrivals = 0, percentage = 96.7),
                    exams = listOf(OfflineSubjectExamRecord("e1_1", "Final", "18.01.2024", "09:00", "301", "Completed", 89.0))
                ),
                OfflineSubjectRecord(
                    id = "s1_phys101", code = "PHYS-101", name = "Umumy fizika I", credits = 5,
                    semesterNumber = 1, academicYear = "2023-2024", teacherName = "M. Ýagmyrow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Umumy fizika kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 82.0, finalExamScore = 86.0, assignmentScore = 85.0, courseworkScore = null, labScore = 88.0, participationScore = 84.0, finalScore = 85.0, letterGrade = "B", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 57, absences = 3, excusedAbsences = 2, unexcusedAbsences = 1, lateArrivals = 1, percentage = 95.0),
                    exams = listOf(OfflineSubjectExamRecord("e1_2", "Final", "22.01.2024", "11:00", "112", "Completed", 86.0))
                ),
                OfflineSubjectRecord(
                    id = "s1_cs101", code = "CS-101", name = "Informatika we algoritmler", credits = 5,
                    semesterNumber = 1, academicYear = "2023-2024", teacherName = "M. Orazow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Maglumat ulgamlary kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 92.0, finalExamScore = 96.0, assignmentScore = 95.0, courseworkScore = null, labScore = 96.0, participationScore = 94.0, finalScore = 95.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 60, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0),
                    exams = listOf(OfflineSubjectExamRecord("e1_3", "Final", "15.01.2024", "09:00", "204", "Completed", 96.0))
                ),
                OfflineSubjectRecord(
                    id = "s1_chem101", code = "CHEM-101", name = "Himiki esaslar we materiallar", credits = 5,
                    semesterNumber = 1, academicYear = "2023-2024", teacherName = "G. Hojamyradowa",
                    teacherTitle = "Mugallym", teacherDepartment = "Umumy himiýa kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 86.0, finalExamScore = 90.0, assignmentScore = 88.0, courseworkScore = null, labScore = 89.0, participationScore = 87.0, finalScore = 88.0, letterGrade = "B+", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 59, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 0, percentage = 98.3)
                ),
                OfflineSubjectRecord(
                    id = "s1_hist101", code = "HIST-101", name = "Türkmenistanyň taryhy", credits = 5,
                    semesterNumber = 1, academicYear = "2023-2024", teacherName = "Ş. Batyrow",
                    teacherTitle = "Dosent", teacherDepartment = "Jemgyýetçilik ylymlary kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 91.0, finalExamScore = 95.0, assignmentScore = 93.0, courseworkScore = null, labScore = null, participationScore = 94.0, finalScore = 93.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 60, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0)
                ),
                OfflineSubjectRecord(
                    id = "s1_eng101", code = "ENG-101", name = "Iňlis dili I", credits = 5,
                    semesterNumber = 1, academicYear = "2023-2024", teacherName = "B. Saparowa",
                    teacherTitle = "Mugallym", teacherDepartment = "Diller kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 88.0, finalExamScore = 92.0, assignmentScore = 90.0, courseworkScore = null, labScore = null, participationScore = 90.0, finalScore = 90.0, letterGrade = "A-", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 60, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0)
                )
            )
            val s1Schedule = listOf(
                OfflineScheduleRecord("s1_sch1", 1, 1, "Duşenbe", "08:30", "10:00", "Ýokary matematika I", "MATH-101", "301", "T. Beglerow", "Leksiýa"),
                OfflineScheduleRecord("s1_sch2", 1, 1, "Duşenbe", "10:15", "11:45", "Umumy fizika I", "PHYS-101", "112", "M. Ýagmyrow", "Leksiýa"),
                OfflineScheduleRecord("s1_sch3", 1, 2, "Sişenbe", "08:30", "10:00", "Informatika we algoritmler", "CS-101", "204", "M. Orazow", "Leksiýa"),
                OfflineScheduleRecord("s1_sch4", 1, 2, "Sişenbe", "10:15", "11:45", "Iňlis dili I", "ENG-101", "402", "B. Saparowa", "Amaly"),
                OfflineScheduleRecord("s1_sch5", 1, 3, "Çarşenbe", "08:30", "10:00", "Ýokary matematika I", "MATH-101", "301", "T. Beglerow", "Tejribe"),
                OfflineScheduleRecord("s1_sch6", 1, 3, "Çarşenbe", "10:15", "11:45", "Himiki esaslar we materiallar", "CHEM-101", "105", "G. Hojamyradowa", "Leksiýa"),
                OfflineScheduleRecord("s1_sch7", 1, 4, "Penşenbe", "08:30", "10:00", "Informatika we algoritmler", "CS-101", "204", "M. Orazow", "Laboratoriýa"),
                OfflineScheduleRecord("s1_sch8", 1, 4, "Penşenbe", "10:15", "11:45", "Umumy fizika I", "PHYS-101", "112", "M. Ýagmyrow", "Laboratoriýa"),
                OfflineScheduleRecord("s1_sch9", 1, 5, "Anna", "08:30", "10:00", "Türkmenistanyň taryhy", "HIST-101", "210", "Ş. Batyrow", "Leksiýa"),
                OfflineScheduleRecord("s1_sch10", 1, 5, "Anna", "10:15", "11:45", "Himiki esaslar we materiallar", "CHEM-101", "105", "G. Hojamyradowa", "Laboratoriýa"),
                OfflineScheduleRecord("s1_sch11", 1, 6, "Şenbe", "09:00", "10:30", "Iňlis dili I", "ENG-101", "402", "B. Saparowa", "Tejribe")
            )
            val sem1 = calculateSemesterRecord("sem_1", 1, "2023-2024", 1, "Güýz", "01.09.2023", "25.01.2024", "Completed", s1Subjects, s1Schedule)

            // -------------------------------------------------------------------------------------
            // YEAR 1 - SEMESTER 2 (Spring 2024)
            // -------------------------------------------------------------------------------------
            val s2Subjects = listOf(
                OfflineSubjectRecord(
                    id = "s2_math102", code = "MATH-102", name = "Ýokary matematika II", credits = 5,
                    semesterNumber = 2, academicYear = "2023-2024", teacherName = "T. Beglerow",
                    teacherTitle = "Dosent", teacherDepartment = "Ýokary matematika kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 89.0, finalExamScore = 93.0, assignmentScore = 91.0, courseworkScore = null, labScore = null, participationScore = 91.0, finalScore = 91.0, letterGrade = "A-", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 59, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 0, percentage = 98.3)
                ),
                OfflineSubjectRecord(
                    id = "s2_phys102", code = "PHYS-102", name = "Umumy fizika II (Elektrodinamika)", credits = 5,
                    semesterNumber = 2, academicYear = "2023-2024", teacherName = "M. Ýagmyrow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Umumy fizika kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 86.0, finalExamScore = 90.0, assignmentScore = 88.0, courseworkScore = null, labScore = 90.0, participationScore = 87.0, finalScore = 88.0, letterGrade = "B+", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 58, absences = 2, excusedAbsences = 1, unexcusedAbsences = 1, lateArrivals = 0, percentage = 96.7)
                ),
                OfflineSubjectRecord(
                    id = "s2_cs102", code = "CS-102", name = "Programmirleme esaslary (Python)", credits = 6,
                    semesterNumber = 2, academicYear = "2023-2024", teacherName = "J. Nazarow",
                    teacherTitle = "Mugallym", teacherDepartment = "Programmirleme kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 95.0, finalExamScore = 98.0, assignmentScore = 97.0, courseworkScore = 96.0, labScore = 97.0, participationScore = 96.0, finalScore = 97.0, letterGrade = "A+", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 72, attendedClasses = 72, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0)
                ),
                OfflineSubjectRecord(
                    id = "s2_cs103", code = "CS-103", name = "Kiberhowpsuzlyga giriş", credits = 5,
                    semesterNumber = 2, academicYear = "2023-2024", teacherName = "D. Gurbanow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Kiberhowpsuzlyk kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 94.0, finalExamScore = 97.0, assignmentScore = 96.0, courseworkScore = null, labScore = 96.0, participationScore = 95.0, finalScore = 96.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 60, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0)
                ),
                OfflineSubjectRecord(
                    id = "s2_eng102", code = "ENG-102", name = "Inženerçilik grafikasy we CAD", credits = 4,
                    semesterNumber = 2, academicYear = "2023-2024", teacherName = "K. Annageldiýew",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Inženerçilik kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 85.0, finalExamScore = 89.0, assignmentScore = 87.0, courseworkScore = null, labScore = 88.0, participationScore = 86.0, finalScore = 87.0, letterGrade = "B+", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 48, attendedClasses = 47, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 1, percentage = 97.9)
                ),
                OfflineSubjectRecord(
                    id = "s2_lang102", code = "LANG-102", name = "Iňlis dili II", credits = 5,
                    semesterNumber = 2, academicYear = "2023-2024", teacherName = "B. Saparowa",
                    teacherTitle = "Mugallym", teacherDepartment = "Diller kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 90.0, finalExamScore = 94.0, assignmentScore = 92.0, courseworkScore = null, labScore = null, participationScore = 92.0, finalScore = 92.0, letterGrade = "A-", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 60, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0)
                )
            )
            val s2Schedule = listOf(
                OfflineScheduleRecord("s2_sch1", 2, 1, "Duşenbe", "08:30", "10:00", "Ýokary matematika II", "MATH-102", "301", "T. Beglerow", "Leksiýa"),
                OfflineScheduleRecord("s2_sch2", 2, 1, "Duşenbe", "10:15", "11:45", "Umumy fizika II (Elektrodinamika)", "PHYS-102", "112", "M. Ýagmyrow", "Leksiýa"),
                OfflineScheduleRecord("s2_sch3", 2, 2, "Sişenbe", "08:30", "10:00", "Programmirleme esaslary (Python)", "CS-102", "208", "J. Nazarow", "Leksiýa"),
                OfflineScheduleRecord("s2_sch4", 2, 2, "Sişenbe", "10:15", "11:45", "Kiberhowpsuzlyga giriş", "CS-103", "305", "D. Gurbanow", "Leksiýa"),
                OfflineScheduleRecord("s2_sch5", 2, 3, "Çarşenbe", "08:30", "10:00", "Ýokary matematika II", "MATH-102", "301", "T. Beglerow", "Tejribe"),
                OfflineScheduleRecord("s2_sch6", 2, 3, "Çarşenbe", "10:15", "11:45", "Inženerçilik grafikasy we CAD", "ENG-102", "115", "K. Annageldiýew", "Tejribe"),
                OfflineScheduleRecord("s2_sch7", 2, 4, "Penşenbe", "08:30", "10:00", "Programmirleme esaslary (Python)", "CS-102", "208", "J. Nazarow", "Laboratoriýa"),
                OfflineScheduleRecord("s2_sch8", 2, 4, "Penşenbe", "10:15", "11:45", "Kiberhowpsuzlyga giriş", "CS-103", "305", "D. Gurbanow", "Tejribe"),
                OfflineScheduleRecord("s2_sch9", 2, 5, "Anna", "08:30", "10:00", "Umumy fizika II (Elektrodinamika)", "PHYS-102", "112", "M. Ýagmyrow", "Laboratoriýa"),
                OfflineScheduleRecord("s2_sch10", 2, 5, "Anna", "10:15", "11:45", "Iňlis dili II", "LANG-102", "402", "B. Saparowa", "Amaly"),
                OfflineScheduleRecord("s2_sch11", 2, 6, "Şenbe", "09:00", "10:30", "Inženerçilik grafikasy we CAD", "ENG-102", "115", "K. Annageldiýew", "Laboratoriýa")
            )
            val sem2 = calculateSemesterRecord("sem_2", 2, "2023-2024", 1, "Bahar", "10.02.2024", "30.06.2024", "Completed", s2Subjects, s2Schedule)

            // -------------------------------------------------------------------------------------
            // YEAR 2 - SEMESTER 3 (Fall 2024)
            // -------------------------------------------------------------------------------------
            val s3Subjects = listOf(
                OfflineSubjectRecord(
                    id = "s3_math201", code = "MATH-201", name = "Diskret matematika we logiki barlaglar", credits = 5,
                    semesterNumber = 3, academicYear = "2024-2025", teacherName = "A. Aşyrow",
                    teacherTitle = "Dosent", teacherDepartment = "Maglumat goragy kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 93.0, finalExamScore = 95.0, assignmentScore = 94.0, courseworkScore = null, labScore = null, participationScore = 94.0, finalScore = 94.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 59, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 0, percentage = 98.3)
                ),
                OfflineSubjectRecord(
                    id = "s3_cs201", code = "CS-201", name = "Maglumat gurluşlary we algoritmler (C++)", credits = 6,
                    semesterNumber = 3, academicYear = "2024-2025", teacherName = "M. Orazow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Maglumat ulgamlary kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 95.0, finalExamScore = 97.0, assignmentScore = 96.0, courseworkScore = 96.0, labScore = 96.0, participationScore = 95.0, finalScore = 96.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 72, attendedClasses = 72, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0)
                ),
                OfflineSubjectRecord(
                    id = "s3_cs202", code = "CS-202", name = "Operasion ulgamlar (Linux/UNIX)", credits = 5,
                    semesterNumber = 3, academicYear = "2024-2025", teacherName = "K. Hydyrow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Kompýuter torlary kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 94.0, finalExamScore = 96.0, assignmentScore = 95.0, courseworkScore = null, labScore = 96.0, participationScore = 95.0, finalScore = 95.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 59, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 0, percentage = 98.3)
                ),
                OfflineSubjectRecord(
                    id = "s3_cs203", code = "CS-203", name = "Web tehnologiýalary (Frontend/Backend)", credits = 6,
                    semesterNumber = 3, academicYear = "2024-2025", teacherName = "D. Gurbanow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Kiberhowpsuzlyk kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 93.0, finalExamScore = 95.0, assignmentScore = 94.0, courseworkScore = 95.0, labScore = 94.0, participationScore = 93.0, finalScore = 94.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 72, attendedClasses = 71, absences = 1, excusedAbsences = 0, unexcusedAbsences = 1, lateArrivals = 1, percentage = 98.6)
                ),
                OfflineSubjectRecord(
                    id = "s3_stat201", code = "STAT-201", name = "Ähtimallyklar teoriýasy we statistika", credits = 5,
                    semesterNumber = 3, academicYear = "2024-2025", teacherName = "T. Beglerow",
                    teacherTitle = "Dosent", teacherDepartment = "Ýokary matematika kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 87.0, finalExamScore = 91.0, assignmentScore = 89.0, courseworkScore = null, labScore = null, participationScore = 89.0, finalScore = 89.0, letterGrade = "B+", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 58, absences = 2, excusedAbsences = 2, unexcusedAbsences = 0, lateArrivals = 0, percentage = 96.7)
                ),
                OfflineSubjectRecord(
                    id = "s3_eng201", code = "ENG-201", name = "Akademiki iňlis dili", credits = 5,
                    semesterNumber = 3, academicYear = "2024-2025", teacherName = "B. Saparowa",
                    teacherTitle = "Mugallym", teacherDepartment = "Diller kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 91.0, finalExamScore = 93.0, assignmentScore = 92.0, courseworkScore = null, labScore = null, participationScore = 92.0, finalScore = 92.0, letterGrade = "A-", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 60, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0)
                )
            )
            val s3Schedule = listOf(
                OfflineScheduleRecord("s3_sch1", 3, 1, "Duşenbe", "08:30", "10:00", "Diskret matematika we logiki barlaglar", "MATH-201", "201", "A. Aşyrow", "Leksiýa"),
                OfflineScheduleRecord("s3_sch2", 3, 1, "Duşenbe", "10:15", "11:45", "Maglumat gurluşlary we algoritmler (C++)", "CS-201", "204", "M. Orazow", "Leksiýa"),
                OfflineScheduleRecord("s3_sch3", 3, 2, "Sişenbe", "08:30", "10:00", "Operasion ulgamlar (Linux/UNIX)", "CS-202", "318", "K. Hydyrow", "Leksiýa"),
                OfflineScheduleRecord("s3_sch4", 3, 2, "Sişenbe", "10:15", "11:45", "Web tehnologiýalary (Frontend/Backend)", "CS-203", "305", "D. Gurbanow", "Leksiýa"),
                OfflineScheduleRecord("s3_sch5", 3, 3, "Çarşenbe", "08:30", "10:00", "Ähtimallyklar teoriýasy we statistika", "STAT-201", "301", "T. Beglerow", "Leksiýa"),
                OfflineScheduleRecord("s3_sch6", 3, 3, "Çarşenbe", "10:15", "11:45", "Maglumat gurluşlary we algoritmler (C++)", "CS-201", "204", "M. Orazow", "Laboratoriýa"),
                OfflineScheduleRecord("s3_sch7", 3, 4, "Penşenbe", "08:30", "10:00", "Operasion ulgamlar (Linux/UNIX)", "CS-202", "318", "K. Hydyrow", "Laboratoriýa"),
                OfflineScheduleRecord("s3_sch8", 3, 4, "Penşenbe", "10:15", "11:45", "Web tehnologiýalary (Frontend/Backend)", "CS-203", "305", "D. Gurbanow", "Laboratoriýa"),
                OfflineScheduleRecord("s3_sch9", 3, 5, "Anna", "08:30", "10:00", "Akademiki iňlis dili", "ENG-201", "402", "B. Saparowa", "Amaly"),
                OfflineScheduleRecord("s3_sch10", 3, 5, "Anna", "10:15", "11:45", "Ähtimallyklar teoriýasy we statistika", "STAT-201", "301", "T. Beglerow", "Tejribe"),
                OfflineScheduleRecord("s3_sch11", 3, 6, "Şenbe", "09:00", "10:30", "Diskret matematika we logiki barlaglar", "MATH-201", "201", "A. Aşyrow", "Tejribe")
            )
            val sem3 = calculateSemesterRecord("sem_3", 3, "2024-2025", 2, "Güýz", "01.09.2024", "25.01.2025", "Completed", s3Subjects, s3Schedule)

            // -------------------------------------------------------------------------------------
            // YEAR 2 - SEMESTER 4 (Spring 2025)
            // -------------------------------------------------------------------------------------
            val s4Subjects = listOf(
                OfflineSubjectRecord(
                    id = "s4_cs204", code = "CS-204", name = "Tor protokollary we arhitekturasy", credits = 6,
                    semesterNumber = 4, academicYear = "2024-2025", teacherName = "K. Hydyrow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Kompýuter torlary kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 93.0, finalExamScore = 95.0, assignmentScore = 94.0, courseworkScore = null, labScore = 95.0, participationScore = 93.0, finalScore = 94.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 72, attendedClasses = 70, absences = 2, excusedAbsences = 2, unexcusedAbsences = 0, lateArrivals = 1, percentage = 97.2)
                ),
                OfflineSubjectRecord(
                    id = "s4_cs205", code = "CS-205", name = "Java bilen kämil programmirleme", credits = 6,
                    semesterNumber = 4, academicYear = "2024-2025", teacherName = "J. Nazarow",
                    teacherTitle = "Mugallym", teacherDepartment = "Programmirleme kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 94.0, finalExamScore = 96.0, assignmentScore = 95.0, courseworkScore = 96.0, labScore = 95.0, participationScore = 94.0, finalScore = 95.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 72, attendedClasses = 71, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 0, percentage = 98.6)
                ),
                OfflineSubjectRecord(
                    id = "s4_cs206", code = "CS-206", name = "Kriptografik esaslar", credits = 5,
                    semesterNumber = 4, academicYear = "2024-2025", teacherName = "A. Aşyrow",
                    teacherTitle = "Dosent", teacherDepartment = "Maglumat goragy kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 89.0, finalExamScore = 93.0, assignmentScore = 91.0, courseworkScore = null, labScore = 92.0, participationScore = 90.0, finalScore = 91.0, letterGrade = "A-", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 58, absences = 2, excusedAbsences = 1, unexcusedAbsences = 1, lateArrivals = 0, percentage = 96.7)
                ),
                OfflineSubjectRecord(
                    id = "s4_cs207", code = "CS-207", name = "Maglumat howpsuzlygynyň kanunçylyk esaslary", credits = 4,
                    semesterNumber = 4, academicYear = "2024-2025", teacherName = "D. Gurbanow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Kiberhowpsuzlyk kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 95.0, finalExamScore = 97.0, assignmentScore = 96.0, courseworkScore = null, labScore = null, participationScore = 96.0, finalScore = 96.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 48, attendedClasses = 48, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0)
                ),
                OfflineSubjectRecord(
                    id = "s4_econ201", code = "ECON-201", name = "Ykdysadyýet esaslary we innowasiýalar", credits = 5,
                    semesterNumber = 4, academicYear = "2024-2025", teacherName = "N. Annagulyýew",
                    teacherTitle = "Dosent", teacherDepartment = "Ykdysadyýet kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 85.0, finalExamScore = 89.0, assignmentScore = 87.0, courseworkScore = null, labScore = null, participationScore = 87.0, finalScore = 87.0, letterGrade = "B+", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 59, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 0, percentage = 98.3)
                ),
                OfflineSubjectRecord(
                    id = "s4_pract201", code = "PRACT-201", name = "Önümçilik tejribeligi I (IT Kompaniýa)", credits = 4,
                    semesterNumber = 4, academicYear = "2024-2025", teacherName = "D. Gurbanow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Kiberhowpsuzlyk kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 95.0, finalExamScore = 97.0, assignmentScore = null, courseworkScore = 97.0, labScore = null, participationScore = 96.0, finalScore = 96.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 48, attendedClasses = 47, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 0, percentage = 97.9)
                )
            )
            val s4Schedule = listOf(
                OfflineScheduleRecord("s4_sch1", 4, 1, "Duşenbe", "08:30", "10:00", "Tor protokollary we arhitekturasy", "CS-204", "318", "K. Hydyrow", "Leksiýa"),
                OfflineScheduleRecord("s4_sch2", 4, 1, "Duşenbe", "10:15", "11:45", "Java bilen kämil programmirleme", "CS-205", "208", "J. Nazarow", "Leksiýa"),
                OfflineScheduleRecord("s4_sch3", 4, 2, "Sişenbe", "08:30", "10:00", "Kriptografik esaslar", "CS-206", "412", "A. Aşyrow", "Leksiýa"),
                OfflineScheduleRecord("s4_sch4", 4, 2, "Sişenbe", "10:15", "11:45", "Maglumat howpsuzlygynyň kanunçylyk esaslary", "CS-207", "215", "D. Gurbanow", "Leksiýa"),
                OfflineScheduleRecord("s4_sch5", 4, 3, "Çarşenbe", "08:30", "10:00", "Ykdysadyýet esaslary we innowasiýalar", "ECON-201", "108", "N. Annagulyýew", "Leksiýa"),
                OfflineScheduleRecord("s4_sch6", 4, 3, "Çarşenbe", "10:15", "11:45", "Tor protokollary we arhitekturasy", "CS-204", "318", "K. Hydyrow", "Laboratoriýa"),
                OfflineScheduleRecord("s4_sch7", 4, 4, "Penşenbe", "08:30", "10:00", "Java bilen kämil programmirleme", "CS-205", "208", "J. Nazarow", "Laboratoriýa"),
                OfflineScheduleRecord("s4_sch8", 4, 4, "Penşenbe", "10:15", "11:45", "Kriptografik esaslar", "CS-206", "412", "A. Aşyrow", "Tejribe"),
                OfflineScheduleRecord("s4_sch9", 4, 5, "Anna", "08:30", "10:00", "Önümçilik tejribeligi I (IT Kompaniýa)", "PRACT-201", "305", "D. Gurbanow", "Tejribe"),
                OfflineScheduleRecord("s4_sch10", 4, 5, "Anna", "10:15", "11:45", "Ykdysadyýet esaslary we innowasiýalar", "ECON-201", "108", "N. Annagulyýew", "Tejribe"),
                OfflineScheduleRecord("s4_sch11", 4, 6, "Şenbe", "09:00", "10:30", "Maglumat howpsuzlygynyň kanunçylyk esaslary", "CS-207", "215", "D. Gurbanow", "Tejribe")
            )
            val sem4 = calculateSemesterRecord("sem_4", 4, "2024-2025", 2, "Bahar", "10.02.2025", "30.06.2025", "Completed", s4Subjects, s4Schedule)

            // -------------------------------------------------------------------------------------
            // YEAR 3 - SEMESTER 5 (Fall 2025)
            // -------------------------------------------------------------------------------------
            val s5Subjects = listOf(
                OfflineSubjectRecord(
                    id = "s5_cs301", code = "CS-301", name = "Amaly kiberhowpsuzlyk I", credits = 6,
                    semesterNumber = 5, academicYear = "2025-2026", teacherName = "D. Gurbanow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Kiberhowpsuzlyk kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 96.0, finalExamScore = 98.0, assignmentScore = 97.0, courseworkScore = 98.0, labScore = 97.0, participationScore = 96.0, finalScore = 97.0, letterGrade = "A+", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 72, attendedClasses = 72, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0)
                ),
                OfflineSubjectRecord(
                    id = "s5_cs302", code = "CS-302", name = "Tor howpsuzlygy we barlaglar", credits = 6,
                    semesterNumber = 5, academicYear = "2025-2026", teacherName = "K. Hydyrow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Kompýuter torlary kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 94.0, finalExamScore = 96.0, assignmentScore = 95.0, courseworkScore = null, labScore = 96.0, participationScore = 94.0, finalScore = 95.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 72, attendedClasses = 71, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 0, percentage = 98.6)
                ),
                OfflineSubjectRecord(
                    id = "s5_cs303", code = "CS-303", name = "Asimmetrik kriptoulgamlar", credits = 5,
                    semesterNumber = 5, academicYear = "2025-2026", teacherName = "A. Aşyrow",
                    teacherTitle = "Dosent", teacherDepartment = "Maglumat goragy kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 92.0, finalExamScore = 94.0, assignmentScore = 93.0, courseworkScore = null, labScore = 94.0, participationScore = 92.0, finalScore = 93.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 59, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 0, percentage = 98.3)
                ),
                OfflineSubjectRecord(
                    id = "s5_cs304", code = "CS-304", name = "Maglumat goragyny awtomatlaşdyrmak", credits = 5,
                    semesterNumber = 5, academicYear = "2025-2026", teacherName = "J. Nazarow",
                    teacherTitle = "Mugallym", teacherDepartment = "Programmirleme kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 93.0, finalExamScore = 95.0, assignmentScore = 94.0, courseworkScore = 95.0, labScore = 94.0, participationScore = 94.0, finalScore = 94.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 60, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0)
                ),
                OfflineSubjectRecord(
                    id = "s5_cs305", code = "CS-305", name = "Maglumat goragynda emeli aň", credits = 5,
                    semesterNumber = 5, academicYear = "2025-2026", teacherName = "S. Berdiýew",
                    teacherTitle = "Professor", teacherDepartment = "Emeli aň kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 95.0, finalExamScore = 97.0, assignmentScore = 96.0, courseworkScore = 97.0, labScore = 96.0, participationScore = 95.0, finalScore = 96.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 59, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 1, percentage = 98.3)
                ),
                OfflineSubjectRecord(
                    id = "s5_mgt301", code = "MGT-301", name = "Taslama dolandyryş (Agile/Scrum)", credits = 5,
                    semesterNumber = 5, academicYear = "2025-2026", teacherName = "M. Orazow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Maglumat ulgamlary kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 90.0, finalExamScore = 94.0, assignmentScore = 92.0, courseworkScore = null, labScore = null, participationScore = 92.0, finalScore = 92.0, letterGrade = "A-", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 60, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0)
                )
            )
            val s5Schedule = listOf(
                OfflineScheduleRecord("s5_sch1", 5, 1, "Duşenbe", "08:30", "10:00", "Amaly kiberhowpsuzlyk I", "CS-301", "305", "D. Gurbanow", "Leksiýa"),
                OfflineScheduleRecord("s5_sch2", 5, 1, "Duşenbe", "10:15", "11:45", "Tor howpsuzlygy we barlaglar", "CS-302", "318", "K. Hydyrow", "Leksiýa"),
                OfflineScheduleRecord("s5_sch3", 5, 2, "Sişenbe", "08:30", "10:00", "Asimmetrik kriptoulgamlar", "CS-303", "412", "A. Aşyrow", "Leksiýa"),
                OfflineScheduleRecord("s5_sch4", 5, 2, "Sişenbe", "10:15", "11:45", "Maglumat goragyny awtomatlaşdyrmak", "CS-304", "208", "J. Nazarow", "Leksiýa"),
                OfflineScheduleRecord("s5_sch5", 5, 3, "Çarşenbe", "08:30", "10:00", "Maglumat goragynda emeli aň", "CS-305", "401", "S. Berdiýew", "Leksiýa"),
                OfflineScheduleRecord("s5_sch6", 5, 3, "Çarşenbe", "10:15", "11:45", "Taslama dolandyryş (Agile/Scrum)", "MGT-301", "204", "M. Orazow", "Leksiýa"),
                OfflineScheduleRecord("s5_sch7", 5, 4, "Penşenbe", "08:30", "10:00", "Amaly kiberhowpsuzlyk I", "CS-301", "305", "D. Gurbanow", "Laboratoriýa"),
                OfflineScheduleRecord("s5_sch8", 5, 4, "Penşenbe", "10:15", "11:45", "Tor howpsuzlygy we barlaglar", "CS-302", "318", "K. Hydyrow", "Laboratoriýa"),
                OfflineScheduleRecord("s5_sch9", 5, 5, "Anna", "08:30", "10:00", "Asimmetrik kriptoulgamlar", "CS-303", "412", "A. Aşyrow", "Laboratoriýa"),
                OfflineScheduleRecord("s5_sch10", 5, 5, "Anna", "10:15", "11:45", "Maglumat goragynda emeli aň", "CS-305", "401", "S. Berdiýew", "Laboratoriýa"),
                OfflineScheduleRecord("s5_sch11", 5, 6, "Şenbe", "09:00", "10:30", "Maglumat goragyny awtomatlaşdyrmak", "CS-304", "208", "J. Nazarow", "Tejribe")
            )
            val sem5 = calculateSemesterRecord("sem_5", 5, "2025-2026", 3, "Güýz", "01.09.2025", "25.01.2026", "Completed", s5Subjects, s5Schedule)

            // -------------------------------------------------------------------------------------
            // YEAR 3 - SEMESTER 6 (Spring 2026 - CURRENT ACTIVE SEMESTER)
            // -------------------------------------------------------------------------------------
            val s6Subjects = listOf(
                OfflineSubjectRecord(
                    id = "s6_cs306", code = "CS-306", name = "Penetration Testing we Etiki Haking", credits = 6,
                    semesterNumber = 6, academicYear = "2025-2026", teacherName = "D. Gurbanow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Kiberhowpsuzlyk kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 96.0, finalExamScore = 98.0, assignmentScore = 97.0, courseworkScore = 98.0, labScore = 98.0, participationScore = 96.0, finalScore = 97.0, letterGrade = "A+", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 72, attendedClasses = 71, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 0, percentage = 98.6),
                    exams = listOf(OfflineSubjectExamRecord("ex6_1", "Final", "20.06.2026", "09:00", "305", "Upcoming", null))
                ),
                OfflineSubjectRecord(
                    id = "s6_cs307", code = "CS-307", name = "Zeper ýetiriji programmalar seljermesi (Reverse Eng)", credits = 6,
                    semesterNumber = 6, academicYear = "2025-2026", teacherName = "A. Aşyrow",
                    teacherTitle = "Dosent", teacherDepartment = "Maglumat goragy kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 93.0, finalExamScore = 95.0, assignmentScore = 94.0, courseworkScore = null, labScore = 95.0, participationScore = 93.0, finalScore = 94.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 72, attendedClasses = 71, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 0, percentage = 98.6),
                    exams = listOf(OfflineSubjectExamRecord("ex6_2", "Final", "24.06.2026", "10:30", "412", "Upcoming", null))
                ),
                OfflineSubjectRecord(
                    id = "s6_cs308", code = "CS-308", name = "Bulut infrastruktura howpsuzlygy", credits = 5,
                    semesterNumber = 6, academicYear = "2025-2026", teacherName = "J. Nazarow",
                    teacherTitle = "Mugallym", teacherDepartment = "Programmirleme kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 90.0, finalExamScore = 94.0, assignmentScore = 92.0, courseworkScore = 93.0, labScore = 92.0, participationScore = 91.0, finalScore = 92.0, letterGrade = "A-", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 60, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0),
                    exams = listOf(OfflineSubjectExamRecord("ex6_3", "Final", "28.06.2026", "09:00", "204", "Upcoming", null))
                ),
                OfflineSubjectRecord(
                    id = "s6_cs309", code = "CS-309", name = "Incident Response we Forenzika", credits = 5,
                    semesterNumber = 6, academicYear = "2025-2026", teacherName = "K. Hydyrow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Kompýuter torlary kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 94.0, finalExamScore = 96.0, assignmentScore = 95.0, courseworkScore = null, labScore = 96.0, participationScore = 94.0, finalScore = 95.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 60, attendedClasses = 59, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 0, percentage = 98.3),
                    exams = listOf(OfflineSubjectExamRecord("ex6_4", "Final", "02.07.2026", "11:00", "318", "Upcoming", null))
                ),
                OfflineSubjectRecord(
                    id = "s6_cs310", code = "CS-310", name = "Maglumat gorag auditi", credits = 4,
                    semesterNumber = 6, academicYear = "2025-2026", teacherName = "D. Gurbanow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Kiberhowpsuzlyk kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 91.0, finalExamScore = 95.0, assignmentScore = 93.0, courseworkScore = null, labScore = null, participationScore = 93.0, finalScore = 93.0, letterGrade = "A", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 48, attendedClasses = 48, absences = 0, excusedAbsences = 0, unexcusedAbsences = 0, lateArrivals = 0, percentage = 100.0)
                ),
                OfflineSubjectRecord(
                    id = "s6_pract301", code = "PRACT-301", name = "Önümçilik tejribeligi II (Kiberhowpsuzlyk merkezi)", credits = 4,
                    semesterNumber = 6, academicYear = "2025-2026", teacherName = "D. Gurbanow",
                    teacherTitle = "Uly mugallym", teacherDepartment = "Kiberhowpsuzlyk kafedrasy",
                    grades = OfflineGradeComponents(midtermScore = 97.0, finalExamScore = 99.0, assignmentScore = null, courseworkScore = 99.0, labScore = null, participationScore = 98.0, finalScore = 98.0, letterGrade = "A+", isPassed = true),
                    attendance = OfflineSubjectAttendance(totalClasses = 48, attendedClasses = 47, absences = 1, excusedAbsences = 1, unexcusedAbsences = 0, lateArrivals = 0, percentage = 97.9)
                )
            )
            val s6Schedule = listOf(
                OfflineScheduleRecord("s6_sch1", 6, 1, "Duşenbe", "08:00", "09:30", "Penetration Testing we Etiki Haking", "CS-306", "305", "D. Gurbanow", "Leksiýa"),
                OfflineScheduleRecord("s6_sch2", 6, 1, "Duşenbe", "10:00", "11:30", "Zeper ýetiriji programmalar seljermesi (Reverse Eng)", "CS-307", "412", "A. Aşyrow", "Leksiýa"),
                OfflineScheduleRecord("s6_sch3", 6, 2, "Sişenbe", "08:00", "09:30", "Bulut infrastruktura howpsuzlygy", "CS-308", "204", "J. Nazarow", "Leksiýa"),
                OfflineScheduleRecord("s6_sch4", 6, 2, "Sişenbe", "10:00", "11:30", "Incident Response we Forenzika", "CS-309", "318", "K. Hydyrow", "Leksiýa"),
                OfflineScheduleRecord("s6_sch5", 6, 3, "Çarşenbe", "08:00", "09:30", "Maglumat gorag auditi", "CS-310", "215", "D. Gurbanow", "Leksiýa"),
                OfflineScheduleRecord("s6_sch6", 6, 3, "Çarşenbe", "10:00", "11:30", "Penetration Testing we Etiki Haking", "CS-306", "305", "D. Gurbanow", "Laboratoriýa"),
                OfflineScheduleRecord("s6_sch7", 6, 4, "Penşenbe", "08:00", "09:30", "Zeper ýetiriji programmalar seljermesi (Reverse Eng)", "CS-307", "412", "A. Aşyrow", "Laboratoriýa"),
                OfflineScheduleRecord("s6_sch8", 6, 4, "Penşenbe", "10:00", "11:30", "Bulut infrastruktura howpsuzlygy", "CS-308", "204", "J. Nazarow", "Laboratoriýa"),
                OfflineScheduleRecord("s6_sch9", 6, 5, "Anna", "08:00", "09:30", "Incident Response we Forenzika", "CS-309", "318", "K. Hydyrow", "Laboratoriýa"),
                OfflineScheduleRecord("s6_sch10", 6, 5, "Anna", "10:00", "11:30", "Önümçilik tejribeligi II (Kiberhowpsuzlyk merkezi)", "PRACT-301", "102", "D. Gurbanow", "Tejribe"),
                OfflineScheduleRecord("s6_sch11", 6, 6, "Şenbe", "09:00", "10:30", "Maglumat gorag auditi", "CS-310", "215", "D. Gurbanow", "Tejribe")
            )
            val sem6 = calculateSemesterRecord("sem_6", 6, "2025-2026", 3, "Bahar", "10.02.2026", "30.06.2026", "In Progress", s6Subjects, s6Schedule)

            // NO SEMESTER 7 OR 8! Student is currently in Semester 6.
            val allSemesters = listOf(sem1, sem2, sem3, sem4, sem5, sem6)
            val completedSemesters = allSemesters.filter { it.status == "Completed" }

            val year1 = OfflineAcademicYearRecord(1, "2023-2024", listOf(sem1, sem2))
            val year2 = OfflineAcademicYearRecord(2, "2024-2025", listOf(sem3, sem4))
            val year3 = OfflineAcademicYearRecord(3, "2025-2026", listOf(sem5, sem6))

            // Mathematical calculation of cumulative GPA across completed semesters:
            val totalCompletedCredits = completedSemesters.sumOf { it.completedCredits }
            val cumulativeGpa = if (totalCompletedCredits > 0) {
                roundOneDecimal(completedSemesters.sumOf { it.semesterGpa * it.completedCredits } / totalCompletedCredits.toDouble())
            } else 0.0

            // Overall Attendance calculation across all semesters:
            val totalClassesHeld = allSemesters.sumOf { it.totalClassesHeld }
            val totalAttended = allSemesters.sumOf { it.totalAttended }
            val totalAbsences = allSemesters.sumOf { it.totalAbsences }
            val excusedAbsences = allSemesters.sumOf { it.excusedAbsences }
            val unexcusedAbsences = allSemesters.sumOf { it.unexcusedAbsences }
            val lateArrivals = allSemesters.sumOf { it.lateArrivals }
            val overallPercentage = if (totalClassesHeld > 0) roundOneDecimal((totalAttended.toDouble() / totalClassesHeld.toDouble()) * 100.0) else 100.0

            val overallAttendance = OfflineOverallAttendance(
                totalClassesHeld = totalClassesHeld,
                totalAttended = totalAttended,
                totalAbsences = totalAbsences,
                excusedAbsences = excusedAbsences,
                unexcusedAbsences = unexcusedAbsences,
                lateArrivals = lateArrivals,
                overallPercentage = overallPercentage
            )

            val teachers = listOf(
                OfflineTeacherRecord("t1", "D. Gurbanow", "Kiberhowpsuzlyk kafedrasy", "Uly mugallym", listOf("Kiberhowpsuzlyk esaslary", "Web howpsuzlygy we audit", "Amaly kiberhowpsuzlyk I", "Penetration Testing we Etiki Haking", "Maglumat gorag auditi", "Önümçilik tejribeligi I", "Önümçilik tejribeligi II"), listOf(2, 3, 4, 5, 6), true),
                OfflineTeacherRecord("t2", "A. Aşyrow", "Maglumat goragy kafedrasy", "Dosent", listOf("Diskret matematika we logiki barlaglar", "Kriptografik esaslar", "Asimmetrik kriptoulgamlar", "Zeper ýetiriji programmalar seljermesi (Reverse Eng)"), listOf(3, 4, 5, 6), true),
                OfflineTeacherRecord("t3", "M. Orazow", "Maglumat ulgamlary kafedrasy", "Uly mugallym", listOf("Informatika we algoritmler", "Maglumat gurluşlary we algoritmler (C++)", "Taslama dolandyryş (Agile/Scrum)"), listOf(1, 3, 5), true),
                OfflineTeacherRecord("t4", "K. Hydyrow", "Kompýuter torlary kafedrasy", "Uly mugallym", listOf("Operasion ulgamlar (Linux/UNIX)", "Tor protokollary we arhitekturasy", "Tor howpsuzlygy we barlaglar", "Incident Response we Forenzika"), listOf(3, 4, 5, 6), true),
                OfflineTeacherRecord("t5", "S. Berdiýew", "Emeli aň kafedrasy", "Professor", listOf("Maglumat goragynda emeli aň"), listOf(5), true),
                OfflineTeacherRecord("t6", "J. Nazarow", "Programmirleme kafedrasy", "Mugallym", listOf("Programmirleme esaslary (Python)", "Java bilen kämil programmirleme", "Maglumat goragyny awtomatlaşdyrmak", "Bulut infrastruktura howpsuzlygy"), listOf(2, 4, 5, 6), true),
                OfflineTeacherRecord("t7", "T. Beglerow", "Ýokary matematika kafedrasy", "Dosent", listOf("Ýokary matematika I", "Ýokary matematika II", "Ähtimallyklar teoriýasy we statistika"), listOf(1, 2, 3), false),
                OfflineTeacherRecord("t8", "M. Ýagmyrow", "Umumy fizika kafedrasy", "Uly mugallym", listOf("Umumy fizika I", "Umumy fizika II (Elektrodinamika)"), listOf(1, 2), false),
                OfflineTeacherRecord("t9", "B. Saparowa", "Diller kafedrasy", "Mugallym", listOf("Iňlis dili I", "Iňlis dili II", "Akademiki iňlis dili"), listOf(1, 2, 3), false),
                OfflineTeacherRecord("t10", "K. Annageldiýew", "Inženerçilik kafedrasy", "Uly mugallym", listOf("Inženerçilik grafikasy we CAD"), listOf(2), false),
                OfflineTeacherRecord("t11", "N. Annagulyýew", "Ykdysadyýet kafedrasy", "Dosent", listOf("Ykdysadyýet esaslary we innowasiýalar"), listOf(4), false)
            )

            val exams = listOf(
                OfflineExamRecord("ex6_1", "Penetration Testing we Etiki Haking", 6, "2026-06-20", "20 Iýun, 2026", "09:00", "305", "Final", 5, "D. Gurbanow"),
                OfflineExamRecord("ex6_2", "Zeper ýetiriji programmalar seljermesi", 6, "2026-06-24", "24 Iýun, 2026", "10:30", "412", "Final", 9, "A. Aşyrow"),
                OfflineExamRecord("ex6_3", "Bulut infrastruktura howpsuzlygy", 6, "2026-06-28", "28 Iýun, 2026", "09:00", "204", "Final", 13, "J. Nazarow"),
                OfflineExamRecord("ex6_4", "Incident Response we Forenzika", 6, "2026-07-02", "02 Iýul, 2026", "11:00", "318", "Final", 17, "K. Hydyrow")
            )

            val announcements = listOf(
                OfflineAnnouncementRecord("an1", "CyberGuard HNGU-2026 CTF bäsleşigi", "14 Iýun", "Bäsleşikler", "Kiberhowpsuzlyk we CTF bäsleşigi 25-nji iýunda başlanýar. Toparlara ýazylmak dowam edýär.", true),
                OfflineAnnouncementRecord("an2", "6-njy semestr jemleýji ekzamen tertibi", "12 Iýun", "Ekzamen", "6-njy semestriň jemleýji döwlet we uniwersitet ekzamen tertipleri tassyklanyldy.", true),
                OfflineAnnouncementRecord("an3", "Sanly ylmy kitaphana elýeterliligi", "10 Iýun", "Kitaphana", "IEEE we Springer ylmy portallaryna uniwersitet torundan mugt elýeterlilik açyldy.", false)
            )

            return OfflineStudentData(
                userId = "Suleyman_05",
                fullName = "Süleýman Nazargulyýew Saparmyradowiç",
                username = "Suleyman_05",
                studentCardNumber = "HNGU-2023-8419",
                faculty = "Sanly tehnologiýalar we kiberhowpsuzlyk fakulteti",
                department = "Kiberhowpsuzlyk we tor inženerligi kafedrasy",
                major = "Maglumat ulgamlary we tehnologiýalary (Kiberhowpsuzlyk)",
                degree = "Bakalawr",
                group = "B topary",
                academicYear = "2025-2026",
                currentYear = 3,
                currentSemester = 6,
                enrollmentDate = "01.09.2023",
                academicStatus = "Gowy okaýan / Honors (Rektorat stipendiýasy)",
                email = "s.nazargulyyew@iogu.edu.tm",
                language = "Türkmençe",
                academicYears = listOf(year1, year2, year3),
                allSemesters = allSemesters,
                cumulativeGpa = cumulativeGpa,
                totalCreditsEarned = totalCompletedCredits,
                totalCreditsRequired = 240,
                currentSemesterCredits = sem6.totalCredits,
                completedCoursesCount = completedSemesters.sumOf { it.totalSubjects },
                attendanceSummary = overallAttendance,
                thesisTopic = null,
                academicAdvisor = "D. Gurbanow (Uly mugallym)",
                honorsAchievements = listOf(
                    "Rektoratyň hormat haty (2024)",
                    "Döwlet talyplar ders bäsleşiginiň I derejeli ýeňijisi (Informatika, 2025)",
                    "CyberGuard HNGU-2025 CTF ýeňijisi"
                ),
                schedule = s6Schedule, // Current schedule is strictly Semester 6 schedule
                teacherHistory = teachers,
                exams = exams,
                announcements = announcements,
                isDemoData = true
            )
        }

        // =========================================================================================
        // 2. MERJEN_M (3rd Year, Petroleum Geology, CURRENT SEMESTER = 5)
        // =========================================================================================
        private fun createMerjenComprehensiveData(): OfflineStudentData {
            val sem1Subjects = listOf(
                OfflineSubjectRecord("m1_1", "GEO-101", "Geologiýa giriş", 6, 1, "2024-2025", "G. Baýramowa", "Dosent", "Umumy geologiýa kafedrasy", OfflineGradeComponents(82.0, 86.0, 84.0, null, null, 84.0, 84.0, "B", true), OfflineSubjectAttendance(72, 70, 2, 2, 0, 1, 97.2)),
                OfflineSubjectRecord("m1_2", "CHEM-101", "Umumy himiýa", 6, 1, "2024-2025", "A. Berdimyradow", "Uly mugallym", "Himiýa kafedrasy", OfflineGradeComponents(78.0, 82.0, 80.0, null, 81.0, 79.0, 80.0, "B-", true), OfflineSubjectAttendance(72, 68, 4, 3, 1, 0, 94.4)),
                OfflineSubjectRecord("m1_3", "MATH-101", "Matematika I", 6, 1, "2024-2025", "K. Şamyradow", "Mugallym", "Matematika kafedrasy", OfflineGradeComponents(80.0, 82.0, 81.0, null, null, 81.0, 81.0, "B-", true), OfflineSubjectAttendance(72, 69, 3, 2, 1, 1, 95.8)),
                OfflineSubjectRecord("m1_4", "DRAW-101", "Çyzuw we kartografiýa", 6, 1, "2024-2025", "S. Jumadurdyýew", "Uly mugallym", "Geodeziýa kafedrasy", OfflineGradeComponents(84.0, 86.0, 85.0, null, 86.0, 84.0, 85.0, "B", true), OfflineSubjectAttendance(72, 71, 1, 1, 0, 0, 98.6)),
                OfflineSubjectRecord("m1_5", "LANG-101", "Iňlis dili I", 6, 1, "2024-2025", "M. Ýazmyradowa", "Mugallym", "Diller kafedrasy", OfflineGradeComponents(78.0, 82.0, 80.0, null, null, 80.0, 80.0, "B-", true), OfflineSubjectAttendance(72, 72, 0, 0, 0, 0, 100.0))
            )
            val m1Schedule = listOf(
                OfflineScheduleRecord("m1_sch1", 1, 1, "Duşenbe", "08:30", "10:00", "Geologiýa giriş", "GEO-101", "104", "G. Baýramowa", "Leksiýa"),
                OfflineScheduleRecord("m1_sch2", 1, 2, "Sişenbe", "08:30", "10:00", "Umumy himiýa", "CHEM-101", "108", "A. Berdimyradow", "Leksiýa")
            )
            val sem1 = calculateSemesterRecord("m_sem_1", 1, "2024-2025", 1, "Güýz", "01.09.2024", "25.01.2025", "Completed", sem1Subjects, m1Schedule)

            val sem2Subjects = listOf(
                OfflineSubjectRecord("m2_1", "GEO-102", "Kristallografiýa", 6, 2, "2024-2025", "N. Saparow", "Uly mugallym", "Mineralogiýa kafedrasy", OfflineGradeComponents(85.0, 87.0, 86.0, null, 87.0, 85.0, 86.0, "B+", true), OfflineSubjectAttendance(72, 71, 1, 1, 0, 0, 98.6)),
                OfflineSubjectRecord("m2_2", "GEO-103", "Palaeontologiýa", 6, 2, "2024-2025", "G. Baýramowa", "Dosent", "Umumy geologiýa kafedrasy", OfflineGradeComponents(83.0, 87.0, 85.0, null, null, 85.0, 85.0, "B", true), OfflineSubjectAttendance(72, 70, 2, 2, 0, 1, 97.2)),
                OfflineSubjectRecord("m2_3", "PHYS-101", "Geofiziki esaslar", 6, 2, "2024-2025", "R. Annageldiýew", "Dosent", "Geofizika kafedrasy", OfflineGradeComponents(80.0, 84.0, 82.0, null, 83.0, 81.0, 82.0, "B-", true), OfflineSubjectAttendance(72, 69, 3, 2, 1, 0, 95.8)),
                OfflineSubjectRecord("m2_4", "GEO-104", "Meýdan geologiýa tejribesi", 6, 2, "2024-2025", "G. Baýramowa", "Dosent", "Umumy geologiýa kafedrasy", OfflineGradeComponents(87.0, 89.0, null, 88.0, null, 88.0, 88.0, "B+", true), OfflineSubjectAttendance(72, 71, 1, 1, 0, 0, 98.6)),
                OfflineSubjectRecord("m2_5", "IT-101", "Geoinformatika we GIS", 6, 2, "2024-2025", "J. Nazarow", "Mugallym", "Programmirleme kafedrasy", OfflineGradeComponents(80.0, 83.0, 82.0, null, 82.0, 81.0, 81.5, "B-", true), OfflineSubjectAttendance(72, 71, 1, 1, 0, 0, 98.6))
            )
            val m2Schedule = listOf(
                OfflineScheduleRecord("m2_sch1", 2, 1, "Duşenbe", "08:30", "10:00", "Kristallografiýa", "GEO-102", "104", "N. Saparow", "Leksiýa")
            )
            val sem2 = calculateSemesterRecord("m_sem_2", 2, "2024-2025", 1, "Bahar", "10.02.2025", "30.06.2025", "Completed", sem2Subjects, m2Schedule)

            val sem3Subjects = listOf(
                OfflineSubjectRecord("m3_1", "GEO-201", "Mineralogiýa I", 6, 3, "2025-2026", "N. Saparow", "Uly mugallym", "Mineralogiýa kafedrasy", OfflineGradeComponents(86.0, 88.0, 87.0, null, 87.0, 86.0, 87.0, "B+", true), OfflineSubjectAttendance(72, 71, 1, 1, 0, 0, 98.6)),
                OfflineSubjectRecord("m3_2", "GEO-202", "Taryhy geologiýa", 6, 3, "2025-2026", "G. Baýramowa", "Dosent", "Umumy geologiýa kafedrasy", OfflineGradeComponents(82.0, 86.0, 84.0, null, null, 84.0, 84.0, "B", true), OfflineSubjectAttendance(72, 70, 2, 2, 0, 1, 97.2)),
                OfflineSubjectRecord("m3_3", "GEO-203", "Geodeziýa we topografiýa", 5, 3, "2025-2026", "S. Jumadurdyýew", "Uly mugallym", "Geodeziýa kafedrasy", OfflineGradeComponents(85.0, 87.0, 86.0, null, 86.0, 85.0, 86.0, "B+", true), OfflineSubjectAttendance(60, 59, 1, 1, 0, 0, 98.3)),
                OfflineSubjectRecord("m3_4", "GEO-204", "Litologiýa", 5, 3, "2025-2026", "N. Saparow", "Uly mugallym", "Mineralogiýa kafedrasy", OfflineGradeComponents(80.0, 84.0, 82.0, null, 82.0, 81.0, 82.0, "B-", true), OfflineSubjectAttendance(60, 59, 1, 1, 0, 0, 98.3)),
                OfflineSubjectRecord("m3_5", "GEO-205", "Burgylama geologiýasy", 5, 3, "2025-2026", "B. Kakyshow", "Uly mugallym", "Gidrogeologiýa kafedrasy", OfflineGradeComponents(85.0, 87.0, 86.0, null, null, 86.0, 86.0, "B+", true), OfflineSubjectAttendance(60, 59, 1, 1, 0, 0, 98.3)),
                OfflineSubjectRecord("m3_6", "LANG-201", "Hünär daşary ýurt dili", 5, 3, "2025-2026", "M. Ýazmyradowa", "Mugallym", "Diller kafedrasy", OfflineGradeComponents(83.0, 87.0, 85.0, null, null, 85.0, 85.0, "B", true), OfflineSubjectAttendance(60, 60, 0, 0, 0, 0, 100.0))
            )
            val sem3 = calculateSemesterRecord("m_sem_3", 3, "2025-2026", 2, "Güýz", "01.09.2025", "25.01.2026", "Completed", sem3Subjects, emptyList())

            val sem4Subjects = listOf(
                OfflineSubjectRecord("m4_1", "GEO-206", "Petrografiýa I", 6, 4, "2025-2026", "N. Saparow", "Uly mugallym", "Mineralogiýa kafedrasy", OfflineGradeComponents(85.0, 87.0, 86.0, null, 86.0, 85.0, 86.0, "B+", true), OfflineSubjectAttendance(72, 71, 1, 1, 0, 0, 98.6)),
                OfflineSubjectRecord("m4_2", "GEO-207", "Geohimiýa", 6, 4, "2025-2026", "A. Berdimyradow", "Uly mugallym", "Himiýa kafedrasy", OfflineGradeComponents(82.0, 86.0, 84.0, null, 85.0, 83.0, 84.0, "B", true), OfflineSubjectAttendance(72, 70, 2, 2, 0, 1, 97.2)),
                OfflineSubjectRecord("m4_3", "GEO-208", "Seýsmiki barlaglar", 6, 4, "2025-2026", "R. Annageldiýew", "Dosent", "Geofizika kafedrasy", OfflineGradeComponents(86.0, 88.0, 87.0, null, 87.0, 86.0, 87.0, "B+", true), OfflineSubjectAttendance(72, 71, 1, 1, 0, 0, 98.6)),
                OfflineSubjectRecord("m4_4", "GEO-209", "Nebit-gaz känleriniň geologiýasy", 6, 4, "2025-2026", "G. Baýramowa", "Dosent", "Umumy geologiýa kafedrasy", OfflineGradeComponents(87.0, 89.0, 88.0, null, null, 88.0, 88.0, "B+", true), OfflineSubjectAttendance(72, 70, 2, 2, 0, 0, 97.2)),
                OfflineSubjectRecord("m4_5", "PRACT-202", "Balkan welaýaty meýdan tejribeligi", 6, 4, "2025-2026", "G. Baýramowa", "Dosent", "Umumy geologiýa kafedrasy", OfflineGradeComponents(82.0, 84.0, null, 83.0, null, 83.0, 83.0, "B", true), OfflineSubjectAttendance(72, 71, 1, 1, 0, 0, 98.6))
            )
            val sem4 = calculateSemesterRecord("m_sem_4", 4, "2025-2026", 2, "Bahar", "10.02.2026", "30.06.2026", "Completed", sem4Subjects, emptyList())

            val sem5Subjects = listOf(
                OfflineSubjectRecord("m5_1", "GEO-301", "Umumy geologiýa", 4, 5, "2026-2027", "G. Baýramowa", "Dosent", "Umumy geologiýa kafedrasy", OfflineGradeComponents(84.0, 88.0, 86.0, null, null, 86.0, 86.0, "B", true), OfflineSubjectAttendance(40, 36, 4, 3, 1, 1, 90.0)),
                OfflineSubjectRecord("m5_2", "GEO-302", "Petrografiýa we mineralogiýa", 5, 5, "2026-2027", "N. Saparow", "Uly mugallym", "Mineralogiýa kafedrasy", OfflineGradeComponents(80.0, 85.0, 82.5, null, 83.0, 82.0, 82.5, "B-", true), OfflineSubjectAttendance(50, 44, 6, 4, 2, 2, 88.0)),
                OfflineSubjectRecord("m5_3", "GEO-303", "Geofiziki barlag usullary", 5, 5, "2026-2027", "R. Annageldiýew", "Dosent", "Geofizika kafedrasy", OfflineGradeComponents(88.0, 82.0, 85.0, null, 85.0, 85.0, 85.0, "B", true), OfflineSubjectAttendance(50, 46, 4, 3, 1, 0, 92.0)),
                OfflineSubjectRecord("m5_4", "GEO-304", "Strukturaly geologiýa", 4, 5, "2026-2027", "G. Baýramowa", "Dosent", "Umumy geologiýa kafedrasy", OfflineGradeComponents(85.0, 81.0, 83.0, null, null, 83.0, 83.0, "B", true), OfflineSubjectAttendance(40, 37, 3, 2, 1, 0, 92.5))
            )
            val m5Schedule = listOf(
                OfflineScheduleRecord("m5_sch1", 5, 1, "Duşenbe", "09:00", "10:30", "Umumy geologiýa", "GEO-301", "104", "G. Baýramowa", "Leksiýa"),
                OfflineScheduleRecord("m5_sch2", 5, 2, "Sişenbe", "09:00", "10:30", "Petrografiýa we mineralogiýa", "GEO-302", "108", "N. Saparow", "Laboratoriýa")
            )
            val sem5 = calculateSemesterRecord("m_sem_5", 5, "2026-2027", 3, "Güýz", "01.09.2026", "25.01.2027", "In Progress", sem5Subjects, m5Schedule)

            val allSemesters = listOf(sem1, sem2, sem3, sem4, sem5)
            val completedSemesters = allSemesters.filter { it.status == "Completed" }

            val year1 = OfflineAcademicYearRecord(1, "2024-2025", listOf(sem1, sem2))
            val year2 = OfflineAcademicYearRecord(2, "2025-2026", listOf(sem3, sem4))
            val year3 = OfflineAcademicYearRecord(3, "2026-2027", listOf(sem5))

            val totalCompletedCredits = completedSemesters.sumOf { it.completedCredits }
            val cumulativeGpa = if (totalCompletedCredits > 0) {
                roundOneDecimal(completedSemesters.sumOf { it.semesterGpa * it.completedCredits } / totalCompletedCredits.toDouble())
            } else 0.0

            val totalClassesHeld = allSemesters.sumOf { it.totalClassesHeld }
            val totalAttended = allSemesters.sumOf { it.totalAttended }
            val totalAbsences = allSemesters.sumOf { it.totalAbsences }
            val excusedAbsences = allSemesters.sumOf { it.excusedAbsences }
            val unexcusedAbsences = allSemesters.sumOf { it.unexcusedAbsences }
            val lateArrivals = allSemesters.sumOf { it.lateArrivals }
            val overallPercentage = if (totalClassesHeld > 0) roundOneDecimal((totalAttended.toDouble() / totalClassesHeld.toDouble()) * 100.0) else 100.0

            val overallAttendance = OfflineOverallAttendance(
                totalClassesHeld = totalClassesHeld,
                totalAttended = totalAttended,
                totalAbsences = totalAbsences,
                excusedAbsences = excusedAbsences,
                unexcusedAbsences = unexcusedAbsences,
                lateArrivals = lateArrivals,
                overallPercentage = overallPercentage
            )

            val teachers = listOf(
                OfflineTeacherRecord("m_t1", "G. Baýramowa", "Umumy geologiýa kafedrasy", "Dosent", listOf("Umumy geologiýa", "Strukturaly geologiýa", "Geologiýa giriş", "Palaeontologiýa", "Meýdan geologiýa tejribesi", "Nebit-gaz känleriniň geologiýasy"), listOf(1, 2, 4, 5), true),
                OfflineTeacherRecord("m_t2", "N. Saparow", "Mineralogiýa kafedrasy", "Uly mugallym", listOf("Petrografiýa we mineralogiýa", "Kristallografiýa", "Mineralogiýa I", "Litologiýa", "Petrografiýa I"), listOf(2, 3, 4, 5), true),
                OfflineTeacherRecord("m_t3", "R. Annageldiýew", "Geofizika kafedrasy", "Dosent", listOf("Geofiziki barlag usullary", "Geofiziki esaslar", "Seýsmiki barlaglar"), listOf(2, 4, 5), true),
                OfflineTeacherRecord("m_t4", "B. Kakyshow", "Gidrogeologiýa kafedrasy", "Uly mugallym", listOf("Gidrogeologiýa", "Burgylama geologiýasy"), listOf(3, 5), true)
            )

            val exams = listOf(
                OfflineExamRecord("m_ex1", "Umumy geologiýa", 5, "2026-09-22", "22 Sentýabr, 2026", "10:00", "104", "Final", 6, "G. Baýramowa"),
                OfflineExamRecord("m_ex2", "Petrografiýa we mineralogiýa", 5, "2026-09-26", "26 Sentýabr, 2026", "09:00", "108", "Final", 10, "N. Saparow"),
                OfflineExamRecord("m_ex3", "Geofiziki barlag usullary", 5, "2026-10-05", "05 Oktýabr, 2026", "11:00", "205", "Final", 19, "R. Annageldiýew")
            )

            val announcements = listOf(
                OfflineAnnouncementRecord("m_an1", "Geologiki ekspedisiýa bäsleşigi", "15 Sentýabr", "Bäsleşik", "Ýaş geologlaryň welaýat ekspedisiýasy yglan edildi.", true)
            )

            return OfflineStudentData(
                userId = "Merjen_M",
                fullName = "Merjen Meredowa Begmyradowna",
                username = "Merjen_M",
                studentCardNumber = "HNGU-2024-4102",
                faculty = "Geologiýa fakulteti",
                department = "Umumy geologiýa kafedrasy",
                major = "Nebit-gaz känleriniň geologiýasy we gözlegi",
                degree = "Bakalawr",
                group = "A topary",
                academicYear = "2026-2027",
                currentYear = 3,
                currentSemester = 5,
                enrollmentDate = "01.09.2024",
                academicStatus = "Gowy okaýan",
                email = "m.meredowa@iogu.edu.tm",
                language = "Türkmençe",
                academicYears = listOf(year1, year2, year3),
                allSemesters = allSemesters,
                cumulativeGpa = cumulativeGpa,
                totalCreditsEarned = totalCompletedCredits,
                totalCreditsRequired = 240,
                currentSemesterCredits = sem5.totalCredits,
                completedCoursesCount = completedSemesters.sumOf { it.totalSubjects },
                attendanceSummary = overallAttendance,
                thesisTopic = null,
                academicAdvisor = "G. Baýramowa (Dosent)",
                honorsAchievements = listOf("Uniwersitetiň iň gowy ylmy makalasy (2025)"),
                schedule = m5Schedule,
                teacherHistory = teachers,
                exams = exams,
                announcements = announcements,
                isDemoData = true
            )
        }

        // =========================================================================================
        // 3. BATYR_A (2nd Year, Oil & Gas Engineering, CURRENT SEMESTER = 3)
        // =========================================================================================
        private fun createBatyrComprehensiveData(): OfflineStudentData {
            val sem1Subjects = listOf(
                OfflineSubjectRecord("b1_1", "ENG-101", "Inženerçilik esaslary", 6, 1, "2025-2026", "A. Durdyýew", "Dosent", "Nebit gaz kafedrasy", OfflineGradeComponents(80.0, 84.0, 82.0, null, null, 82.0, 82.0, "B-", true), OfflineSubjectAttendance(72, 69, 3, 2, 1, 0, 95.8)),
                OfflineSubjectRecord("b1_2", "MATH-101", "Inženerçilik matematikasy I", 6, 1, "2025-2026", "T. Beglerow", "Dosent", "Ýokary matematika kafedrasy", OfflineGradeComponents(82.0, 85.0, 83.0, null, null, 84.0, 83.5, "B", true), OfflineSubjectAttendance(72, 70, 2, 2, 0, 1, 97.2)),
                OfflineSubjectRecord("b1_3", "PHYS-101", "Tehiki fizika", 6, 1, "2025-2026", "M. Ýagmyrow", "Uly mugallym", "Umumy fizika kafedrasy", OfflineGradeComponents(79.0, 83.0, 81.0, null, 82.0, 80.0, 81.0, "B-", true), OfflineSubjectAttendance(72, 68, 4, 3, 1, 0, 94.4))
            )
            val b1Schedule = listOf(
                OfflineScheduleRecord("b1_sch1", 1, 1, "Duşenbe", "08:30", "10:00", "Inženerçilik esaslary", "ENG-101", "201", "A. Durdyýew", "Leksiýa")
            )
            val sem1 = calculateSemesterRecord("b_sem_1", 1, "2025-2026", 1, "Güýz", "01.09.2025", "25.01.2026", "Completed", sem1Subjects, b1Schedule)

            val sem2Subjects = listOf(
                OfflineSubjectRecord("b2_1", "ENG-102", "Gidrawlika we gidrogazodinamika", 6, 2, "2025-2026", "A. Durdyýew", "Dosent", "Nebit gaz kafedrasy", OfflineGradeComponents(84.0, 88.0, 86.0, null, 86.0, 85.0, 86.0, "B+", true), OfflineSubjectAttendance(72, 70, 2, 2, 0, 1, 97.2)),
                OfflineSubjectRecord("b2_2", "MATH-102", "Inženerçilik matematikasy II", 6, 2, "2025-2026", "T. Beglerow", "Dosent", "Ýokary matematika kafedrasy", OfflineGradeComponents(81.0, 85.0, 83.0, null, null, 83.0, 83.0, "B", true), OfflineSubjectAttendance(72, 71, 1, 1, 0, 0, 98.6))
            )
            val b2Schedule = listOf(
                OfflineScheduleRecord("b2_sch1", 2, 1, "Duşenbe", "08:30", "10:00", "Gidrawlika we gidrogazodinamika", "ENG-102", "201", "A. Durdyýew", "Leksiýa")
            )
            val sem2 = calculateSemesterRecord("b_sem_2", 2, "2025-2026", 1, "Bahar", "10.02.2026", "30.06.2026", "Completed", sem2Subjects, b2Schedule)

            val sem3Subjects = listOf(
                OfflineSubjectRecord("b3_1", "ENG-201", "Guýulary burawlamak tehnikasy", 6, 3, "2026-2027", "A. Durdyýew", "Dosent", "Nebit gaz kafedrasy", OfflineGradeComponents(85.0, 89.0, 87.0, null, 88.0, 86.0, 87.0, "B+", true), OfflineSubjectAttendance(50, 48, 2, 2, 0, 0, 96.0)),
                OfflineSubjectRecord("b3_2", "ENG-202", "Termodinamika we ýylylyk geçirijilik", 6, 3, "2026-2027", "O. Myradow", "Uly mugallym", "Inženerçilik kafedrasy", OfflineGradeComponents(80.0, 84.0, 82.0, null, null, 82.0, 82.0, "B-", true), OfflineSubjectAttendance(50, 47, 3, 2, 1, 1, 94.0))
            )
            val b3Schedule = listOf(
                OfflineScheduleRecord("b3_sch1", 3, 1, "Duşenbe", "09:00", "10:30", "Guýulary burawlamak tehnikasy", "ENG-201", "201", "A. Durdyýew", "Leksiýa"),
                OfflineScheduleRecord("b3_sch2", 3, 2, "Sişenbe", "09:00", "10:30", "Termodinamika we ýylylyk geçirijilik", "ENG-202", "205", "O. Myradow", "Leksiýa")
            )
            val sem3 = calculateSemesterRecord("b_sem_3", 3, "2026-2027", 2, "Güýz", "01.09.2026", "25.01.2027", "In Progress", sem3Subjects, b3Schedule)

            val allSemesters = listOf(sem1, sem2, sem3)
            val completedSemesters = allSemesters.filter { it.status == "Completed" }

            val year1 = OfflineAcademicYearRecord(1, "2025-2026", listOf(sem1, sem2))
            val year2 = OfflineAcademicYearRecord(2, "2026-2027", listOf(sem3))

            val totalCompletedCredits = completedSemesters.sumOf { it.completedCredits }
            val cumulativeGpa = if (totalCompletedCredits > 0) {
                roundOneDecimal(completedSemesters.sumOf { it.semesterGpa * it.completedCredits } / totalCompletedCredits.toDouble())
            } else 0.0

            val totalClassesHeld = allSemesters.sumOf { it.totalClassesHeld }
            val totalAttended = allSemesters.sumOf { it.totalAttended }
            val totalAbsences = allSemesters.sumOf { it.totalAbsences }
            val excusedAbsences = allSemesters.sumOf { it.excusedAbsences }
            val unexcusedAbsences = allSemesters.sumOf { it.unexcusedAbsences }
            val lateArrivals = allSemesters.sumOf { it.lateArrivals }
            val overallPercentage = if (totalClassesHeld > 0) roundOneDecimal((totalAttended.toDouble() / totalClassesHeld.toDouble()) * 100.0) else 100.0

            val overallAttendance = OfflineOverallAttendance(
                totalClassesHeld = totalClassesHeld,
                totalAttended = totalAttended,
                totalAbsences = totalAbsences,
                excusedAbsences = excusedAbsences,
                unexcusedAbsences = unexcusedAbsences,
                lateArrivals = lateArrivals,
                overallPercentage = overallPercentage
            )

            val teachers = listOf(
                OfflineTeacherRecord("b_t1", "A. Durdyýew", "Nebit gaz kafedrasy", "Dosent", listOf("Inženerçilik esaslary", "Gidrawlika we gidrogazodinamika", "Guýulary burawlamak tehnikasy"), listOf(1, 2, 3), true),
                OfflineTeacherRecord("b_t2", "T. Beglerow", "Ýokary matematika kafedrasy", "Dosent", listOf("Inženerçilik matematikasy I", "Inženerçilik matematikasy II"), listOf(1, 2), false)
            )

            val exams = listOf(
                OfflineExamRecord("b_ex1", "Guýulary burawlamak tehnikasy", 3, "2026-09-25", "25 Sentýabr, 2026", "09:00", "201", "Final", 9, "A. Durdyýew")
            )

            return OfflineStudentData(
                userId = "Batyr_A",
                fullName = "Batyr Amanow Rustamowiç",
                username = "Batyr_A",
                studentCardNumber = "HNGU-2025-1088",
                faculty = "Nebit-gaz fakulteti",
                department = "Nebit-gaz känlerini özleşdirmek kafedrasy",
                major = "Nebit-gaz inženerligi",
                degree = "Bakalawr",
                group = "C topary",
                academicYear = "2026-2027",
                currentYear = 2,
                currentSemester = 3,
                enrollmentDate = "01.09.2025",
                academicStatus = "Gowy okaýan",
                email = "b.amanow@iogu.edu.tm",
                language = "Türkmençe",
                academicYears = listOf(year1, year2),
                allSemesters = allSemesters,
                cumulativeGpa = cumulativeGpa,
                totalCreditsEarned = totalCompletedCredits,
                totalCreditsRequired = 240,
                currentSemesterCredits = sem3.totalCredits,
                completedCoursesCount = completedSemesters.sumOf { it.totalSubjects },
                attendanceSummary = overallAttendance,
                thesisTopic = null,
                academicAdvisor = "A. Durdyýew (Dosent)",
                honorsAchievements = emptyList(),
                schedule = b3Schedule,
                teacherHistory = teachers,
                exams = exams,
                announcements = emptyList(),
                isDemoData = true
            )
        }
    }
}
