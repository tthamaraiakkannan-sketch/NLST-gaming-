package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@JsonClass(generateAdapter = true)
data class YouTubeOEmbedResponse(
    val title: String = "",
    @Json(name = "author_name") val authorName: String = "",
    @Json(name = "author_url") val authorUrl: String = "",
    @Json(name = "thumbnail_url") val thumbnailUrl: String = "",
)

interface YouTubeOEmbedService {
    @GET("oembed")
    suspend fun getStreamInfo(
        @Query("url") videoUrl: String,
        @Query("format") format: String = "json",
    ): YouTubeOEmbedResponse
}

sealed interface RtmpProbeResult {
    data object Idle : RtmpProbeResult
    data object Probing : RtmpProbeResult
    data class Success(val host: String, val port: Int, val latencyMs: Long, val hasStreamKey: Boolean) : RtmpProbeResult
    data class Failure(val reason: String) : RtmpProbeResult
}

object YouTubeNetworkHelper {
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val oEmbedService: YouTubeOEmbedService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.youtube.com/")
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(YouTubeOEmbedService::class.java)
    }

    /**
     * Performs a real TCP socket connection + RTMP C0/C1 handshake exchange with the configured
     * RTMP ingest endpoint (e.g. a.rtmp.youtube.com:1935) to verify network reachability.
     */
    suspend fun testRtmpHandshake(rtmpUrl: String, streamKey: String): RtmpProbeResult =
        withContext(Dispatchers.IO) {
            val trimmedUrl = rtmpUrl.trim()
            if (trimmedUrl.isBlank()) {
                return@withContext RtmpProbeResult.Failure("RTMP server URL is empty.")
            }
            val parsed = try {
                URI(trimmedUrl)
            } catch (e: Exception) {
                return@withContext RtmpProbeResult.Failure("Invalid RTMP URI: ${e.message}")
            }
            val host = parsed.host
            if (host.isNullOrBlank()) {
                return@withContext RtmpProbeResult.Failure("Missing host in RTMP URL.")
            }
            val port = if (parsed.port > 0) parsed.port else 1935
            val startTime = System.currentTimeMillis()
            val socket = Socket()
            try {
                socket.connect(InetSocketAddress(host, port), 5000)
                socket.soTimeout = 5000
                val out: OutputStream = socket.getOutputStream()
                val input: InputStream = socket.getInputStream()

                // RTMP C0 (version 0x03) + C1 (1536 bytes)
                val c0c1 = ByteArray(1537)
                c0c1[0] = 0x03
                Random.nextBytes(c0c1, 9, c0c1.size)
                out.write(c0c1)
                out.flush()

                // Expect S0 (0x03) from RTMP server
                val s0 = input.read()
                val elapsed = System.currentTimeMillis() - startTime
                if (s0 == 0x03) {
                    RtmpProbeResult.Success(
                        host = host,
                        port = port,
                        latencyMs = elapsed,
                        hasStreamKey = streamKey.isNotBlank(),
                    )
                } else {
                    RtmpProbeResult.Failure("Unexpected RTMP S0 byte: $s0")
                }
            } catch (e: Exception) {
                RtmpProbeResult.Failure(e.message ?: "TCP connection to $host:$port failed")
            } finally {
                runCatching { socket.close() }
            }
        }
}
