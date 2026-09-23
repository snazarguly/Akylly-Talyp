package com.example.features.study

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.core.model.ExamItem
import com.example.core.model.ExamType
import com.example.core.model.LessonType
import com.example.core.model.QuizQuestion
import com.example.core.model.QuizSet
import com.example.core.model.ScheduleItem
import com.example.core.model.StudentProfile
import com.example.core.model.SubjectItem
import com.example.core.util.CampusCalendar
import com.example.ui.components.AppCard
import com.example.ui.components.AppPrimaryButton
import com.example.ui.components.AppSecondaryButton
import com.example.ui.components.AppSectionHeader
import com.example.ui.components.AppSegmentedControl
import com.example.ui.components.AppStatusBadge
import com.example.ui.components.AppTopHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.AppComponentSize
import com.example.ui.theme.AppIconSize
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppTypography
import com.example.ui.theme.PrimaryBrand
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusErrorContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningContainer

/**
 * Study Hub Screen (Okuw Bölümi)
 *
 * Exclusively provides 3 distinct academic sections:
 * 1. Raspisaniýa (Weekly timetable & lessons by day)
 * 2. Ekzamen wagty (Exams timetable with countdown & room information)
 * 3. Bahalar (Cumulative GPA, semester progression, and subject grade breakdown)
 */
@Composable
fun StudyHubScreen(
    strings: AppStrings,
    scheduleList: List<ScheduleItem>,
    subjectsList: List<SubjectItem>,
    examsList: List<ExamItem>,
    student: StudentProfile,
    unreadCount: Int,
    selectedSubTab: Int,
    onSubTabChange: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = Raspisaniýa, 1 = Ekzamen wagty, 2 = Bahalar, 3 = Testler.
    // Hoisted to the caller so "open exams" from Home/Search/Notifications always switches the tab.
    val selectedTab = selectedSubTab.coerceIn(0, 3)

    // Schedule Day Filter: 1=Duş … 6=Şen; starts on today (Sunday falls back to Monday)
    var selectedDayOfWeek by rememberSaveable {
        mutableIntStateOf(CampusCalendar.todayDayOfWeek().let { if (it == 7) 1 else it })
    }

    // Exam Filter: 0=Ählisi, 1=Ýakynlaşýan, 2=Geçen
    var selectedExamFilter by rememberSaveable { mutableIntStateOf(0) }

    // Grades Semester Filter: defaults to the latest semester that has subjects
    var selectedSemester by rememberSaveable { mutableIntStateOf(subjectsList.maxOfOrNull { it.semester } ?: 1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("study_hub_screen")
    ) {
        // App Top Header
        AppTopHeader(
            title = strings.navStudy,
            subtitle = strings.universityShort,
            strings = strings,
            unreadCount = unreadCount,
            onSearchClick = onSearchClick,
            onNotificationClick = onNotificationClick
        )

        // 4-Way Segmented Control: Raspisaniýa, Ekzamen, Bahalar, Testler
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs)
        ) {
            AppSegmentedControl(
                items = listOf("Raspisaniýa", "Ekzamen", "Bahalar", "Testler"),
                selectedIndex = selectedTab,
                onItemSelected = onSubTabChange,
                testTag = "study_hub_segmented_tabs"
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.xxs))

        when (selectedTab) {
            0 -> ScheduleSection(
                scheduleList = scheduleList,
                selectedDayOfWeek = selectedDayOfWeek,
                onDaySelected = { selectedDayOfWeek = it },
                strings = strings
            )
            1 -> ExamsSection(
                examsList = examsList,
                filterIndex = selectedExamFilter,
                onFilterChanged = { selectedExamFilter = it },
                strings = strings
            )
            2 -> GradesSection(
                student = student,
                subjectsList = subjectsList,
                selectedSemester = selectedSemester,
                onSemesterSelected = { selectedSemester = it },
                strings = strings
            )
            3 -> QuizzesSection(
                strings = strings,
                subjectsList = subjectsList
            )
        }
    }
}

// -------------------------------------------------------------
// 1. RASPISANIÝA (SCHEDULE) SECTION
// -------------------------------------------------------------

