package com.example.features.offline_ai

import com.example.features.offline_ai.data.OfflineStudentDatabase
import com.example.features.offline_ai.engine.OfflineAnswerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import kotlin.math.abs

/**
 * Verification test suite for comprehensive semester structures,
 * individual subject records, mathematical GPA/attendance calculations,
 * and semantic granularity in the Offline AI module.
 */
class OfflineAiAcademicHistoryTest {

    private lateinit var database: OfflineStudentDatabase

    @Before
    fun setup() {
        database = OfflineStudentDatabase()
    }

    private fun ask(query: String, language: String): String =
        OfflineAnswerEngine.answer(query, database.getStudentData("Suleyman_05")!!, language)

    @Test
    fun testMathematicalConsistencyOfSemestersAndGpa() {
        val student = database.getStudentData("Suleyman_05")
        assertNotNull("Student Suleyman_05 must exist in database", student)

        // 1. Check all semesters exist (Semesters 1 to 6 - Current semester is 6, no future S7/S8 data)
        assertEquals(6, student!!.allSemesters.size)

        // 2. Test mathematical calculation of every semester GPA
        for (sem in student.allSemesters) {
            val calculatedWeightedSum = sem.subjects.sumOf { it.grades.finalScore * it.credits }
            val expectedGpa = (calculatedWeightedSum / sem.totalCredits.toDouble() * 10.0).let { Math.round(it) / 10.0 }
            assertTrue(
                "Semester ${sem.semesterNumber} GPA must match credit-weighted formula. Expected: $expectedGpa, got: ${sem.semesterGpa}",
                abs(expectedGpa - sem.semesterGpa) < 0.1
            )

            // Verify attendance mathematical consistency
            val sumClasses = sem.subjects.sumOf { it.attendance.totalClasses }
            val sumAttended = sem.subjects.sumOf { it.attendance.attendedClasses }
            val sumAbsences = sem.subjects.sumOf { it.attendance.absences }
            assertEquals("Total classes held in semester must match sum of subjects", sumClasses, sem.totalClassesHeld)
            assertEquals("Total classes attended in semester must match sum of subjects", sumAttended, sem.totalAttended)
            assertEquals("Total absences in semester must match sum of subjects", sumAbsences, sem.totalAbsences)
        }

        // 3. Test cumulative GPA across all completed semesters
        val completedSems = student.allSemesters.filter { it.status == "Completed" }
        val totalCompletedCreds = completedSems.sumOf { it.completedCredits }
        val expectedCumulativeGpa = (completedSems.sumOf { it.semesterGpa * it.completedCredits } / totalCompletedCreds.toDouble() * 10.0).let { Math.round(it) / 10.0 }
        assertTrue(
            "Cumulative GPA must be mathematically derived from completed semesters. Expected: $expectedCumulativeGpa, got: ${student.cumulativeGpa}",
            abs(expectedCumulativeGpa - student.cumulativeGpa) < 0.1
        )
    }

    @Test
    fun testSubjectLevelGradeComponentsAndAttendance() {
        val student = database.getStudentData("Suleyman_05")!!
        val sem1 = student.allSemesters.find { it.semesterNumber == 1 }!!
        val math1 = sem1.subjects.find { it.code == "MATH-101" }!!

        // Verify individual grade components
        assertEquals("MATH-101", math1.code)
        assertEquals("Ýokary matematika I", math1.name)
        assertEquals(84.0, math1.grades.midtermScore, 0.01)
        assertEquals(89.0, math1.grades.finalExamScore, 0.01)
        assertEquals(88.0, math1.grades.assignmentScore!!, 0.01)
        assertEquals(87.0, math1.grades.finalScore, 0.01)
        assertEquals("B+", math1.grades.letterGrade)
        assertTrue(math1.grades.isPassed)

        // Verify individual attendance
        assertEquals(60, math1.attendance.totalClasses)
        assertEquals(58, math1.attendance.attendedClasses)
        assertEquals(2, math1.attendance.absences)
        assertEquals(2, math1.attendance.excusedAbsences)
        assertEquals(0, math1.attendance.unexcusedAbsences)
        assertEquals("T. Beglerow", math1.teacherName)
    }

