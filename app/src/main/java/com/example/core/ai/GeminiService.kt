package com.example.core.ai

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = 0.7f,
    @Json(name = "topP") val topP: Float? = 0.95f,
    @Json(name = "topK") val topK: Int? = 40
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

interface GeminiRestService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateContentRequest
    ): GeminiGenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiRestService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiRestService::class.java)
    }
}

/**
 * Dedicated, independent Online AI service communicating strictly with Google's Gemini API.
 * Responsible ONLY for:
 * - Building and dispatching Gemini API requests
 * - Parsing Gemini API responses
 * - Propagating network/API errors clearly without silent fallbacks
 */
class GeminiService(
    private val restService: GeminiRestService = GeminiClient.service
) {
    /**
     * Sends the user's prompt (and prior multi-turn context) directly to Gemini API.
     * Throws an Exception if the network request fails, key is invalid, or quota is exceeded.
     */
    suspend fun generateResponse(
        userPrompt: String,
        conversationHistory: List<com.example.core.model.ChatMessage> = emptyList(),
        systemPrompt: String? = null,
        apiKey: String? = null
    ): String {
        val effectiveApiKey = apiKey?.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
            ?: com.example.BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }

        if (effectiveApiKey.isNullOrBlank()) {
            throw IllegalStateException("API key is missing or not configured. Please set GEMINI_API_KEY in Secrets or app settings.")
        }

        // Build valid contents array:
        // Gemini API requires that:
        // 1. Roles are 'user' and 'model'
        // 2. The first content element has role 'user'
        val contents = mutableListOf<GeminiContent>()

        // Find recent history (up to last 6 messages)
        val recentHistory = conversationHistory.takeLast(6)
        val firstUserIdx = recentHistory.indexOfFirst { it.isUser }
        if (firstUserIdx != -1) {
            val candidateHistory = recentHistory.subList(firstUserIdx, recentHistory.size)
            for (msg in candidateHistory) {
                // If message matches userPrompt at the tail, skip to avoid duplicate
                if (msg.text.trim() == userPrompt.trim() && msg == candidateHistory.last()) continue
                contents.add(
                    GeminiContent(
                        role = if (msg.isUser) "user" else "model",
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            }
        }

        // Ensure user's current question is appended as the active 'user' turn
        contents.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = userPrompt))
            )
        )

        android.util.Log.d("GeminiService", "Dispatching generateContent for user prompt: '$userPrompt'")

        val request = GeminiGenerateContentRequest(
            systemInstruction = systemPrompt?.let {
                GeminiContent(parts = listOf(GeminiPart(text = it)))
            },
            contents = contents,
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                topK = 40
            )
        )

        // Try primary model first, fallback to alternate if needed
        val modelsToTry = listOf("gemini-2.5-flash", "gemini-3.5-flash")
        var lastException: Exception? = null

        for (model in modelsToTry) {
            try {
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    restService.generateContent(model, effectiveApiKey, request)
                }

                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    return text.trim()
                }
            } catch (e: Exception) {
                lastException = e
                android.util.Log.w("GeminiService", "Model $model request failed: ${e.message}")
            }
        }

        throw lastException ?: IllegalStateException("Gemini API returned an empty response.")
    }
}
