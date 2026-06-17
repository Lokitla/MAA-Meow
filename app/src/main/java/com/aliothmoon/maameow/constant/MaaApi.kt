package com.aliothmoon.maameow.constant

object MaaApi {
    // 主 API 服务器
    const val MAA_API = "https://api.maa.plus/MaaAssistantArknights/api/"

    // 备用 API 服务器
    const val MAA_API_BACKUP = "https://api2.maa.plus/MaaAssistantArknights/api/"

    // 活动关卡 API 路径
    const val STAGE_ACTIVITY_API = "gui/StageActivityV2.json"

    // 任务配置 API 路径
    const val TASKS_API = "resource/tasks.json"

    /**
     * 获取全球服 tasks.json API 路径
     */
    fun getGlobalTasksApi(clientType: String): String {
        return "resource/global/${clientType}/resource/tasks.json"
    }

    val API_URLS = listOf(
        MAA_API,
        MAA_API_BACKUP,
        "https://maa-api.oss-cn-beijing.aliyuncs.com/MaaAssistantArknights/api/"
    )


    // MirrorChyan 基础地址
    const val MIRROR_CHYAN_BASE = "https://mirrorchyan.com/"

    // MirrorChyan 更新源
    const val MIRROR_CHYAN_RESOURCE = "https://mirrorchyan.com/api/resources/MaaResource/latest"

    // GitHub 资源直链
    const val GITHUB_RESOURCE =
        "https://github.com/MaaAssistantArknights/MaaResource/archive/refs/heads/main.zip"

    // GitHub 镜像加速
    const val GITHUB_RESOURCE_MIRROR1 =
        "https://mirror.ghproxy.com/https://github.com/MaaAssistantArknights/MaaResource/archive/refs/heads/main.zip"
    const val GITHUB_RESOURCE_MIRROR2 =
        "https://ghfast.top/https://github.com/MaaAssistantArknights/MaaResource/archive/refs/heads/main.zip"

    val GITHUB_RESOURCE_URLS = listOf(
        GITHUB_RESOURCE,
        GITHUB_RESOURCE_MIRROR1,
        GITHUB_RESOURCE_MIRROR2
    )

    // ==================== App 更新 ====================

    // GitHub 仓库
    const val APP_GITHUB_OWNER = "Lokitla"
    const val APP_GITHUB_REPO = "MAA-Meow"

    // GitHub Release API (包含 prerelease)
    const val APP_GITHUB_RELEASES =
        "https://api.github.com/repos/$APP_GITHUB_OWNER/$APP_GITHUB_REPO/releases?per_page=1"

    const val APP_GITHUB_RELEASES_BETA =
        "https://api.github.com/repos/$APP_GITHUB_OWNER/$APP_GITHUB_REPO/releases?per_page=5"

    fun appGitHubReleaseByTag(tag: String): String =
        "https://api.github.com/repos/$APP_GITHUB_OWNER/$APP_GITHUB_REPO/releases/tags/$tag"

    // GitHub API 镜像（国内加速）
    const val APP_GITHUB_RELEASES_MIRROR =
        "https://mirror.ghproxy.com/https://api.github.com/repos/$APP_GITHUB_OWNER/$APP_GITHUB_REPO/releases?per_page=1"

    // MirrorChyan App 更新源
    const val MIRROR_CHYAN_APP_RESOURCE = "https://mirrorchyan.com/api/resources/MAA-Meow/latest"

    const val BASE_SCHEDULING_SCHEMA =
        "https://maa.plus/docs/zh-cn/protocol/base-scheduling-schema.html"

}