package com.example.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.localization.AppStrings
import com.example.core.model.StudentProfile
import com.example.ui.components.AppCard
import com.example.ui.components.AppSectionHeader
import com.example.ui.components.AppStatusBadge
import com.example.ui.components.AppTopHeader
import com.example.ui.theme.AppComponentSize
import com.example.ui.theme.AppIconSize
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppTypography
import com.example.ui.theme.PrimaryBrand
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.ThemeMode

@Composable
fun ProfileScreen(
    strings: AppStrings,
    student: StudentProfile,
    currentLanguage: AppLanguage,
    currentThemeMode: ThemeMode,
    isOffline: Boolean,
    notifySchedule: Boolean,
    notifyExams: Boolean,
    notifyAnnouncements: Boolean,
    unreadCount: Int,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onToggleOfflineSimulation: () -> Unit,
    onToggleNotifySchedule: (Boolean) -> Unit,
    onToggleNotifyExams: (Boolean) -> Unit,
    onToggleNotifyAnnouncements: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("profile_screen")
    ) {
        AppTopHeader(
            title = strings.navProfile,
            subtitle = strings.universityShort,
            strings = strings,
            unreadCount = unreadCount,
            onSearchClick = onSearchClick,
            onNotificationClick = onNotificationClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap)
        ) {
            item { Spacer(modifier = Modifier.height(AppSpacing.xxs)) }

            // Student Profile Card
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.cardInternal),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(2.dp, PrimaryBrand),
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.hngu_logo),
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.s))

                        Text(
                            text = student.fullName,
                            style = AppTypography.Headline,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.xxs))

                        AppStatusBadge(
                            text = strings.demoModeBadge,
                            color = PrimaryBrand
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.m))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(AppSpacing.xs))

                        ProfileInfoItem(
                            icon = Icons.Default.School,
                            label = strings.facultyLabel,
                            value = student.faculty
                        )
                        ProfileInfoItem(
                            icon = Icons.Default.School,
                            label = strings.majorLabel,
                            value = student.major
                        )
                        ProfileInfoItem(
                            icon = Icons.Default.Badge,
                            label = strings.groupLabel,
                            value = "${student.degree} • ${student.year}-nji ýyl • ${student.group}"
                        )
                        ProfileInfoItem(
                            icon = Icons.Default.Badge,
                            label = strings.studentCardNumber,
                            value = student.studentCardNumber
                        )
                        ProfileInfoItem(
                            icon = Icons.Default.Email,
                            label = "Elektron poçta",
                            value = student.email
                        )
                    }
                }
            }

            // Language & Theme Settings
            item {
                AppSectionHeader(title = strings.settingsTitle)
            }

            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.cardInternal)) {
                        // Language Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = PrimaryBrand,
                                modifier = Modifier.size(AppIconSize.small)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.xs))
                            Text(
                                text = strings.languageLabel,
                                style = AppTypography.Body.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.s))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                        ) {
                            AppLanguage.entries.forEach { lang ->
                                val isSelected = lang == currentLanguage
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(AppComponentSize.touchTargetCompact)
                                        .clip(RoundedCornerShape(AppRadius.medium))
                                        .background(if (isSelected) PrimaryBrand else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { onLanguageChange(lang) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lang.displayName,
                                        style = AppTypography.Caption.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.m))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(AppSpacing.m))

                        // Theme Mode Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = PrimaryBrand,
                                modifier = Modifier.size(AppIconSize.small)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.xs))
                            Text(
                                text = strings.themeLabel,
                                style = AppTypography.Body.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.s))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                        ) {
                            val modes = listOf(
                                Pair(ThemeMode.SYSTEM, strings.themeSystem),
                                Pair(ThemeMode.LIGHT, strings.themeLight),
                                Pair(ThemeMode.DARK, strings.themeDark)
                            )
                            modes.forEach { (mode, label) ->
                                val isSelected = mode == currentThemeMode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(AppComponentSize.touchTargetCompact)
                                        .clip(RoundedCornerShape(AppRadius.medium))
                                        .background(if (isSelected) PrimaryBrand else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { onThemeModeChange(mode) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = AppTypography.Caption.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notification & Offline Toggles
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.cardInternal)) {
                        ToggleRow(
                            icon = Icons.Default.Notifications,
                            title = strings.notifyScheduleTitle,
                            checked = notifySchedule,
                            onCheckedChange = onToggleNotifySchedule
                        )
                        ToggleRow(
                            icon = Icons.Default.Notifications,
                            title = strings.notifyExamsTitle,
                            checked = notifyExams,
                            onCheckedChange = onToggleNotifyExams
                        )
                        ToggleRow(
                            icon = Icons.Default.Notifications,
                            title = strings.notifyAnnouncementsTitle,
                            checked = notifyAnnouncements,
                            onCheckedChange = onToggleNotifyAnnouncements
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(AppSpacing.xs))

                        // Offline Mode Simulation
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = AppComponentSize.minTouchTarget)
                                .padding(vertical = AppSpacing.xxs),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = if (isOffline) StatusError else PrimaryBrand,
                                    modifier = Modifier.size(AppIconSize.medium)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.m))
                                Column {
                                    Text(
                                        text = "Offline tertip synagy (Simulýasiýa)",
                                        style = AppTypography.Body.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                    Text(
                                        text = "Internet ýok wagty lokal keşlenen maglumatlar görkeziler",
                                        style = AppTypography.Caption,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isOffline,
                                onCheckedChange = { onToggleOfflineSimulation() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryBrand)
                            )
                        }
                    }
                }
            }

            // About & Support Links
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.xs)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = AppComponentSize.minTouchTarget)
                                .clip(RoundedCornerShape(AppRadius.medium))
                                .clickable { showAboutDialog = true }
                                .padding(horizontal = AppSpacing.s, vertical = AppSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PrimaryBrand,
                                    modifier = Modifier.size(AppIconSize.medium)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.m))
                                Text(
                                    text = strings.aboutAppTitle,
                                    style = AppTypography.Body.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(AppIconSize.small)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = AppComponentSize.minTouchTarget)
                                .clip(RoundedCornerShape(AppRadius.medium))
                                .clickable { showContactDialog = true }
                                .padding(horizontal = AppSpacing.s, vertical = AppSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = PrimaryBrand,
                                    modifier = Modifier.size(AppIconSize.medium)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.m))
                                Text(
                                    text = strings.supportTitle,
                                    style = AppTypography.Body.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(AppIconSize.small)
                            )
                        }
                    }
                }
            }

            // Logout Button
            item {
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppComponentSize.buttonHeight)
                        .testTag("profile_logout_btn"),
                    shape = RoundedCornerShape(AppRadius.medium),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                    border = BorderStroke(1.dp, StatusError.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(AppIconSize.medium)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                    Text(
                        text = strings.logoutButton,
                        style = AppTypography.Body.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(AppComponentSize.bottomNavContentPadding))
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = strings.logoutButton, style = AppTypography.Title) },
            text = { Text(text = strings.logoutConfirm, style = AppTypography.Body) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text(text = strings.logoutButton, color = Color.White, style = AppTypography.Body.copy(fontWeight = FontWeight.Bold))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = strings.cancel, style = AppTypography.Body)
                }
            }
        )
    }

    // About App Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(text = strings.aboutAppTitle, style = AppTypography.Title) },
            text = {
                Column {
                    Text(text = "AKYLLY TALYP", style = AppTypography.Headline, color = PrimaryBrand)
                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                    Text(text = "Wersiýa 1.0.0 (MVP)", style = AppTypography.Caption)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(text = strings.universityFullName, style = AppTypography.BodySmall)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(
                        text = "Sanly tehnologiýalar we kiberhowpsuzlyk fakulteti tarapyndan döredildi.",
                        style = AppTypography.Caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(text = strings.close, color = PrimaryBrand, style = AppTypography.Body.copy(fontWeight = FontWeight.Bold))
                }
            }
        )
    }

    // University Contact Dialog
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text(text = strings.supportTitle, style = AppTypography.Title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(text = "Ýagşygeldi Kakaýew adyndaky HNGU", style = AppTypography.Headline)
                    Text(text = "Salgy: Aşgabat ş., Arçabil şaýoly, 8", style = AppTypography.BodySmall)
                    Text(text = "Dekanat: (+993 12) 39-13-05", style = AppTypography.BodySmall)
                    Text(text = "E-mail: info@iogu.edu.tm", style = AppTypography.BodySmall)
                    Text(text = "Sanly ulgam merkezi: helpdesk@iogu.edu.tm", style = AppTypography.BodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showContactDialog = false }) {
                    Text(text = strings.close, color = PrimaryBrand, style = AppTypography.Body.copy(fontWeight = FontWeight.Bold))
                }
            }
        )
    }
}

@Composable
fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .padding(vertical = AppSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBrand,
            modifier = Modifier.size(AppIconSize.small)
        )
        Spacer(modifier = Modifier.width(AppSpacing.m))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = AppTypography.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = AppTypography.Body.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AppComponentSize.minTouchTarget)
            .padding(vertical = AppSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBrand,
                modifier = Modifier.size(AppIconSize.medium)
            )
            Spacer(modifier = Modifier.width(AppSpacing.m))
            Text(
                text = title,
                style = AppTypography.Body.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryBrand)
        )
    }
}

