package com.example.core.ai

import com.example.core.model.ChatMessage
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
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
        @Header("x-goog-api-key") apiKey: String,
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

    // Adapters are generated at compile time (moshi-kotlin-codegen), so no reflection is needed.
    private val moshi: Moshi = Moshi.Builder().build()

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
        apiKey: String,
        conversationHistory: List<ChatMessage> = emptyList(),
        systemPrompt: String? = null
    ): String {
        require(apiKey.isNotBlank()) { "API key is missing or not configured." }

        // Gemini expects roles 'user'/'model' and the first turn to be from the user.
        // The history already ends with the current prompt, so drop it and append it explicitly.
        val history = conversationHistory
            .dropLastWhile { it.isUser && it.text.trim() == userPrompt.trim() }
            .takeLast(6)
            .dropWhile { !it.isUser }
        val contents = history.map {
            GeminiContent(role = if (it.isUser) "user" else "model", parts = listOf(GeminiPart(text = it.text)))
        } + GeminiContent(role = "user", parts = listOf(GeminiPart(text = userPrompt)))

        val request = GeminiGenerateContentRequest(
            systemInstruction = systemPrompt?.let { GeminiContent(parts = listOf(GeminiPart(text = it))) },
            contents = contents,
            generationConfig = GeminiGenerationConfig()
        )

        // Try primary model first, fallback to alternate if needed
        val modelsToTry = listOf("gemini-2.5-flash", "gemini-3.5-flash")
        var lastException: Exception? = null

        for (model in modelsToTry) {
            try {
                val response = withContext(Dispatchers.IO) {
                    restService.generateContent(model, apiKey, request)
                }
                val text = response.candidates?.firstOrNull()?.content?.parts
                    ?.mapNotNull { it.text }
                    ?.joinToString("")
                if (!text.isNullOrBlank()) {
                    return text.trim()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                // A bad or unauthorized key fails the same way on every model.
                if (e.code() == 400 || e.code() == 401 || e.code() == 403) throw e
                lastException = e
            } catch (e: Exception) {
                lastException = e
            }
        }

        throw lastException ?: IllegalStateException("Gemini API returned an empty response.")
    }
}
