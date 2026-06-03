package com.aliothmoon.maameow.manager

import android.os.IBinder
import com.aliothmoon.maameow.domain.models.RemoteBackend

interface RemoteServiceConnectorBackend {
    val backend: RemoteBackend

    /**
     * manager 层连接超时保护的时限。
     * 应大于 backend 内部所有异步操作之和，确保 backend 自身的错误处理（回调、日志）先于
     * manager 超时完成。默认值 [Long.MAX_VALUE] 表示 backend 自己管理超时，manager 不介入。
     */
    val connectTimeoutMs: Long get() = Long.MAX_VALUE

    fun connect(callbacks: Callbacks)

    fun disconnect(currentBinder: IBinder?)

    interface Callbacks {
        fun onConnected(backend: RemoteBackend, binder: IBinder)

        fun onDisconnected(backend: RemoteBackend)

        fun onError(backend: RemoteBackend, throwable: Throwable)
    }
}
