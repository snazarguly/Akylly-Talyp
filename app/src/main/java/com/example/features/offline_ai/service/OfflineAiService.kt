package com.example.features.offline_ai.service

import android.content.Context
import com.example.features.offline_ai.inference.InferenceConfig
import com.example.features.offline_ai.inference.LocalInferenceEngine
import com.example.features.offline_ai.inference.LlamaCppEngine
import com.example.features.offline_ai.prompt.PromptManager
import com.example.features.offline_ai.rag.ContextBuilder
import com.example.features.offline_ai.rag.EmbeddingEngine
import com.example.features.offline_ai.rag.SimpleEmbeddingEngine
import com.example.features.offline_ai.rag.VectorStore
import com.example.features.offline_ai.rag.SimpleVectorStore
import com.example.features.offline_ai.response.ResponseGuard
import com.example.features.offline_ai.security.StudentPrivacyGuard
import com.example.features.offline_ai.student.StudentDataProvider
import com.example.features.offline_ai.data.OfflineStudentDatabase
import com.example.features.offline_ai.knowledge.GeneralHnguKnowledge
import com.example.features.offline_ai.model.ModelInferenceState
import com.example.features.offline_ai.model.OfflineChatMessage
import com.example.features.offline_ai.model.OfflineStudentData
import com.example.features.offline_ai.memory.ConversationMemoryManager
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
 * Orchestrates the 100% Local LLM + RAG Pipeline.
 * Loads the model directly from APK assets for maximum efficiency.
 */
class OfflineAiService(
    private val context: Context? = null
) {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Data providers
    private val studentDb = OfflineStudentDatabase()
    private val studentProvider = StudentDataProvider()
    private val memoryManager = ConversationMemoryManager()

    // Architecture components
    private val inferenceEngine: LocalInferenceEngine = LlamaCppEngine()
    private val embeddingEngine: EmbeddingEngine = SimpleEmbeddingEngine()
    private val vectorStore: VectorStore = SimpleVectorStore()

    private val _currentUserId = MutableStateFlow("Suleyman_05")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _messages = MutableStateFlow<List<OfflineChatMessage>>(emptyList())
    val messages: StateFlow<List<OfflineChatMessage>> = _messages.asStateFlow()

    private val _inferenceState = MutableStateFlow(ModelInferenceState.READY)
    val inferenceState: StateFlow<ModelInferenceState> = _inferenceState.asStateFlow()

    private var activeJob: Job? = null

    init {
        initializePipeline()
    }

    private fun initializePipeline() {
        serviceScope.launch {
            // 1. Vector Store Init
            initializeKnowledgeBase()

            // 2. Direct Asset Initialization (Zero-Copy)
            context?.let { ctx ->
                _inferenceState.value = ModelInferenceState.LOADING_WEIGHTS
                inferenceEngine.initializeFromAssets(
                    context = ctx,
                    assetPath = "models/Qwen3-4B-Instruct-Q4_K_M.gguf",
                    config = InferenceConfig()
                )
            }

            _inferenceState.value = ModelInferenceState.READY
            switchUser(_currentUserId.value)
        }
    }

    private fun initializeKnowledgeBase() {
        serviceScope.launch {
            vectorStore.clear()
            GeneralHnguKnowledge.TOPICS.forEach { topic ->
                val chunk = VectorStore.Chunk(
                    id = topic.id,
                    text = "${topic.title}: ${topic.contentTk}",
                    embedding = embeddingEngine.getEmbedding(topic.contentTk)
                )
                vectorStore.addChunk(chunk)
            }
        }
    }

    fun switchUser(newUserId: String) {
        activeJob?.cancel()
        _currentUserId.value = newUserId
        val history = memoryManager.getHistory(newUserId)
        _messages.value = if (history.isEmpty()) {
            val studentData = studentDb.getStudentData(newUserId)
            val welcome = OfflineChatMessage(
                id = "welcome_${System.currentTimeMillis()}",
                text = "Salam, ${studentData?.fullName ?: "Talyp"}! Men Akylly Talyp Offline AI. 100% lokal model arkaly işleýärin.",
                isUser = false,
                timestamp = getCurrentTime()
            )
            memoryManager.addMessage(newUserId, welcome)
            listOf(welcome)
        } else history
    }

    fun sendMessage(userText: String, languageCode: String = "tk") {
        val uid = _currentUserId.value
        val studentData = studentDb.getStudentData(uid) ?: return

        val userMsg = OfflineChatMessage("u_${System.currentTimeMillis()}", userText, true, getCurrentTime())
        memoryManager.addMessage(uid, userMsg)
        _messages.value = memoryManager.getHistory(uid)

        activeJob?.cancel()
        activeJob = serviceScope.launch {
            _inferenceState.value = ModelInferenceState.RETRIEVING_CONTEXT

            val guardResult = StudentPrivacyGuard.validateQuery(userText, uid)
            if (guardResult is StudentPrivacyGuard.GuardResult.Blocked) {
                postAiMessage(uid, guardResult.localizedMessage, "Security Guard")
                return@launch
            }

            val queryEmbedding = embeddingEngine.getEmbedding(userText)
            val relevantChunks = vectorStore.search(queryEmbedding)

            val studentContext = studentProvider.getStudentContext(studentData)
            val systemPrompt = PromptManager.getSystemPrompt(languageCode)
            val history = memoryManager.buildContextHistory(uid)

            val fullPrompt = ContextBuilder.build(
                systemPrompt = systemPrompt,
                retrievedChunks = relevantChunks,
                studentContext = studentContext,
                conversationHistory = history,
                userQuery = userText
            )

            _inferenceState.value = ModelInferenceState.GENERATING
            val botMsgId = "ai_${System.currentTimeMillis()}"
            var generatedAccumulator = ""

            inferenceEngine.generateResponse(fullPrompt).collect { token ->
                generatedAccumulator += token.text
                val liveMsg = OfflineChatMessage(
                    id = botMsgId,
                    text = generatedAccumulator,
                    isUser = false,
                    timestamp = getCurrentTime(),
                    tokensPerSec = token.tokensPerSecond,
                    retrievedContextPreview = "Direct Asset + Qwen3"
                )
                _messages.value = memoryManager.getHistory(uid).filter { it.id != botMsgId } + liveMsg
            }

            val finalMsgText = ResponseGuard.process(generatedAccumulator)
            val finalMsg = OfflineChatMessage(botMsgId, finalMsgText, false, getCurrentTime(), retrievedContextPreview = "Direct Asset + Qwen3")
            memoryManager.addMessage(uid, finalMsg)
            _messages.value = memoryManager.getHistory(uid)
            _inferenceState.value = ModelInferenceState.READY
        }
    }

    private fun postAiMessage(uid: String, text: String, preview: String) {
        val msg = OfflineChatMessage("ai_${System.currentTimeMillis()}", text, false, getCurrentTime(), retrievedContextPreview = preview)
        memoryManager.addMessage(uid, msg)
        _messages.value = memoryManager.getHistory(uid)
        _inferenceState.value = ModelInferenceState.READY
    }

    fun clearConversation() {
        val uid = _currentUserId.value
        memoryManager.clearHistory(uid)
        switchUser(uid)
    }

    fun stopGeneration() {
        activeJob?.cancel()
        inferenceEngine.stop()
        _inferenceState.value = ModelInferenceState.READY
    }

    fun getCurrentStudent(): OfflineStudentData? = studentDb.getStudentData(_currentUserId.value)
    fun getAvailableUsers(): List<String> = studentDb.getAllUserIds()
    private fun getCurrentTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
