package com.example.features.offline_ai.evaluation

/**
 * Benchmark dataset containing EXACTLY 200 comprehensive evaluation questions.
 * Fully aligned with the rich multi-year student dataset:
 * - Student profile & verification (identity, year, advisor, thesis)
 * - Current semester weekly schedule, rooms, timing, and free hours
 * - Multi-year historical semesters (1st to 6th semesters, past GPAs, credits, passed courses)
 * - Teachers & faculty directory (historical teachers, departments, courses taught)
 * - Attendance & absence history (overall attendance %, excused/unexcused absences, subject breakdown)
 * - Grades, scores, calculations, comparisons between semesters, progression trends
 * - Upcoming and historical exams (rooms, dates, days remaining, examiners)
 * - Multi-turn conversational flows & follow-up questions
 * - Cross-record reasoning (attendance vs grades, teacher vs department, credits vs graduation)
 * - Multilingual support (Turkmen, Russian, English, Turkish)
 *
 * NOTE: These questions are NEVER hardcoded into if-else rules.
 * They are evaluated dynamically by the offline Local LLM + Generic Retrieval Engine.
 */
object OfflineEvaluationDataset {

    val questions: List<String> = listOf(
        // 1 - 20: Profile, Degree, Advisor & Thesis
        "Men kim we haýsy fakultetde okaýaryn?",
        "Talyp kart belgim näçenji?",
        "Men näçinji ýyl talyp we haýsy topar?",
        "Meniň hünärim we kafedram näme?",
        "Meniň ylmy ýolbaşçym kim we diplom işimiň temasy näme?",
        "Okuwa kabul edilen senäm haçan?",
        "Meniň gazanan hormat hatlarym ýa-da üstünliklerim barmy?",
        "Meniň uniwersitetdäki resmi elektron poçtam näme?",
        "Akademiki ýagdaýym nähili (stipendiýa, honors)?",
        "Döwlet ders bäsleşiginde haýsy ugur boýunça ýeňiji boldum?",
        "What is my full registered name and student ID?",
        "Which faculty, department, and degree program am I in?",
        "Who is my academic advisor and what is my thesis topic?",
        "When did I officially enroll at the university?",
        "Do I have any honors or special achievements on my record?",
        "Каково мое полное имя, факультет и номер студенческого билета?",
        "Кто является моим научным руководителем и какая тема диплома?",
        "Когда я поступил в университет и какой у меня академический статус?",
        "Hangi fakülte ve bölümde öğrenim görüyorum?",
        "Tez danışmanım kim ve bitirme tezimin konusu nedir?",

        // 21 - 45: Multi-Year Historical Semesters & Past Academic Records
        "Birinji ýylyň birinji semestrinde haýsy dersleri okapdym?",
        "1-nji semestrdäki GPA balym näçe bolupdy?",
        "Ikinji semestrde Ýokary matematika II dersinden näçe baha aldym?",
        "Öňki semestrleriň içinde iň ýokary GPA alan semestrim haýsy?",
        "3-nji semestrde meniň GPA balym näçe boldy?",
        "4-nji semestrde haýsy dersleri geçdim?",
        "5-nji semestrde Kiberhowpsuzlyk dersinden baham näçe boldy?",
        "Geçen 6-njy semestrde haýsy derslerden synag tabşyrdym?",
        "Öňki semestrlerde hiç ýykylyp, gaýtadan tabşyran (retaken) dersim barmy?",
        "1-nji semestr bilen 3-nji semestrdäki GPA balymy deňeşdirip ber?",
        "Öňki ýyllarda jemi näçe kredit toplap bildim?",
        "Which subjects did I study during my first semester at university?",
        "What was my semester GPA in my second semester?",
        "What grade did I receive in Data Structures during semester 3?",
        "Did I ever fail or retake any courses in previous years?",
        "How many credits did I complete in my second year?",
        "Compare my GPA between semester 1 and semester 5.",
        "Какие дисциплины я изучал на первом курсе?",
        "Какой средний балл GPA был у меня в третьем семестре?",
        "Были ли у меня академические задолженности или пересдачи в прошлых семестрах?",
        "Сколько кредитов я закрыл за первые два года обучения?",
        "İlk senemde aldığım dersler ve notlarım nasıldı?",
        "Geçmiş dönemler arasında en yüksek not ortalamam hangi dönemdeydi?",
        "Daha önce alttan aldığım veya kaldığım ders oldu mu?",
        "2. sınıfta kaç kredi tamamladım?",

        // 46 - 70: Teacher History, Faculty & Departments
        "1-nji ýylda maňa ýokary matematikadan kim ders berdi?",
        "Fizika mugallymym kim bolupdy we ol haýsy kafedradan?",
        "D. Gurbanow mugallym maňa haýsy dersleri okatdy?",
        "Kriptografiýa dersini haýsy mugallym okadýar we onuň ylmy derejesi näme?",
        "M. Orazow mugallym haýsy kafedrada işleýär?",
        "Bütin okuw döwrümde maňa iň köp ders okan mugallym kim?",
        "Kiberhowpsuzlyk kafedrasyndan haýsy mugallymlar sapak berdi?",
        "Iňlis dili mugallymymyň ady kim?",
        "Häzirki semestrde maňa sapak berýän mugallymlary sanap ber?",
        "Öňki ýyllardaky mugallymlarym bilen häzirki mugallymlarym kimler?",
        "Who taught me Higher Mathematics during my freshman year?",
        "Which department does professor A. Aşyrow belong to?",
        "Which teachers have taught me cybersecurity subjects so far?",
        "Who has been my English language teacher across semesters?",
        "Which faculty member has taught me the highest number of courses?",
        "Кто вел у меня высшую математику на первом курсе?",
        "С каких кафедр преподаватели вели у меня профильные предметы?",
        "Какие предметы преподавал мне преподаватель Д. Гурбанов?",
        "Кто преподает мне криптографию и базы данных в этом семестре?",
        "Сколько преподавателей сейчас ведут занятия в моей группе?",
        "İlk yılımda bana matematik dersi veren hocanın adı neydi?",
        "D. Gurbanow şu ana kadar bana hangi dersleri verdi?",
        "Bölümümde en çok dersime giren öğretim görevlisi kimdir?",
        "Kriptografi hocamız hangi anabilim dalında görev yapmaktadır?",
        "Geçmiş dönemlerdeki İngilizce öğretmenim kimdi?",

        // 71 - 95: Attendance & Absence History
        "Meniň umumy gatnaşyk göterimim näçe?",
        "Jemi näçe sagat sapaga gelmändirin?",
        "Gelmedik sapaklarymyň näçesi sebäpli, näçesi sebäpsiz?",
        "Men jemi näçe gezek sapaga giç galdym?",
        "Häzirki semestrde iň köp gelmedik dersim haýsy?",
        "DBMS dersinden näçe sapak sypdyrdym?",
        "Öňki semestrlerde iň köp haýsy semestrde sapak sypdyrypdyryn?",
        "Kiberhowpsuzlyk esaslary dersine gatnaşygym näçe göterim?",
        "Gatnaşygyma görä meniň düzgün-tertip ýagdaýym nähili?",
        "Gelmedik sapaklarymyň hemmesi hasaba alnanmy?",
        "What is my overall attendance percentage across all classes?",
        "How many total hours of classes have I missed?",
        "How many of my absences were excused versus unexcused?",
        "Which subject in the current semester has the highest number of missed hours?",
        "How many times have I been marked late to class?",
        "Каков мой общий процент посещаемости занятий?",
        "Сколько всего академических часов я пропустил?",
        "Сколько пропусков было по уважительной причине, а сколько без?",
        "По какому предмету в этом семестре у меня больше всего пропусков?",
        "Были ли у меня опоздания на занятия в текущем семестре?",
        "Genel ders devamlılık oranım yüzde kaç?",
        "Şu ana kadar toplam kaç saat devamsızlığım var?",
        "Devamsızlıklarımın kaçı mazeretli, kaçı mazeretsiz?",
        "En çok devamsızlık yaptığım ders hangisidir?",
        "Derslere kaç kez geç kaldım?",

        // 96 - 120: Grades, Cumulative GPA, Credits & Graduation Progress
        "Meniň häzirki umumy GPA balym näçe göterim?",
        "Jemi näçe kredit topladym we diplom almaga näçe kredit galdy?",
        "Bu semestr tamamlanandan soň mende jemi näçe kredit bolar?",
        "Kiberhowpsuzlyk esaslaryndan häzirki balym näçe?",
        "Kriptografiýa we maglumat goragy dersinden aralyk synag balym näçe?",
        "DBMS dersinden häzirki umumy balym näçe?",
        "Kompýuter torlary dersinden alan balym näçe?",
        "Emeli aň ulgamlary dersinde aralyk balym näçe?",
        "Bulut tehnologiýalary dersinden häzirki balym näçe?",
        "Bu semestrde iň pes bal alan dersim haýsy?",
        "Bu semestrde iň ýokary bal alan dersim haýsy?",
        "Ähli dersler boýunça bahalarym geçiş balyndan ýokarymy?",
        "Semestrleriň dowamynda GPA balymyň ösüş meýli (trend) nähili boldy?",
        "What is my current cumulative GPA?",
        "How many credits have I accumulated and how many remain for graduation?",
        "What is my highest and lowest grade in the current semester?",
        "What score did I receive on the midterm for Cryptography?",
        "How has my semester GPA evolved from semester 1 to semester 6?",
        "Каков мой текущий кумулятивный средний балл GPA?",
        "Сколько кредитов мне осталось набрать для получения диплома?",
        "По какому предмету в этом семестре у меня наивысшая успеваемость?",
        "Какова динамика изменения моего среднего балла по семестрам?",
        "Şu anki kümülatif genel not ortalamam (GPA) kaçtır?",
        "Mezun olabilmek için geriye kaç kredim kaldı?",
        "Bu dönem en yüksek ve en düşük notlarım hangi derslerde?",

        // 121 - 145: Current Weekly Schedule & Classroom Timetable
        "Duşenbe güni irden ilkinji sapagym haýsy otagda geçirilýär?",
        "Duşenbe güni sapaklarym sagat näçede başlap, näçede gutarýar?",
        "Sişenbe güni jemi näçe sany sapak bar?",
        "Sişenbe güni laboratoriýa sapagy barmy we haýsy otagda?",
        "Çarşenbe güni ir sagat 09:00-da haýsy dersi okamaly?",
        "Penşenbe güni D. Gurbanow mugallym haýsy dersden sapak berýär?",
        "Anna güni haýsy dersler bar we olar leksiýamy ýa-da tejribe?",
        "Hepdäniň haýsy günleri iň köp sapak bar?",
        "Hepdäniň haýsy gününde sapaklar ir tamamlanýar?",
        "415-nji otagda haýsy sapaklar geçilýär?",
        "What classes do I have scheduled on Monday morning?",
        "At what time does my last lecture finish on Tuesday?",
        "Do I have any lab sessions scheduled on Tuesday or Wednesday?",
        "Which classroom should I go to for Cloud Technologies?",
        "Who is the lecturer for my Thursday classes?",
        "What does my timetable look like on Friday?",
        "Which day of the week has the heaviest course load?",
        "Какие пары стоят в моем расписании на понедельник?",
        "В какой аудитории проходит занятие по криптографии?",
        "Во сколько заканчивается последняя пара во вторник?",
        "Есть ли у меня лабораторные работы в расписании?",
        "Pazartesi günü ilk dersim saat kaçta ve hangi derslikte?",
        "Salı günü öğleden sonra laboratuvar dersim var mı?",
        "Çarşamba günkü ders programımı özetler misin?",
        "Cuma günü hangi derslerim var?",

        // 146 - 165: Free Time, Gaps, Study Planning & Conflicts
        "Ertir sapaklardan soň günortan meniň boş wagtym barmy?",
        "Duşenbe güni iki sapagyň arasynda näçe minut arakesme bar?",
        "Sişenbe güni sagat 12:15-den soň boş wagtym barmy ýa-da sapak barmy?",
        "Penşenbe güni sagat 12:30-dan soň meýilnamam boşmy?",
        "Hepdäniň haýsy güni özbaşdak taýýarlyk üçin iň köp boş wagt bar?",
        "Am I free after lunch on Tuesday?",
        "How much free time do I have between classes on Monday?",
        "Looking at my timetable, what is the best time slot for independent study?",
        "Do I have any long breaks or gaps between lectures on Thursday?",
        "Could I schedule a research meeting on Friday afternoon?",
        "Свободен ли я во вторник после 12:15?",
        "Сколько свободного времени у меня между парами в понедельник?",
        "Какой день недели наиболее удобен для подготовки к диплому?",
        "Успею ли я пообедать между занятиями во вторник?",
        "Есть ли свободные окна в расписании в пятницу?",
        "Yarın öğleden sonra boş vaktim olacak mı?",
        "Salı günü son dersten sonra serbest zamanım var mı?",
        "Ders aralarındaki en uzun boşluk hangi gün?",
        "Haftanın hangi günü kendi çalışmalarıma daha çok vakit ayırabilirim?",
        "Pazartesi günü saat 12:15'ten sonra boş muyum?",

        // 166 - 180: Upcoming & Historical Exams
        "Meniň iň ýakyn ekzamenim haçan we haýsy dersden?",
        "Kiberhowpsuzlyk esaslary boýunça ekzamene näçe gün galdy?",
        "Kriptografiýa ekzameni haýsy senede we sagat näçede başlanýar?",
        "Ekzamenler haýsy otaglarda guralýar?",
        "Sentýabr aýynda jemi näçe ekzamen tabşyrmaly?",
        "Oktýabr aýynda haýsy derslerden ekzamen bellenen?",
        "Ilkinji ekzamen bilen ikinji ekzameniň arasynda näçe gün dynç alyş bar?",
        "Ekzamenleriň haýsysy 'Final', haýsysy 'Midterm' görnüşinde?",
        "Which exam comes first on my schedule?",
        "How many days remain until my Cryptography exam?",
        "What is the date, time, and room for the Computer Networks final exam?",
        "Когда состоится мой самый ближайший экзамен?",
        "Сколько дней осталось до экзамена по основам кибербезопасности?",
        "В каких аудиториях будут проходить осенние экзамены?",
        "En yakın sınavım hangi tarihte ve hangi saatte?",

        // 181 - 190: University Announcements & Campus Activities
        "Uniwersitetde soňky günlerde haýsy täze bildirişler çykdy?",
        "CyberGuard HNGU-2026 bäsleşigi barada nämeler mälim?",
        "Bäsleşige gatnaşmak üçin haçana çenli hasaba durmaly?",
        "Sanly ylmy kitaphana nähili elýeterli edildi?",
        "Kitaphanada haýsy halkara ylmy portallara elýeterlilik bar?",
        "What are the latest official announcements from the university?",
        "Could you provide details regarding the CyberGuard HNGU-2026 contest?",
        "How can students access the IEEE and Springer scientific library?",
        "Какие важные университетские объявления были опубликованы недавно?",
        "Что известно о предстоящем соревновании CyberGuard HNGU-2026?",

        // 191 - 200: Multi-Turn Continuity & Complex Context Reasoning
        "Ertir ir bilen näme sapagym bar?", // Step 1
        "Ondan soňky sapak sagat näçede başlanýar?", // Step 2 (Follow-up)
        "Indiki ekzamenim haçan?", // Step 1
        "Oňa näçe gün galdy?", // Step 2 (Follow-up)
        "Otagy nirede we kime tabşyrmaly?", // Step 3 (Follow-up)
        "Which course carries the highest number of credits in this semester?",
        "And what is my current score in that particular course?",
        "Who is the professor teaching it?",
        "Geçen semestrdäki iň ýokary bal alan dersim haýsy bolupdy?",
        "Akademiki ýagdaýymy, gatnaşygymy we derslerimi umumy jemläp ber."
    )
}
