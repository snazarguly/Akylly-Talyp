package com.example

import android.app.Application
import com.example.core.data.DemoDataRepository
import com.example.features.offline_ai.service.OfflineAiService

/**
 * Holds app-wide singletons so they survive Activity recreation (rotation, theme or language change)
 * instead of being rebuilt — and losing chat history — every time the Activity is recreated.
 */
class AkyllyTalypApp : Application() {
    val repository by lazy { DemoDataRepository(this) }
    val offlineAiService by lazy { OfflineAiService(this) }
}
