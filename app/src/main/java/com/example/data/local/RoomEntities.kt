package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "voice_rooms")
data class VoiceRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val gameTitle: String,
    val roomCode: String,
    val hostName: String,
    val maxSlots: Int = 4,
    val voiceEffect: String = "NORMAL",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = VoiceRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("roomId")],
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val senderName: String,
    val senderRole: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gamerTag: String,
    val gameTitle: String = "Free Fire",
    val freeFireUid: String = "",
    val addedAt: Long = System.currentTimeMillis(),
)
