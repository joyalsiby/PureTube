package com.puretube.player.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.puretube.player.extractor.OkHttpDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class ReleaseUpdateInfo(
    val versionName: String,
    val tagName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val apkFileName: String,
    val apkSizeBytes: Long,
    val htmlUrl: String
)

object AppUpdateManager {

    const val GITHUB_OWNER = "joyalsiby"
    const val GITHUB_REPO = "PureTube"
    const val GITHUB_REPO_URL = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO"
    const val GITHUB_RELEASES_URL = "$GITHUB_REPO_URL/releases"
    const val GITHUB_ISSUES_URL = "$GITHUB_REPO_URL/issues"
    const val AUTHOR_NAME = "Joyal Siby"
    const val AUTHOR_WEBSITE_URL = "https://joyalsiby.com"
    const val AUTHOR_GITHUB_URL = "https://github.com/joyalsiby"

    private const val LATEST_RELEASE_API =
        "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
    private const val ALL_RELEASES_API =
        "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases?per_page=5"

    fun getCurrentVersionName(context: Context): String {
        return runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "0.3.007"
    }

    /**
     * Checks GitHub Releases for a newer version of PureTube.
     */
    suspend fun checkForUpdate(
        context: Context,
        ignoreComparison: Boolean = false
    ): Result<ReleaseUpdateInfo?> = withContext(Dispatchers.IO) {
        runCatching {
            val currentVersion = getCurrentVersionName(context)
            val releaseJson = fetchLatestReleaseJson() ?: return@runCatching null

            val tagName = releaseJson.optString("tag_name", "").trim()
            if (tagName.isEmpty()) return@runCatching null

            val remoteVersion = tagName.removePrefix("v").removePrefix("V").trim()
            val releaseTitle = releaseJson.optString("name", "PureTube v$remoteVersion").ifBlank {
                "PureTube v$remoteVersion"
            }
            val releaseNotes = releaseJson.optString("body", "").trim()
            val htmlUrl = releaseJson.optString("html_url", GITHUB_RELEASES_URL)

            // Find attached .apk asset
            var apkDownloadUrl = ""
            var apkFileName = "PureTube-v$remoteVersion.apk"
            var apkSizeBytes = 0L

            val assets = releaseJson.optJSONArray("assets") ?: JSONArray()
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                val name = asset.optString("name", "")
                val downloadUrl = asset.optString("browser_download_url", "")
                if (name.endsWith(".apk", ignoreCase = true) && downloadUrl.isNotEmpty()) {
                    apkDownloadUrl = downloadUrl
                    apkFileName = name
                    apkSizeBytes = asset.optLong("size", 0L)
                    break
                }
            }

            // Fallback direct URL if asset list is empty
            if (apkDownloadUrl.isEmpty()) {
                apkDownloadUrl =
                    "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/download/$tagName/PureTube-v$remoteVersion.apk"
            }

            val isNewer = isRemoteVersionNewer(currentVersion, remoteVersion)
            if (isNewer || ignoreComparison) {
                if (isNewer) {
                    ReleaseUpdateInfo(
                        versionName = remoteVersion,
                        tagName = tagName,
                        releaseTitle = releaseTitle,
                        releaseNotes = releaseNotes,
                        apkDownloadUrl = apkDownloadUrl,
                        apkFileName = apkFileName,
                        apkSizeBytes = apkSizeBytes,
                        htmlUrl = htmlUrl
                    )
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    private fun fetchLatestReleaseJson(): JSONObject? {
        val client = OkHttpDownloader.sharedHttpClient

        // 1. Try /releases/latest
        val latestReq = Request.Builder()
            .url(LATEST_RELEASE_API)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "PureTube-Android-Updater")
            .build()

        runCatching {
            client.newCall(latestReq).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    if (body.isNotBlank()) {
                        return JSONObject(body)
                    }
                }
            }
        }

        // 2. Fallback to /releases list (in case latest is pre-release or tag list)
        val listReq = Request.Builder()
            .url(ALL_RELEASES_API)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "PureTube-Android-Updater")
            .build()

        runCatching {
            client.newCall(listReq).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val arr = JSONArray(body)
                    if (arr.length() > 0) {
                        return arr.optJSONObject(0)
                    }
                }
            }
        }

        return null
    }

    /**
     * Compares version strings like "0.3.006" vs "0.3.007" or "0.4.000".
     */
    fun isRemoteVersionNewer(current: String, remote: String): Boolean {
        val cleanCur = current.removePrefix("v").removePrefix("V").trim()
        val cleanRem = remote.removePrefix("v").removePrefix("V").trim()
        if (cleanCur == cleanRem || cleanRem.isEmpty()) return false

        val curParts = cleanCur.split(".", "-").mapNotNull { it.toIntOrNull() }
        val remParts = cleanRem.split(".", "-").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(curParts.size, remParts.size)
        for (i in 0 until maxLen) {
            val c = curParts.getOrElse(i) { 0 }
            val r = remParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    /**
     * Downloads the APK into context.externalCacheDir/updates (or cacheDir/updates) with live progress.
     */
    suspend fun downloadUpdateApk(
        context: Context,
        updateInfo: ReleaseUpdateInfo,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val updatesDir = File(context.cacheDir, "updates").apply {
                if (!exists()) mkdirs()
            }
            // Clean old APKs
            updatesDir.listFiles()?.forEach { it.delete() }

            val targetFile = File(updatesDir, updateInfo.apkFileName.ifBlank { "PureTube-update.apk" })

            val request = Request.Builder()
                .url(updateInfo.apkDownloadUrl)
                .header("User-Agent", "PureTube-Android-Updater")
                .build()

            OkHttpDownloader.sharedHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Download failed (HTTP ${response.code})")
                }
                val body = response.body ?: throw IllegalStateException("Empty APK response")
                val totalBytes = body.contentLength().takeIf { it > 0 } ?: updateInfo.apkSizeBytes

                body.byteStream().use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(16 * 1024)
                        var bytesRead: Int
                        var downloadedBytes = 0L
                        var lastReportedPercent = -1

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            if (totalBytes > 0) {
                                val fraction = (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                                val pct = (fraction * 100).toInt()
                                if (pct != lastReportedPercent) {
                                    lastReportedPercent = pct
                                    onProgress(fraction)
                                }
                            }
                        }
                        output.flush()
                    }
                }
            }
            onProgress(1f)
            targetFile
        }
    }

    /**
     * Triggers the native Android Package Installer for the downloaded APK file.
     */
    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val permissionIntent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(permissionIntent)
                return
            }
        }

        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }
}
