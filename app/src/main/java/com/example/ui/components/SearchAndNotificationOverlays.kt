package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.core.model.AnnouncementItem
import com.example.core.model.ExamItem
import com.example.core.model.NotificationItem
import com.example.core.model.ScheduleItem
import com.example.core.model.SubjectItem
import com.example.ui.theme.AppComponentSize
import com.example.ui.theme.AppIconSize
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppTypography
import com.example.ui.theme.PrimaryBrand
import com.example.ui.theme.StatusError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchModal(
    strings: AppStrings,
    subjects: List<SubjectItem>,
    schedules: List<ScheduleItem>,
    exams: List<ExamItem>,
    announcements: List<AnnouncementItem>,
    onDismiss: () -> Unit,
    onNavigateToSubject: (SubjectItem) -> Unit,
    onNavigateToExam: (ExamItem) -> Unit,
    onNavigateToAnnouncement: (AnnouncementItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isDark = isAppDarkTheme()
    val sheetContainerColor = if (isDark) Color(0xF2121720) else Color(0xF6FBFDFF)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor,
        modifier = Modifier.testTag("global_search_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = AppSpacing.screenHorizontal)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppComponentSize.minTouchTarget),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.searchPlaceholder,
                    style = AppTypography.Title,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(AppComponentSize.minTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.close,
                        modifier = Modifier.size(AppIconSize.medium)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("global_search_input"),
                placeholder = { Text(strings.searchPlaceholder, style = AppTypography.Body) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = PrimaryBrand,
                        modifier = Modifier.size(AppIconSize.medium)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(AppIconSize.small)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(AppRadius.medium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBrand,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(AppSpacing.m))

            val queryClean = searchQuery.trim().lowercase()

            val filteredSubjects = if (queryClean.isEmpty()) emptyList() else subjects.filter {
                it.name.lowercase().contains(queryClean) ||
                        it.teacherName.lowercase().contains(queryClean) ||
                        it.code.lowercase().contains(queryClean)
            }

            val filteredExams = if (queryClean.isEmpty()) emptyList() else exams.filter {
                it.subjectName.lowercase().contains(queryClean) ||
                        it.classroom.lowercase().contains(queryClean) ||
                        it.teacherName.lowercase().contains(queryClean)
            }

            val filteredAnnouncements = if (queryClean.isEmpty()) emptyList() else announcements.filter {
                it.title.lowercase().contains(queryClean) ||
                        it.shortDescription.lowercase().contains(queryClean) ||
                        it.category.lowercase().contains(queryClean)
            }

            if (queryClean.isNotEmpty() && filteredSubjects.isEmpty() && filteredExams.isEmpty() && filteredAnnouncements.isEmpty()) {
                EmptyStateView(
                    title = strings.emptyDataTitle,
                    subtitle = strings.searchNoResults
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    if (filteredSubjects.isNotEmpty()) {
                        item {
                            Text(
                                text = strings.tabSubjects,
                                style = AppTypography.Caption.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBrand
                                ),
                                modifier = Modifier.padding(vertical = AppSpacing.xxs)
                            )
                        }
                        items(filteredSubjects) { subject ->
                            AppCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToSubject(subject) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(AppSpacing.cardInternal),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Class,
                                        contentDescription = null,
                                        tint = PrimaryBrand,
                                        modifier = Modifier.size(AppIconSize.medium)
                                    )
                                    Spacer(modifier = Modifier.width(AppSpacing.m))
                                    Column {
                                        Text(
                                            text = subject.name,
                                            style = AppTypography.Body.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                        Text(
                                            text = "${subject.teacherName} • ${subject.credits} ${strings.creditsLabel}",
                                            style = AppTypography.Caption,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (filteredExams.isNotEmpty()) {
                        item {
                            Text(
                                text = strings.examsTitle,
                                style = AppTypography.Caption.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBrand
                                ),
                                modifier = Modifier.padding(top = AppSpacing.s, bottom = AppSpacing.xxs)
                            )
                        }
                        items(filteredExams) { exam ->
                            AppCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToExam(exam) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(AppSpacing.cardInternal),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EventNote,
                                        contentDescription = null,
                                        tint = PrimaryBrand,
                                        modifier = Modifier.size(AppIconSize.medium)
                                    )
                                    Spacer(modifier = Modifier.width(AppSpacing.m))
                                    Column {
                                        Text(
                                            text = "${exam.subjectName} (${exam.displayDate})",
                                            style = AppTypography.Body.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                        Text(
                                            text = "${exam.time} • ${exam.classroom} • ${exam.teacherName}",
                                            style = AppTypography.Caption,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (filteredAnnouncements.isNotEmpty()) {
                        item {
                            Text(
                                text = strings.announcementsTitle,
                                style = AppTypography.Caption.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBrand
                                ),
                                modifier = Modifier.padding(top = AppSpacing.s, bottom = AppSpacing.xxs)
                            )
                        }
                        items(filteredAnnouncements) { ann ->
                            AppCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToAnnouncement(ann) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(AppSpacing.cardInternal),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = PrimaryBrand,
                                        modifier = Modifier.size(AppIconSize.medium)
                                    )
                                    Spacer(modifier = Modifier.width(AppSpacing.m))
                                    Column {
                                        Text(
                                            text = ann.title,
                                            style = AppTypography.Body.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                        Text(
                                            text = "${ann.category} • ${ann.date}",
                                            style = AppTypography.Caption,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSheet(
    strings: AppStrings,
    notifications: List<NotificationItem>,
    onDismiss: () -> Unit,
    onSimulatePush: (String, String, String) -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit,
    onNotificationClick: (NotificationItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = isAppDarkTheme()
    val sheetContainerColor = if (isDark) Color(0xF2121720) else Color(0xF6FBFDFF)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor,
        modifier = Modifier.testTag("notification_center_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = AppSpacing.screenHorizontal)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppComponentSize.minTouchTarget),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = PrimaryBrand,
                        modifier = Modifier.size(AppIconSize.medium)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                    Text(
                        text = strings.pushNotificationsTitle,
                        style = AppTypography.Title,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(AppComponentSize.minTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.close,
                        modifier = Modifier.size(AppIconSize.medium)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onMarkAllRead) {
                    Text(text = strings.markAllRead, color = PrimaryBrand, style = AppTypography.Caption.copy(fontWeight = FontWeight.Bold))
                }
                TextButton(onClick = onClearAll) {
                    Text(text = strings.clearAll, color = StatusError, style = AppTypography.Caption.copy(fontWeight = FontWeight.Bold))
                }
            }

            // Quick Simulate Push Action buttons
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacing.xs)
            ) {
                Column(modifier = Modifier.padding(AppSpacing.cardInternal)) {
                    Text(
                        text = strings.simulatePushNotification,
                        style = AppTypography.Caption.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrand
                        )
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onSimulatePush(
                                    "10 minutdan Maglumat ulgamlary sapagyň bar",
                                    "302-nji otagda O. Aşyrow bilen tejribe sapagy.",
                                    "schedule"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(AppComponentSize.touchTargetCompact),
                            shape = RoundedCornerShape(AppRadius.medium)
                        ) {
                            Text("Sapak ýatlatmasy", style = AppTypography.Caption, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                onSimulatePush(
                                    "Ertir Kiberhowpsuzlyk boýunça ekzamen bar",
                                    "20 Sentýabr, 10:00, 205-nji otag.",
                                    "exams"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(AppComponentSize.touchTargetCompact),
                            shape = RoundedCornerShape(AppRadius.medium)
                        ) {
                            Text("Ekzamen", style = AppTypography.Caption, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            if (notifications.isEmpty()) {
                EmptyStateView(
                    title = strings.emptyDataTitle,
                    subtitle = strings.emptyDataSubtitle
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    items(notifications) { notif ->
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNotificationClick(notif) }
                        ) {
                            Row(
                                modifier = Modifier.padding(AppSpacing.cardInternal),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (notif.isRead) MaterialTheme.colorScheme.surfaceVariant else PrimaryBrand,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(AppSpacing.m))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notif.title,
                                        style = AppTypography.Body.copy(
                                            fontWeight = if (notif.isRead) FontWeight.Medium else FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                    Text(
                                        text = notif.body,
                                        style = AppTypography.Caption,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                    Text(
                                        text = notif.timestamp,
                                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

