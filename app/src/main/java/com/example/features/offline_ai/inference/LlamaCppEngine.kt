package com.example.features.offline_ai.inference

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.io.File

/**
 * MediaPipe GenAI adapter for the Gemma 3 1B IT .task model.
 */
class LlamaCppEngine : LocalInferenceEngine {

    private val _state = MutableStateFlow(ModelState.UNINITIALIZED)
    override val state = _state
    private var inference: LlmInference? = null

    override suspend fun initializeFromAssets(context: Context, assetPath: String, config: InferenceConfig): Result<Unit> {
        _state.value = ModelState.LOADING
        val result = runCatching<Unit> {
            val modelFile = File(context.filesDir, "models/gemma3.task")
            if (!modelFile.exists() || modelFile.length() == 0L) {
                modelFile.parentFile?.mkdirs()
                context.assets.open(assetPath).use { input ->
                    modelFile.outputStream().use { output -> input.copyTo(output) }
                }
            }
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(config.maxOutputTokens)
                .build()
            inference?.close()
            inference = LlmInference.createFromOptions(context, options)
        }
        _state.value = if (result.isSuccess) ModelState.READY else ModelState.ERROR
        return result
    }

    override fun generateResponse(prompt: String): Flow<InferenceToken> = callbackFlow {
        check(_state.value == ModelState.READY) { "Local model is not loaded" }
        val model = inference ?: error("Local model is not loaded")
        _state.value = ModelState.GENERATING
        val session = LlmInferenceSession.createFromOptions(
            model,
            LlmInferenceSession.LlmInferenceSessionOptions.builder().build()
        )
        session.addQueryChunk(prompt)
        session.generateResponseAsync { result, done ->
            trySend(InferenceToken(result, done))
            if (done) {
                _state.value = ModelState.READY
                close()
            }
        }
        awaitClose {
            session.close()
            if (_state.value == ModelState.GENERATING) _state.value = ModelState.READY
        }
    }

    override fun stop() {
        if (_state.value == ModelState.GENERATING) _state.value = ModelState.READY
    }

    override fun release() {
        inference?.close()
        inference = null
        _state.value = ModelState.UNINITIALIZED
    }
}
