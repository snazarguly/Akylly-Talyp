package com.example.features.offline_ai.data

import com.example.features.offline_ai.data.db.OfflineAiRoomDatabase
import com.example.features.offline_ai.data.db.PersonalKnowledgeDao
import com.example.features.offline_ai.data.db.PersonalKnowledgeEntity
import com.example.features.offline_ai.data.db.UnknownQuestionDao
import com.example.features.offline_ai.data.db.UnknownQuestionEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.max

/**
 * Repository orchestrating:
 * 1. Strictly user-scoped storage for Unknown Questions & Personal Knowledge
 * 2. Semantic matching & retrieval for learned personal knowledge
 * 3. Deduplication of pending unknown questions
 * 4. Invalidation of obsolete personal knowledge when structured records take priority
 * 5. Clean interface/queue for future online AI processing
 */
class OfflineKnowledgeRepository(
    private val database: OfflineAiRoomDatabase? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val unknownDao: UnknownQuestionDao? get() = database?.unknownQuestionDao()
    private val personalDao: PersonalKnowledgeDao? get() = database?.personalKnowledgeDao()

    // In-memory fallback if database is not available (e.g. headless unit tests)
    private val inMemoryUnknown = mutableMapOf<String, MutableList<UnknownQuestionEntity>>()
    private val inMemoryPersonal = mutableMapOf<String, MutableList<PersonalKnowledgeEntity>>()

    data class PersonalKnowledgeMatch(
        val entity: PersonalKnowledgeEntity,
        val similarityScore: Double,
        val matchedTokens: List<String>
    )

    // =============================================================================================
    // 1. UNKNOWN QUESTIONS QUEUE (USER-SCOPED)
    // =============================================================================================

    fun getPendingQuestionsFlow(userId: String): Flow<List<UnknownQuestionEntity>> {
        val dao = unknownDao
        return if (dao != null) {
            dao.getPendingQuestionsForUser(userId)
        } else {
            flowOf(inMemoryUnknown[userId]?.filter { it.status == "pending" } ?: emptyList())
        }
    }

    suspend fun getPendingQuestionsList(userId: String): List<UnknownQuestionEntity> = withContext(ioDispatcher) {
        val dao = unknownDao
        if (dao != null) {
            dao.getPendingQuestionsList(userId)
        } else {
            inMemoryUnknown[userId]?.filter { it.status == "pending" } ?: emptyList()
        }
    }

    /**
     * Records an unknown question into the user's pending queue.
     * Uses deduplication: if an equivalent question is already pending, updates its timestamp
     * and context rather than creating duplicate records.
     */
    suspend fun recordUnknownQuestion(
        userId: String,
        question: String,
        language: String,
        contextSummary: String
    ): Long = withContext(ioDispatcher) {
        val trimmedQ = question.trim()
        val dao = unknownDao

        if (dao != null) {
            // Check for exact duplicate in pending
            val existing = dao.findExactPendingQuestion(userId, trimmedQ)
            if (existing != null) {
                val updated = existing.copy(
                    timestamp = System.currentTimeMillis(),
                    contextSummary = contextSummary
                )
                dao.insertUnknownQuestion(updated)
                return@withContext existing.id
            }

            val entity = UnknownQuestionEntity(
                userId = userId,
                question = trimmedQ,
                language = language,
                timestamp = System.currentTimeMillis(),
                contextSummary = contextSummary,
                status = "pending"
            )
            val newId = dao.insertUnknownQuestion(entity)

            // Keep storage compact: maximum 50 pending questions per user
            val list = dao.getPendingQuestionsList(userId)
            if (list.size > 50) {
                val oldest = list.drop(50)
                oldest.forEach { dao.deleteQuestion(it.id) }
            }
            newId
        } else {
            val userList = inMemoryUnknown.getOrPut(userId) { mutableListOf() }
            val existingIndex = userList.indexOfFirst { it.question.equals(trimmedQ, ignoreCase = true) && it.status == "pending" }
            if (existingIndex >= 0) {
                val existing = userList[existingIndex]
                val updated = existing.copy(
                    timestamp = System.currentTimeMillis(),
                    contextSummary = contextSummary
                )
                userList[existingIndex] = updated
                existing.id
            } else {
                val newId = System.currentTimeMillis()
                val entity = UnknownQuestionEntity(
                    id = newId,
                    userId = userId,
                    question = trimmedQ,
                    language = language,
                    timestamp = System.currentTimeMillis(),
                    contextSummary = contextSummary,
                    status = "pending"
                )
                userList.add(0, entity)
                if (userList.size > 50) userList.removeLastOrNull()
                newId
            }
        }
    }

    /**
     * Resolves an unknown question and promotes it to personal learned knowledge.
     */
    suspend fun resolveUnknownQuestion(
        id: Long,
        userId: String,
        answer: String,
        source: String = "online_ai_resolution"
    ): Boolean = withContext(ioDispatcher) {
        val dao = unknownDao
        var resolvedQuestion: UnknownQuestionEntity? = null

        if (dao != null) {
            val list = dao.getPendingQuestionsList(userId)
            val target = list.find { it.id == id }
            if (target != null) {
                dao.updateStatus(id, "resolved")
                resolvedQuestion = target
            }
        } else {
            val list = inMemoryUnknown[userId]
            val index = list?.indexOfFirst { it.id == id } ?: -1
            if (index >= 0) {
                val item = list!![index]
                list[index] = item.copy(status = "resolved")
                resolvedQuestion = item
            }
        }

        if (resolvedQuestion != null) {
            savePersonalKnowledge(
                userId = userId,
                question = resolvedQuestion.question,
                answer = answer,
                language = resolvedQuestion.language,
                contextSummary = resolvedQuestion.contextSummary,
                source = source
            )
            true
        } else {
            false
        }
    }

    suspend fun dismissUnknownQuestion(id: Long, userId: String) = withContext(ioDispatcher) {
        val dao = unknownDao
        if (dao != null) {
            dao.updateStatus(id, "dismissed")
        } else {
            val list = inMemoryUnknown[userId]
            val index = list?.indexOfFirst { it.id == id } ?: -1
            if (index >= 0) {
                val item = list!![index]
                list[index] = item.copy(status = "dismissed")
            }
        }
    }

    /**
     * Finds and resolves any pending questions in the user's queue that semantically or literally match
     * a newly answered question, updating their status to resolved and preventing duplicates.
     */
    suspend fun resolveMatchingPendingQuestions(
        question: String,
        userId: String,
        answer: String,
        source: String = "online_ai_learned"
    ): Int = withContext(ioDispatcher) {
        val pendingList = getPendingQuestionsList(userId)
        var resolvedCount = 0
        val queryTokens = extractSemanticKeywords(question)

        for (item in pendingList) {
            val itemTokens = extractSemanticKeywords(item.question)
            val sim = calculateJaccardSimilarity(queryTokens, itemTokens)
            if (sim >= 0.45 || item.question.equals(question, ignoreCase = true)) {
                val success = resolveUnknownQuestion(item.id, userId, answer, source)
                if (success) resolvedCount++
            }
        }
        resolvedCount
    }

    // =============================================================================================
    // 2. PERSONAL LEARNED KNOWLEDGE (USER-SCOPED & SEMANTIC)
    // =============================================================================================

    fun getPersonalKnowledgeFlow(userId: String): Flow<List<PersonalKnowledgeEntity>> {
        val dao = personalDao
        return if (dao != null) {
            dao.getKnowledgeForUser(userId)
        } else {
            flowOf(inMemoryPersonal[userId]?.filter { it.status != "obsolete" } ?: emptyList())
        }
    }

    suspend fun getPersonalKnowledgeList(userId: String): List<PersonalKnowledgeEntity> = withContext(ioDispatcher) {
        val dao = personalDao
        if (dao != null) {
            dao.getKnowledgeListForUser(userId)
        } else {
            inMemoryPersonal[userId]?.filter { it.status != "obsolete" } ?: emptyList()
        }
    }

    /**
     * Stores or updates personal learned knowledge for a specific user.
     * Extracts normalized semantic tokens for future retrieval.
     */
    suspend fun savePersonalKnowledge(
        userId: String,
        question: String,
        answer: String,
        language: String,
        contextSummary: String = "",
        source: String = "online_ai_resolution"
    ): Long = withContext(ioDispatcher) {
        val normalizedKeywords = extractSemanticKeywords(question)
        val keywordsStr = normalizedKeywords.joinToString(" ")
        val dao = personalDao

        if (dao != null) {
            val existingList = dao.getKnowledgeListForUser(userId)
            val existing = existingList.find {
                calculateJaccardSimilarity(normalizedKeywords, it.keywords.split(" ").toSet()) >= 0.75
            }

            if (existing != null) {
                val updated = existing.copy(
                    question = question.trim(),
                    answer = answer.trim(),
                    language = language,
                    contextSummary = contextSummary,
                    source = source,
                    updatedAt = System.currentTimeMillis(),
                    keywords = keywordsStr,
                    status = "resolved"
                )
                dao.updateKnowledge(updated)
                return@withContext existing.id
            }

            val entity = PersonalKnowledgeEntity(
                userId = userId,
                question = question.trim(),
                answer = answer.trim(),
                language = language,
                contextSummary = contextSummary,
                source = source,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                keywords = keywordsStr,
                status = "resolved"
            )
            val newId = dao.insertKnowledge(entity)

            // Storage limit: maximum 100 personal knowledge records per user
            val currentList = dao.getKnowledgeListForUser(userId)
            if (currentList.size > 100) {
                val excess = currentList.drop(100)
                excess.forEach { dao.deleteById(it.id) }
            }
            newId
        } else {
            val list = inMemoryPersonal.getOrPut(userId) { mutableListOf() }
            val existingIndex = list.indexOfFirst {
                calculateJaccardSimilarity(normalizedKeywords, it.keywords.split(" ").toSet()) >= 0.75
            }
            if (existingIndex >= 0) {
                val existing = list[existingIndex]
                val updated = existing.copy(
                    question = question.trim(),
                    answer = answer.trim(),
                    language = language,
                    contextSummary = contextSummary,
                    source = source,
                    updatedAt = System.currentTimeMillis(),
                    keywords = keywordsStr,
                    status = "resolved"
                )
                list[existingIndex] = updated
                existing.id
            } else {
                val newId = System.currentTimeMillis()
                val entity = PersonalKnowledgeEntity(
                    id = newId,
                    userId = userId,
                    question = question.trim(),
                    answer = answer.trim(),
                    language = language,
                    contextSummary = contextSummary,
                    source = source,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    keywords = keywordsStr,
                    status = "resolved"
                )
                list.add(0, entity)
                if (list.size > 100) list.removeLastOrNull()
                newId
            }
        }
    }

    /**
     * Semantically searches personal learned knowledge for the given user.
     * Understands semantic rephrasing, e.g.:
     * "Geçen semestr haýsy derslerden geçip bilmedim?" vs "Öňki semestrde haýsy derslerimden geçip bilmedim?"
     */
    suspend fun findSemanticPersonalKnowledge(
        userId: String,
        query: String
    ): PersonalKnowledgeMatch? = withContext(ioDispatcher) {
        val queryTokens = extractSemanticKeywords(query)
        if (queryTokens.isEmpty()) return@withContext null

        val records = getPersonalKnowledgeList(userId)
        var bestMatch: PersonalKnowledgeEntity? = null
        var highestScore = 0.0
        var matchedTokensList = emptyList<String>()

        for (record in records) {
            if (record.status == "obsolete") continue

            val recordTokens = if (record.keywords.isNotBlank()) {
                record.keywords.split(" ").filter { it.isNotBlank() }.toSet()
            } else {
                extractSemanticKeywords(record.question)
            }

            val overlap = queryTokens.intersect(recordTokens)
            if (overlap.isEmpty()) continue

            val similarity = calculateJaccardSimilarity(queryTokens, recordTokens)

            // Bonus score if specific key intent tags match (e.g. both refer to semester failure or absence)
            var boostedScore = similarity
            if (hasCommonCriticalIntent(queryTokens, recordTokens)) {
                boostedScore += 0.25
            }

            if (boostedScore > highestScore && boostedScore >= 0.40) {
                highestScore = boostedScore
                bestMatch = record
                matchedTokensList = overlap.toList()
            }
        }

        bestMatch?.let {
            PersonalKnowledgeMatch(
                entity = it,
                similarityScore = highestScore,
                matchedTokens = matchedTokensList
            )
        }
    }

    /**
     * Invalidates personal learned knowledge when structured data changes.
     * Marks conflicting records as 'obsolete'.
     */
    suspend fun invalidatePersonalKnowledge(userId: String, categoryKeyword: String) = withContext(ioDispatcher) {
        val dao = personalDao
        if (dao != null) {
            dao.markObsoleteByKeyword(userId, categoryKeyword)
        } else {
            val list = inMemoryPersonal[userId]
            val targetKw = categoryKeyword.lowercase(Locale.ROOT)
            list?.forEachIndexed { index, item ->
                if (item.keywords.lowercase(Locale.ROOT).contains(targetKw) ||
                    item.question.lowercase(Locale.ROOT).contains(targetKw) ||
                    item.contextSummary.lowercase(Locale.ROOT).contains(targetKw)) {
                    list[index] = item.copy(status = "obsolete")
                }
            }
        }
    }

    // =============================================================================================
    // 3. SEMANTIC TOKENIZATION & SIMILARITY UTILITIES
    // =============================================================================================

    private fun extractSemanticKeywords(text: String): Set<String> {
        val normalized = text.lowercase(Locale.ROOT)
        val tokens = normalized.split(Regex("[\\s,?.!;:()\"'\\-\\_/]+"))
            .map { it.trim() }
            .filter { it.length > 1 }

        val stopWords = setOf(
            "meniň", "men", "seniň", "sen", "onun", "onuň", "biziň", "haýsy", "haysy", "näme", "name",
            "barada", "ayt", "aýt", "bar", "bolan", "ýok", "yok", "näçe", "nace", "boldy", "bolupdy",
            "the", "a", "an", "is", "are", "in", "on", "at", "my", "your", "what", "which", "how", "tell", "about",
            "что", "какой", "какие", "был", "было", "были", "мне", "мои", "моя", "моем", "расскажи", "сколько",
            "neler", "hangisi", "benim", "bana", "hakkinda", "hakkında", "nedir", "kaç"
        )

        val result = mutableSetOf<String>()
        for (token in tokens) {
            if (token in stopWords) continue
            val canonical = canonicalizeToken(token)
            result.add(canonical)
        }
        return result
    }

    private fun canonicalizeToken(token: String): String {
        val intentMatch = when {
            // Past semester / previous synonyms
            token in setOf("geçen", "gecen", "öňki", "onki", "öň", "past", "previous", "прошлый", "прошлые", "önceki", "gecmis") ->
                "__intent_past_term__"

            // Failed / retake / Bergä galan synonyms
            token in setOf("geçip", "gecip", "bilmedim", "galypdyryn", "galan", "bergä", "berge", "failed", "retake", "не сдал", "задолженность", "kaldım") ->
                "__intent_failed_course__"

            // Most / maximum synonyms
            token in setOf("köp", "iň", "kop", "most", "maximum", "highest", "больше", "максимум", "en çok") ->
                "__intent_maximum__"

            // Least / minimum synonyms
            token in setOf("az", "pes", "lowest", "minimum", "least", "меньше", "минимум", "en az") ->
                "__intent_minimum__"

            // Absence synonyms
            token in setOf("gelmedik", "sypdyrylan", "propusk", "absence", "absences", "missed", "пропуск", "пропуски", "devamsızlık") ->
                "__intent_absence__"

            // Semester synonyms
            token in setOf("semestr", "semestrde", "semestrimde", "döwür", "term", "semester", "семестр", "семестре", "dönem") ->
                "__intent_semester__"

            // Grade synonyms
            token in setOf("baha", "baham", "bahasy", "bahalar", "bal", "balym", "baly", "netije", "grade", "score", "оценка", "балл", "not") ->
                "__intent_grade__"

            // Teacher synonyms
            token in setOf("mugallym", "mugallymym", "mugallymlar", "teacher", "professor", "преподаватель", "hoca") ->
                "__intent_teacher__"

            // Exam synonyms
            token in setOf("ekzamen", "synag", "exam", "экзамен", "sınav") ->
                "__intent_exam__"

            else -> null
        }
        if (intentMatch != null) return intentMatch

        // Light morphological stemmer for Turkmen/Turkish suffixes to enable flexible semantic matching
        var stem = token
        val suffixes = listOf(
            "synda", "sinde", "syndan", "sinden", "larynda", "lerinde", "lary", "leri", "lar", "ler",
            "da", "de", "dan", "den", "nyň", "niň", "yn", "in", "my", "mi", "ym", "im", "um", "üm", "y", "i"
        )
        for (suffix in suffixes) {
            if (stem.length >= suffix.length + 3 && stem.endsWith(suffix)) {
                stem = stem.substring(0, stem.length - suffix.length)
                break
            }
        }
        return stem
    }

    private fun calculateJaccardSimilarity(set1: Set<String>, set2: Set<String>): Double {
        if (set1.isEmpty() || set2.isEmpty()) return 0.0
        val intersection = set1.intersect(set2).size
        val union = set1.union(set2).size
        return if (union > 0) intersection.toDouble() / union.toDouble() else 0.0
    }

    private fun hasCommonCriticalIntent(set1: Set<String>, set2: Set<String>): Boolean {
        val criticalIntents = setOf(
            "__intent_past_term__",
            "__intent_failed_course__",
            "__intent_absence__",
            "__intent_grade__",
            "__intent_maximum__",
            "__intent_minimum__"
        )
        return criticalIntents.any { it in set1 && it in set2 }
    }
}
