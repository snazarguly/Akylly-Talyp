package com.example.features.offline_ai.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing an unknown question encountered by the offline AI.
 *
 * CRITICAL REQUIREMENTS:
 * 1. Strictly user-scoped (belongs ONLY to `userId`).
 * 2. Does NOT store entire long conversations unnecessarily. Stores only the
 *    question, language, timestamp, and compact relevant academic context (e.g. currentSemester).
 * 3. Status tracks the queue lifecycle: "pending" -> "processing" -> "resolved" -> "dismissed".
 */
@Entity(
    tableName = "unknown_questions",
    indices = [
        Index(value = ["userId", "status"]),
        Index(value = ["userId", "timestamp"])
    ]
)
data class UnknownQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val question: String,
    val language: String = "tk",
    val timestamp: Long = System.currentTimeMillis(),
    val contextSummary: String = "",
    val status: String = "pending" // "pending", "processing", "resolved", "dismissed"
)

/**
 * Entity representing personal learned knowledge for a specific user.
 *
 * CRITICAL REQUIREMENTS:
 * 1. PRIVATE + USER-SCOPED: User A's learned answers are NEVER accessible to User B.
 * 2. Semantic reuse: Contains normalized semantic keywords and question text to allow
 *    answering rephrased questions with the same meaning without exact string matches.
 * 3. Compact storage: Deduplicated and updated rather than storing unlimited copies.
 * 4. Invalidation: Can be marked "obsolete" if student's authoritative academic data updates.
 */
@Entity(
    tableName = "personal_knowledge",
    indices = [
        Index(value = ["userId", "status"]),
        Index(value = ["userId", "keywords"])
    ]
)
data class PersonalKnowledgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val question: String,
    val answer: String,
    val language: String = "tk",
    val contextSummary: String = "",
    val source: String = "online_ai_resolution", // "online_ai_resolution", "academic_record", "offline_verified"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val keywords: String = "", // Space-separated normalized semantic tokens
    val status: String = "resolved" // "resolved", "verified", "obsolete"
)
