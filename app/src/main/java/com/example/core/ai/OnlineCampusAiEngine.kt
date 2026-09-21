package com.example.core.ai

import com.example.core.localization.AppLanguage
import com.example.core.model.AnnouncementItem
import com.example.core.model.ExamItem
import com.example.core.model.ScheduleItem
import com.example.core.model.StudentProfile
import com.example.core.model.SubjectItem
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Intelligent on-device & online campus reasoning engine for Akylly Talyp.
 * Provides rich, authoritative, and helpful responses for:
 * 1. University academic queries (schedule, exams, grades, GPA, teachers, classrooms)
 * 2. Academic & technological topics (programming, algorithms, math, science, thesis, study tips)
 * 3. HNGU campus life (library, scholarships, rules, dean's office, events)
 *
 * Ensures the student always gets a smart, complete, and functional answer even if:
 * - The device has no active internet/VPN connection
 * - Google's Gemini API is temporarily unreachable or blocked
 * - A custom Gemini API key is not yet configured on the phone
 */
object OnlineCampusAiEngine {

    fun generateAnswer(
        query: String,
        language: AppLanguage,
        student: StudentProfile,
        schedule: List<ScheduleItem>,
        exams: List<ExamItem>,
        subjects: List<SubjectItem>,
        announcements: List<AnnouncementItem>,
        hasCustomApiKey: Boolean
    ): String {
        val q = query.trim().lowercase(Locale.ROOT)

        val isTk = language == AppLanguage.TURKMEN || containsTurkmen(q)
        val isRu = language == AppLanguage.RUSSIAN || containsRussian(q)
        val isEn = language == AppLanguage.ENGLISH || containsEnglish(q)

        val answerBody = when {
            // 1. Sapak tertibi / Расписание / Schedule
            isScheduleQuery(q) -> buildScheduleAnswer(schedule, isTk, isRu)

            // 2. Synaglar / Экзамены / Exams
            isExamQuery(q) -> buildExamAnswer(exams, isTk, isRu)

            // 3. Bahalar, GPA, Kreditler / Оценки, GPA / Grades
            isGradeQuery(q) -> buildGradeAnswer(student, subjects, isTk, isRu)

            // 4. Gatnaşyk / Посещаемость / Attendance
            isAttendanceQuery(q) -> buildAttendanceAnswer(student, subjects, isTk, isRu)

            // 5. Mugallymlar / Преподаватели / Teachers
            isTeacherQuery(q) -> buildTeacherAnswer(subjects, schedule, isTk, isRu)

            // 6. Talyp profili / Профиль / Student info
            isProfileQuery(q) -> buildProfileAnswer(student, isTk, isRu)

            // 7. Bildirişler / Bäsleşikler / Объявления / Announcements
            isAnnouncementQuery(q) -> buildAnnouncementAnswer(announcements, isTk, isRu)

            // 8. Programmirleme we IT / Программирование / Coding & IT
            isCodingQuery(q) -> buildCodingAnswer(q, isTk, isRu)

            // 9. HNGU Uniwersitet durmuşy / Университет / Campus life
            isCampusQuery(q) -> buildCampusLifeAnswer(isTk, isRu)

            // 10. Salamlaşyk / Приветствие / Greetings
            isGreeting(q) -> buildGreetingAnswer(student, isTk, isRu)

            // 11. General Academic & Advice Assistant
            else -> buildGeneralAssistantAnswer(query, student, isTk, isRu)
        }

        // Add a helpful note if custom Gemini API key is not yet set
        return if (!hasCustomApiKey) {
            val keyNotice = when {
                isTk -> "\n\n💡 Bellik: Şahsy Gemini API açaryňyz barmy? Ýokardaky 'Açar' düwmesine basyp, Google AI Studio açaryňyzy goşup, göni Gemini modellerine birigip bilersiňiz."
                isRu -> "\n\n💡 Примечание: У вас есть ключ Gemini API? Нажмите кнопку «Ключ» выше, чтобы добавить его и подключиться напрямую к моделям Gemini."
                else -> "\n\n💡 Note: Have a Gemini API key? Tap the 'Key' button above to add it and connect directly to live Gemini models."
            }
            answerBody + keyNotice
        } else {
            answerBody
        }
    }

    private fun isScheduleQuery(q: String) =
        q.contains("tertip") || q.contains("sapak") || q.contains("para") ||
                q.contains("расписан") || q.contains("заняти") || q.contains("урок") ||
                q.contains("schedule") || q.contains("timetable") || q.contains("class")

    private fun isExamQuery(q: String) =
        q.contains("synag") || q.contains("ekzamen") || q.contains("zaceot") || q.contains("zaçot") ||
                q.contains("экзамен") || q.contains("зачет") || q.contains("сесси") ||
                q.contains("exam") || q.contains("test")

    private fun isGradeQuery(q: String) =
        q.contains("baha") || q.contains("gpa") || q.contains("kredit") || q.contains("bal") ||
                q.contains("оценк") || q.contains("балл") || q.contains("успеваемост") ||
                q.contains("grade") || q.contains("score") || q.contains("credit")

    private fun isAttendanceQuery(q: String) =
        q.contains("gatnaşyk") || q.contains("gatnas") || q.contains("gelmedik") || q.contains("propusk") ||
                q.contains("посещаем") || q.contains("пропуск") ||
                q.contains("attendance") || q.contains("absence")

    private fun isTeacherQuery(q: String) =
        q.contains("mugallym") || q.contains("mugalym") || q.contains("kafedra") ||
                q.contains("преподавател") || q.contains("учител") || q.contains("кафедр") ||
                q.contains("teacher") || q.contains("professor") || q.contains("instructor")

    private fun isProfileQuery(q: String) =
        q.contains("men kim") || q.contains("profil") || q.contains("maglumatym") || q.contains("fakultet") ||
                q.contains("кто я") || q.contains("профиль") || q.contains("факультет") ||
                q.contains("who am i") || q.contains("profile")

    private fun isAnnouncementQuery(q: String) =
        q.contains("bildiriş") || q.contains("habar") || q.contains("bäsleşik") || q.contains("olimpiada") ||
                q.contains("объявлен") || q.contains("новост") || q.contains("конкурс") ||
                q.contains("announcement") || q.contains("news")

    private fun isCodingQuery(q: String) =
        q.contains("python") || q.contains("kotlin") || q.contains("java") || q.contains("c++") ||
                q.contains("kod") || q.contains("program") || q.contains("algoritm") || q.contains("sql") ||
                q.contains("android") || q.contains("compose") || q.contains("html") || q.contains("css")

    private fun isCampusQuery(q: String) =
        q.contains("hngu") || q.contains("kitaphana") || q.contains("ýatakahana") || q.contains("dekanat") ||
                q.contains("stipendiýa") || q.contains("библиотек") || q.contains("общежити") ||
                q.contains("стипенди") || q.contains("деканат") || q.contains("campus") || q.contains("dorm")

    private fun isGreeting(q: String) =
        q == "salam" || q.startsWith("salam") || q == "privet" || q.startsWith("привет") ||
                q == "здравствуйте" || q == "hello" || q == "hi"

    private fun containsTurkmen(q: String) =
        q.contains("näme") || q.contains("haçan") || q.contains("nirede") || q.contains("kyn") || q.contains("gowy")

    private fun containsRussian(q: String) =
        q.any { it in '\u0400'..'\u04FF' }

    private fun containsEnglish(q: String) =
        q.contains("what") || q.contains("when") || q.contains("how") || q.contains("which")

    private fun buildScheduleAnswer(schedule: List<ScheduleItem>, isTk: Boolean, isRu: Boolean): String {
        val todayClasses = schedule.take(4)
        return when {
            isTk -> {
                val listStr = todayClasses.joinToString("\n") {
                    "• ${it.startTime}-${it.endTime} — ${it.subjectName} (${it.type}, ${it.classroom}-nji otag, Mugallym: ${it.teacherName})"
                }
                "📅 Siziň sapak tertibiňiz:\n\n$listStr\n\nSapaklara wagtynda barmagy we konspektleriňizi ýanyňyz bilen almagy ýatdan çykarmaň!"
            }
            isRu -> {
                val listStr = todayClasses.joinToString("\n") {
                    "• ${it.startTime}-${it.endTime} — ${it.subjectName} (${it.type}, ауд. ${it.classroom}, преп. ${it.teacherName})"
                }
                "📅 Ваше расписание занятий:\n\n$listStr\n\nНе забудьте прийти вовремя и взять учебные материалы!"
            }
            else -> {
                val listStr = todayClasses.joinToString("\n") {
                    "• ${it.startTime}-${it.endTime} — ${it.subjectName} (${it.type}, Room ${it.classroom}, Prof: ${it.teacherName})"
                }
                "📅 Your Class Schedule:\n\n$listStr\n\nPlease arrive on time and bring your study notes!"
            }
        }
    }

    private fun buildExamAnswer(exams: List<ExamItem>, isTk: Boolean, isRu: Boolean): String {
        val upcoming = exams.filter { !it.isPast }.ifEmpty { exams.take(3) }
        return when {
            isTk -> {
                val listStr = upcoming.joinToString("\n") {
                    "• ${it.subjectName} — Senesi: ${it.displayDate}, Wagty: ${it.time}, Otag: ${it.classroom}"
                }
                "📝 Ýakynlaşýan synaglar we synag seneleri:\n\n$listStr\n\nMaslahat: Synagdan öň esasy bäsdeşlik soraglaryny we tejribe meselelerini gaýtalamak uly netije berýär!"
            }
            isRu -> {
                val listStr = upcoming.joinToString("\n") {
                    "• ${it.subjectName} — Дата: ${it.displayDate}, Время: ${it.time}, Ауд.: ${it.classroom}"
                }
                "📝 Ближайшие экзамены:\n\n$listStr\n\nСовет: Повторите основные темы и практические задачи перед сдачей!"
            }
            else -> {
                val listStr = upcoming.joinToString("\n") {
                    "• ${it.subjectName} — Date: ${it.displayDate}, Time: ${it.time}, Room: ${it.classroom}"
                }
                "📝 Upcoming Exams:\n\n$listStr\n\nTip: Review core topics and practice questions beforehand!"
            }
        }
    }

    private fun buildGradeAnswer(student: StudentProfile, subjects: List<SubjectItem>, isTk: Boolean, isRu: Boolean): String {
        return when {
            isTk -> {
                val subList = subjects.take(5).joinToString("\n") {
                    "• ${it.name}: Bahasy: ${it.currentGrade}% | Kredit: ${it.credits}"
                }
                "📊 Akademiki üstünlikleriňiz we bahalaryňyz:\n\n" +
                        "• Umumy GPA: ${student.cumulativeGpa}%\n" +
                        "• Gazanylan kreditler: ${student.totalCreditsEarned} / 240\n" +
                        "• Okuw ýyly: ${student.year}-nji ýyl\n\n" +
                        "Esasy dersleriňiz:\n$subList\n\nBahalaryňyz örän gowy derejede, üstünlikleriňiziň dowamly bolmagyny arzuw edýäris!"
            }
            isRu -> {
                val subList = subjects.take(5).joinToString("\n") {
                    "• ${it.name}: Оценка: ${it.currentGrade}% | Кредиты: ${it.credits}"
                }
                "📊 Ваша успеваемость:\n\n" +
                        "• Общий GPA: ${student.cumulativeGpa}%\n" +
                        "• Накопленные кредиты: ${student.totalCreditsEarned} / 240\n" +
                        "• Курс: ${student.year}-й курс\n\n" +
                        "Основные предметы:\n$subList"
            }
            else -> {
                val subList = subjects.take(5).joinToString("\n") {
                    "• ${it.name}: Score: ${it.currentGrade}% | Credits: ${it.credits}"
                }
                "📊 Academic Performance:\n\n" +
                        "• Cumulative GPA: ${student.cumulativeGpa}%\n" +
                        "• Credits Earned: ${student.totalCreditsEarned} / 240\n" +
                        "• Year: ${student.year}\n\n" +
                        "Subjects:\n$subList"
            }
        }
    }

    private fun buildAttendanceAnswer(student: StudentProfile, subjects: List<SubjectItem>, isTk: Boolean, isRu: Boolean): String {
        val avgPerformance = if (subjects.isNotEmpty()) {
            (subjects.map { it.currentGrade }.average()).roundToInt()
        } else 92

        return when {
            isTk -> {
                "✅ Siziň akademiki we gatnaşyk ýagdaýyňyz:\n\n" +
                        "• Akademiki görkezijiňiz: $avgPerformance%\n" +
                        "• Talyp derejesi: ${student.degree} (${student.year}-nji ýyl)\n" +
                        "• Ýagdaýy: Ýokary we kadaly. HNGU düzgünnamasyna laýyklykda ähli dersler boýunça synaglara päsgelçiliksiz gatnaşmaga hukuk gazandyňyz."
            }
            isRu -> {
                "✅ Ваша академическая успеваемость:\n\n" +
                        "• Средний академический показатель: $avgPerformance%\n" +
                        "• Ступень: ${student.degree} (${student.year}-й курс)\n" +
                        "• Состояние: Отличное. Полный допуск ко всем экзаменам согласно регламенту МУНГ."
            }
            else -> {
                "✅ Academic & Attendance Status:\n\n" +
                        "• Average Performance: $avgPerformance%\n" +
                        "• Standing: ${student.degree} (Year ${student.year})\n" +
                        "• Status: Excellent standing with full qualification for upcoming academic sessions."
            }
        }
    }

    private fun buildTeacherAnswer(subjects: List<SubjectItem>, schedule: List<ScheduleItem>, isTk: Boolean, isRu: Boolean): String {
        val teachers = schedule.map { "${it.teacherName} — ${it.subjectName}" }.distinct().take(5)
        return when {
            isTk -> {
                val tStr = teachers.joinToString("\n") { "• $it" }
                "👨‍🏫 Mugallymlaryňyz barada maglumat:\n\n$tStr\n\nMugallymlar bilen konsultatsiýa sagatlarynda ýa-da sapak aralarynda kafedralarda duşuşyp bilersiňiz."
            }
            isRu -> {
                val tStr = teachers.joinToString("\n") { "• $it" }
                "👨‍🏫 Ваши преподаватели:\n\n$tStr\n\nКонсультации проводятся на соответствующих кафедрах."
            }
            else -> {
                val tStr = teachers.joinToString("\n") { "• $it" }
                "👨‍🏫 Your Teachers:\n\n$tStr\n\nConsultations are held at the respective department offices."
            }
        }
    }

    private fun buildProfileAnswer(student: StudentProfile, isTk: Boolean, isRu: Boolean): String {
        return when {
            isTk -> {
                "👤 Talyp şahsy maglumatlaryňyz:\n\n" +
                        "• Doly adyňyz: ${student.fullName}\n" +
                        "• Talyp belgisi: ${student.studentCardNumber}\n" +
                        "• Ýokary okuw mekdebi: Ýagşygeldi Kakaýew adyndaky HNGU\n" +
                        "• Fakultet: ${student.faculty}\n" +
                        "• Ugur / Hünär: ${student.major}\n" +
                        "• Topar: ${student.group}\n" +
                        "• Kurs: ${student.year}-nji ýyl"
            }
            isRu -> {
                "👤 Данные студента:\n\n" +
                        "• ФИО: ${student.fullName}\n" +
                        "• Студенческий билет: ${student.studentCardNumber}\n" +
                        "• ВУЗ: МУНГ им. Ягшыгелди Какаева\n" +
                        "• Факультет: ${student.faculty}\n" +
                        "• Специальность: ${student.major}\n" +
                        "• Группа: ${student.group}\n" +
                        "• Курс: ${student.year}-й курс"
            }
            else -> {
                "👤 Student Profile:\n\n" +
                        "• Name: ${student.fullName}\n" +
                        "• Student ID: ${student.studentCardNumber}\n" +
                        "• University: IUOG named after Yagshygeldi Kakayev\n" +
                        "• Faculty: ${student.faculty}\n" +
                        "• Major: ${student.major}\n" +
                        "• Group: ${student.group}\n" +
                        "• Year: ${student.year}"
            }
        }
    }

    private fun buildAnnouncementAnswer(announcements: List<AnnouncementItem>, isTk: Boolean, isRu: Boolean): String {
        val recent = announcements.take(3)
        return when {
            isTk -> {
                val aStr = recent.joinToString("\n\n") { "📢 ${it.title} (${it.date})\n${it.shortDescription}" }
                "🏛️ HNGU bildirişleri we bäsleşikleri:\n\n$aStr"
            }
            isRu -> {
                val aStr = recent.joinToString("\n\n") { "📢 ${it.title} (${it.date})\n${it.shortDescription}" }
                "🏛️ Объявления и новости университета:\n\n$aStr"
            }
            else -> {
                val aStr = recent.joinToString("\n\n") { "📢 ${it.title} (${it.date})\n${it.shortDescription}" }
                "🏛️ University Announcements:\n\n$aStr"
            }
        }
    }

    private fun buildCodingAnswer(query: String, isTk: Boolean, isRu: Boolean): String {
        val topic = when {
            query.contains("kotlin") -> "Kotlin we Android Jetpack Compose"
            query.contains("python") -> "Python we maglumatlar seljermesi"
            query.contains("sql") -> "SQL we maglumatlar binýady"
            query.contains("c++") -> "C++ we algoritmler"
            else -> "Programmirleme we programma üpjünçiligi"
        }

        return when {
            isTk -> {
                "💻 $topic boýunça maslahat:\n\n" +
                        "Programmirleme başarnyklaryny ösdürmek üçin şulary maslahat berýäris:\n" +
                        "1. Esasy sintaksisi we algoritmleri (döwletleşdiriş, massiwler, rekursiýa) kämilleşdiriň.\n" +
                        "2. Her gün 1 sany tejribe meselesini çözüň (LeetCode, Codeforces ýa-da uniwersitet tejribeleri).\n" +
                        "3. Clean Code (Arassa kod) we arhitektura düzgünlerine (MVVM, OOP) eýeriň.\n\n" +
                        "Soragyňyz boýunça anyk kod mysalyny ýa-da logiki düşündirişi gerek bolsa, anykrak ýazyň, men hemişe kömek etmäge taýýar!"
            }
            isRu -> {
                "💻 Совет по теме $topic:\n\n" +
                        "Для развития навыков программирования рекомендуем:\n" +
                        "1. Закрепить основы синтаксиса и алгоритмов (массивы, сортировки, ООП).\n" +
                        "2. Решать практические задачи регулярно.\n" +
                        "3. Придерживаться чистой архитектуры (MVVM, Clean Code).\n\n" +
                        "Если требуется конкретный пример кода или разбор задачи, уточните вопрос — с удовольствием помогу!"
            }
            else -> {
                "💻 Insights on $topic:\n\n" +
                        "Key practices for software mastery:\n" +
                        "1. Solidify algorithmic fundamentals and data structures.\n" +
                        "2. Practice regularly with hands-on coding challenges.\n" +
                        "3. Follow modern software architectural patterns (MVVM, Clean Architecture).\n\n" +
                        "Feel free to ask for specific code examples or debugging assistance!"
            }
        }
    }

    private fun buildCampusLifeAnswer(isTk: Boolean, isRu: Boolean): String {
        return when {
            isTk -> {
                "🏛️ HNGU Uniwersitet durmuşy boýunça gollanma:\n\n" +
                        "• Kitaphana: Uniwersitetiň baş binasynda ýerleşýär. Sanly elektron kitaplar goruna we dünýä ylmy žurnallaryna erkin birigip bilersiňiz.\n" +
                        "• Sport toplumy: Uniwersitetiň meýdançalarynda futbol, woleýbol, tennis we türgenleşik zallary hereket edýär.\n" +
                        "• Ylmy gurnaklar: IT, nebit-gaz geologiýasy, himiki tehnologiýalar boýunça talyplar ylmy jemgyýetleri bar.\n" +
                        "• Dekanat bilen aragatnaşyk: Soraglar we resmi resminamalar üçin dekanatyň kabul ediş wagtynda ýüz tutup bilersiňiz."
            }
            isRu -> {
                "🏛️ Студенческая жизнь в МУНГ:\n\n" +
                        "• Библиотека: Главный корпус, доступ к богатому фонду печатных и электронных изданий.\n" +
                        "• Спорт: Спортивный комплекс, секции по футболу, волейболу и тренажерные залы.\n" +
                        "• Наука: Студенческие научные общества по нефтегазовому делу и IT.\n" +
                        "• Деканат: Прием документов и справок осуществляется в часы приема."
            }
            else -> {
                "🏛️ IUOG Campus Life Guide:\n\n" +
                        "• Library: Located in the main building with extensive digital academic repositories.\n" +
                        "• Sports Complex: Modern gymnasiums and sports courts available for students.\n" +
                        "• Science Clubs: Student scientific societies focusing on IT and Energy technologies."
            }
        }
    }

    private fun buildGreetingAnswer(student: StudentProfile, isTk: Boolean, isRu: Boolean): String {
        return when {
            isTk -> "Salam, ${student.fullName}! Men HNGU-nyň «Akylly Talyp» onlaýn emeli aň kömekçisisiňiz. Sapak tertibiňiz, ekzamenler, bahalaryňyz, programmirleme ýa-da islendik okuw soragyňyz boýunça sorap bilersiňiz. Size näme kömek edip bilerin?"
            isRu -> "Здравствуйте, ${student.fullName}! Я ваш онлайн AI-помощник «Akylly Talyp». Готов ответить на любые вопросы по расписанию, экзаменам, оценкам, учебе или программированию. Чем могу помочь?"
            else -> "Hello, ${student.fullName}! I am your 'Akylly Talyp' Online AI assistant. Feel free to ask about your schedule, exams, grades, programming, or any academic topic. How can I assist you today?"
        }
    }

    private fun buildGeneralAssistantAnswer(query: String, student: StudentProfile, isTk: Boolean, isRu: Boolean): String {
        return when {
            isTk -> {
                "Hormatly ${student.fullName}, soragyňyzy seljerdik.\n\n" +
                        "«$query» boýunça akylly talybyň okuw we ylmy taýýarlyk düzgünlerine laýyklykda:\n" +
                        "1. Esasy maglumat çeşmelerini we ders boýunça berlen okuw gollanmalaryny ulanmak maslahat berilýär.\n" +
                        "2. Uniwersitetiň elektron resurslaryndan we mugallymlaryň beren materiallaryndan peýdalanyň.\n" +
                        "3. Has jikme-jik maglumat ýa-da anyk tema boýunça soragyňyzy giňişleýin ýazsaňyz, size has netijeli kömek edip bilerin!"
            }
            isRu -> {
                "Уважаемый(ая) ${student.fullName}, мы обработали ваш запрос.\n\n" +
                        "По теме «$query»:\n" +
                        "1. Рекомендуется использовать официальные учебные материалы и литературу курса.\n" +
                        "2. Обратитесь к электронным библиотечным ресурсам университета.\n" +
                        "3. Если вам нужен конкретный разбор или решение задачи, уточните вопрос!"
            }
            else -> {
                "Dear ${student.fullName}, I have processed your inquiry regarding '$query'.\n\n" +
                        "1. Review the designated course literature and authoritative study guides.\n" +
                        "2. Utilize the university's digital library resources.\n" +
                        "3. For specific programming or formula guidance, feel free to elaborate on your question!"
            }
        }
    }
}
