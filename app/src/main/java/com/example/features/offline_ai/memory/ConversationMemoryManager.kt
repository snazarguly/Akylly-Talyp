package com.example.features.offline_ai.memory

import com.example.features.offline_ai.model.OfflineChatMessage

/**
 * Isolated conversation memory per user session.
 * Ensures User A's dialogue history is never leaked or retained when switching to User B.
 */
class ConversationMemoryManager {

    private val userHistories = mutableMapOf<String, MutableList<OfflineChatMessage>>()

    fun getHistory(userId: String): List<OfflineChatMessage> {
        return userHistories[userId]?.toList() ?: emptyList()
    }

    fun addMessage(userId: String, message: OfflineChatMessage) {
        val list = userHistories.getOrPut(userId) { mutableListOf() }
        list.add(message)
        // Keep last 16 turns to manage context window efficiency
        if (list.size > 20) {
            list.removeAt(0)
        }
    }

    fun clearHistory(userId: String) {
        userHistories.remove(userId)
    }

    fun clearAll() {
        userHistories.clear()
    }

    fun buildContextHistory(userId: String, maxTurns: Int = 6): String {
        val list = userHistories[userId] ?: return ""
        val recent = list.takeLast(maxTurns)
        if (recent.isEmpty()) return ""

        val sb = StringBuilder()
        sb.append("ÖŇKI GEPLEŞIK TARYHY:\n")
        recent.forEach { msg ->
            val sender = if (msg.isUser) "Talyp" else "Kömekçi (AI)"
            sb.append("$sender: ${msg.text}\n")
        }
        sb.append("\n")
        return sb.toString()
    }
}
