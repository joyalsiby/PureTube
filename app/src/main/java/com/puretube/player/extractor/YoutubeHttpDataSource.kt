package com.puretube.player.extractor

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.SequenceInputStream
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

/**
 * Preloads the initial byte range (MP4 initialization box + sidx segment index + first few seconds of media)
 * of video and audio streams in parallel as soon as extraction finishes.
 *
 * Because DashMediaSource requests:
 *  1) Initialization range (0..initEnd, ~700 bytes)
 *  2) SegmentBase indexRange (indexStart..indexEnd, ~1500 bytes)
 *  3) First media segment (~2200..400000 bytes)
 * all three requests fall inside bytes [0..PREFETCH_BYTES] and are served in 0ms from RAM!
 */
object YoutubeStreamPreloader {
    const val VIDEO_PREFETCH_BYTES = 640 * 1024 // 640 KB covers init + sidx + first ~2.5-4s of 1080p video
    const val AUDIO_PREFETCH_BYTES = 192 * 1024 // 192 KB covers init + sidx + first ~10s of M4A audio

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Keep last 6 stream prefix buffers in memory
    private val completedCache = Collections.synchronizedMap(
        object : LinkedHashMap<String, ByteArray>(8, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ByteArray>?): Boolean {
                return size > 6
            }
        }
    )

    private val inFlightTasks = ConcurrentHashMap<String, Deferred<ByteArray?>>()

    fun preloadStreams(videoUrl: String?, audioUrl: String?) {
        if (!videoUrl.isNullOrBlank()) {
            preloadPrefixAsync(videoUrl, VIDEO_PREFETCH_BYTES)
        }
        if (!audioUrl.isNullOrBlank()) {
            preloadPrefixAsync(audioUrl, AUDIO_PREFETCH_BYTES)
        }
    }

    fun preloadPrefixAsync(url: String, byteCount: Int) {
        val cacheKey = normalizeStreamKey(url)
        if (completedCache.containsKey(cacheKey) || inFlightTasks.containsKey(cacheKey)) {
            return
        }

        val deferred = scope.async {
            try {
                fetchPrefixBytes(url, byteCount)?.also { bytes ->
                    if (bytes.isNotEmpty()) {
                        completedCache[cacheKey] = bytes
                    }
                }
            } catch (_: Throwable) {
                null
            } finally {
                inFlightTasks.remove(cacheKey)
            }
        }
        inFlightTasks[cacheKey] = deferred
        scope.launch {
            deferred.await()
        }
    }

    /**
     * Returns cached prefix bytes starting at byte 0 for [url], waiting briefly if a prefetch
     * request is already in flight on the network.
     */
    fun getCachedPrefix(url: String): ByteArray? {
        val cacheKey = normalizeStreamKey(url)
        completedCache[cacheKey]?.let { return it }

        val inFlight = inFlightTasks[cacheKey] ?: return null
        if (inFlight.isCompleted) {
            return runCatching { inFlight.getCompleted() }.getOrNull()
        }
        return runCatching {
            runBlocking {
                withTimeoutOrNull(1_500L) {
                    inFlight.await()
                }
            }
        }.getOrNull()
    }

    private fun fetchPrefixBytes(url: String, byteCount: Int): ByteArray? {
        val uri = Uri.parse(url)
        val totalLen = YoutubeHttpDataSource.extractClen(uri)
        val endByte = if (totalLen > 0L) {
            min(byteCount.toLong() - 1L, totalLen - 1L)
        } else {
            byteCount.toLong() - 1L
        }
        if (endByte <= 0L) return null

        val rangedUrl = YoutubeHttpDataSource.buildRangedGoogleVideoUrl(uri, 0L, endByte)
        val userAgent = YoutubeHttpDataSource.resolveUserAgentForUrl(uri)

        val requestBuilder = Request.Builder()
            .url(rangedUrl)
            .header("User-Agent", userAgent)
            .header("Accept", "*/*")
            .header("Origin", "https://www.youtube.com")
            .header("Referer", "https://www.youtube.com/")

        if (!YoutubeHttpDataSource.isGoogleVideoUrl(uri)) {
            requestBuilder.header("Range", "bytes=0-$endByte")
        }

        OkHttpDownloader.sharedHttpClient.newCall(requestBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) return null
            val bytes = response.body?.bytes() ?: return null
            return if (bytes.isNotEmpty()) bytes else null
        }
    }

    private fun normalizeStreamKey(url: String): String {
        val idx = url.indexOf("&range=")
        return if (idx >= 0) {
            val rest = url.substring(idx + 7)
            val nextAmp = rest.indexOf('&')
            if (nextAmp >= 0) url.substring(0, idx) + rest.substring(nextAmp) else url.substring(0, idx)
        } else {
            url
        }
    }
}

