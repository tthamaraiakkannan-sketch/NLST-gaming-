package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessageEntity
import com.example.data.local.FriendEntity
import com.example.data.local.NlstDatabase
import com.example.data.local.VoiceRoomEntity
import com.example.data.prefs.GamerProfile
import com.example.data.prefs.UserPrefsRepository
import com.example.data.prefs.YouTubeLiveConfig
import com.example.data.remote.RtmpProbeResult
import com.example.data.remote.YouTubeNetworkHelper
import com.example.data.remote.YouTubeOEmbedResponse
import com.example.i18n.AppLanguage
import com.example.media.FreeFireInstallStatus
import com.example.media.FreeFireLauncher
import com.example.media.GameCaptureTarget
import com.example.media.ScreenCaptureState
import com.example.media.StreamCaptureRegistry
import com.example.media.StreamOrientation
import com.example.media.StreamQuality
import com.example.media.VoiceEffect
import com.example.media.VoiceEffectsEngine
import com.example.media.VoiceEngineState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface YouTubeMetadataUiState {
    data object Idle : YouTubeMetadataUiState
    data object Loading : YouTubeMetadataUiState
    data class Success(val data: YouTubeOEmbedResponse, val sourceUrl: String) : YouTubeMetadataUiState
    data class Error(val message: String) : YouTubeMetadataUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class NlstViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = UserPrefsRepository(application)
    private val dao = NlstDatabase.getInstance(application).nlstDao()
    val voiceEngine = VoiceEffectsEngine(application)

    val profileState: StateFlow<GamerProfile> = prefsRepo.profileFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GamerProfile(),
    )

    val youtubeConfigState: StateFlow<YouTubeLiveConfig> = prefsRepo.youtubeConfigFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = YouTubeLiveConfig(),
    )

    val roomsState: StateFlow<List<VoiceRoomEntity>> = dao.observeAllRooms().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val friendsState: StateFlow<List<FriendEntity>> = dao.observeFriends().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    private val _activeRoomId = MutableStateFlow<Long?>(null)
    val activeRoomId: StateFlow<Long?> = _activeRoomId.asStateFlow()

    val activeRoomState: StateFlow<VoiceRoomEntity?> = _activeRoomId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else dao.observeRoom(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    val activeRoomMessages: StateFlow<List<ChatMessageEntity>> = _activeRoomId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else dao.observeMessages(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val voiceEngineState: StateFlow<VoiceEngineState> = voiceEngine.state
    val captureState: StateFlow<ScreenCaptureState> = StreamCaptureRegistry.state

    private val _rtmpProbeResult = MutableStateFlow<RtmpProbeResult>(RtmpProbeResult.Idle)
    val rtmpProbeResult: StateFlow<RtmpProbeResult> = _rtmpProbeResult.asStateFlow()

    private val _youtubeMetadataState = MutableStateFlow<YouTubeMetadataUiState>(YouTubeMetadataUiState.Idle)
    val youtubeMetadataState: StateFlow<YouTubeMetadataUiState> = _youtubeMetadataState.asStateFlow()

    private val _freeFireStatus = MutableStateFlow(FreeFireLauncher.checkInstalled(application))
    val freeFireStatus: StateFlow<FreeFireInstallStatus> = _freeFireStatus.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun refreshFreeFireStatus() {
        _freeFireStatus.value = FreeFireLauncher.checkInstalled(getApplication())
    }

    fun signInLocally(gamerTag: String, displayName: String, freeFireUid: String) {
        val cleanTag = gamerTag.trim().ifEmpty { "NLST_Striker" }
        val cleanName = displayName.trim().ifEmpty { cleanTag }
        viewModelScope.launch {
            prefsRepo.saveProfile(
                gamerTag = cleanTag,
                displayName = cleanName,
                freeFireUid = freeFireUid.trim(),
                isLoggedIn = true,
            )
        }
    }

    fun updateProfile(gamerTag: String, displayName: String, freeFireUid: String, freeFireRank: String) {
        val cleanTag = gamerTag.trim().ifEmpty { return }
        val cleanName = displayName.trim().ifEmpty { cleanTag }
        viewModelScope.launch {
            prefsRepo.saveProfile(
                gamerTag = cleanTag,
                displayName = cleanName,
                freeFireUid = freeFireUid.trim(),
                freeFireRank = freeFireRank.trim(),
                isLoggedIn = true,
            )
        }
    }

    fun toggleLanguage() {
        val next = if (profileState.value.language == AppLanguage.ENGLISH) {
            AppLanguage.TAMIL
        } else {
            AppLanguage.ENGLISH
        }
        setLanguage(next)
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            prefsRepo.setLanguage(language)
        }
    }

    fun signOut() {
        voiceEngine.stop()
        _activeRoomId.value = null
        viewModelScope.launch {
            prefsRepo.signOut()
        }
    }

    fun addFriend(gamerTag: String, gameTitle: String, freeFireUid: String) {
        val cleanTag = gamerTag.trim()
        if (cleanTag.isEmpty()) return
        viewModelScope.launch {
            dao.insertFriend(
                FriendEntity(
                    gamerTag = cleanTag,
                    gameTitle = gameTitle.ifBlank { "Free Fire" },
                    freeFireUid = freeFireUid.trim(),
                ),
            )
        }
    }

    fun deleteFriend(friendId: Long) {
        viewModelScope.launch {
            dao.deleteFriend(friendId)
        }
    }

    fun inviteFriendToVoiceRoom(friend: FriendEntity) {
        val host = profileState.value.gamerTag.ifEmpty { "NLST_Striker" }
        val code = "NLST-${Random.nextInt(1000, 9999)}"
        viewModelScope.launch {
            val roomId = dao.insertRoom(
                VoiceRoomEntity(
                    title = "${friend.gamerTag} Duo/Squad",
                    gameTitle = friend.gameTitle,
                    roomCode = code,
                    hostName = host,
                    maxSlots = 4,
                    voiceEffect = voiceEngineState.value.activeEffect.id,
                ),
            )
            dao.insertMessage(
                ChatMessageEntity(
                    roomId = roomId,
                    senderName = "SYSTEM",
                    senderRole = "SYSTEM",
                    message = "Voice room $code opened for ${friend.gamerTag}. Share code to connect externally.",
                ),
            )
            _activeRoomId.value = roomId
        }
    }

    fun createRoom(title: String, gameTitle: String, maxSlots: Int) {
        val cleanTitle = title.trim().ifEmpty { "Free Fire Squad" }
        val host = profileState.value.gamerTag.ifEmpty { "NLST_Striker" }
        val code = "NLST-${Random.nextInt(1000, 9999)}"
        viewModelScope.launch {
            val roomId = dao.insertRoom(
                VoiceRoomEntity(
                    title = cleanTitle,
                    gameTitle = gameTitle,
                    roomCode = code,
                    hostName = host,
                    maxSlots = maxSlots.coerceIn(2, 8),
                    voiceEffect = voiceEngineState.value.activeEffect.id,
                ),
            )
            dao.insertMessage(
                ChatMessageEntity(
                    roomId = roomId,
                    senderName = "SYSTEM",
                    senderRole = "SYSTEM",
                    message = "Room $code created by $host ($gameTitle). Local chat & on-device voice FX ready.",
                ),
            )
            _activeRoomId.value = roomId
        }
    }

    fun joinRoomByCode(code: String, onJoined: () -> Unit = {}) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val found = dao.findRoomByCode(trimmed)
            if (found != null) {
                _activeRoomId.value = found.id
                voiceEngine.setEffect(VoiceEffect.fromId(found.voiceEffect))
                dao.insertMessage(
                    ChatMessageEntity(
                        roomId = found.id,
                        senderName = profileState.value.gamerTag.ifEmpty { "Player" },
                        senderRole = "SQUAD",
                        message = "Joined local squad room ${found.roomCode}",
                    ),
                )
                onJoined()
            } else {
                _snackbarMessage.value =
                    "Room '$trimmed' not found locally. Remote room discovery requires a cloud backend."
            }
        }
    }

    fun selectRoom(room: VoiceRoomEntity) {
        _activeRoomId.value = room.id
        voiceEngine.setEffect(VoiceEffect.fromId(room.voiceEffect))
    }

    fun leaveRoom() {
        voiceEngine.stop()
        _activeRoomId.value = null
    }

    fun deleteRoom(roomId: Long) {
        if (_activeRoomId.value == roomId) {
            voiceEngine.stop()
            _activeRoomId.value = null
        }
        viewModelScope.launch {
            dao.deleteRoom(roomId)
        }
    }

    fun sendChatMessage(text: String) {
        val roomId = _activeRoomId.value ?: return
        val clean = text.trim()
        if (clean.isEmpty()) return
        val sender = profileState.value.gamerTag.ifEmpty { "Player" }
        val isHost = activeRoomState.value?.hostName == sender
        viewModelScope.launch {
            dao.insertMessage(
                ChatMessageEntity(
                    roomId = roomId,
                    senderName = sender,
                    senderRole = if (isHost) "HOST" else "SQUAD",
                    message = clean,
                ),
            )
        }
    }

    fun selectVoiceEffect(effect: VoiceEffect) {
        voiceEngine.setEffect(effect)
        val roomId = _activeRoomId.value ?: return
        viewModelScope.launch {
            dao.updateVoiceEffect(roomId, effect.id)
        }
    }

    fun toggleMicMonitor() {
        if (voiceEngineState.value.isRunning) {
            voiceEngine.stop()
        } else {
            voiceEngine.start()
        }
    }

    fun updateStreamConfig(
        targetGame: GameCaptureTarget = captureState.value.targetGame,
        quality: StreamQuality = captureState.value.quality,
        orientation: StreamOrientation = captureState.value.orientation,
    ) {
        StreamCaptureRegistry.updateConfig(targetGame, quality, orientation)
    }

    fun saveYouTubeConfig(rtmpServerUrl: String, streamKey: String, broadcastUrl: String) {
        viewModelScope.launch {
            prefsRepo.saveYouTubeConfig(rtmpServerUrl, streamKey, broadcastUrl)
        }
    }

    fun testRtmpHandshake(rtmpServerUrl: String, streamKey: String) {
        _rtmpProbeResult.value = RtmpProbeResult.Probing
        viewModelScope.launch {
            _rtmpProbeResult.value = YouTubeNetworkHelper.testRtmpHandshake(rtmpServerUrl, streamKey)
        }
    }

    fun fetchYouTubeMetadata(rawUrl: String) {
        val url = rawUrl.trim()
        if (url.isEmpty()) {
            _youtubeMetadataState.value = YouTubeMetadataUiState.Error("Enter a valid YouTube Live or video URL.")
            return
        }
        _youtubeMetadataState.value = YouTubeMetadataUiState.Loading
        viewModelScope.launch {
            try {
                val info = YouTubeNetworkHelper.oEmbedService.getStreamInfo(videoUrl = url)
                _youtubeMetadataState.value = YouTubeMetadataUiState.Success(info, url)
            } catch (e: Exception) {
                _youtubeMetadataState.value = YouTubeMetadataUiState.Error(
                    e.message ?: "Failed to fetch YouTube oEmbed metadata",
                )
            }
        }
    }

    override fun onCleared() {
        voiceEngine.stop()
        super.onCleared()
    }
}
