package com.example.features.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.core.model.AnnouncementItem
import com.example.core.model.ExamItem
import com.example.core.model.ScheduleItem
import com.example.core.model.StudentProfile
import com.example.core.util.CampusCalendar
import java.util.Calendar
import java.util.Locale
import com.example.ui.components.AppCard
import com.example.ui.components.AppPrimaryButton
import com.example.ui.components.AppSectionHeader
import com.example.ui.components.AppStatusBadge
import com.example.ui.components.AppTopHeader
import com.example.ui.components.GlassCard
import com.example.ui.components.OfflineBanner
import com.example.ui.theme.AppComponentSize
import com.example.ui.theme.AppIconSize
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppTypography
import com.example.ui.theme.PrimaryBrand
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    strings: AppStrings,
    student: StudentProfile,
    schedule: List<ScheduleItem>,
    exams: List<ExamItem>,
    announcements: List<AnnouncementItem>,
    unreadNotificationCount: Int,
    isOffline: Boolean,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToGrades: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onScheduleItemClick: (ScheduleItem) -> Unit,
    onExamClick: (ExamItem) -> Unit,
    onAnnouncementClick: (AnnouncementItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshScope = rememberCoroutineScope()

    // Re-evaluated every few seconds so the "next class" card follows the real clock.
    val now by rememberClock(periodMillis = 5_000)
    val today = CampusCalendar.todayDayOfWeek(now)
    val nextClass = schedule
        .filter { it.dayOfWeek == today }
        .mapNotNull { item -> CampusCalendar.minutesOfDay(item.startTime)?.let { item to it * 60 } }
        .filter { (_, startSecond) -> startSecond > now.secondOfDay() }
        .minByOrNull { (_, startSecond) -> startSecond }
    val nextExam = exams.firstOrNull { !it.isPast }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            OfflineBanner(isOffline = isOffline, message = strings.offlineNotice)

            AppTopHeader(
                title = strings.appName,
                subtitle = strings.universityShort,
                strings = strings,
                unreadCount = unreadNotificationCount,
                onSearchClick = onSearchClick,
                onNotificationClick = onNotificationClick
            )

            val pullState = rememberPullToRefreshState()

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    refreshScope.launch {
                        isRefreshing = true
                        delay(600)
                        isRefreshing = false
                    }
                },
                state = pullState,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppSpacing.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
                ) {
                    // 1. Greeting Section
                    item {
                        Column(modifier = Modifier.padding(top = AppSpacing.xs)) {
                            Text(
                                text = "Salam, ${student.firstName} 👋",
                                style = AppTypography.LargeTitle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.xxs))
                            Text(
                                text = "Şu gün näme öwrenersiň?",
                                style = AppTypography.Body,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 2. Academic Status Card (Priority #1)
                    item {
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToGrades() },
                            testTag = "home_academic_performance_card"
                        ) {
                            Column(modifier = Modifier.padding(AppSpacing.cardInternal)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.sectionAcademicStatus,
                                        style = AppTypography.Headline,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = StatusSuccess,
                                            modifier = Modifier.size(AppIconSize.small)
                                        )
                                        Spacer(modifier = Modifier.width(AppSpacing.xxs))
                                        val academicDegree = if (student.cumulativeGpa >= 90.0) "Ýokary" else "Gowy"
                                        Text(
                                            text = "$academicDegree dereje",
                                            style = AppTypography.Caption.copy(
                                                color = StatusSuccess,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(AppSpacing.m))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = "${student.cumulativeGpa}%",
                                            style = AppTypography.LargeTitle.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryBrand
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                        Text(
                                            text = strings.gpaLabel,
                                            style = AppTypography.Caption,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${student.totalCreditsEarned}",
                                            style = AppTypography.LargeTitle.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                        Text(
                                            text = strings.totalCreditsLabel,
                                            style = AppTypography.Caption,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = "${student.completedCoursesCount}",
                                            style = AppTypography.LargeTitle.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                        Text(
                                            text = strings.passedCoursesLabel,
                                            style = AppTypography.Caption,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Next Class & Primary Action CTA
                    if (nextClass != null) {
                        val (nextLesson, nextLessonStartSecond) = nextClass
                        item {
                            AppCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onScheduleItemClick(nextLesson) },
                                testTag = "home_next_class_card"
                            ) {
                                Column(modifier = Modifier.padding(AppSpacing.cardInternal)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AppStatusBadge(
                                            text = "Indiki sapak",
                                            color = PrimaryBrand
                                        )

                                        Surface(
                                            color = StatusWarning.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(AppRadius.xs)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccessTime,
                                                    contentDescription = null,
                                                    tint = StatusWarning,
                                                    modifier = Modifier.size(AppIconSize.micro)
                                                )
                                                Spacer(modifier = Modifier.width(AppSpacing.xxs))
                                                CountdownText(startSecondOfDay = nextLessonStartSecond)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(AppSpacing.s))

                                    Text(
                                        text = nextLesson.subjectName,
                                        style = AppTypography.Title.copy(fontSize = 18.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(AppSpacing.xxs))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(AppIconSize.micro)
                                        )
                                        Spacer(modifier = Modifier.width(AppSpacing.xxs))
                                        Text(
                                            text = "${nextLesson.classroom} • ${nextLesson.teacherName} • ${nextLesson.startTime}",
                                            style = AppTypography.BodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(AppSpacing.m))

                                    // Primary CTA button
                                    AppPrimaryButton(
                                        text = "Sapak tertibini doly gör",
                                        leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                                        onClick = onNavigateToSchedule,
                                        modifier = Modifier.fillMaxWidth(),
                                        testTag = "home_cta_schedule_btn"
                                    )
                                }
                            }
                        }
                    }

                    // 4. Quick Actions (Clean 4-item grid)
                    item {
                        Column {
                            AppSectionHeader(
                                title = "Çalt amallar"
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s)
                            ) {
                                QuickActionTile(
                                    title = "Habarlar",
                                    icon = Icons.Default.Campaign,
                                    color = PrimaryBrand,
                                    onClick = onNavigateToAnnouncements,
                                    modifier = Modifier.weight(1f)
                                )
                                QuickActionTile(
                                    title = "Sapak tertibi",
                                    icon = Icons.Default.CalendarMonth,
                                    color = Color(0xFF1976D2),
                                    onClick = onNavigateToSchedule,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(AppSpacing.s))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s)
                            ) {
                                QuickActionTile(
                                    title = "Synaglar",
                                    icon = Icons.Default.EventNote,
                                    color = StatusWarning,
                                    onClick = onNavigateToExams,
                                    modifier = Modifier.weight(1f)
                                )
                                QuickActionTile(
                                    title = "AI Kömekçi",
                                    icon = Icons.Default.AutoAwesome,
                                    color = Color(0xFF7B1FA2),
                                    onClick = onOpenAiAssistant,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 5. Upcoming Exam Card (if present)
                    if (nextExam != null) {
                        item {
                            Column {
                                AppSectionHeader(
                                    title = "Ýakynlaşýan synag"
                                )
                                Spacer(modifier = Modifier.height(AppSpacing.xs))
                                AppCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onExamClick(nextExam) },
                                    testTag = "home_upcoming_exam_card"
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(AppSpacing.cardInternal),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = nextExam.subjectName,
                                                style = AppTypography.Headline.copy(fontSize = 15.sp),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                            Text(
                                                text = "${nextExam.displayDate} • ${nextExam.time} • ${nextExam.classroom}",
                                                style = AppTypography.BodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Surface(
                                            color = StatusWarning.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(AppRadius.xs)
                                        ) {
                                            Text(
                                                text = String.format(strings.daysLeftPattern, nextExam.daysLeft),
                                                style = AppTypography.Caption.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = StatusWarning
                                                ),
                                                modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. Latest Announcements preview
                    item {
                        AppSectionHeader(
                            title = strings.sectionLatestAnnouncements,
                            actionText = strings.viewAll,
                            onActionClick = onNavigateToAnnouncements
                        )
                    }

                    items(announcements.take(2)) { ann ->
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAnnouncementClick(ann) }
                        ) {
                            Column(modifier = Modifier.padding(AppSpacing.cardInternal)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppStatusBadge(
                                        text = if (ann.isImportant) strings.importantBadge else ann.category.uppercase(),
                                        color = if (ann.isImportant) StatusWarning else PrimaryBrand
                                    )
                                    Text(
                                        text = ann.date,
                                        style = AppTypography.Caption,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(AppSpacing.xs))

                                Text(
                                    text = ann.title,
                                    style = AppTypography.Headline.copy(fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(AppSpacing.xxs))

                                Text(
                                    text = ann.shortDescription,
                                    style = AppTypography.BodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(AppComponentSize.bottomNavContentPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionTile(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        shape = RoundedCornerShape(AppRadius.medium),
        onClick = onClick,
        modifier = modifier.height(72.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(AppComponentSize.iconBoxMedium)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(AppIconSize.medium)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.s))

            Text(
                text = title,
                style = AppTypography.BodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun rememberClock(periodMillis: Long): State<Calendar> =
    produceState(initialValue = Calendar.getInstance()) {
        while (true) {
            delay(periodMillis)
            value = Calendar.getInstance()
        }
    }

private fun Calendar.secondOfDay(): Int =
    get(Calendar.HOUR_OF_DAY) * 3600 + get(Calendar.MINUTE) * 60 + get(Calendar.SECOND)

/** Ticks every second on its own, so only this text recomposes rather than the whole Home screen. */
@Composable
private fun CountdownText(startSecondOfDay: Int) {
    val now by rememberClock(periodMillis = 1_000)
    Text(
        text = formatCountdown((startSecondOfDay - now.secondOfDay()).coerceAtLeast(0)) + " galdy",
        style = AppTypography.Caption.copy(
            color = StatusWarning,
            fontWeight = FontWeight.Bold
        )
    )
}

private fun formatCountdown(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
    }
}
