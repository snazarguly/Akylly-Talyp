package com.example.features.offline_ai.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UnknownQuestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnknownQuestion(entity: UnknownQuestionEntity): Long

    @Query("SELECT * FROM unknown_questions WHERE userId = :userId AND status = 'pending' ORDER BY timestamp DESC")
    fun getPendingQuestionsForUser(userId: String): Flow<List<UnknownQuestionEntity>>

    @Query("SELECT * FROM unknown_questions WHERE userId = :userId AND status = 'pending' ORDER BY timestamp DESC")
    suspend fun getPendingQuestionsList(userId: String): List<UnknownQuestionEntity>

    @Query("SELECT * FROM unknown_questions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllQuestionsForUser(userId: String): Flow<List<UnknownQuestionEntity>>

    @Query("SELECT * FROM unknown_questions WHERE userId = :userId AND question = :question AND status = 'pending' LIMIT 1")
    suspend fun findExactPendingQuestion(userId: String, question: String): UnknownQuestionEntity?

    @Query("UPDATE unknown_questions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM unknown_questions WHERE id = :id")
    suspend fun deleteQuestion(id: Long)

    @Query("DELETE FROM unknown_questions WHERE userId = :userId")
    suspend fun clearAllForUser(userId: String)

    @Query("SELECT COUNT(*) FROM unknown_questions WHERE userId = :userId AND status = 'pending'")
    suspend fun getPendingCountForUser(userId: String): Int
}

@Dao
interface PersonalKnowledgeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledge(entity: PersonalKnowledgeEntity): Long

    @Update
    suspend fun updateKnowledge(entity: PersonalKnowledgeEntity)

    @Query("SELECT * FROM personal_knowledge WHERE userId = :userId AND status != 'obsolete' ORDER BY updatedAt DESC")
    fun getKnowledgeForUser(userId: String): Flow<List<PersonalKnowledgeEntity>>

    @Query("SELECT * FROM personal_knowledge WHERE userId = :userId AND status != 'obsolete' ORDER BY updatedAt DESC")
    suspend fun getKnowledgeListForUser(userId: String): List<PersonalKnowledgeEntity>

    @Query("SELECT * FROM personal_knowledge WHERE userId = :userId AND id = :id LIMIT 1")
    suspend fun getKnowledgeById(userId: String, id: Long): PersonalKnowledgeEntity?

    @Query("UPDATE personal_knowledge SET status = 'obsolete' WHERE userId = :userId AND (keywords LIKE '%' || :keyword || '%' OR question LIKE '%' || :keyword || '%')")
    suspend fun markObsoleteByKeyword(userId: String, keyword: String)

    @Query("DELETE FROM personal_knowledge WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM personal_knowledge WHERE userId = :userId")
    suspend fun clearAllForUser(userId: String)

    @Query("SELECT COUNT(*) FROM personal_knowledge WHERE userId = :userId")
    suspend fun getKnowledgeCountForUser(userId: String): Int
}
