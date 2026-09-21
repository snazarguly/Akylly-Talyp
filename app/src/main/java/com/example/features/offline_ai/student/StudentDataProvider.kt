package com.example.features.offline_ai.student

import com.example.features.offline_ai.model.OfflineStudentData
import java.util.Locale

/**
 * Component responsible for providing the current student's data as a context block for the LLM.
 * Strictly enforces the "Semester Rule": No data beyond the current semester.
 */
class StudentDataProvider {

    /**
     * Formats student record into a readable context block.
     * @param student The student data object.
     * @param targetSemester Optional filter for a specific semester.
     */
    fun getStudentContext(student: OfflineStudentData, targetSemester: Int? = null): String {
        val sb = StringBuilder()
        val currentSem = student.currentSemester
        
        sb.append("CURRENT STUDENT PROFILE:\n")
        sb.append("- Full Name: ${student.fullName}\n")
        sb.append("- Major: ${student.major}\n")
        sb.append("- Faculty: ${student.faculty}\n")
        sb.append("- Current Semester: $currentSem\n")
        sb.append("- Cumulative GPA: ${student.cumulativeGpa}%\n\n")

        val semsToInclude = if (targetSemester != null) {
            // Validate semester rule
            if (targetSemester > currentSem) {
                return "ACCESS DENIED: Requested semester $targetSemester is in the future."
            }
            student.allSemesters.filter { it.semesterNumber == targetSemester }
        } else {
            // Include all semesters up to current
            student.allSemesters.filter { it.semesterNumber <= currentSem }
        }

        sb.append("ACADEMIC RECORDS:\n")
        semsToInclude.forEach { sem ->
            sb.append("--- Semester ${sem.semesterNumber} ---\n")
            sb.append("- Status: ${sem.status}\n")
            sb.append("- GPA: ${sem.semesterGpa}%\n")
            sb.append("- Subjects:\n")
            sem.subjects.forEach { sub ->
                sb.append("  • ${sub.name}: ${sub.grades.finalScore}% [${sub.grades.letterGrade}]\n")
            }
            sb.append("\n")
        }

        return sb.toString()
    }

    /**
     * Provides today's schedule context.
     */
    fun getTodayScheduleContext(student: OfflineStudentData): String {
        val schedule = student.schedule
        if (schedule.isEmpty()) return "No classes scheduled for today."
        
        val sb = StringBuilder()
        sb.append("TODAY'S SCHEDULE:\n")
        schedule.forEach { s ->
            sb.append("- ${s.startTime}-${s.endTime}: ${s.subjectName} (Room: ${s.classroom}, Teacher: ${s.teacherName})\n")
        }
        return sb.toString()
    }
}
