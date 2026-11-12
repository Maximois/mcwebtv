package com.webtv

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val REMOTE_CONFIG_URL = "https://raw.githubusercontent.com/Maximois/mcwebtv/main/webtv-config.json"
private const val PREFS_NAME = "webtv_remote_config"
private const val PREFS_KEY = "config_json"
private const val DEFAULT_ASSET_NAME = "default_config.json"

data class RemoteConfig(
    val channels: List<RemoteChannel> = emptyList(),
    val events: List<RemoteEvent> = emptyList()
)

data class RemoteChannel(
    val title: String,
    val videoUrl: String,
    val cardImageUrl: String? = null,
    val category: String? = null,
    val referer: String? = null,
    val userAgent: String? = null
)

data class RemoteEvent(
    val title: String,
    val description: String? = null,
    val videoUrl: String? = null,
    val cardImageUrl: String? = null,
    val referer: String? = null,
    val userAgent: String? = null
)

object RemoteConfigParser {
    fun parse(json: String?): RemoteConfig {
        if (json.isNullOrBlank()) return RemoteConfig()

        return try {
            val root = JSONObject(json)
            val channels = mutableListOf<RemoteChannel>()
            val events = mutableListOf<RemoteEvent>()

            root.optJSONArray("channels")?.let { array ->
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val title = item.optString("title").takeIf { it.isNotBlank() } ?: continue
                    channels.add(
                        RemoteChannel(
                            title = title,
                            videoUrl = item.optString("videoUrl").takeIf { it.isNotBlank() } ?: "",
                            cardImageUrl = item.optString("cardImageUrl").takeIf { it.isNotBlank() },
                            category = item.optString("category").takeIf { it.isNotBlank() },
                            referer = item.optString("referer").takeIf { it.isNotBlank() },
                            userAgent = item.optString("userAgent").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }

            root.optJSONArray("events")?.let { array ->
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val title = item.optString("title").takeIf { it.isNotBlank() } ?: continue
                    events.add(
                        RemoteEvent(
                            title = title,
                            description = item.optString("description").takeIf { it.isNotBlank() },
                            videoUrl = item.optString("videoUrl").takeIf { it.isNotBlank() },
                            cardImageUrl = item.optString("cardImageUrl").takeIf { it.isNotBlank() },
                            referer = item.optString("referer").takeIf { it.isNotBlank() },
                            userAgent = item.optString("userAgent").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }

            RemoteConfig(channels = channels, events = events)
        } catch (_: Exception) {
            RemoteConfig()
        }
    }
}

object RemoteConfigRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    fun load(context: Context): RemoteConfig {
        val appContext = context.applicationContext
        val cachedConfig = readCachedConfig(appContext)

        val remoteJson = fetchRemoteConfig()
        if (!remoteJson.isNullOrBlank()) {
            saveConfig(appContext, remoteJson)
            return RemoteConfigParser.parse(remoteJson)
        }

        val assetJson = loadAssetConfig(appContext)
        if (!assetJson.isNullOrBlank()) {
            return RemoteConfigParser.parse(assetJson)
        }

        return RemoteConfigParser.parse(cachedConfig)
    }

    private fun fetchRemoteConfig(): String? {
        return try {
            val request = Request.Builder().url(REMOTE_CONFIG_URL).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrBlank()) body else null
        } catch (_: Exception) {
            null
        }
    }

    private fun loadAssetConfig(context: Context): String? {
        return try {
            context.assets.open(DEFAULT_ASSET_NAME).bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            null
        }
    }

    private fun readCachedConfig(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREFS_KEY, null)
    }

    private fun saveConfig(context: Context, json: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREFS_KEY, json).apply()
    }
}
