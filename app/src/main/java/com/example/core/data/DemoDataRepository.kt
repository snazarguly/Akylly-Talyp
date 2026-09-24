package com.example.core.data

import android.content.Context
import android.content.SharedPreferences
import com.example.core.localization.AppLanguage
import com.example.core.model.AnnouncementItem
import com.example.core.model.ChatMessage
import com.example.core.model.ExamItem
import com.example.core.model.ExamType
import com.example.core.model.LessonType
import com.example.core.model.NotificationItem
import com.example.core.model.ScheduleItem
import com.example.core.model.SemesterProgress
import com.example.core.model.StudentProfile
import com.example.core.model.SubjectItem
import com.example.ui.theme.ThemeMode
import com.example.BuildConfig
import com.example.core.ai.GeminiService
import com.example.core.ai.OnlineCampusAiEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DemoDataRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("akylly_talyp_prefs", Context.MODE_PRIVATE)

    // Auth & Session
    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", true)) // default true for demo convenience
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isOnboardingCompleted =
        MutableStateFlow(prefs.getBoolean("onboarding_completed", true))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _rememberPassword =
        MutableStateFlow(prefs.getBoolean("remember_pwd", true))
    val rememberPassword: StateFlow<Boolean> = _rememberPassword.asStateFlow()

    private val _savedLogin =
        MutableStateFlow(prefs.getString("saved_login", "Suleyman_05") ?: "Suleyman_05")
    val savedLogin: StateFlow<String> = _savedLogin.asStateFlow()

    // Settings
    private val _appLanguage = MutableStateFlow(
        AppLanguage.fromCode(prefs.getString("selected_lang", "tk") ?: "tk")
    )
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Offline mode simulation state
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // Notification Toggles
    private val _notifySchedule = MutableStateFlow(prefs.getBoolean("notif_sched", true))
    val notifySchedule: StateFlow<Boolean> = _notifySchedule.asStateFlow()

    private val _notifyExams = MutableStateFlow(prefs.getBoolean("notif_exams", true))
    val notifyExams: StateFlow<Boolean> = _notifyExams.asStateFlow()

    private val _notifyAnnouncements = MutableStateFlow(prefs.getBoolean("notif_announ", true))
    val notifyAnnouncements: StateFlow<Boolean> = _notifyAnnouncements.asStateFlow()

    // In-memory data states
    val studentProfile = MutableStateFlow(StudentProfile())

    private val _scheduleList = MutableStateFlow(createInitialSchedule())
    val scheduleList: StateFlow<List<ScheduleItem>> = _scheduleList.asStateFlow()

    private val _subjectsList = MutableStateFlow(createInitialSubjects())
    val subjectsList: StateFlow<List<SubjectItem>> = _subjectsList.asStateFlow()

    private val _examsList = MutableStateFlow(createInitialExams())
    val examsList: StateFlow<List<ExamItem>> = _examsList.asStateFlow()

    private val _announcementsList = MutableStateFlow(createInitialAnnouncements())
    val announcementsList: StateFlow<List<AnnouncementItem>> = _announcementsList.asStateFlow()

    private val _notifications = MutableStateFlow(createInitialNotifications())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    val semesterProgressList = listOf(
        SemesterProgress(1, "Semestr 1", 87.0),
        SemesterProgress(2, "Semestr 2", 88.5),
        SemesterProgress(3, "Semestr 3", 89.2),
        SemesterProgress(4, "Semestr 4", 90.1),
        SemesterProgress(5, "Semestr 5", 91.0),
        SemesterProgress(6, "Semestr 6", 91.8),
        SemesterProgress(7, "Semestr 7", 92.4)
    )

    // Coroutine Scope for asynchronous operations like AI queries
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Chat with AI
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "m1",
                text = "Salam, Süleýman! Men HNGU talyplary üçin akylly sanly kömekçiňiz. Sapak tertibiňiz, ekzamen seneleri, bahalaryňyz, bäsleşikler, ýa-da islendik erkin soragyňyz (programmirleme, ylym, uniwersitet durmuşy) boýunça sorap bilersiňiz.",
                isUser = false,
                timestamp = "10:00"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _customGeminiApiKey = MutableStateFlow(prefs.getString("custom_gemini_api_key", "") ?: "")
    val customGeminiApiKey: StateFlow<String> = _customGeminiApiKey.asStateFlow()

    fun setCustomGeminiApiKey(key: String) {
        val trimmed = key.trim()
        prefs.edit().putString("custom_gemini_api_key", trimmed).apply()
        _customGeminiApiKey.value = trimmed
    }

    fun getEffectiveApiKey(): String {
        val custom = _customGeminiApiKey.value.trim()
        if (custom.isNotBlank()) return custom
        val buildKey = com.example.BuildConfig.GEMINI_API_KEY.trim()
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") return buildKey
        return ""
    }

    fun isGeminiKeyConfigured(): Boolean {
        return getEffectiveApiKey().isNotBlank()
    }

    private val geminiService = GeminiService()

    fun login(loginInput: String, passwordInput: String): Boolean {
        if (loginInput == "Suleyman_05" && passwordInput == "03122005.sns") {
            _isLoggedIn.value = true
            prefs.edit().putBoolean("is_logged_in", true).apply()
            if (_rememberPassword.value) {
                prefs.edit().putString("saved_login", loginInput).apply()
                _savedLogin.value = loginInput
            }
            return true
        }
        return false
    }

    fun logout() {
        _isLoggedIn.value = false
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    fun setRememberPassword(remember: Boolean) {
        _rememberPassword.value = remember
        prefs.edit().putBoolean("remember_pwd", remember).apply()
    }

    fun completeOnboarding() {
        _isOnboardingCompleted.value = true
        prefs.edit().putBoolean("onboarding_completed", true).apply()
    }

    fun setLanguage(language: AppLanguage) {
        _appLanguage.value = language
        prefs.edit().putString("selected_lang", language.code).apply()
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun toggleOfflineSimulation() {
        _isOffline.value = !_isOffline.value
    }

    fun toggleNotifySchedule(enabled: Boolean) {
        _notifySchedule.value = enabled
        prefs.edit().putBoolean("notif_sched", enabled).apply()
    }

    fun toggleNotifyExams(enabled: Boolean) {
        _notifyExams.value = enabled
        prefs.edit().putBoolean("notif_exams", enabled).apply()
    }

    fun toggleNotifyAnnouncements(enabled: Boolean) {
        _notifyAnnouncements.value = enabled
        prefs.edit().putBoolean("notif_announ", enabled).apply()
    }

    fun markAllNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    fun triggerSimulatedPush(title: String, body: String, targetScreen: String) {
        val newItem = NotificationItem(
            id = "notif_${System.currentTimeMillis()}",
            title = title,
            body = body,
            timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            targetScreen = targetScreen,
            isRead = false
        )
        _notifications.value = listOf(newItem) + _notifications.value
    }

    fun sendAiMessage(userText: String) {
        val userMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            text = userText,
            isUser = true,
            timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiThinking.value = true

        repositoryScope.launch {
            val effectiveKey = getEffectiveApiKey()
            var generatedReply: String? = null

            if (effectiveKey.isNotBlank()) {
                try {
                    val systemPrompt = buildSystemContext(_appLanguage.value)
                    val geminiReply = geminiService.generateResponse(
                        userPrompt = userText,
                        conversationHistory = _chatMessages.value,
                        systemPrompt = systemPrompt,
                        apiKey = effectiveKey
                    )
                    if (geminiReply.isNotBlank()) {
                        generatedReply = geminiReply
                    }
                } catch (e: Exception) {
                    val errorMsg = e.message ?: ""
                    generatedReply = when {
                        errorMsg.contains("Unable to resolve host") || errorMsg.contains("timeout") || errorMsg.contains("timed out") -> {
                            when (_appLanguage.value) {
                                AppLanguage.TURKMEN -> "Online AI häzir elýeterli däl. Internet birikmesini barlaň ýa-da Offline AI režimini ulanyň."
                                AppLanguage.RUSSIAN -> "Online AI сейчас недоступен. Проверьте подключение к интернету или используйте режим Offline AI."
                                else -> "Online AI is currently unavailable. Please check your internet connection or use Offline AI mode."
                            }
                        }
                        errorMsg.contains("401") || errorMsg.contains("API key") || errorMsg.contains("403") -> {
                            when (_appLanguage.value) {
                                AppLanguage.TURKMEN -> "Online AI konfigurasiýasynda mesele bar. API açaryňyzy barlaň."
                                AppLanguage.RUSSIAN -> "Проблема с конфигурацией Online AI. Проверьте ваш API ключ."
                                else -> "Online AI configuration issue. Please check your API key."
                            }
                        }
                        else -> {
                            when (_appLanguage.value) {
                                AppLanguage.TURKMEN -> "Online AI hyzmatynda garaşylmadyk ýalňyşlyk ýüze çykdy."
                                AppLanguage.RUSSIAN -> "Произошла непредвиденная ошибка в работе Online AI."
                                else -> "An unexpected error occurred in the Online AI service."
                            }
                        }
                    }
                }
            } else {
                generatedReply = when (_appLanguage.value) {
                    AppLanguage.TURKMEN -> "Online AI üçin Gemini API açary gurnalmadyk. Lütwen, sazlamalardan açar goşuň."
                    AppLanguage.RUSSIAN -> "Ключ Gemini API для Online AI не настроен. Пожалуйста, добавьте ключ в настройках."
                    else -> "Gemini API key for Online AI is not configured. Please add a key in settings."
                }
            }

            val finalReplyText = generatedReply ?: ""

            val botMsg = ChatMessage(
                id = "reply_${System.currentTimeMillis()}",
                text = finalReplyText,
                isUser = false,
                timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            )
            _chatMessages.value = _chatMessages.value + botMsg
            _isAiThinking.value = false
        }
    }

    fun clearAiChat() {
        _chatMessages.value = emptyList()
        _isAiThinking.value = false
    }

    private fun buildSystemContext(language: AppLanguage): String {
        val student = studentProfile.value
        val schedule = _scheduleList.value
        val exams = _examsList.value
        val announcements = _announcementsList.value
        val subjects = _subjectsList.value

        val langInstruction = when (language) {
            AppLanguage.TURKMEN -> "Jogaplary türkmen dilinde, mähirli, akylly, düşnükli we talyba goldaw beriji äheňde ýaz."
            AppLanguage.RUSSIAN -> "Отвечай на русском языке вежливо, грамотно, дружелюбно и информативно."
            AppLanguage.ENGLISH -> "Respond in clear, friendly, and helpful English suitable for a university student assistant."
        }

        return """
            Sen Ýagşygeldi Kakaýew adyndaky Halkara nebit we gaz uniwersitetiniň (HNGU) «Akylly Talyp» emeli aň kömekçisisiň (AI Assistant).
            Sen diňe bir taýyn soraglara däl, eýsem talybyň beren ISLENDIK soragyna (akademiki maslahatlar, dersler, programmirleme, okuw usullary, ylmy işler, uniwersitet düzgünleri, motivasiýa) hakyky ösen emeli aň hökmünde tebigy, çuňňur we peýdaly jogap berýärsiň.
            
            $langInstruction
            
            Talyp barada maglumat:
            - Ady: ${student.fullName}
            - Talyp belgisi: ${student.studentCardNumber}
            - Fakultet: ${student.faculty}
            - Hünär / Ugur: ${student.major}
            - Topar: ${student.group}
            - Ýyl / Kurs: ${student.year}-nji ýyl
            - Umumy GPA: ${student.cumulativeGpa}%
            - Toplanan kreditler: ${student.totalCreditsEarned} / 240
            
            Häzirki ýakyn ekzamenler:
            ${exams.filter { !it.isPast }.joinToString("\n") { "- ${it.subjectName} (${it.displayDate}, ${it.time}, ${it.classroom}-nji otag)" }}
            
            Uniwersitetiň soňky täzelikleri we bildirişleri:
            ${announcements.joinToString("\n") { "- ${it.title} (${it.date}): ${it.shortDescription}" }}
            
            Dersler we mugallymlar:
            ${subjects.joinToString("\n") { "- ${it.name} (${it.teacherName}): baha ${it.currentGrade}%" }}
            
            Sapak tertibi (Häzirki hepde):
            ${schedule.take(6).joinToString("\n") { "- ${it.subjectName} (${it.dayOfWeek}-nji gün, ${it.startTime}-${it.endTime}, ${it.classroom}-nji otag, ${it.teacherName})" }}
            
            Göz öňünde tutmaly düzgünler:
            1. Talyp öz okuwy, sapaklary, ekzamenleri, uniwersitet habarlary barada sorasa, ýokardaky takyk maglumatlary ulanyp jogap ber.
            2. Eger umumy sorag sorasa (mysal üçin, 'Python dilini nähili öwrenmeli?', 'Kiberhowpsuzlyk boýunça maslahat ber', 'Ylmy bäsleşige nähili taýýarlanmaly?', 'Diplom işi nähili ýazylýar?'), giňişleýin, hakyky emeli aň (ChatGPT/Gemini) ýaly peýdaly, gurluşly maslahatlar ber.
            3. Jogaplaryňy gysga abzaslar, sanawlar (bullet points) we zerur ýerinde emojiler bilen aýdyň formatla.
        """.trimIndent()
    }

    private fun createInitialSchedule(): List<ScheduleItem> {
        return listOf(
            // Tuesday (Today: Sep 15, 2026 is Tuesday)
            ScheduleItem(
                id = "sch_1",
                dayOfWeek = 2,
                startTime = "09:00",
                endTime = "10:20",
                subjectName = "Maglumat ulgamlary",
                classroom = "304-nji otag",
                teacherName = "O. Aşyrow",
                type = LessonType.LECTURE
            ),
            ScheduleItem(
                id = "sch_2",
                dayOfWeek = 2,
                startTime = "11:00",
                endTime = "12:20",
                subjectName = "Kiberhowpsuzlyk",
                classroom = "205-nji otag",
                teacherName = "D. Gurbanow",
                type = LessonType.PRACTICE
            ),
            ScheduleItem(
                id = "sch_3",
                dayOfWeek = 2,
                startTime = "13:00",
                endTime = "14:20",
                subjectName = "Maglumatlar bazasy",
                classroom = "412-nji otag",
                teacherName = "M. Baýramowa",
                type = LessonType.LAB
            ),
            ScheduleItem(
                id = "sch_4",
                dayOfWeek = 2,
                startTime = "14:00",
                endTime = "15:20",
                subjectName = "Maglumat ulgamlary we tehnologiýalary",
                classroom = "302-nji otag",
                teacherName = "O. Aşyrow",
                type = LessonType.PRACTICE
            ),

            // Monday
            ScheduleItem(
                id = "sch_5",
                dayOfWeek = 1,
                startTime = "09:00",
                endTime = "10:20",
                subjectName = "Programmirleme",
                classroom = "108-nji otag",
                teacherName = "A. Myradow",
                type = LessonType.LECTURE
            ),
            ScheduleItem(
                id = "sch_6",
                dayOfWeek = 1,
                startTime = "11:00",
                endTime = "12:20",
                subjectName = "Ulgamlaýyn analiz",
                classroom = "304-nji otag",
                teacherName = "B. Berdiýew",
                type = LessonType.PRACTICE
            ),

            // Wednesday
            ScheduleItem(
                id = "sch_7",
                dayOfWeek = 3,
                startTime = "09:00",
                endTime = "10:20",
                subjectName = "Kiberhowpsuzlyk",
                classroom = "205-nji otag",
                teacherName = "D. Gurbanow",
                type = LessonType.LAB
            ),
            ScheduleItem(
                id = "sch_8",
                dayOfWeek = 3,
                startTime = "11:00",
                endTime = "12:20",
                subjectName = "Programmirleme",
                classroom = "108-nji otag",
                teacherName = "A. Myradow",
                type = LessonType.LAB
            ),

            // Thursday
            ScheduleItem(
                id = "sch_9",
                dayOfWeek = 4,
                startTime = "09:00",
                endTime = "10:20",
                subjectName = "Kompýuter torlary",
                classroom = "310-njy otag",
                teacherName = "E. Amanow",
                type = LessonType.LECTURE
            ),
            ScheduleItem(
                id = "sch_10",
                dayOfWeek = 4,
                startTime = "11:00",
                endTime = "12:20",
                subjectName = "Maglumat tehnologiýalary",
                classroom = "208-nji otag",
                teacherName = "S. Durdyýew",
                type = LessonType.PRACTICE
            ),

            // Friday
            ScheduleItem(
                id = "sch_11",
                dayOfWeek = 5,
                startTime = "09:00",
                endTime = "10:20",
                subjectName = "Emeli aňyň esaslary",
                classroom = "402-nji otag",
                teacherName = "K. Kakajanow",
                type = LessonType.LECTURE
            ),
            ScheduleItem(
                id = "sch_12",
                dayOfWeek = 5,
                startTime = "11:00",
                endTime = "12:20",
                subjectName = "Maglumatlar bazasy",
                classroom = "412-nji otag",
                teacherName = "M. Baýramowa",
                type = LessonType.PRACTICE
            ),

            // Saturday
            ScheduleItem(
                id = "sch_13",
                dayOfWeek = 6,
                startTime = "09:00",
                endTime = "10:20",
                subjectName = "Ulgamlaýyn analiz",
                classroom = "304-nji otag",
                teacherName = "B. Berdiýew",
                type = LessonType.LECTURE
            )
        )
    }

    private fun createInitialSubjects(): List<SubjectItem> {
        return listOf(
            SubjectItem(
                id = "sub_1",
                code = "MUT-401",
                name = "Maglumat ulgamlary",
                teacherName = "O. Aşyrow",
                credits = 6,
                totalHours = 90,
                semester = 7,
                currentGrade = 89.0,
                midtermScore = 88.0,
                finalScore = 90.0,
                examScore = 89.0,
                isPassed = true,
                description = "Korporatiw we pudaklaýyn maglumat ulgamlarynyň arhitekturasy we taslamalaşdyrylyşy."
            ),
            SubjectItem(
                id = "sub_2",
                code = "MBZ-402",
                name = "Maglumatlar bazasy",
                teacherName = "M. Baýramowa",
                credits = 5,
                totalHours = 75,
                semester = 7,
                currentGrade = 91.0,
                midtermScore = 90.0,
                finalScore = 92.0,
                examScore = 91.0,
                isPassed = true,
                description = "Relasion we paýlanan maglumatlar bazalary, SQL we NoSQL gurluşlary."
            ),
            SubjectItem(
                id = "sub_3",
                code = "KHB-403",
                name = "Kiberhowpsuzlyk",
                teacherName = "D. Gurbanow",
                credits = 5,
                totalHours = 75,
                semester = 7,
                currentGrade = 94.0,
                midtermScore = 93.0,
                finalScore = 95.0,
                examScore = 94.0,
                isPassed = true,
                description = "Kriptografiýa, tor howpsuzlygy, gowşaklyklary ýüze çykarmak we maglumat goragy."
            ),
            SubjectItem(
                id = "sub_4",
                code = "PRG-404",
                name = "Programmirleme",
                teacherName = "A. Myradow",
                credits = 6,
                totalHours = 90,
                semester = 7,
                currentGrade = 96.0,
                midtermScore = 95.0,
                finalScore = 97.0,
                examScore = 96.0,
                isPassed = true,
                description = "Objekt-ugrukdyrylan we döwrebap funksional programmirleme usullary."
            ),
            SubjectItem(
                id = "sub_5",
                code = "ULA-405",
                name = "Ulgamlaýyn analiz",
                teacherName = "B. Berdiýew",
                credits = 4,
                totalHours = 60,
                semester = 7,
                currentGrade = 90.0,
                midtermScore = 89.0,
                finalScore = 91.0,
                examScore = 90.0,
                isPassed = true,
                description = "Çylşyrymly maglumat ulgamlaryny modelirlemek we biznes prosesleri awtomatlaşdyrmak."
            ),
            SubjectItem(
                id = "sub_6",
                code = "KTR-406",
                name = "Kompýuter torlary",
                teacherName = "E. Amanow",
                credits = 5,
                totalHours = 75,
                semester = 7,
                currentGrade = 92.0,
                midtermScore = 91.0,
                finalScore = 93.0,
                examScore = 92.0,
                isPassed = true,
                description = "OSI modeli, TCP/IP protokollary, marşrutizasiýa we sanly torlar."
            ),
            SubjectItem(
                id = "sub_7",
                code = "MTE-407",
                name = "Maglumat tehnologiýalary",
                teacherName = "S. Durdyýew",
                credits = 4,
                totalHours = 60,
                semester = 7,
                currentGrade = 93.0,
                midtermScore = 92.0,
                finalScore = 94.0,
                examScore = 93.0,
                isPassed = true,
                description = "Nebit-gaz senagatynda döwrebap sanly tehnologiýalary ulanmak."
            ),
            SubjectItem(
                id = "sub_8",
                code = "EAS-408",
                name = "Emeli aňyň esaslary",
                teacherName = "K. Kakajanow",
                credits = 5,
                totalHours = 75,
                semester = 7,
                currentGrade = 95.0,
                midtermScore = 94.0,
                finalScore = 96.0,
                examScore = 95.0,
                isPassed = true,
                description = "Maşyn öwrenmesi, neýron torlary we maglumatlary intellektual seljermek."
            )
        )
    }

    private fun createInitialExams(): List<ExamItem> {
        return listOf(
            ExamItem(
                id = "ex_1",
                subjectName = "Kiberhowpsuzlyk",
                dateIso = "2026-09-20",
                displayDate = "20 Sentýabr, 2026",
                time = "10:00",
                classroom = "205-nji otag",
                type = ExamType.FINAL,
                daysLeft = 4,
                teacherName = "D. Gurbanow",
                isPast = false
            ),
            ExamItem(
                id = "ex_2",
                subjectName = "Maglumatlar bazasy",
                dateIso = "2026-09-24",
                displayDate = "24 Sentýabr, 2026",
                time = "09:00",
                classroom = "412-nji otag",
                type = ExamType.FINAL,
                daysLeft = 8,
                teacherName = "M. Baýramowa",
                isPast = false
            ),
            ExamItem(
                id = "ex_3",
                subjectName = "Maglumat ulgamlary",
                dateIso = "2026-09-28",
                displayDate = "28 Sentýabr, 2026",
                time = "11:00",
                classroom = "304-nji otag",
                type = ExamType.FINAL,
                daysLeft = 12,
                teacherName = "O. Aşyrow",
                isPast = false
            ),
            ExamItem(
                id = "ex_4",
                subjectName = "Programmirleme",
                dateIso = "2026-10-02",
                displayDate = "2 Oktýabr, 2026",
                time = "14:00",
                classroom = "108-nji otag",
                type = ExamType.FINAL,
                daysLeft = 16,
                teacherName = "A. Myradow",
                isPast = false
            ),
            ExamItem(
                id = "ex_5",
                subjectName = "Kompýuter torlary",
                dateIso = "2026-06-12",
                displayDate = "12 Iýun, 2026",
                time = "10:00",
                classroom = "202-nji otag",
                type = ExamType.MIDTERM,
                daysLeft = 0,
                teacherName = "E. Amanow",
                isPast = true
            )
        )
    }

    private fun createInitialAnnouncements(): List<AnnouncementItem> {
        return listOf(
            AnnouncementItem(
                id = "ann_1",
                title = "2026/2027-nji okuw ýylynyň güýzki ekzamen tertibi tassyklandy",
                date = "14 Sentýabr, 2026",
                category = "Ekzamen",
                shortDescription = "Sanly tehnologiýalar we kiberhowpsuzlyk fakultetiniň 4-nji ýyl talyplarynyň jemleýji döwlet synaglary we ekzamen tertibi çap edildi.",
                fullContent = "Ýagşygeldi Kakaýew adyndaky Halkara nebit we gaz uniwersitetiniň rektorlygy tarapyndan 2026/2027-nji okuw ýylynyň güýzki semestriniň ekzamen tertibi doly tassyklanyldy. B toparynyň talyplarynyň synaglary 20-nji sentýabrda 'Kiberhowpsuzlyk' dersinden başlap, 205-nji otagda geçiriler. Talyplaryň synag wagtyndan 15 minut öň talyp şahsyýetnamasy bilen gelmekleri berk talap edilýär.",
                isImportant = true,
                author = "HNGU Okuw bölümi"
            ),
            AnnouncementItem(
                id = "ann_2",
                title = "HNGU sanly ylmy kitaphanasynyň täzelenen maglumatlar gory",
                date = "12 Sentýabr, 2026",
                category = "Okuw",
                shortDescription = "Nebit-gaz senagatynda emeli aň we maglumat howpsuzlygy boýunça halkara ylmy žurnallaryň elýeterliligi açyldy.",
                fullContent = "Uniwersitetiň sanly kitaphanasy IEEE, Springer we ScienceDirect elektron neşirleri bilen täzelendi. Talyplar öz şahsy talyp kart belgisi arkaly uniwersitetiň torundan erkin peýdalanyp bilerler.",
                isImportant = false,
                author = "HNGU Sanly Kitaphana"
            ),
            AnnouncementItem(
                id = "ann_3",
                title = "Halkara kibersport we talyplar arasynda programmaçylar bäsleşigi",
                date = "10 Sentýabr, 2026",
                category = "Çäreler",
                shortDescription = "Kiberhowpsuzlyk we algoritmler boýunça HNGU-nyň içki çempionatyna hasaba alyş başlandy.",
                fullContent = "Sanly tehnologiýalar we kiberhowpsuzlyk fakultetiniň guramagynda talyplar arasynda 'CyberGuard HNGU-2026' atly algoritmiki programmirleme bäsleşigi geçiriler. Ýeňijilere diplomlar we gymmat bahaly baýraklar gowşurylar.",
                isImportant = true,
                author = "Sanly tehnologiýalar fakulteti"
            ),
            AnnouncementItem(
                id = "ann_4",
                title = "Nebit-gaz pudagynda kiberhowpsuzlyk: Ylmy-amaly maslahat",
                date = "08 Sentýabr, 2026",
                category = "Uniwersitet",
                shortDescription = "Pudaklaýyn edaralaryň wekilleriniň gatnaşmagynda tegelek stol we duşuşyk meýilleşdirildi.",
                fullContent = "Uniwersitetiň uly mejlisler zalynda ýurdumyzyň nebit-gaz toplumynyň öňdebaryjy hünärmenleri bilen talyplaryň duşuşygy geçiriler. Gatnaşmak isleýän talyplar öz ylmy işlerini ugradyp bilerler.",
                isImportant = false,
                author = "Ylmy barlag bölümi"
            )
        )
    }

    private fun createInitialNotifications(): List<NotificationItem> {
        return listOf(
            NotificationItem(
                id = "n_1",
                title = "10 minutdan Maglumat ulgamlary sapagyň bar",
                body = "302-nji otagda Mugallym O. Aşyrow bilen amaly sapak başlaýar.",
                timestamp = "13:50",
                targetScreen = "schedule",
                isRead = false
            ),
            NotificationItem(
                id = "n_2",
                title = "Ertir Kiberhowpsuzlyk boýunça ekzamen bar",
                body = "20 Sentýabr, 10:00, 205-nji otag. Synag tertibini barlaň.",
                timestamp = "09:15",
                targetScreen = "exams",
                isRead = false
            ),
            NotificationItem(
                id = "n_3",
                title = "Uniwersitetden täze bildiriş geldi",
                body = "2026/2027-nji okuw ýylynyň güýzki ekzamen tertibi tassyklandy.",
                timestamp = "Dünä",
                targetScreen = "announcements",
                isRead = true
            ),
            NotificationItem(
                id = "n_4",
                title = "Ekzamen tertibinde üýtgeşme boldy",
                body = "Maglumatlar bazasy dersi boýunça synag 412-nji otaga geçirildi.",
                timestamp = "13 Sent",
                targetScreen = "exams",
                isRead = true
            )
        )
    }
}
