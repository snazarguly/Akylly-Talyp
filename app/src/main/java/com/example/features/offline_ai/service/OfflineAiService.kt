package com.example.features.offline_ai.service

import android.content.Context
import com.example.features.offline_ai.analyzer.OfflineQuestionAnalyzer
import com.example.features.offline_ai.data.OfflineStudentDatabase
import com.example.features.offline_ai.data.db.OfflineAiRoomDatabase
import com.example.features.offline_ai.data.db.UnknownQuestionEntity
import com.example.features.offline_ai.intent.AiIntent
import com.example.features.offline_ai.inference.InferenceConfig
import com.example.features.offline_ai.inference.LlamaCppEngine
import com.example.features.offline_ai.inference.ModelState
import com.example.features.offline_ai.intent.OfflineIntentDetector
import com.example.features.offline_ai.knowledge.GeneralHnguKnowledge
import com.example.features.offline_ai.memory.ConversationMemoryManager
import com.example.features.offline_ai.model.ModelInferenceState
import com.example.features.offline_ai.model.OfflineChatMessage
import com.example.features.offline_ai.model.OfflineStudentData
import com.example.features.offline_ai.retrieval.LocalRetrievalEngine
import com.example.features.offline_ai.response.ResponseGuard
import com.example.features.offline_ai.security.OfflineSecurityGate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Orchestrates the Offline AI pipeline EXCLUSIVELY using Qwen 3 (gemmaEngine).
 */
