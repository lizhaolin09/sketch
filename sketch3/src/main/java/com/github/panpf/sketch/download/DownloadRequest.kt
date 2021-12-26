package com.github.panpf.sketch.download

import android.net.Uri
import com.github.panpf.sketch.common.ImageRequest
import com.github.panpf.sketch.common.cache.CachePolicy

class DownloadRequest private constructor(
    override val uri: Uri,
    val diskCacheKey: String,
    val diskCachePolicy: CachePolicy,
    val listener: DownloadListener?,
    val progressListener: DownloadProgressListener?,
) : ImageRequest {

    fun newBuilder(
        configBlock: (Builder.() -> Unit)? = null
    ): Builder = Builder(this).apply {
        configBlock?.invoke(this)
    }

    fun new(
        configBlock: (Builder.() -> Unit)? = null
    ): DownloadRequest = Builder(this).apply {
        configBlock?.invoke(this)
    }.build()

    companion object {
        fun new(
            uri: Uri,
            configBlock: (Builder.() -> Unit)? = null
        ): DownloadRequest = Builder(uri).apply {
            configBlock?.invoke(this)
        }.build()

        fun new(
            uriString: String,
            configBlock: (Builder.() -> Unit)? = null
        ): DownloadRequest = Builder(uriString).apply {
            configBlock?.invoke(this)
        }.build()
    }

    class Builder {
        private val uri: Uri
        private var diskCacheKey: String?
        private var diskCachePolicy: CachePolicy?
        private var listener: DownloadListener?
        private var progressListener: DownloadProgressListener?

        constructor(uri: Uri) {
            this.uri = uri
            this.diskCacheKey = null
            this.diskCachePolicy = null
            this.listener = null
            this.progressListener = null
        }

        constructor(uriString: String) : this(Uri.parse(uriString))

        internal constructor(request: DownloadRequest) {
            this.uri = request.uri
            this.diskCacheKey = request.diskCacheKey
            this.diskCachePolicy = request.diskCachePolicy
            this.listener = request.listener
            this.progressListener = request.progressListener
        }

        fun diskCacheKey(diskCacheKey: String?): Builder = apply {
            this.diskCacheKey = diskCacheKey
        }

        fun diskCachePolicy(diskCachePolicy: CachePolicy?): Builder = apply {
            this.diskCachePolicy = diskCachePolicy
        }

        fun listener(listener: DownloadListener?): Builder = apply {
            this.listener = listener
        }

        inline fun listener(
            crossinline onStart: (request: DownloadRequest) -> Unit = {},
            crossinline onCancel: (request: DownloadRequest) -> Unit = {},
            crossinline onError: (request: DownloadRequest, result: Throwable) -> Unit = { _, _ -> },
            crossinline onSuccess: (request: DownloadRequest, result: DownloadData) -> Unit = { _, _ -> }
        ) = listener(object : DownloadListener {
            override fun onStart(request: DownloadRequest) = onStart(request)
            override fun onCancel(request: DownloadRequest) = onCancel(request)
            override fun onError(request: DownloadRequest, throwable: Throwable) =
                onError(request, throwable)

            override fun onSuccess(request: DownloadRequest, result: DownloadData) =
                onSuccess(request, result)
        })

        fun progressListener(progressListener: DownloadProgressListener?): Builder = apply {
            this.progressListener = progressListener
        }

        fun build(): DownloadRequest = DownloadRequest(
            uri = uri,
            diskCacheKey = diskCacheKey ?: uri.toString(),
            diskCachePolicy = diskCachePolicy ?: CachePolicy.ENABLED,
            listener = listener,
            progressListener = progressListener,
        )
    }
}