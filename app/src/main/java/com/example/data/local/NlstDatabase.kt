package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VoiceRoomEntity::class, ChatMessageEntity::class, FriendEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class NlstDatabase : RoomDatabase() {
    abstract fun nlstDao(): NlstDao

    companion object {
        @Volatile
        private var instance: NlstDatabase? = null

        fun getInstance(context: Context): NlstDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NlstDatabase::class.java,
                    "nlst_gaming_local.db",
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