class OfflineAiService(
    private val context: Context? = null
) {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val studentDb = OfflineStudentDatabase()
    private val retrievalEngine = LocalRetrievalEngine()
    private val gemmaEngine = LlamaCppEngine()
    private val memoryManager = ConversationMemoryManager()
    private val roomDb = context?.let { OfflineAiRoomDatabase.getDatabase(it) }

    private val _currentUserId = MutableStateFlow("Suleyman_05")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _messages = MutableStateFlow<List<OfflineChatMessage>>(emptyList())
    val messages: StateFlow<List<OfflineChatMessage>> = _messages.asStateFlow()

    private val _inferenceState = MutableStateFlow(ModelInferenceState.READY)
    val inferenceState: StateFlow<ModelInferenceState> = _inferenceState.asStateFlow()

    private var activeJob: Job? = null

    init {
        switchUser("Suleyman_05")
        _inferenceState.value = ModelInferenceState.READY
        context?.let { appContext ->
            serviceScope.launch {
                runCatching {
                    gemmaEngine.initializeFromAssets(
                        appContext,
                        InferenceConfig().modelAssetPath,
                        InferenceConfig()
                    )
                }
                _inferenceState.value = ModelState.READY.let { ModelInferenceState.READY }
            }
        }
    }

    fun switchUser(newUserId: String) {
        activeJob?.cancel()
        _currentUserId.value = newUserId
        val studentData = studentDb.getStudentData(newUserId)

        val history = memoryManager.getHistory(newUserId)
        if (history.isEmpty()) {
            val welcomeMsg = OfflineChatMessage(
                id = "welcome_${System.currentTimeMillis()}",
                text = "Salam, ${studentData?.fullName ?: "Talyp"}! Men HNGU-nyň awtonom emeli aň kömekçisisiňiz. Okuw tertibiňiz, bahalaryňyz we uniwersitet düzgünleri barada sorap bilersiňiz.",
                isUser = false,
                timestamp = getCurrentTime()
            )
            memoryManager.addMessage(newUserId, welcomeMsg)
            _messages.value = listOf(welcomeMsg)
        } else {
            _messages.value = history
        }
        _inferenceState.value = ModelInferenceState.READY
    }

    fun sendMessage(userText: String, languageCode: String = "tk") {
        val uid = _currentUserId.value
        val studentData = studentDb.getStudentData(uid) ?: studentDb.getStudentData("Suleyman_05") ?: return

        val userMessage = OfflineChatMessage(
            id = "user_${System.currentTimeMillis()}",
            text = userText,
            isUser = true,
            timestamp = getCurrentTime()
        )

        memoryManager.addMessage(uid, userMessage)
        _messages.value = memoryManager.getHistory(uid)

        activeJob?.cancel()
        activeJob = serviceScope.launch {
            _inferenceState.value = ModelInferenceState.RETRIEVING_CONTEXT

            // 1. Security Check
            val security = OfflineSecurityGate.checkQuery(userText, uid, studentData, languageCode)
            if (security is OfflineSecurityGate.SecurityCheckResult.Blocked) {
                postSystemMessage(uid, security.message, "Howpsuzlyk: Şahsy maglumatlaryň goragy")
                return@launch
            }

            // 2. Analyze & Detect Intent
            val analyzed = OfflineQuestionAnalyzer.analyze(userText, languageCode)
            val historyContext = memoryManager.buildContextHistory(uid)
            val intent = OfflineIntentDetector.detect(analyzed, historyContext)

            // 3. Knowledge Base Pre-search
            val generalMatch = GeneralHnguKnowledge.findMatch(analyzed.normalized, analyzed.languageCode)

            // 4. Retrieve Structured Data
            val retrieval = retrievalEngine.retrieve(
                analyzed = analyzed,
                intent = intent,
                studentData = studentData,
                personalKnowledgeList = emptyList(),
                generalKnowledgeMatch = generalMatch
            )

            // 5. Save Unknown if detected
            if (intent == AiIntent.UNKNOWN) {
                saveUnknownQuestion(uid, userText, analyzed.languageCode)
            }

            // 6. EXCLUSIVELY use Qwen 3 (gemmaEngine) for response generation
            _inferenceState.value = ModelInferenceState.GENERATING
            val botMsgId = "ai_${System.currentTimeMillis()}"
            var finalBotText = ""

            val responseStream = try {
                gemmaEngine.generateResponse(buildGemmaPrompt(userText, analyzed.languageCode, retrieval))
                    .let { tokens ->
                        kotlinx.coroutines.flow.flow {
                            val buffer = StringBuilder()
                            tokens.collect { token ->
                                buffer.append(token.text)
                                emit(ResponseGuard.process(buffer.toString()))
                            }
                        }
                    }
            } catch (e: Exception) {
                kotlinx.coroutines.flow.flow {
                    emit("Qwen 3 jogap döretmek ýalňyşlygy: ${e.localizedMessage}")
                }
            }

            responseStream.collect { partialText ->
                finalBotText = partialText
                val liveMsg = OfflineChatMessage(
                    id = botMsgId,
                    text = partialText,
                    isUser = false,
                    timestamp = getCurrentTime(),
                    tokensPerSec = 0.0,
                    retrievedContextPreview = "Qwen 3 Lokal AI ulanyldy"
                )
                _messages.value = memoryManager.getHistory(uid).filter { it.id != botMsgId } + liveMsg
            }

            // Persistence
            val finalMsg = OfflineChatMessage(
                id = botMsgId,
                text = finalBotText,
                isUser = false,
                timestamp = getCurrentTime(),
                retrievedContextPreview = "Qwen 3 Lokal AI ulanyldy"
            )
            memoryManager.addMessage(uid, finalMsg)
            _messages.value = memoryManager.getHistory(uid)
            _inferenceState.value = ModelInferenceState.READY
        }
    }

    private fun buildGemmaPrompt(
        userText: String,
        languageCode: String,
        retrieval: LocalRetrievalEngine.RetrievalResult
    ): String {
        val language = when (languageCode) {
            "ru" -> "Russian"
            "en" -> "English"
            else -> "Turkmen"
        }
        return """<start_of_turn>user
You are Akylly Talyp, a helpful offline university assistant at HNGU.
Reply only in $language. Use the provided context to answer the student accurately. If the context does not have the answer, state that the offline database has no exact info.

Context:
${retrieval.contextSummary}

Question: $userText<end_of_turn>
<start_of_turn>model
"""
    }

    private fun postSystemMessage(uid: String, text: String, preview: String) {
        val msg = OfflineChatMessage(
            id = "sys_${System.currentTimeMillis()}",
            text = text,
            isUser = false,
            timestamp = getCurrentTime(),
            retrievedContextPreview = preview
        )
        memoryManager.addMessage(uid, msg)
        _messages.value = memoryManager.getHistory(uid)
        _inferenceState.value = ModelInferenceState.READY
    }

    private suspend fun saveUnknownQuestion(userId: String, question: String, lang: String, status: String = "pending") {
        roomDb?.unknownQuestionDao()?.insertUnknownQuestion(
            UnknownQuestionEntity(
                userId = userId,
                question = question,
                language = lang,
                status = status
            )
        )
    }

    fun stopGeneration() {
        activeJob?.cancel()
        gemmaEngine.stop()
        _inferenceState.value = ModelInferenceState.READY
    }

    fun clearConversation() {
        val uid = _currentUserId.value
        memoryManager.clearHistory(uid)
        switchUser(uid)
    }

    fun getCurrentStudent(): OfflineStudentData? = studentDb.getStudentData(_currentUserId.value)

    fun getAvailableUsers(): List<String> = studentDb.getAllUserIds()

    fun releaseModel() {
        gemmaEngine.release()
    }

    private fun getCurrentTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
