package com.example.features.offline_ai.knowledge

import java.util.Locale

/**
 * Layer A: General HNGU Knowledge Base.
 * Expanded with comprehensive topics covering academic rules, campus life, and student activities.
 */
object GeneralHnguKnowledge {

    data class KnowledgeTopic(
        val id: String,
        val title: String,
        val keywords: List<String>,
        val contentTk: String,
        val contentRu: String,
        val contentEn: String,
        val contentTr: String
    )

    data class GeneralKnowledgeMatch(
        val topic: KnowledgeTopic,
        val matchedKeyword: String,
        val formattedContent: String
    )

    val TOPICS = listOf(
        KnowledgeTopic(
            id = "university_overview",
            title = "HNGU barada umumy maglumat",
            keywords = listOf("hngu", "universitet", "iogu", "yagsygeldi kakayew", "nebit gaz", "taryhy"),
            contentTk = """
                Ýagşygeldi Kakaýew adyndaky Halkara nebit we gaz uniwersiteti (HNGU):
                • Ýeri: Aşgabat şäheri, Arçabil şaýoly.
                • Esaslandyrylan ýyly: 2012-nji ýyl.
                • Maksady: Türkmenistanyň nebit-gaz, geologiýa we IT pudaklary üçin hünärmen taýýarlamak.
            """.trimIndent(),
            contentRu = "Международный университет нефти и газа (ХНГУ): Основан в 2012 году в Ашхабаде.",
            contentEn = "IUOG (HNGU): International Oil and Gas University in Ashgabat, founded in 2012.",
            contentTr = "HNGU: 2012 yılında Aşkabat'ta kurulan Uluslararası Petrol ve Gaz Üniversitesi."
        ),
        KnowledgeTopic(
            id = "grading_system",
            title = "GPA we Bahalandyryş",
            keywords = listOf("gpa", "baha", "bal", "grading", "ects", "hasap", "grade"),
            contentTk = """
                HNGU-nyň Bahalandyryş ulgamy:
                • 90-100: A (4.0)
                • 80-89: B (3.0-3.5)
                • 70-79: C (2.0-2.5)
                • 60-69: D (1.0-1.5)
                • 0-59: F (Geçmedi)
                GPA: Her dersiň balynyň kreditine köpeldilip, jemi kredite bölünmegi arkaly hasaplanýar.
            """.trimIndent(),
            contentRu = "Система оценок ХНГУ: A (90-100), B (80-89), C (70-79), D (60-69), F (ниже 60).",
            contentEn = "IOGU Grading: A (90-100), B (80-89), C (70-79), D (60-69), F (Fail).",
            contentTr = "Not Sistemi: A (90-100), B (80-89), C (70-79), D (60-69), F (Kaldı)."
        ),
        KnowledgeTopic(
            id = "it_center",
            title = "Innowasion IT-merkezi",
            keywords = listOf("it merkez", "startap", "startup", "it center", "innowasiýa"),
            contentTk = "HNGU Innowasion IT-merkezi talyplaryň IT taslamalaryny, startaplaryny we koding bäsleşiklerini goldaýar.",
            contentRu = "Инновационный ИТ-центр поддерживает студенческие стартапы и ИТ-проекты.",
            contentEn = "Innovation IT Center supports student startups and coding initiatives.",
            contentTr = "BT Merkezi: Öğrenci startup projelerini ve teknoloji girişimlerini destekler."
        ),
        KnowledgeTopic(
            id = "cyberguard",
            title = "CyberGuard Klup",
            keywords = listOf("cyberguard", "kiberhowpsuzlyk", "ctf", "cybersecurity"),
            contentTk = "CyberGuard: Kiberhowpsuzlyk we CTF (Capture The Flag) ýaryşlaryna taýýarlyk gurnagy.",
            contentRu = "CyberGuard: Клуб по кибербезопасности и подготовке к CTF-турнирам.",
            contentEn = "CyberGuard: Cybersecurity and CTF competition preparation club.",
            contentTr = "CyberGuard: Siber güvenlik ve CTF yarışma eğitim kulübü."
        ),
        KnowledgeTopic(
            id = "practice",
            title = "Hünär tejribeligi",
            keywords = listOf("tejribe", "praktika", "practice", "internship", "önümçilik"),
            contentTk = "Hünär tejribeligi tomus paslynda 'Türkmengaz', 'Türkmennebit' we beýleki degişli edaralarda geçirilýär.",
            contentRu = "Производственная практика проходит летом в компаниях нефтегазового сектора.",
            contentEn = "Industrial practice takes place in summer at major energy companies.",
            contentTr = "Staj: Yaz döneminde petrol ve gaz sektörü kurumlarında pratik eğitim."
        ),
        KnowledgeTopic(
            id = "thesis",
            title = "Diplom işi",
            keywords = listOf("diplom", "thesis", "diplom isi", "graduating work"),
            contentTk = "Diplom işi 4-nji ýylyň ahyrynda ylmy ýolbaşçy bilen yzygiderli maslahatlaşyp ýerine ýetirilýär.",
            contentRu = "Дипломная работа защищается на выпускном курсе под руководством преподавателя.",
            contentEn = "Thesis work is completed and defended in the graduation year.",
            contentTr = "Bitirme Tezi: Son sınıfta danışman eşliğinde bilimsel araştırma."
        ),
        KnowledgeTopic(
            id = "ethics",
            title = "Akademiki etika",
            keywords = listOf("etika", "plagiat", "ethics", "integrity", "düzgün"),
            contentTk = "Plagiat (başganyň işini ogurlamak) gadagandyr. Akademiki dürüstlük synaglarda we ylmy işlerde talap edilýär.",
            contentRu = "Плагиат запрещен. Академическая честность — основа обучения в ХНГУ.",
            contentEn = "Plagiarism is strictly prohibited. Academic integrity is expected from all students.",
            contentTr = "Etik: İntihal yasaktır. Akademik dürüstlük her çalışmada esastır."
        ),
        KnowledgeTopic(
            id = "blocks",
            title = "Binalar we Bloklar",
            keywords = listOf("bina", "blok", "block", "campus", "otag"),
            contentTk = "A blok: Rektorat we dolandyryş. B blok: Esasy okuw otaglary. Ç blok: Tehniki laboratoriýalar.",
            contentRu = "Блок А: Администрация. Блок Б: Аудитории. Блок В: Лабораторный корпус.",
            contentEn = "Block A: Rectorate. Block B: Main classrooms. Block C: Technical laboratories.",
            contentTr = "Bloklar: A-Yönetim, B-Derslikler, C-Teknik laboratuvarlar."
        ),
        KnowledgeTopic(
            id = "sports",
            title = "Sport seksiýalary",
            keywords = listOf("sport", "futbol", "basketbol", "gym", "tennis"),
            contentTk = "Futbol, woleýbol, basketbol we tennis seksiýalary uniwersitetiň sport toplumynda darsden soň işleýär.",
            contentRu = "Секции футбола, волейбола и тенниса доступны в спорткомплексе университета.",
            contentEn = "Sports sections like football and basketball are active in the campus gym.",
            contentTr = "Spor: Futbol, voleybol ve basketbol branşlarında eğitim verilmektedir."
        ),
        KnowledgeTopic(
            id = "midterm",
            title = "Aralyk synaglar (Midterms)",
            keywords = listOf("aralyk", "midterm", "atestasiya", "assessment"),
            contentTk = "Aralyk synaglar (attestasiýalar) her semestriň 8-nji we 9-njy hepdesinde geçirilýär.",
            contentRu = "Промежуточная аттестация проводится в середине семестра на 8-9 неделе.",
            contentEn = "Midterm assessments take place during the 8th and 9th weeks of the semester.",
            contentTr = "Ara Sınav: Dönem ortası değerlendirmeleri 8-9. haftalarda yapılır."
        ),
        KnowledgeTopic(
            id = "langclubs",
            title = "Dil klublary",
            keywords = listOf("dil", "english club", "rus dili", "speaking"),
            contentTk = "Iňlis we Rus dilleri boýunça Speaking klublary talyplar üçin darsden boş wagty işleýär.",
            contentRu = "Разговорные клубы английского и русского языков открыты для всех студентов.",
            contentEn = "English and Russian conversation clubs help improve speaking skills.",
            contentTr = "Dil: İngilizce ve Rusça konuşma kulüpleri aktiftir."
        ),
        KnowledgeTopic(
            id = "canteen",
            title = "Naharanalar",
            keywords = listOf("naharhana", "canteen", "nahar", "iýmit"),
            contentTk = "Baş naharhana we her blogyň içinde talyplar üçin kiçi bufetler elýeterlidir.",
            contentRu = "Главная столовая и буфеты расположены во всех учебных корпусах.",
            contentEn = "The main canteen and small snack bars are available throughout the campus.",
            contentTr = "Yemek: Ana yemekhane ve bloklardaki kantinler hizmet vermektedir."
        ),
        KnowledgeTopic(
            id = "dresscode",
            title = "Egin-eşik düzgüni",
            keywords = listOf("egin eshik", "dress code", "uniforma", "lybas"),
            contentTk = "Talyplar uniwersitete resmi-işewür lybasda (ak köýnek, gara balak) gelmelidirler.",
            contentRu = "Студенты обязаны соблюдать официально-деловой стиль в одежде.",
            contentEn = "Students are required to follow a formal business dress code.",
            contentTr = "Kıyafet: Resmi akademik giyim kuralı (beyaz gömlek) uygulanmaktadır."
        ),
        KnowledgeTopic(
            id = "robotics",
            title = "Robototehnika gurnagy",
            keywords = listOf("robot", "robotics", "arduino", "dron"),
            contentTk = "Robototehnika gurnagynda talyplar Arduino, dronlar we awtomatika taslamalaryny ýasaýarlar.",
            contentRu = "В клубе робототехники изучают Arduino, дроны и автоматизацию.",
            contentEn = "Robotics club focuses on Arduino, drones, and automation systems.",
            contentTr = "Robotik: Arduino ve drone teknolojileri üzerine projeler geliştirilir."
        ),
        KnowledgeTopic(
            id = "career",
            title = "Karyera mümkinçilikleri",
            keywords = listOf("ish", "job", "career", "karyera"),
            contentTk = "Uçurymlar Türkmenistanyň nebit-gaz toplumynda, IT edaralarynda we halkara kompaniýalarda işleýärler.",
            contentRu = "Выпускники трудоустраиваются в компании ТЭК и ИТ-сектор.",
            contentEn = "Graduates work in energy sectors, IT firms, and international entities.",
            contentTr = "Kariyer: Mezunlar enerji ve teknoloji sektöründe iş bulabilmektedir."
        ),
        KnowledgeTopic(
            id = "lateness",
            title = "Sapaga giç galmak",
            keywords = listOf("gic galmak", "lateness", "giç"),
            contentTk = "Sapaga giç galmak gatnaşyga we akademiki netijä ýaramaz täsir edip biler.",
            contentRu = "Опоздания на занятия могут повлиять на учет посещаемости и оценки.",
            contentEn = "Frequent lateness can negatively impact attendance records and grades.",
            contentTr = "Geç Kalma: Derse geç kalmak devamsızlık olarak işlenebilir."
        ),
        KnowledgeTopic(
            id = "events",
            title = "Medeni çäreler",
            keywords = listOf("bayramcylyk", "event", "concert", "dabara"),
            contentTk = "Nowruz, Täze ýyl we Garaşsyzlyk baýramy mynasybetli uly dabaralar geçirilýär.",
            contentRu = "В университете проводятся праздничные мероприятия и концерты.",
            contentEn = "The university hosts grand celebrations for national holidays and events.",
            contentTr = "Etkinlikler: Milli bayramlar ve kültürel konserler düzenlenir."
        ),
        KnowledgeTopic(
            id = "lab_safety",
            title = "Laboratoriýa howpsuzlygy",
            keywords = listOf("laboratoriya", "howpsuzlyk", "safety", "madda"),
            contentTk = "Laboratoriýada diňe ýörite gorag halatynda we mugallymyň gözegçiliginde işlemeli.",
            contentRu = "В лабораториях обязательны халаты и соблюдение техники безопасности.",
            contentEn = "Lab coats and adherence to safety protocols are mandatory in laboratories.",
            contentTr = "Güvenlik: Laboratuvarlarda önlük kullanımı ve güvenlik kuralları zorunludur."
        ),
        KnowledgeTopic(
            id = "resources",
            title = "Okuw resurslary",
            keywords = listOf("kitap", "resurs", "Syllabus", "kitaphana"),
            contentTk = "Kitaphananyň elektron binýadyndan we içki tordan okuw materiallaryny alyp bilersiňiz.",
            contentRu = "Учебные материалы доступны в электронной библиотеке и внутренней сети.",
            contentEn = "Study materials are accessible via the digital library and internal network.",
            contentTr = "Kaynaklar: Ders notları kütüphaneden ve iç ağdan temin edilebilir."
        ),
        KnowledgeTopic(
            id = "publications",
            title = "Ylmy makalalar",
            keywords = listOf("makala", "ylmy", "publication", "ylm"),
            contentTk = "Talyplar öz ylmy açyşlaryny uniwersitetiň ylmy žurnallarynda çap edip bilerler.",
            contentRu = "Студенты могут подавать научные статьи для публикации в изданиях вуза.",
            contentEn = "Students can submit research papers for publication in university journals.",
            contentTr = "Makale: Bilimsel araştırmalar üniversite dergilerinde yayınlanabilir."
        ),
        KnowledgeTopic(
            id = "attendance_rules",
            title = "Gatnaşyk düzgüni",
            keywords = listOf("gatnaşyk", "attendance", "80%", "gelmedik"),
            contentTk = "Talyp semestr boýunça dersleriň azyndan 80%-ine gatnaşmaga borçludyr.",
            contentRu = "Студент обязан посетить не менее 80% занятий по каждому предмету.",
            contentEn = "Students must maintain at least 80% attendance for each course.",
            contentTr = "Devam: Her derste en az %80 devam zorunluluğu vardır."
        )
    )

