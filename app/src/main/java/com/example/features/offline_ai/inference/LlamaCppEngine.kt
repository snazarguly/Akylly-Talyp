package com.example.features.offline_ai.inference

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Placeholder for a llama.cpp backend that loads a GGUF model directly from APK assets
 * ('gguf' is marked noCompress in build.gradle.kts).
 *
 * The native bindings are not bundled yet, so initialization always fails and
 * OfflineAiService answers with OfflineAnswerEngine instead of pretending to run a model.
 */
class LlamaCppEngine : LocalInferenceEngine {

    private val _state = MutableStateFlow(ModelState.UNINITIALIZED)
    override val state = _state

    override suspend fun initializeFromAssets(context: Context, assetPath: String, config: InferenceConfig): Result<Unit> {
        _state.value = ModelState.LOADING
        val result = runCatching<Unit> {
            // Throws when the model file is not packaged; the descriptor is only probed, never leaked.
            context.assets.openFd(assetPath).close()
            // Real implementation: pass fd, startOffset and length to native code here.
            throw UnsupportedOperationException("Native llama.cpp bindings are not bundled in this build")
        }
        _state.value = if (result.isSuccess) ModelState.READY else ModelState.ERROR
        return result
    }

    override fun generateResponse(prompt: String): Flow<InferenceToken> = flow {
        check(_state.value == ModelState.READY) { "Local model is not loaded" }
    }

    override fun stop() {
        if (_state.value == ModelState.GENERATING) _state.value = ModelState.READY
    }

    override fun release() {
        _state.value = ModelState.UNINITIALIZED
    }
}
