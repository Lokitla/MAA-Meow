package com.aliothmoon.maameow.data.datasource.update

import com.aliothmoon.maameow.constant.MaaApi
import com.aliothmoon.maameow.data.api.HttpClientHelper
import com.aliothmoon.maameow.data.api.model.GitHubRelease
import com.aliothmoon.maameow.data.model.update.UpdateChannel
import com.aliothmoon.maameow.domain.service.update.resolver.AppDownloadUrlResolver
import com.aliothmoon.maameow.utils.JsonUtils
import timber.log.Timber

class GitHubAppDownloadUrlResolver(
    private val httpClient: HttpClientHelper,
    private val customMirrorUrl: String? = null
) : AppDownloadUrlResolver {

    private val json = JsonUtils.common

    override suspend fun resolve(version: String, channel: UpdateChannel): Result<String> {
        return runCatching {
            val tag = if (version.startsWith("v", ignoreCase = true)) version else "v$version"
            val response = httpClient.get(MaaApi.appGitHubReleaseByTag(tag))

            if (!response.isSuccessful) {
                throw Exception("GitHub API 请求失败 (HTTP ${response.code})")
            }

            val release = json.decodeFromString<GitHubRelease>(response.body.string())

            val apkAsset = release.assets.firstOrNull { it.name.endsWith("universal.apk") }
                ?: throw Exception("Release 中未找到 APK 文件")

            val originalUrl = apkAsset.browserDownloadUrl
            Timber.i("GitHub 原始下载链接: $originalUrl")

            // 生成候选 URL 并测速选最优
            val candidates = MirrorLatencyTester.buildCandidateUrls(originalUrl, customMirrorUrl)
            if (candidates.size > 1) {
                Timber.i("开始镜像测速，候选数: ${candidates.size}")
                val selected = MirrorLatencyTester.selectFastest(candidates)
                Timber.i("最终选择: $selected")
                selected
            } else {
                originalUrl
            }
        }.onFailure { e ->
            Timber.e(e, "GitHub 获取 Release 失败: $version")
        }
    }
}
