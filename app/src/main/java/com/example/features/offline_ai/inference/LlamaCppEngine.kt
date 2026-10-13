package com.example.features.offline_ai.inference

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

/**
 * MediaPipe GenAI adapter for the Qwen 3 .task model.
 */
class LlamaCppEngine : LocalInferenceEngine {

    private val _state = MutableStateFlow(ModelState.UNINITIALIZED)
    override val state = _state
    private var inference: LlmInference? = null

    override suspend fun initializeFromAssets(context: Context, assetPath: String, config: InferenceConfig): Result<Unit> {
        _state.value = ModelState.LOADING
        val result = runCatching<Unit> {
            val modelFile = File(context.filesDir, "models/qwen3.task")
            if (!modelFile.exists() || modelFile.length() == 0L) {
                modelFile.parentFile?.mkdirs()
                try {
                    context.assets.open(assetPath).use { input ->
                        modelFile.outputStream().use { output ->
                            val buffer = ByteArray(65536)
                            var read: Int
                            while (input.read(buffer).also { read = it } >= 0) {
                                output.write(buffer, 0, read)
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (!modelFile.exists() || modelFile.length() == 0L) {
                        error("Qwen 3 model file (qwen3.task) not found: ${e.localizedMessage}")
                    }
                }
            }
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(config.maxOutputTokens)
                .setPreferredBackend(LlmInference.Backend.CPU)
                .build()
            inference?.close()
            inference = LlmInference.createFromOptions(context, options)
        }
        _state.value = if (result.isSuccess) ModelState.READY else ModelState.ERROR
        return result
    }

    override fun generateResponse(prompt: String): Flow<InferenceToken> = callbackFlow {
        if (_state.value != ModelState.READY) {
            trySend(InferenceToken("Qwen 3 model is not ready (state: ${_state.value})", true))
            close()
            return@callbackFlow
        }
        val model = inference
        if (model == null) {
            trySend(InferenceToken("Qwen 3 model instance is null", true))
            close()
            return@callbackFlow
        }
        _state.value = ModelState.GENERATING
        try {
            val session = LlmInferenceSession.createFromOptions(
                model,
                LlmInferenceSession.LlmInferenceSessionOptions.builder()
                    .setTemperature(0.7f)
                    .setTopK(40)
                    .setTopP(0.95f)
                    .build()
            )
            session.addQueryChunk(prompt)
            session.generateResponseAsync { result, done ->
                trySend(InferenceToken(result, done))
                if (done) {
                    _state.value = ModelState.READY
                    close()
                }
            }
        } catch (e: Exception) {
            trySend(InferenceToken("Generation error: ${e.localizedMessage}", true))
            _state.value = ModelState.READY
            close()
        }
        awaitClose {
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
