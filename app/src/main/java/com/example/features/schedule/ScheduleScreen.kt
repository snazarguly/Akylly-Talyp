package com.example.features.schedule

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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.core.model.ScheduleItem
import com.example.core.model.SubjectItem
import com.example.ui.components.AppTopHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GlassCard
import com.example.ui.components.glassEffect
import com.example.ui.components.isAppDarkTheme
import com.example.ui.theme.AcademicTeal
import com.example.ui.theme.AppRadius
import com.example.ui.theme.LightAccent
import com.example.ui.theme.PrimaryBrand
import com.example.ui.theme.SuccessGreen

@Composable
fun ScheduleScreen(
    strings: AppStrings,
    scheduleList: List<ScheduleItem>,
    subjectsList: List<SubjectItem>,
    unreadCount: Int,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMainTab by remember { mutableIntStateOf(0) } // 0 = Schedule, 1 = Subjects
    var selectedScheduleSubTab by remember { mutableIntStateOf(0) } // 0 = This Week, 1 = List
    var selectedDayOfWeek by remember { mutableIntStateOf(2) } // Tuesday by default
    var selectedScheduleItem by remember { mutableStateOf<ScheduleItem?>(null) }
    var selectedSubjectItem by remember { mutableStateOf<SubjectItem?>(null) }

    val daysOfWeek = listOf(
        Pair(1, strings.dayMon),
        Pair(2, strings.dayTue),
        Pair(3, strings.dayWed),
        Pair(4, strings.dayThu),
        Pair(5, strings.dayFri),
        Pair(6, strings.daySat),
        Pair(7, strings.daySun)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("schedule_screen")
    ) {
        AppTopHeader(
            title = strings.navSchedule,
            subtitle = strings.universityShort,
            strings = strings,
            unreadCount = unreadCount,
            onSearchClick = onSearchClick,
            onNotificationClick = onNotificationClick
        )

        // Main Tab switcher: Schedule vs Subjects with Glassmorphism
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .glassEffect(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedMainTab == 0) AcademicTeal else Color.Transparent)
                        .clickable { selectedMainTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.navSchedule,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (selectedMainTab == 0) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedMainTab == 1) AcademicTeal else Color.Transparent)
                        .clickable { selectedMainTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.tabSubjects,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (selectedMainTab == 1) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        if (selectedMainTab == 0) {
            // Schedule View
            // Sub-tabs: Şu hepde (Calendar) vs Sanaw (List)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedScheduleSubTab == 0) AcademicTeal.copy(alpha = 0.15f) else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedScheduleSubTab == 0) AcademicTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.clickable { selectedScheduleSubTab = 0 }
                    ) {
                        Text(
                            text = strings.tabThisWeek,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedScheduleSubTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedScheduleSubTab == 0) AcademicTeal else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedScheduleSubTab == 1) AcademicTeal.copy(alpha = 0.15f) else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedScheduleSubTab == 1) AcademicTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.clickable { selectedScheduleSubTab = 1 }
                    ) {
                        Text(
                            text = strings.tabList,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedScheduleSubTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedScheduleSubTab == 1) AcademicTeal else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (selectedScheduleSubTab == 0) {
                // Calendar Weekday Strip: DUŞ, SIŞ, ÇAR, PEN, ANN, ŞEN, ÝEK
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(daysOfWeek) { (dayNum, dayName) ->
                        val isSelected = selectedDayOfWeek == dayNum
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) AcademicTeal else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AcademicTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier
                                .clickable { selectedDayOfWeek = dayNum }
                                .width(46.dp)
                                .height(56.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${13 + dayNum}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Schedule Items list
            val filteredSchedule = if (selectedScheduleSubTab == 0) {
                scheduleList.filter { it.dayOfWeek == selectedDayOfWeek }
            } else {
                scheduleList.sortedBy { it.dayOfWeek }
            }

            if (filteredSchedule.isEmpty()) {
                EmptyStateView(
                    title = strings.emptyDataTitle,
                    subtitle = strings.emptyDataSubtitle
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSchedule) { item ->
                        GlassCard(
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedScheduleItem = item }
                                .testTag("schedule_item_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = AcademicTeal.copy(alpha = 0.1f),
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Text(
                                            text = item.startTime,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = AcademicTeal
                                            )
                                        )
                                        Text(
                                            text = item.endTime,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.subjectName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${item.classroom} • ${item.teacherName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = when (item.type) {
                                            com.example.core.model.LessonType.LECTURE -> strings.lessonTypeLecture
                                            com.example.core.model.LessonType.PRACTICE -> strings.lessonTypePractice
                                            com.example.core.model.LessonType.LAB -> strings.lessonTypeLab
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        } else {
            // Subjects View (Section 15: Dersler)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subjectsList) { subject ->
                    GlassCard(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSubjectItem = subject }
                            .testTag("subject_item_${subject.id}")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = AcademicTeal.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = subject.code,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AcademicTeal
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    color = SuccessGreen.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${strings.gpaLabel}: ${subject.currentGrade.toInt()}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${strings.teacherLabel}: ${subject.teacherName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${subject.credits} ${strings.creditsLabel} • ${subject.totalHours} ${strings.hoursLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "${strings.semesterLabel} ${subject.semester}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = AcademicTeal
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Schedule Item Detail Bottom Sheet
    selectedScheduleItem?.let { item ->
        ScheduleDetailModal(
            strings = strings,
            item = item,
            onDismiss = { selectedScheduleItem = null }
        )
    }

    // Subject Item Detail Bottom Sheet
    selectedSubjectItem?.let { subject ->
        SubjectDetailModal(
            strings = strings,
            subject = subject,
            onDismiss = { selectedSubjectItem = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailModal(
    strings: AppStrings,
    item: ScheduleItem,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = isAppDarkTheme()
    val sheetContainerColor = if (isDark) Color(0xF2121720) else Color(0xF6FBFDFF)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.subjectLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AcademicTeal
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = strings.close)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.subjectName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(icon = Icons.Default.Person, label = strings.teacherLabel, value = item.teacherName)
            DetailRow(icon = Icons.Default.AccessTime, label = strings.timeLabel, value = "${item.startTime} – ${item.endTime}")
            DetailRow(icon = Icons.Default.LocationOn, label = strings.classroomLabel, value = item.classroom)
            DetailRow(icon = Icons.Default.School, label = strings.facultyLabel, value = item.faculty)
            DetailRow(icon = Icons.Default.Class, label = strings.groupLabel, value = "${item.year}-nji ýyl • ${item.group}")

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AcademicTeal)
            ) {
                Text(text = strings.close)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailModal(
    strings: AppStrings,
    subject: SubjectItem,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = isAppDarkTheme()
    val sheetContainerColor = if (isDark) Color(0xF2121720) else Color(0xF6FBFDFF)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${strings.tabSubjects} • ${subject.code}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AcademicTeal
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = strings.close)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subject.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subject.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(icon = Icons.Default.Person, label = strings.teacherLabel, value = subject.teacherName)
            DetailRow(icon = Icons.Default.Book, label = strings.creditsLabel, value = "${subject.credits} kredit (${subject.totalHours} sagat)")
            DetailRow(icon = Icons.Default.School, label = strings.semesterLabel, value = "${subject.semester}-nji semestr")
            DetailRow(icon = Icons.Default.CheckCircle, label = strings.midtermGrade, value = "${subject.midtermScore}")
            DetailRow(icon = Icons.Default.CheckCircle, label = strings.finalGrade, value = "${subject.finalScore}")
            DetailRow(icon = Icons.Default.CheckCircle, label = strings.examGrade, value = "${subject.examScore}")

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AcademicTeal)
            ) {
                Text(text = strings.close)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AcademicTeal,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
