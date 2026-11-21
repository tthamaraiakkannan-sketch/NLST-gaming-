package com.example.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nlst_user_prefs")

data class GamerProfile(
    val isLoggedIn: Boolean = true,
    val gamerTag: String = "NLST_Striker",
    val displayName: String = "NLST Gamer",
    val freeFireUid: String = "",
    val freeFireRank: String = "Heroic",
    val language: AppLanguage = AppLanguage.ENGLISH,
)

data class YouTubeLiveConfig(
    val rtmpServerUrl: String = "rtmp://a.rtmp.youtube.com/live2",
    val streamKey: String = "",
    val broadcastUrl: String = "",
)

class UserPrefsRepository(private val context: Context) {

    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val GAMER_TAG = stringPreferencesKey("gamer_tag")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val FREE_FIRE_UID = stringPreferencesKey("free_fire_uid")
        val FREE_FIRE_RANK = stringPreferencesKey("free_fire_rank")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val RTMP_SERVER_URL = stringPreferencesKey("rtmp_server_url")
        val STREAM_KEY = stringPreferencesKey("stream_key")
        val BROADCAST_URL = stringPreferencesKey("broadcast_url")
    }

    val profileFlow: Flow<GamerProfile> = context.dataStore.data.map { prefs ->
        GamerProfile(
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: true,
            gamerTag = prefs[Keys.GAMER_TAG] ?: "NLST_Striker",
            displayName = prefs[Keys.DISPLAY_NAME] ?: "NLST Gamer",
            freeFireUid = prefs[Keys.FREE_FIRE_UID] ?: "",
            freeFireRank = prefs[Keys.FREE_FIRE_RANK] ?: "Heroic",
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE_CODE] ?: "en"),
        )
    }

    val youtubeConfigFlow: Flow<YouTubeLiveConfig> = context.dataStore.data.map { prefs ->
        YouTubeLiveConfig(
            rtmpServerUrl = prefs[Keys.RTMP_SERVER_URL] ?: "rtmp://a.rtmp.youtube.com/live2",
            streamKey = prefs[Keys.STREAM_KEY] ?: "",
            broadcastUrl = prefs[Keys.BROADCAST_URL] ?: "",
        )
    }

    suspend fun saveProfile(
        gamerTag: String,
        displayName: String,
        freeFireUid: String,
        freeFireRank: String = "Heroic",
        isLoggedIn: Boolean = true,
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = isLoggedIn
            prefs[Keys.GAMER_TAG] = gamerTag.trim()
            prefs[Keys.DISPLAY_NAME] = displayName.trim()
            prefs[Keys.FREE_FIRE_UID] = freeFireUid.trim()
            prefs[Keys.FREE_FIRE_RANK] = freeFireRank.trim().ifEmpty { "Heroic" }
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE_CODE] = language.code
        }
    }

    suspend fun saveYouTubeConfig(
        rtmpServerUrl: String,
        streamKey: String,
        broadcastUrl: String,
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.RTMP_SERVER_URL] = rtmpServerUrl.trim().ifEmpty { "rtmp://a.rtmp.youtube.com/live2" }
            prefs[Keys.STREAM_KEY] = streamKey.trim()
            prefs[Keys.BROADCAST_URL] = broadcastUrl.trim()
        }
    }

    suspend fun signOut() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = false
        }
    }
}
