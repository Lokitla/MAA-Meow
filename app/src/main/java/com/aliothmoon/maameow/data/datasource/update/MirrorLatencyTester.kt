package com.aliothmoon.maameow.data.datasource.update

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * 镜像延迟测试工具
 * 对多个候选 URL 并发 HEAD 请求，返回延迟最低的可用 URL
 */
object MirrorLatencyTester {

    private val headClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    /**
     * 从候选 URL 列表中选择延迟最低的可用 URL
     * @param urls 候选 URL 列表（第一个是原始 URL，后面是镜像）
     * @return 延迟最低的可用 URL，全部不可用时返回第一个
     */
    suspend fun selectFastest(urls: List<String>): String {
        if (urls.size <= 1) return urls.first()

        val results = testAll(urls)
        val fastest = results.filter { it.latencyMs >= 0 }.minByOrNull { it.latencyMs }

        if (fastest != null) {
            Timber.i("镜像测速完成，最快: ${fastest.url} (${fastest.latencyMs}ms)")
            return fastest.url
        }

        Timber.w("所有镜像均不可用，回退到原始 URL")
        return urls.first()
    }

    /**
     * 测试所有 URL 的延迟
     * @return 测试结果列表（含延迟，-1 表示不可用）
     */
    suspend fun testAll(urls: List<String>): List<MirrorResult> = coroutineScope {
        urls.map { url ->
            async {
                try {
                    val request = Request.Builder()
                        .url(url)
                        .head()
                        .build()
                    val start = System.currentTimeMillis()
                    headClient.newCall(request).execute().use { response ->
                        val latency = (System.currentTimeMillis() - start).toDouble()
                        MirrorResult(
                            url = url,
                            latencyMs = if (response.isSuccessful) latency else -1.0,
                            statusCode = response.code
                        )
                    }
                } catch (e: Exception) {
                    Timber.d(e, "HEAD 请求失败: $url")
                    MirrorResult(url = url, latencyMs = -1.0, statusCode = -1)
                }
            }
        }.awaitAll()
    }

    /**
     * 从原始 URL 和镜像前缀生成候选 URL 列表
     * @param originalUrl 原始 GitHub URL
     * @param mirrorPrefix 镜像前缀（如 https://xget.2511016.xyz/gh/）
     * @return 候选 URL 列表（镜像在前，原始在后）
     */
    fun buildCandidateUrls(originalUrl: String, mirrorPrefix: String?): List<String> {
        val urls = mutableListOf<String>()

        // 镜像优先
        if (!mirrorPrefix.isNullOrBlank()) {
            val prefix = mirrorPrefix.trimEnd('/')
            // 从原始 URL 提取 github.com 之后的路径
            val ghPrefix = "https://github.com/"
            val releasesPrefix = "https://releases.githubusercontent.com/"

            val mirrorUrl = when {
                originalUrl.startsWith(ghPrefix) ->
                    "$prefix/gh/${originalUrl.removePrefix(ghPrefix)}"
                originalUrl.startsWith(releasesPrefix) ->
                    "$prefix/gh-releases/${originalUrl.removePrefix(releasesPrefix)}"
                else -> null
            }
            if (mirrorUrl != null) {
                urls.add(mirrorUrl)
            }
        }

        // 原始 URL
        urls.add(originalUrl)
        return urls
    }

    data class MirrorResult(
        val url: String,
        val latencyMs: Double,
        val statusCode: Int
    )

    /**
     * 测试单个 URL 的延迟
     * @param url 要测试的 URL
     * @return 延迟毫秒数，-1 表示不可用
     */
    suspend fun testLatency(url: String): Double {
        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()
            val start = System.currentTimeMillis()
            headClient.newCall(request).execute().use { response ->
                val latency = (System.currentTimeMillis() - start).toDouble()
                if (response.isSuccessful) latency else -1.0
            }
        } catch (e: Exception) {
            Timber.d(e, "HEAD 请求失败: $url")
            -1.0
        }
    }
}
