package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NlstDao {
    @Query("SELECT * FROM voice_rooms ORDER BY createdAt DESC")
    fun observeAllRooms(): Flow<List<VoiceRoomEntity>>

    @Query("SELECT * FROM voice_rooms WHERE id = :roomId LIMIT 1")
    fun observeRoom(roomId: Long): Flow<VoiceRoomEntity?>

    @Query("SELECT * FROM voice_rooms WHERE UPPER(roomCode) = UPPER(:code) LIMIT 1")
    suspend fun findRoomByCode(code: String): VoiceRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: VoiceRoomEntity): Long

    @Query("UPDATE voice_rooms SET voiceEffect = :effect WHERE id = :roomId")
    suspend fun updateVoiceEffect(roomId: Long, effect: String)

    @Query("DELETE FROM voice_rooms WHERE id = :roomId")
    suspend fun deleteRoom(roomId: Long)

    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun observeMessages(roomId: Long): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("SELECT * FROM friends ORDER BY addedAt DESC")
    fun observeFriends(): Flow<List<FriendEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity): Long

    @Query("DELETE FROM friends WHERE id = :friendId")
    suspend fun deleteFriend(friendId: Long)
}