/**
 * Custom Media3 DataSource optimized specifically for YouTube (googlevideo.com) streams:
 * 1. Shares OkHttpDownloader's HTTP/2 + pooled OkHttpClient to eliminate redundant TCP/TLS handshakes.
 * 2. Matches User-Agent to the exact YouTube Innertube client (`c=IOS`, `c=ANDROID`, `c=ANDROID_VR`, etc.)
 *    that signed the stream URL.
 * 3. Serves initial bytes (0..640KB video / 0..192KB audio) directly from [YoutubeStreamPreloader] RAM cache
 *    so MP4 initialization (`ftyp`+`moov`), `sidx` index, and the first segment load in 0ms.
 * 4. Appends bounded `&range=start-end` query parameters on `googlevideo.com` URLs (and chunks any
 *    open-ended requests into fast bounded chunks) to completely prevent YouTube CDN throttling.
 */
@OptIn(UnstableApi::class)
class YoutubeHttpDataSource(
    private val client: OkHttpClient = OkHttpDownloader.sharedHttpClient
) : BaseDataSource(/* isNetwork = */ true), DataSource {

    class Factory(
        private val client: OkHttpClient = OkHttpDownloader.sharedHttpClient
    ) : DataSource.Factory {
        override fun createDataSource(): DataSource = YoutubeHttpDataSource(client)
    }

    companion object {
        private const val INITIAL_CHUNK_SIZE = 512L * 1024L     // 512 KB fast first burst for progressive fallback
        private const val STANDARD_CHUNK_SIZE = 2L * 1024L * 1024L // 2 MB unthrottled chunks

        private const val UA_WEB =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0"
        private const val UA_ANDROID =
            "com.google.android.youtube/19.44.38 (Linux; U; Android 14) gzip"
        private const val UA_ANDROID_VR =
            "com.google.android.apps.youtube.vr.oculus/1.60.19 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip"
        private const val UA_IOS =
            "com.google.ios.youtube/19.45.4 (iPhone16,2; U; CPU iOS 18_1_0 like Mac OS X;)"
        private const val UA_TVHTML5 =
            "Mozilla/5.0 (SMART-TV; Linux; Tizen 6.0) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/4.0 Chrome/76.0.3809.146 TV Safari/537.36"

        fun isGoogleVideoUrl(uri: Uri): Boolean {
            val host = uri.host?.lowercase().orEmpty()
            return host.contains("googlevideo.com")
        }

        fun extractClen(uri: Uri): Long {
            return runCatching {
                uri.getQueryParameter("clen")?.toLongOrNull() ?: C.LENGTH_UNSET.toLong()
            }.getOrDefault(C.LENGTH_UNSET.toLong())
        }

        fun resolveUserAgentForUrl(uri: Uri): String {
            val clientName = runCatching { uri.getQueryParameter("c")?.uppercase() }.getOrNull()
            return when {
                clientName == null -> UA_WEB
                clientName.startsWith("IOS") -> UA_IOS
                clientName == "ANDROID_VR" -> UA_ANDROID_VR
                clientName.startsWith("ANDROID") -> UA_ANDROID
                clientName.startsWith("TVHTML5") -> UA_TVHTML5
                else -> UA_WEB
            }
        }

        fun buildRangedGoogleVideoUrl(uri: Uri, start: Long, endInclusive: Long): String {
            val baseStr = uri.toString()
            if (!isGoogleVideoUrl(uri)) return baseStr
            // Strip any existing &range=... parameter before appending the bounded range
            val cleanBase = baseStr.replace(Regex("([?&])range=[^&]*&?"), "$1")
                .trimEnd('&', '?')
            val separator = if (cleanBase.contains("?")) "&" else "?"
            return "${cleanBase}${separator}range=${start}-${endInclusive}"
        }
    }

    private var dataSpec: DataSpec? = null
    private var response: Response? = null
    private var currentInputStream: InputStream? = null
    private var opened = false

    private var baseUri: Uri = Uri.EMPTY
    private var isGoogleVideoStream: Boolean = false
    private var isChunkedOpenEnded: Boolean = false

    private var nextReadPosition: Long = 0L
    private var targetEndPositionExclusive: Long = C.LENGTH_UNSET.toLong()
    private var bytesRemaining: Long = C.LENGTH_UNSET.toLong()
    private var currentChunkRemaining: Long = 0L
    private var chunksLoadedCount: Int = 0

    @Throws(HttpDataSource.HttpDataSourceException::class)
    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        transferInitializing(dataSpec)

        baseUri = dataSpec.uri
        isGoogleVideoStream = isGoogleVideoUrl(baseUri)
        nextReadPosition = dataSpec.position
        chunksLoadedCount = 0

        val urlClen = if (isGoogleVideoStream) extractClen(baseUri) else C.LENGTH_UNSET.toLong()

        try {
            if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
                // Bounded range request (used by DashMediaSource for init, sidx, and media segments)
                isChunkedOpenEnded = false
                bytesRemaining = dataSpec.length
                targetEndPositionExclusive = dataSpec.position + dataSpec.length
                openBoundedStream(dataSpec.position, targetEndPositionExclusive - 1L, dataSpec)
            } else if (isGoogleVideoStream) {
                // Open-ended request on googlevideo.com (used by ProgressiveMediaSource):
                // Convert into bounded &range=start-end chunks to avoid YouTube CDN throttling!
                isChunkedOpenEnded = true
                if (urlClen > 0L && dataSpec.position < urlClen) {
                    targetEndPositionExclusive = urlClen
                    bytesRemaining = urlClen - dataSpec.position
                } else {
                    targetEndPositionExclusive = C.LENGTH_UNSET.toLong()
                    bytesRemaining = C.LENGTH_UNSET.toLong()
                }
                openNextChunk(dataSpec)
            } else {
                // Standard non-googlevideo HTTP request (e.g. subtitles / HLS playlists)
                isChunkedOpenEnded = false
                openStandardHttpStream(dataSpec)
            }
        } catch (e: IOException) {
            closeConnectionQuietly()
            throw HttpDataSource.HttpDataSourceException.createForIOException(
                e,
                dataSpec,
                HttpDataSource.HttpDataSourceException.TYPE_OPEN
            )
        }

        opened = true
        transferStarted(dataSpec)
        return bytesRemaining
    }

    @Throws(IOException::class)
    private fun openBoundedStream(start: Long, endInclusive: Long, spec: DataSpec) {
        val totalNeeded = endInclusive - start + 1L
        val cachedPrefix = if (isGoogleVideoStream) {
            YoutubeStreamPreloader.getCachedPrefix(baseUri.toString())
        } else {
            null
        }

        if (cachedPrefix != null && start < cachedPrefix.size) {
            val cacheEndInclusive = min(endInclusive, cachedPrefix.size.toLong() - 1L)
            val cacheSliceLen = (cacheEndInclusive - start + 1L).toInt()
            val memoryStream = ByteArrayInputStream(cachedPrefix, start.toInt(), cacheSliceLen)

            if (cacheEndInclusive >= endInclusive) {
                // 100% satisfied from RAM cache (0ms network latency!)
                currentInputStream = memoryStream
                currentChunkRemaining = totalNeeded
                return
            } else {
                // Prefix satisfied from RAM cache, fetch remaining tail from network
                val netStart = cacheEndInclusive + 1L
                val netResponse = executeRangeCall(netStart, endInclusive, spec)
                response = netResponse
                val netStream = netResponse.body?.byteStream()
                    ?: throw IOException("Empty response body for tail range $netStart-$endInclusive")
                currentInputStream = SequenceInputStream(memoryStream, netStream)
                currentChunkRemaining = totalNeeded
                return
            }
        }

        val netResponse = executeRangeCall(start, endInclusive, spec)
        response = netResponse
        currentInputStream = netResponse.body?.byteStream()
            ?: throw IOException("Empty response body for range $start-$endInclusive")
        currentChunkRemaining = totalNeeded
    }

    @Throws(IOException::class)
    private fun openNextChunk(spec: DataSpec): Boolean {
        closeConnectionQuietly()

        if (targetEndPositionExclusive != C.LENGTH_UNSET.toLong() &&
            nextReadPosition >= targetEndPositionExclusive
        ) {
            return false
        }

        val chunkSize = if (chunksLoadedCount == 0) INITIAL_CHUNK_SIZE else STANDARD_CHUNK_SIZE
        val endInclusive = if (targetEndPositionExclusive != C.LENGTH_UNSET.toLong()) {
            min(nextReadPosition + chunkSize - 1L, targetEndPositionExclusive - 1L)
        } else {
            nextReadPosition + chunkSize - 1L
        }

        if (endInclusive < nextReadPosition) {
            return false
        }

        // Check if initial chunk can be served from preloader RAM cache
        val cachedPrefix = YoutubeStreamPreloader.getCachedPrefix(baseUri.toString())
        if (cachedPrefix != null && nextReadPosition < cachedPrefix.size) {
            val sliceLen = (cachedPrefix.size.toLong() - nextReadPosition).toInt()
            currentInputStream = ByteArrayInputStream(cachedPrefix, nextReadPosition.toInt(), sliceLen)
            currentChunkRemaining = sliceLen.toLong()
            chunksLoadedCount++
            return true
        }

        val netResponse = executeRangeCall(nextReadPosition, endInclusive, spec)
        if (!netResponse.isSuccessful && netResponse.code == 416) {
            netResponse.close()
            return false
        }

        response = netResponse

        // If clen wasn't in the URL, try parsing total length from Content-Range header
        if (targetEndPositionExclusive == C.LENGTH_UNSET.toLong()) {
            val contentRange = netResponse.header("Content-Range")
            val parsedTotal = parseTotalFromContentRange(contentRange)
            if (parsedTotal > 0L) {
                targetEndPositionExclusive = parsedTotal
                if (bytesRemaining == C.LENGTH_UNSET.toLong()) {
                    bytesRemaining = parsedTotal - spec.position
                }
            }
        }

        val body = netResponse.body ?: throw IOException("Empty chunk body")
        val contentLength = body.contentLength()
        currentChunkRemaining = if (contentLength > 0L) {
            contentLength
        } else {
            endInclusive - nextReadPosition + 1L
        }
        currentInputStream = body.byteStream()
        chunksLoadedCount++
        return true
    }

    @Throws(IOException::class)
    private fun executeRangeCall(start: Long, endInclusive: Long, spec: DataSpec): Response {
        val targetUrl = if (isGoogleVideoStream) {
            buildRangedGoogleVideoUrl(baseUri, start, endInclusive)
        } else {
            baseUri.toString()
        }

        val userAgent = resolveUserAgentForUrl(baseUri)
        val requestBuilder = Request.Builder()
            .url(targetUrl)
            .header("User-Agent", userAgent)
            .header("Accept", "*/*")

        for ((k, v) in spec.httpRequestHeaders) {
            requestBuilder.header(k, v)
        }

        if (!isGoogleVideoStream) {
            requestBuilder.header("Range", "bytes=$start-$endInclusive")
        }

        val netResponse = client.newCall(requestBuilder.build()).execute()
        if (!netResponse.isSuccessful && netResponse.code != 416) {
            val code = netResponse.code
            netResponse.close()
            throw IOException("HTTP $code while fetching range $start-$endInclusive")
        }
        return netResponse
    }

    @Throws(IOException::class)
    private fun openStandardHttpStream(spec: DataSpec) {
        val requestBuilder = Request.Builder()
            .url(baseUri.toString())
            .header("User-Agent", resolveUserAgentForUrl(baseUri))

        for ((k, v) in spec.httpRequestHeaders) {
            requestBuilder.header(k, v)
        }

        if (spec.position > 0L || spec.length != C.LENGTH_UNSET.toLong()) {
            val endStr = if (spec.length != C.LENGTH_UNSET.toLong()) {
                (spec.position + spec.length - 1L).toString()
            } else {
                ""
            }
            requestBuilder.header("Range", "bytes=${spec.position}-$endStr")
        }

        val netResponse = client.newCall(requestBuilder.build()).execute()
        if (!netResponse.isSuccessful) {
            val code = netResponse.code
            netResponse.close()
            throw IOException("HTTP $code opening ${baseUri}")
        }
        response = netResponse
        val body = netResponse.body ?: throw IOException("Empty response body")
        val contentLen = body.contentLength()
        bytesRemaining = if (spec.length != C.LENGTH_UNSET.toLong()) {
            spec.length
        } else if (contentLen >= 0L) {
            contentLen
        } else {
            C.LENGTH_UNSET.toLong()
        }
        currentChunkRemaining = bytesRemaining
        currentInputStream = body.byteStream()
    }

    @Throws(HttpDataSource.HttpDataSourceException::class)
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT

        val spec = dataSpec ?: return C.RESULT_END_OF_INPUT

        try {
            if (isChunkedOpenEnded && currentChunkRemaining == 0L) {
                if (!openNextChunk(spec)) {
                    return C.RESULT_END_OF_INPUT
                }
            }

            val stream = currentInputStream ?: return C.RESULT_END_OF_INPUT
            val maxToRead = buildMaxReadCount(length)
            var bytesRead = stream.read(buffer, offset, maxToRead)

            if (bytesRead == -1) {
                if (isChunkedOpenEnded) {
                    currentChunkRemaining = 0L
                    if (!openNextChunk(spec)) {
                        return C.RESULT_END_OF_INPUT
                    }
                    bytesRead = currentInputStream?.read(buffer, offset, buildMaxReadCount(length)) ?: -1
                    if (bytesRead == -1) {
                        return C.RESULT_END_OF_INPUT
                    }
                } else {
                    return C.RESULT_END_OF_INPUT
                }
            }

            nextReadPosition += bytesRead
            if (currentChunkRemaining != C.LENGTH_UNSET.toLong()) {
                currentChunkRemaining = (currentChunkRemaining - bytesRead).coerceAtLeast(0L)
            }
            if (bytesRemaining != C.LENGTH_UNSET.toLong()) {
                bytesRemaining = (bytesRemaining - bytesRead).coerceAtLeast(0L)
            }
            bytesTransferred(bytesRead)
            return bytesRead
        } catch (e: IOException) {
            throw HttpDataSource.HttpDataSourceException.createForIOException(
                e,
                spec,
                HttpDataSource.HttpDataSourceException.TYPE_READ
            )
        }
    }

    private fun buildMaxReadCount(requestedLength: Int): Int {
        var maxToRead = requestedLength.toLong()
        if (bytesRemaining != C.LENGTH_UNSET.toLong()) {
            maxToRead = min(maxToRead, bytesRemaining)
        }
        if (isChunkedOpenEnded && currentChunkRemaining > 0L) {
            maxToRead = min(maxToRead, currentChunkRemaining)
        }
        return maxToRead.toInt().coerceAtLeast(1)
    }

    override fun getUri(): Uri? = response?.request?.url?.toString()?.let { Uri.parse(it) } ?: dataSpec?.uri

    override fun close() {
        try {
            closeConnectionQuietly()
        } finally {
            if (opened) {
                opened = false
                transferEnded()
            }
            dataSpec = null
        }
    }

    private fun closeConnectionQuietly() {
        try {
            currentInputStream?.close()
        } catch (_: IOException) {
        }
        currentInputStream = null

        try {
            response?.close()
        } catch (_: Exception) {
        }
        response = null
    }

    private fun parseTotalFromContentRange(contentRange: String?): Long {
        if (contentRange.isNullOrBlank()) return C.LENGTH_UNSET.toLong()
        val slashIndex = contentRange.lastIndexOf('/')
        if (slashIndex < 0 || slashIndex == contentRange.length - 1) return C.LENGTH_UNSET.toLong()
        return contentRange.substring(slashIndex + 1).trim().toLongOrNull() ?: C.LENGTH_UNSET.toLong()
    }
}
