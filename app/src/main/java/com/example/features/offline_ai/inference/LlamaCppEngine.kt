package com.example.features.offline_ai.inference

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of LocalInferenceEngine that loads models directly from APK assets.
 * Requires the .gguf files to be set as 'noCompress' in build.gradle.kts.
 */
class LlamaCppEngine : LocalInferenceEngine {

    private val _state = MutableStateFlow(ModelState.UNINITIALIZED)
    override val state = _state

    override suspend fun initializeFromAssets(context: Context, assetPath: String, config: InferenceConfig): Result<Unit> {
        _state.value = ModelState.LOADING
        
        return try {
            val assetManager = context.assets
            val fd = assetManager.openFd(assetPath)
            
            // In a real implementation, we pass the file descriptor, offset, and length to native code.
            // val nativePointer = nativeInitFromFd(fd.fileDescriptor, fd.startOffset, fd.length)
            
            delay(300) // Simulate quick native initialization
            _state.value = ModelState.READY
            Result.success(Unit)
        } catch (e: Exception) {
            _state.value = ModelState.ERROR
            Result.failure(e)
        }
    }

    override fun generateResponse(prompt: String): Flow<InferenceToken> = flow {
        _state.value = ModelState.GENERATING
        
        // Mock generation for logic verification
        val response = "Bu jogap göni APK-nyň içindäki modelden gelýär. GGUF faýly gysylman ýerleşdirildi."
        val tokens = response.split(" ")
        
        for (token in tokens) {
            delay(40)
            emit(InferenceToken("$token ", tokensPerSecond = 22.5))
        }
        
        emit(InferenceToken("", isLast = true))
        _state.value = ModelState.READY
    }

    override fun stop() {
        _state.value = ModelState.READY
    }

    override fun release() {
        _state.value = ModelState.UNINITIALIZED
    }
}
