package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        OrganizationEntity::class,
        ContactEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        LeadEntity::class,
        ProductEntity::class,
        ProductCategoryEntity::class,
        OrderEntity::class,
        AppointmentEntity::class,
        AutomationWorkflowEntity::class,
        KnowledgeDocumentEntity::class,
        AiSettingsEntity::class,
        TeamMemberEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "whatsapp_sales_ai_saas.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
