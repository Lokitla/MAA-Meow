package com.aliothmoon.maameow.manager

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.aliothmoon.maameow.BuildConfig
import com.aliothmoon.maameow.domain.models.RemoteBackend
import com.aliothmoon.maameow.remote.RemoteServiceImpl
import rikka.shizuku.Shizuku
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

object ShizukuRemoteServiceConnector : RemoteServiceConnectorBackend {

    override val backend: RemoteBackend = RemoteBackend.SHIZUKU

    // Shizuku 没有内置连接超时，manager 需要在此时限内未收到回调时主动介入
    override val connectTimeoutMs: Long = 15_000L

    // debug 下每次递增强制重建服务，release 下用 VERSION_CODE 同版本可复用
    private val debugVersion = AtomicInteger(100)

    @Volatile
    private var activeBinding: ActiveBinding? = null

    override fun connect(callbacks: RemoteServiceConnectorBackend.Callbacks) {
        val args = createServiceArgs()
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val binding = activeBinding
                if (binding?.connection !== this) {
                    Timber.w("Ignoring stale Shizuku connection: %s", name)
                    return
                }
                if (binder == null) {
                    callbacks.onError(
                        backend,
                        IllegalStateException("RemoteService binder is null")
                    )
                    return
                }
                Timber.i("RemoteService connected by Shizuku: %s", name)
                callbacks.onConnected(backend, binder)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                if (activeBinding?.connection !== this) {
                    return
                }
                Timber.i("RemoteService disconnected by Shizuku: %s", name)
                callbacks.onDisconnected(backend)
            }
        }

        val binding = ActiveBinding(args, connection)
        activeBinding = binding

        runCatching {
            Timber.i("Binding remote service via Shizuku: %s", args)
            Shizuku.bindUserService(args, connection)
        }.onFailure { throwable ->
            if (activeBinding == binding) {
                activeBinding = null
            }
            Timber.e(throwable, "bindUserService failed")
            callbacks.onError(backend, throwable)
        }
    }

    override fun disconnect(currentBinder: IBinder?) {
        val binding = activeBinding ?: return
        activeBinding = null
        runCatching {
            Shizuku.unbindUserService(binding.args, binding.connection, true)
        }.onFailure {
            Timber.w(it, "unbindUserService failed")
        }
    }

    private fun createServiceArgs(): Shizuku.UserServiceArgs {
        return Shizuku.UserServiceArgs(
            ComponentName(BuildConfig.APPLICATION_ID, RemoteServiceImpl::class.java.name)
        ).apply {
            processNameSuffix("service")
            daemon(false)
            tag("remote_service")
            version(if (BuildConfig.DEBUG) debugVersion.incrementAndGet() else BuildConfig.VERSION_CODE)
            debuggable(BuildConfig.DEBUG)
        }
    }

    private data class ActiveBinding(
        val args: Shizuku.UserServiceArgs,
        val connection: ServiceConnection
    )
}
