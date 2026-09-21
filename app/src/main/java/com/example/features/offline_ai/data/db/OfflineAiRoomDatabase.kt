package com.example.features.offline_ai.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UnknownQuestionEntity::class,
        PersonalKnowledgeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OfflineAiRoomDatabase : RoomDatabase() {

    abstract fun unknownQuestionDao(): UnknownQuestionDao
    abstract fun personalKnowledgeDao(): PersonalKnowledgeDao

    companion object {
        @Volatile
        private var INSTANCE: OfflineAiRoomDatabase? = null

        fun getDatabase(context: Context): OfflineAiRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OfflineAiRoomDatabase::class.java,
                    "hngu_offline_ai.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun createInMemory(context: Context): OfflineAiRoomDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                OfflineAiRoomDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