@Composable
private fun ScheduleSection(
    scheduleList: List<ScheduleItem>,
    selectedDayOfWeek: Int,
    onDaySelected: (Int) -> Unit,
    strings: AppStrings
) {
    val days = listOf("Duş", "Siş", "Çar", "Pen", "Ann", "Şen").mapIndexed { index, name ->
        val dayNum = index + 1
        Triple(dayNum, name, CampusCalendar.dayOfMonthInCurrentWeek(dayNum).toString())
    }

    val dayLessons = scheduleList
        .filter { it.dayOfWeek == selectedDayOfWeek }
        .sortedBy { CampusCalendar.minutesOfDay(it.startTime) ?: Int.MAX_VALUE }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Day Selector
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            items(days) { (dayNum, dayName, dayDate) ->
                val isSelected = selectedDayOfWeek == dayNum
                Surface(
                    shape = RoundedCornerShape(AppRadius.medium),
                    color = if (isSelected) PrimaryBrand else MaterialTheme.colorScheme.surface,
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .clickable { onDaySelected(dayNum) }
                        .testTag("day_selector_$dayNum")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayName,
                            style = AppTypography.Caption.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dayDate,
                            style = AppTypography.Headline.copy(fontSize = 15.sp),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.xs))

        if (dayLessons.isEmpty()) {
            EmptyStateView(
                title = "Şu gün sapak meýilleşdirilmedik",
                subtitle = "Bellenen günde hiç hili okuw sapagy ýok. Dynç alyş ýa-da özbaşdak taýýarlyk güni."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
            ) {
                item {
                    Text(
                        text = "Sapaklar tertibi (${dayLessons.size} sapak)",
                        style = AppTypography.TitleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(dayLessons) { lesson ->
                    LessonCardItem(lesson = lesson)
                }

                item {
                    Spacer(modifier = Modifier.height(AppComponentSize.bottomNavContentPadding))
                }
            }
        }
    }
}

@Composable
private fun LessonCardItem(lesson: ScheduleItem) {
    val typeBadgeColor = when (lesson.type) {
        LessonType.LECTURE -> PrimaryBrand
        LessonType.PRACTICE -> Color(0xFF1976D2)
        LessonType.LAB -> Color(0xFF7B1FA2)
    }

    val typeBadgeText = when (lesson.type) {
        LessonType.LECTURE -> "Leksiýa"
        LessonType.PRACTICE -> "Tejribe"
        LessonType.LAB -> "Tejribehana"
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_card_${lesson.id}")
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardInternal)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time range with icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = PrimaryBrand,
                        modifier = Modifier.size(AppIconSize.small)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xxs))
                    Text(
                        text = "${lesson.startTime} - ${lesson.endTime}",
                        style = AppTypography.Headline.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Lesson Type Badge
                AppStatusBadge(
                    text = typeBadgeText,
                    color = typeBadgeColor
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.s))

            Text(
                text = lesson.subjectName,
                style = AppTypography.Title.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(AppSpacing.s))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Classroom
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppIconSize.small)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xxs))
                    Text(
                        text = lesson.classroom,
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Teacher
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppIconSize.small)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xxs))
                    Text(
                        text = lesson.teacherName,
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. EKZAMEN WAGTY (EXAMS) SECTION
// -------------------------------------------------------------

@Composable
private fun ExamsSection(
    examsList: List<ExamItem>,
    filterIndex: Int,
    onFilterChanged: (Int) -> Unit,
    strings: AppStrings
) {
    val filters = listOf("Ählisi", "Ýakynlaşýan", "Geçen")

    val filteredExams = examsList.filter { exam ->
        when (filterIndex) {
            1 -> !exam.isPast
            2 -> exam.isPast
            else -> true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            items(filters.size) { index ->
                val isSelected = filterIndex == index
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterChanged(index) },
                    label = {
                        Text(
                            text = filters[index],
                            style = AppTypography.Caption.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBrand,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(AppRadius.medium)
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.xxs))

        if (filteredExams.isEmpty()) {
            EmptyStateView(
                title = "Synaglar tapylmady",
                subtitle = "Bellenen filtr boýunça hiç hili ekzamen ýok."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
            ) {
                item {
                    Text(
                        text = "Ekzamenler we synag seneleri (${filteredExams.size})",
                        style = AppTypography.TitleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(filteredExams) { exam ->
                    ExamCardItem(exam = exam)
                }

                item {
                    Spacer(modifier = Modifier.height(AppComponentSize.bottomNavContentPadding))
                }
            }
        }
    }
}

@Composable
private fun ExamCardItem(exam: ExamItem) {
    val isFinal = exam.type == ExamType.FINAL

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exam_card_${exam.id}")
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardInternal)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exam type badge
                AppStatusBadge(
                    text = if (isFinal) "Jemleýji synag" else "Aralyk synag",
                    color = if (isFinal) StatusWarning else PrimaryBrand
                )

                // Countdown badge
                val (badgeBg, badgeText, badgeColor) = when {
                    exam.isPast -> Triple(MaterialTheme.colorScheme.surfaceVariant, "Geçen", MaterialTheme.colorScheme.onSurfaceVariant)
                    exam.daysLeft <= 1 -> Triple(StatusErrorContainer, "Ertir synag!", StatusError)
                    else -> Triple(StatusWarningContainer, "${exam.daysLeft} gün galdy", StatusWarning)
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(AppRadius.small)
                ) {
                    Text(
                        text = badgeText,
                        style = AppTypography.Caption.copy(fontWeight = FontWeight.Bold),
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.s))

            Text(
                text = exam.subjectName,
                style = AppTypography.Title.copy(fontSize = 17.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(AppSpacing.s))

            // Date & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = PrimaryBrand,
                        modifier = Modifier.size(AppIconSize.small)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xxs))
                    Text(
                        text = "${exam.displayDate}, ${exam.time}",
                        style = AppTypography.BodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Classroom
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppIconSize.small)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xxs))
                    Text(
                        text = exam.classroom,
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xxs))

            // Teacher
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(AppIconSize.small)
                )
                Spacer(modifier = Modifier.width(AppSpacing.xxs))
                Text(
                    text = exam.teacherName,
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 3. BAHALAR (GRADES & GPA) SECTION
// -------------------------------------------------------------

@Composable
private fun GradesSection(
    student: StudentProfile,
    subjectsList: List<SubjectItem>,
    selectedSemester: Int,
    onSemesterSelected: (Int) -> Unit,
    strings: AppStrings
) {
    // Only offer semesters that actually have subjects, and show just that semester's subjects.
    val semesters = subjectsList.map { it.semester }.distinct().sorted()
    val semesterSubjects = subjectsList.filter { it.semester == selectedSemester }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
    ) {
        // GPA Overview Card
        item {
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gpa_overview_card")
            ) {
                Column(modifier = Modifier.padding(AppSpacing.cardInternal)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Jemi Ortaça Baha (GPA)",
                                style = AppTypography.Caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${student.cumulativeGpa} / 100",
                                style = AppTypography.Display.copy(fontSize = 28.sp, color = PrimaryBrand)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = StatusSuccessContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = StatusSuccess,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.s))

                    LinearProgressIndicator(
                        progress = { (student.cumulativeGpa / 100.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(AppRadius.full)),
                        color = PrimaryBrand,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.m))

                    // Academic Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Gazanylan kredit",
                                style = AppTypography.Caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${student.totalCreditsEarned} kredit",
                                style = AppTypography.Headline.copy(fontSize = 15.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Geçilen dersler",
                                style = AppTypography.Caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${student.completedCoursesCount} ders",
                                style = AppTypography.Headline.copy(fontSize = 15.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Semester Selector Chips
        item {
            Column {
                Text(
                    text = "Semestr boýunça bahalar",
                    style = AppTypography.TitleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    semesters.forEach { sem ->
                        val isSelected = selectedSemester == sem
                        Surface(
                            shape = RoundedCornerShape(AppRadius.medium),
                            color = if (isSelected) PrimaryBrand else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSemesterSelected(sem) }
                                .testTag("semester_filter_$sem")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$sem-nji semestr",
                                    style = AppTypography.Caption.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Subject Grades List
        items(semesterSubjects) { subject ->
            SubjectGradeCard(subject = subject)
        }

        item {
            Spacer(modifier = Modifier.height(AppComponentSize.bottomNavContentPadding))
        }
    }
}

@Composable
private fun SubjectGradeCard(subject: SubjectItem) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_grade_${subject.id}")
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardInternal)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        style = AppTypography.Title.copy(fontSize = 16.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${subject.code} • ${subject.credits} kredit • ${subject.teacherName}",
                        style = AppTypography.Caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(AppRadius.small),
                    color = if (subject.isPassed) StatusSuccessContainer else StatusErrorContainer
                ) {
                    Text(
                        text = "${subject.currentGrade.toInt()}%",
                        style = AppTypography.Headline.copy(
                            fontSize = 15.sp,
                            color = if (subject.isPassed) StatusSuccess else StatusError
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.s))

            LinearProgressIndicator(
                progress = { (subject.currentGrade / 100.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(AppRadius.full)),
                color = if (subject.isPassed) PrimaryBrand else StatusError,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(AppSpacing.s))

            // Breakdown of Scores: Midterm, Final, Exam
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Aralyk", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${subject.midtermScore.toInt()}/100", style = AppTypography.BodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Jemleýji", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${subject.finalScore.toInt()}/100", style = AppTypography.BodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Synag", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${subject.examScore.toInt()}/100", style = AppTypography.BodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. TESTLER (QUIZZES) SECTION
// -------------------------------------------------------------

@Composable
private fun QuizzesSection(
    strings: AppStrings,
    subjectsList: List<SubjectItem>
) {
    val quizSets = remember { getDemoQuizSets() }
    var activeQuiz by remember { mutableStateOf<QuizSet?>(null) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var isQuizCompleted by remember { mutableStateOf(false) }

    fun resetQuiz() {
        currentQuestionIndex = 0
        selectedOption = null
        isSubmitted = false
        correctCount = 0
        isQuizCompleted = false
    }

    if (activeQuiz != null) {
        val currentQuiz = activeQuiz!!
        val questions = currentQuiz.questions

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap)
        ) {
            item {
                Spacer(modifier = Modifier.height(AppSpacing.s))

                // Quiz Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentQuiz.title,
                            style = AppTypography.Title.copy(fontSize = 17.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${currentQuiz.subjectName} • ${currentQuiz.durationMinutes} min",
                            style = AppTypography.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                activeQuiz = null
                                resetQuiz()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Ýap",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (!isQuizCompleted) {
                val currentQ = questions[currentQuestionIndex]
                val progress = (currentQuestionIndex + 1).toFloat() / questions.size

                item {
                    // Progress bar & counter
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sorag ${currentQuestionIndex + 1} / ${questions.size}",
                                style = AppTypography.Caption.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryBrand
                            )
                            Text(
                                text = "${((currentQuestionIndex + 1) * 100 / questions.size)}%",
                                style = AppTypography.Caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(AppRadius.full)),
                            color = PrimaryBrand,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                item {
                    // Question Card
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(AppSpacing.cardInternal)) {
                            Text(
                                text = currentQ.question,
                                style = AppTypography.Title.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Options
                items(currentQ.options.size) { optionIndex ->
                    val optionText = currentQ.options[optionIndex]
                    val isSelected = selectedOption == optionIndex
                    val isCorrect = optionIndex == currentQ.correctOptionIndex

                    val borderColor = when {
                        isSubmitted && isCorrect -> StatusSuccess
                        isSubmitted && isSelected && !isCorrect -> StatusError
                        isSelected -> PrimaryBrand
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }

                    val containerColor = when {
                        isSubmitted && isCorrect -> StatusSuccessContainer.copy(alpha = 0.5f)
                        isSubmitted && isSelected && !isCorrect -> StatusErrorContainer.copy(alpha = 0.5f)
                        isSelected -> PrimaryBrand.copy(alpha = 0.08f)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isSubmitted) {
                                selectedOption = optionIndex
                            },
                        shape = RoundedCornerShape(AppRadius.medium),
                        color = containerColor,
                        border = BorderStroke(if (isSelected || (isSubmitted && isCorrect)) 1.5.dp else 1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = AppSpacing.m, vertical = AppSpacing.s),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val optionPrefix = when (optionIndex) {
                                0 -> "A"
                                1 -> "B"
                                2 -> "C"
                                3 -> "D"
                                else -> "${optionIndex + 1}"
                            }

                            Surface(
                                shape = CircleShape,
                                color = when {
                                    isSubmitted && isCorrect -> StatusSuccess
                                    isSubmitted && isSelected && !isCorrect -> StatusError
                                    isSelected -> PrimaryBrand
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = optionPrefix,
                                        style = AppTypography.Caption.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected || isSubmitted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(AppSpacing.s))

                            Text(
                                text = optionText,
                                style = AppTypography.Body.copy(fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (isSubmitted && currentQ.explanation.isNotBlank()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(AppRadius.small),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(AppSpacing.s),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = PrimaryBrand,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.xs))
                                Text(
                                    text = currentQ.explanation,
                                    style = AppTypography.Caption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(AppSpacing.xs))

                    if (!isSubmitted) {
                        AppPrimaryButton(
                            text = "Jogaby barla",
                            onClick = {
                                if (selectedOption != null) {
                                    isSubmitted = true
                                    if (selectedOption == currentQ.correctOptionIndex) {
                                        correctCount++
                                    }
                                }
                            },
                            enabled = selectedOption != null,
                            leadingIcon = Icons.Default.Check,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "quiz_check_button"
                        )
                    } else {
                        val isLastQuestion = currentQuestionIndex == questions.size - 1
                        AppPrimaryButton(
                            text = if (isLastQuestion) "Netijäni gör" else "Indiki sorag",
                            onClick = {
                                if (isLastQuestion) {
                                    isQuizCompleted = true
                                } else {
                                    currentQuestionIndex++
                                    selectedOption = null
                                    isSubmitted = false
                                }
                            },
                            leadingIcon = Icons.Default.ArrowForward,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "quiz_next_button"
                        )
                    }
                }
            } else {
                // Quiz Completed Result Card
                item {
                    val percent = (correctCount * 100) / questions.size
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.l),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (percent >= 70) StatusSuccessContainer else StatusWarningContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (percent >= 70) StatusSuccess else StatusWarning,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(AppSpacing.s))

                            Text(
                                text = if (percent >= 70) "Gaty gowy!" else "Gaýtadan synanyşyň!",
                                style = AppTypography.LargeTitle.copy(fontSize = 20.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "$correctCount / ${questions.size} dogry jogap ($percent%)",
                                style = AppTypography.Title.copy(fontSize = 18.sp, color = PrimaryBrand)
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.xs))

                            Text(
                                text = if (percent >= 70)
                                    "Siz bu tema boýunça synaga doly taýýarsyňyz."
                                else
                                    "Dersiň temalaryny gaýtadan okap, testi täzeden geçmegi maslahat berýäris.",
                                style = AppTypography.BodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.m))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s)
                            ) {
                                AppSecondaryButton(
                                    text = "Täzeden",
                                    onClick = { resetQuiz() },
                                    leadingIcon = Icons.Default.Replay,
                                    modifier = Modifier.weight(1f)
                                )

                                AppPrimaryButton(
                                    text = "Testler sanawy",
                                    onClick = {
                                        activeQuiz = null
                                        resetQuiz()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(AppComponentSize.bottomNavContentPadding))
            }
        }
    } else {
        // Quiz Catalog List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap)
        ) {
            item {
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                AppSectionHeader(
                    title = "Synaglara taýýarlyk testleri"
                )
            }

            items(quizSets) { quiz ->
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_card_${quiz.id}")
                ) {
                    Column(modifier = Modifier.padding(AppSpacing.cardInternal)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(AppRadius.small),
                                color = PrimaryBrand.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = quiz.subjectName,
                                    style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = PrimaryBrand,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${quiz.durationMinutes} min",
                                    style = AppTypography.Caption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.xs))

                        Text(
                            text = quiz.title,
                            style = AppTypography.Title.copy(fontSize = 16.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = quiz.description,
                            style = AppTypography.BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.s))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${quiz.questions.size} sany sorag",
                                style = AppTypography.Caption.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            AppPrimaryButton(
                                text = "Teste başla",
                                onClick = {
                                    activeQuiz = quiz
                                    resetQuiz()
                                },
                                leadingIcon = Icons.Default.PlayArrow,
                                modifier = Modifier.height(38.dp),
                                testTag = "start_quiz_${quiz.id}"
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(AppComponentSize.bottomNavContentPadding))
            }
        }
    }
}

private fun getDemoQuizSets(): List<QuizSet> {
    return listOf(
        QuizSet(
            id = "quiz_cyber",
            subjectName = "Kiberhowpsuzlyk",
            title = "Kiberhowpsuzlyk we kriptografiýa",
            description = "Şifirleme usullary, tor howpsuzlygy we goranmak çäreleri boýunça synag soraglary.",
            durationMinutes = 10,
            questions = listOf(
                QuizQuestion(
                    id = "q1",
                    question = "Kriptografiýada maglumaty şifirlemek we açmak üçin birmeňzeş açar ulanylýan usula näme diýilýär?",
                    options = listOf(
                        "Simmetrik şifirleme",
                        "Asimmetrik şifirleme",
                        "Heşleme (Hashing)",
                        "Sanly golçekme"
                    ),
                    correctOptionIndex = 0,
                    explanation = "Simmetrik şifirlemede maglumaty şifirlemek we açmak üçin şol bir gizlin açar ulanylýar (mysal üçin AES)."
                ),
                QuizQuestion(
                    id = "q2",
                    question = "\"Phishing\" (Fişing) hüjüminiň esasy maksady näme?",
                    options = listOf(
                        "Serweri aşa ýüklemek (DDoS)",
                        "Ulanyjynyň şahsy maglumatlaryny aldaw ýoly bilen ele salmak",
                        "Faýllary şifirläp pul talap etmek",
                        "Enjamyň mikroshemasyny zaýalamak"
                    ),
                    correctOptionIndex = 1,
                    explanation = "Fişing — aldawçylyk sahypalary ýa-da hatlary arkaly login, parol we gizlin maglumatlary ogurlamakdyr."
                ),
                QuizQuestion(
                    id = "q3",
                    question = "Tor howpsuzlygynda \"Firewall\" (Mawut diwary) esasy wezipesi näme?",
                    options = listOf(
                        "Maglumatlar bazasyny saklamak",
                        "Gelýän we gidýän trafigi howpsuzlyk düzgünlerine görä süzmek",
                        "Kompýuteriň temperaturasyny ölçeýän enjam",
                        "Internet tizligini çäklendirmek"
                    ),
                    correctOptionIndex = 1,
                    explanation = "Firewall tor trafigini seljerip, zyýanly paketleri we rugsat berilmedik elýeterliligi bloklaýar."
                ),
                QuizQuestion(
                    id = "q4",
                    question = "HTTPS protokolynyň adaty HTTP-den esasy tapawudy nämede?",
                    options = listOf(
                        "Trafigiň TLS/SSL arkaly şifirlenmegi",
                        "Sahypanyň diňe suratlaryny açmagy",
                        "Internet birikmesiz offline işlemegi",
                        "Diňe wideo faýllary geçirmegi"
                    ),
                    correctOptionIndex = 0,
                    explanation = "HTTPS maglumat alyş-çalşygyny şifirlemek üçin SSL/TLS sertifikatlaryny ulanýar."
                )
            )
        ),
        QuizSet(
            id = "quiz_db",
            subjectName = "Maglumatlar bazasy",
            title = "Relasion bazalar we SQL esaslary",
            description = "Tablisalar, Primary Key, JOIN amallary we ACID ýörelgeleri boýunça barlag.",
            durationMinutes = 10,
            questions = listOf(
                QuizQuestion(
                    id = "qdb1",
                    question = "Relasion maglumatlar bazasynda setiri birkemsiz kesgitleýän meýdan näme?",
                    options = listOf(
                        "Foreign Key",
                        "Primary Key",
                        "Index",
                        "Trigger"
                    ),
                    correctOptionIndex = 1,
                    explanation = "Primary Key (Esasy açar) tablisadaky her bir ýazgynyň gaýtalanmajak bolmagyny üpjün edýär."
                ),
                QuizQuestion(
                    id = "qdb2",
                    question = "SQL-de tablisadan maglumatlary saýlap almak üçin haýsy operator ulanylýar?",
                    options = listOf(
                        "INSERT",
                        "UPDATE",
                        "SELECT",
                        "DELETE"
                    ),
                    correctOptionIndex = 2,
                    explanation = "SELECT buýrugy bazadaky tablisalardan maglumatlary okamak we görkezmek üçin esasy buýrukdyr."
                ),
                QuizQuestion(
                    id = "qdb3",
                    question = "Iki tablisany umumy sütün arkaly birikdirmek üçin näme ulanylýar?",
                    options = listOf(
                        "JOIN",
                        "UNION",
                        "GROUP BY",
                        "ORDER BY"
                    ),
                    correctOptionIndex = 0,
                    explanation = "JOIN amaly (INNER, LEFT, RIGHT) iki ýa-da has köp tablisany baglanyşdyrýar."
                ),
                QuizQuestion(
                    id = "qdb4",
                    question = "ACID düzgünler toplumynda \"A\" nämäni aňladýar?",
                    options = listOf(
                        "Atomicity (Bütewilik)",
                        "Accuracy (Takyklyk)",
                        "Availability (Elýeterlilik)",
                        "Authentication (Tassyklama)"
                    ),
                    correctOptionIndex = 0,
                    explanation = "Atomicity tranzaksiýanyň ýa doly ýerine ýetmegini, ýa-da asla ýerine ýetmezligini kepillendirýär."
                )
            )
        ),
        QuizSet(
            id = "quiz_prog",
            subjectName = "Programmirleme",
            title = "Kotlin & Jetpack Compose",
            description = "Kotlin sintaksisi, null safety, coroutines we häzirki zaman Android gurluşy.",
            durationMinutes = 10,
            questions = listOf(
                QuizQuestion(
                    id = "qp1",
                    question = "Kotlin dilinde bahasy üýtgewsiz (read-only) üýtgeýji haýsy söz bilen yglan edilýär?",
                    options = listOf(
                        "var",
                        "val",
                        "const val",
                        "static"
                    ),
                    correctOptionIndex = 1,
                    explanation = "\"val\" bir gezek baha berlensoň üýtgedilmeýän üýtgeýjidir."
                ),
                QuizQuestion(
                    id = "qp2",
                    question = "Kotlinde NullPointerException ýalňyşlygynyň öňüni almak üçin näme ulanylýar?",
                    options = listOf(
                        "Null Safety barlaglary (Safe call ?. we Elvis operator ?:)",
                        "Garaşsyz JVM sazlamalary",
                        "Diňe try-catch ulanmak",
                        "Global obýektleri aýyrmak"
                    ),
                    correctOptionIndex = 0,
                    explanation = "Kotlin dili nullable (?) we non-null tipleri aýyl-saýyl edip, noly barlamagy mejbur edýär."
                ),
                QuizQuestion(
                    id = "qp3",
                    question = "Kotlin Coroutines näme üçin ulanylýar?",
                    options = listOf(
                        "Ýeňil agramly asinhron amallary arka fonda ýerine ýetirmek",
                        "Suratlary gysmak üçin",
                        "Diňe Bluetooth baglanyşygy üçin",
                        "Dizaýny reňklemek üçin"
                    ),
                    correctOptionIndex = 0,
                    explanation = "Coroutines ulgamyň esasy akymyny (Main thread) doňdurmazdan asinhron işleri ýerine ýetirýär."
                ),
                QuizQuestion(
                    id = "qp4",
                    question = "Jetpack Compose-da ekranyň gaýtadan çyzylmagyna (recomposition) näme sebäp bolýar?",
                    options = listOf(
                        "Baglanyşykly State (Ýagdaý) bahasynyň üýtgemegi",
                        "Telefonyň zarýadynyň azalmagy",
                        "Ulgamyň sagadynyň üýtgemegi",
                        "Internet tizliginiň haýallamagy"
                    ),
                    correctOptionIndex = 0,
                    explanation = "Compose-da State üýtgände şol state-e garaşly komponentler awtomatiki gaýtadan hasaplanyp çyzylýar."
                )
            )
        )
    )
}