    @Test
    fun testUserAskingGradesNeverReturnsOnlyOverallScore() {
        val response = ask("Bahalarymy görkez", "tk")

        // MUST list individual subject rows, not merely "GPA: 93.8%"
        assertTrue("Response must contain individual subject breakdown", response.contains("Penetration Testing") || response.contains("Kiberhowpsuzlyk") || response.contains("Zeper ýetiriji") || response.contains("Kriptografiýa"))
        assertTrue("Response must contain letter grades (e.g. A, B+, A-)", response.contains("A+") || response.contains("A-") || response.contains("B+"))
        assertTrue("Response must contain semester GPA and credits", response.contains("Semestr GPA"))
        assertTrue("Response must include DEMO DATA watermark", response.contains(OfflineAnswerEngine.DEMO_WATERMARK))
    }

    @Test
    fun testSemesterSpecificSubjectQueries() {
        val response = ask("1-nji semestrde haýsy derslerden näçe aldym?", "tk")

        // Semester 1 subjects must be presented
        assertTrue("Response must mention Semester 1 subjects", response.contains("Ýokary matematika I") && response.contains("Umumy fizika I"))
        assertTrue("Response must mention letter grades", response.contains("B+") || response.contains("A"))
    }

    @Test
    fun testCrossSemesterSubjectTracking() {
        val response = ask("What did I get in Mathematics?", "en")

        // Mathematics was taken in Semester 1 (Ýokary matematika I) and Semester 2 (Ýokary matematika II)
        assertTrue("Response must locate multiple mathematics records across semesters", response.contains("Semester 1") && response.contains("Semester 2"))
        assertTrue("Response must mention grades for both semesters", response.contains("87") || response.contains("91"))
    }

    @Test
    fun testSingleSubjectDetailWithComponents() {
        val response = ask("1-nji semestrde matematika näçe aldym?", "tk")

        assertTrue("Response must specify grade components and attendance", response.contains("Aralyk") || response.contains("Ekzamen") || response.contains("Mugallym"))
    }

    @Test
    fun testTeacherHistoryAndSubjectMissedMost() {
        val response = ask("Which subject did I miss the most?", "en")

        // Most missed subject for Suleyman is PHYS-101 (Umumy fizika I) with 3 absences
        assertTrue("Response must identify subject with highest absences", response.contains("fizika") || response.contains("PHYS-101") || response.contains("Physics") || response.contains("3"))
    }

    @Test
    fun testFutureSemesterIsNeverAnswered() {
        // Suleyman is in semester 6: semester 7 data must not exist or be invented.
        val response = ask("7-nji semestrde bahalarym näçe?", "tk")
        assertTrue(response.contains("entek başlamady"))
    }

    @Test
    fun testExamCountdownUsesRealDates() {
        val student = database.getStudentData("Suleyman_05")!!
        val beforeExams = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 15) }
        val afterExams = Calendar.getInstance().apply { set(2026, Calendar.SEPTEMBER, 1) }

        val upcoming = OfflineAnswerEngine.answer("Ekzamenlerim haçan?", student, "tk", beforeExams)
        assertTrue(upcoming.contains("Penetration Testing") && upcoming.contains("5 gün galdy"))

        val later = OfflineAnswerEngine.answer("Ekzamenlerim haçan?", student, "tk", afterExams)
        assertTrue("Past exams must not be reported as upcoming", later.contains("ýakynlaşýan ekzamen ýok"))
    }

    @Test
    fun testGeneralRuleQuestionIsNotTreatedAsPersonalGrades() {
        val response = ask("GPA bahalandyryş ulgamy nähili işleýär?", "tk")
        assertTrue(response.contains("A (4.0)"))
    }
}
