package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.core.data.DemoDataRepository
import com.example.core.localization.AppStrings
import com.example.features.ai.DedicatedAiScreen
import com.example.features.announcements.AnnouncementsScreen
import com.example.features.auth.LoginScreen
import com.example.features.auth.OnboardingScreen
import com.example.features.auth.SplashScreen
import com.example.features.home.HomeScreen
import com.example.features.offline_ai.service.OfflineAiService
import com.example.features.profile.ProfileScreen
import com.example.features.study.StudyHubScreen
import com.example.ui.components.GlassBottomNavigation
import com.example.ui.components.GlassNavTabItem
import com.example.ui.components.NotificationSheet
import com.example.ui.components.SearchModal
import com.example.ui.theme.AppComponentSize
import com.example.ui.theme.AppIconSize
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppTypography
import com.example.ui.theme.PrimaryBrand

enum class AppDestination {
    SPLASH,
    LOGIN,
    ONBOARDING,
    MAIN
}

@Composable
fun MainAppNav(
    repository: DemoDataRepository,
    offlineAiService: OfflineAiService,
    modifier: Modifier = Modifier
) {
    val isLoggedIn by repository.isLoggedIn.collectAsState()
    val isOnboardingCompleted by repository.isOnboardingCompleted.collectAsState()
    val currentLang by repository.appLanguage.collectAsState()
    val currentThemeMode by repository.themeMode.collectAsState()
    val isOffline by repository.isOffline.collectAsState()
    val savedLogin by repository.savedLogin.collectAsState()
    val rememberPassword by repository.rememberPassword.collectAsState()

    val student by repository.studentProfile.collectAsState()
    val scheduleList by repository.scheduleList.collectAsState()
    val subjectsList by repository.subjectsList.collectAsState()
    val examsList by repository.examsList.collectAsState()
    val announcementsList by repository.announcementsList.collectAsState()
    val notifications by repository.notifications.collectAsState()

    val notifySchedule by repository.notifySchedule.collectAsState()
    val notifyExams by repository.notifyExams.collectAsState()
    val notifyAnnouncements by repository.notifyAnnouncements.collectAsState()

    val strings = AppStrings.forLanguage(currentLang)
    val unreadNotificationsCount = notifications.count { !it.isRead }

    // rememberSaveable keeps the current screen across rotation and process recreation.
    var currentDestination by rememberSaveable {
        mutableStateOf(
            if (!isLoggedIn) AppDestination.LOGIN
            else if (!isOnboardingCompleted) AppDestination.ONBOARDING
            else AppDestination.MAIN
        )
    }

    var isSplashFinished by rememberSaveable { mutableStateOf(false) }

    // Bottom Navigation tab: 0=Home, 1=Study, 2=Announcements, 3=AI, 4=Profile
    var selectedBottomTab by rememberSaveable { mutableIntStateOf(0) }
    var studyHubSubTab by rememberSaveable { mutableIntStateOf(0) }

    // Overlays
    var isSearchOpen by rememberSaveable { mutableStateOf(false) }
    var isNotificationSheetOpen by rememberSaveable { mutableStateOf(false) }

    if (!isSplashFinished) {
        SplashScreen(
            strings = strings,
            onSplashFinished = {
                isSplashFinished = true
            }
        )
        return
    }

    when (currentDestination) {
        AppDestination.SPLASH -> {}

        AppDestination.LOGIN -> {
            LoginScreen(
                strings = strings,
                currentLanguage = currentLang,
                onLanguageChange = { repository.setLanguage(it) },
                savedLogin = savedLogin,
                rememberPassword = rememberPassword,
                onRememberPasswordChange = { repository.setRememberPassword(it) },
                onLogin = { login, password ->
                    val success = repository.login(login, password)
                    if (success) {
                        selectedBottomTab = 0
                        currentDestination = if (!isOnboardingCompleted) AppDestination.ONBOARDING else AppDestination.MAIN
                    }
                    success
                }
            )
        }

        AppDestination.ONBOARDING -> {
            OnboardingScreen(
                strings = strings,
                onComplete = {
                    repository.completeOnboarding()
                    currentDestination = AppDestination.MAIN
                }
            )
        }

        AppDestination.MAIN -> {
            val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
            val isKeyboardOpen = imeBottomPadding > 0.dp

            // System back returns to the Home tab first instead of closing the app from any tab.
            BackHandler(enabled = selectedBottomTab != 0) { selectedBottomTab = 0 }

            val navItems = listOf(
                GlassNavTabItem(strings.navHome, Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
                GlassNavTabItem(strings.navStudy, Icons.Filled.School, Icons.Outlined.School, "nav_tab_study"),
                GlassNavTabItem(strings.navAnnouncements, Icons.Filled.Campaign, Icons.Outlined.Campaign, "nav_tab_announcements"),
                GlassNavTabItem(strings.navAi, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_tab_ai"),
                GlassNavTabItem(strings.navProfile, Icons.Filled.Person, Icons.Outlined.Person, "nav_tab_profile")
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Screen Content Area
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = AppComponentSize.maxContentWidth)
                    ) {
                        when (selectedBottomTab) {
                            0 -> HomeScreen(
                                strings = strings,
                                student = student,
                                schedule = scheduleList,
                                exams = examsList,
                                announcements = announcementsList,
                                unreadNotificationCount = unreadNotificationsCount,
                                isOffline = isOffline,
                                onSearchClick = { isSearchOpen = true },
                                onNotificationClick = { isNotificationSheetOpen = true },
                                onNavigateToSchedule = {
                                    studyHubSubTab = 0
                                    selectedBottomTab = 1
                                },
                                onNavigateToExams = {
                                    studyHubSubTab = 1
                                    selectedBottomTab = 1
                                },
                                onNavigateToGrades = {
                                    studyHubSubTab = 2
                                    selectedBottomTab = 1
                                },
                                onNavigateToAnnouncements = { selectedBottomTab = 2 },
                                onOpenAiAssistant = { selectedBottomTab = 3 },
                                onScheduleItemClick = {
                                    studyHubSubTab = 0
                                    selectedBottomTab = 1
                                },
                                onExamClick = {
                                    studyHubSubTab = 1
                                    selectedBottomTab = 1
                                },
                                onAnnouncementClick = { selectedBottomTab = 2 }
                            )

                            1 -> StudyHubScreen(
                                strings = strings,
                                scheduleList = scheduleList,
                                subjectsList = subjectsList,
                                examsList = examsList,
                                student = student,
                                unreadCount = unreadNotificationsCount,
                                selectedSubTab = studyHubSubTab,
                                onSubTabChange = { studyHubSubTab = it },
                                onSearchClick = { isSearchOpen = true },
                                onNotificationClick = { isNotificationSheetOpen = true }
                            )

                            2 -> AnnouncementsScreen(
                                strings = strings,
                                announcements = announcementsList,
                                unreadCount = unreadNotificationsCount,
                                onSearchClick = { isSearchOpen = true },
                                onNotificationClick = { isNotificationSheetOpen = true }
                            )

                            3 -> DedicatedAiScreen(
                                strings = strings,
                                currentLanguage = currentLang,
                                repository = repository,
                                offlineService = offlineAiService,
                                modifier = Modifier.fillMaxSize()
                            )

                            4 -> ProfileScreen(
                                strings = strings,
                                student = student,
                                currentLanguage = currentLang,
                                currentThemeMode = currentThemeMode,
                                isOffline = isOffline,
                                notifySchedule = notifySchedule,
                                notifyExams = notifyExams,
                                notifyAnnouncements = notifyAnnouncements,
                                unreadCount = unreadNotificationsCount,
                                onLanguageChange = { repository.setLanguage(it) },
                                onThemeModeChange = { repository.setThemeMode(it) },
                                onToggleOfflineSimulation = { repository.toggleOfflineSimulation() },
                                onToggleNotifySchedule = { repository.toggleNotifySchedule(it) },
                                onToggleNotifyExams = { repository.toggleNotifyExams(it) },
                                onToggleNotifyAnnouncements = { repository.toggleNotifyAnnouncements(it) },
                                onLogout = {
                                    repository.logout()
                                    currentDestination = AppDestination.LOGIN
                                },
                                onSearchClick = { isSearchOpen = true },
                                onNotificationClick = { isNotificationSheetOpen = true }
                            )
                        }
                    }
                }

                // Floating iOS Glassmorphism Bottom Navigation Bar (automatically hides when keyboard is open)
                if (!isKeyboardOpen) {
                    GlassBottomNavigation(
                        tabs = navItems,
                        selectedTabIndex = selectedBottomTab,
                        onTabSelected = { selectedBottomTab = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }

    // Global Search Modal
    if (isSearchOpen) {
        SearchModal(
            strings = strings,
            subjects = subjectsList,
            schedules = scheduleList,
            exams = examsList,
            announcements = announcementsList,
            onDismiss = { isSearchOpen = false },
            onNavigateToSubject = {
                isSearchOpen = false
                studyHubSubTab = 0
                selectedBottomTab = 1
            },
            onNavigateToExam = {
                isSearchOpen = false
                studyHubSubTab = 1
                selectedBottomTab = 1
            },
            onNavigateToAnnouncement = {
                isSearchOpen = false
                selectedBottomTab = 2
            }
        )
    }

    // Notifications Center Sheet
    if (isNotificationSheetOpen) {
        NotificationSheet(
            strings = strings,
            notifications = notifications,
            onDismiss = { isNotificationSheetOpen = false },
            onSimulatePush = { title, body, screen ->
                repository.triggerSimulatedPush(title, body, screen)
            },
            onMarkAllRead = { repository.markAllNotificationsRead() },
            onClearAll = { repository.clearAllNotifications() },
            onNotificationClick = { notif ->
                isNotificationSheetOpen = false
                when (notif.targetScreen) {
                    "schedule" -> {
                        studyHubSubTab = 0
                        selectedBottomTab = 1
                    }
                    "exams" -> {
                        studyHubSubTab = 1
                        selectedBottomTab = 1
                    }
                    "grades" -> {
                        studyHubSubTab = 2
                        selectedBottomTab = 1
                    }
                    "announcements" -> {
                        selectedBottomTab = 2
                    }
                    else -> selectedBottomTab = 0
                }
            }
        )
    }
}