    /**
     * Enhanced semantic matching logic.
     * Splits query into tokens and calculates a score based on keyword density and importance.
     */
    fun findMatch(query: String, languageCode: String): GeneralKnowledgeMatch? {
        val q = query.lowercase(Locale.ROOT).trim()
        val queryTokens = q.split(" ").filter { it.length > 2 }.toSet()

        var bestTopic: KnowledgeTopic? = null
        var bestScore = 0.0

        for (topic in TOPICS) {
            var currentScore = 0.0
            for (kw in topic.keywords) {
                val kwLower = kw.lowercase(Locale.ROOT)
                if (q.contains(kwLower)) {
                    // Base score for presence
                    currentScore += 1.0
                    
                    // Bonus for exact token match
                    if (queryTokens.contains(kwLower)) {
                        currentScore += 5.0
                    }
                    
                    // Bonus for title keywords
                    if (topic.title.lowercase(Locale.ROOT).contains(kwLower)) {
                        currentScore += 2.0
                    }
                }
            }
            
            if (currentScore > bestScore) {
                bestScore = currentScore
                bestTopic = topic
            }
        }

        // Only return if confidence is high enough
        if (bestTopic != null && bestScore >= 1.0) {
            val content = when (languageCode) {
                "ru" -> bestTopic.contentRu
                "en" -> bestTopic.contentEn
                "tr" -> bestTopic.contentTr
                else -> bestTopic.contentTk
            }
            return GeneralKnowledgeMatch(
                topic = bestTopic,
                matchedKeyword = "semantic_match",
                formattedContent = content
            )
        }

        return null
    }
}
