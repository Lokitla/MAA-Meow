package com.aliothmoon.maameow.data.datasource.update

import com.aliothmoon.maameow.constant.MaaApi
import com.aliothmoon.maameow.domain.service.update.resolver.ResourceDownloadUrlResolver
import timber.log.Timber

class GitHubResourceDownloadUrlResolver(
    private val customMirrorUrl: String? = null
) : ResourceDownloadUrlResolver {

    override suspend fun resolve(currentVersion: String): Result<String> {
        return runCatching {
            val originalUrl = MaaApi.GITHUB_RESOURCE
            Timber.i("GitHub 资源原始链接: $originalUrl")

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
        }
    }
}
