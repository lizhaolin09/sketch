package com.github.panpf.sketch

import android.content.Context
import android.net.Uri
import androidx.annotation.AnyThread
import com.github.panpf.sketch.cache.BitmapPool
import com.github.panpf.sketch.cache.BitmapPoolHelper
import com.github.panpf.sketch.cache.DiskCache
import com.github.panpf.sketch.cache.LruBitmapPool
import com.github.panpf.sketch.cache.LruDiskCache
import com.github.panpf.sketch.cache.LruMemoryCache
import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.sketch.cache.MemorySizeCalculator
import com.github.panpf.sketch.decode.internal.BitmapFactoryDecoder
import com.github.panpf.sketch.fetch.AndroidResUriFetcher
import com.github.panpf.sketch.fetch.ApkIconUriFetcher
import com.github.panpf.sketch.fetch.AppIconUriFetcher
import com.github.panpf.sketch.fetch.AssetUriFetcher
import com.github.panpf.sketch.fetch.Base64UriFetcher
import com.github.panpf.sketch.fetch.ContentUriFetcher
import com.github.panpf.sketch.fetch.DrawableResUriFetcher
import com.github.panpf.sketch.fetch.FileUriFetcher
import com.github.panpf.sketch.fetch.HttpUriFetcher
import com.github.panpf.sketch.http.HttpStack
import com.github.panpf.sketch.http.HurlStack
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.Disposable
import com.github.panpf.sketch.request.DownloadRequest
import com.github.panpf.sketch.request.DownloadResult
import com.github.panpf.sketch.request.ExecuteResult
import com.github.panpf.sketch.request.Interceptor
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.request.LoadResult
import com.github.panpf.sketch.request.OneShotDisposable
import com.github.panpf.sketch.request.internal.DisplayEngineInterceptor
import com.github.panpf.sketch.request.internal.DisplayExecutor
import com.github.panpf.sketch.request.internal.DownloadEngineInterceptor
import com.github.panpf.sketch.request.internal.DownloadExecutor
import com.github.panpf.sketch.request.internal.LoadEngineInterceptor
import com.github.panpf.sketch.request.internal.LoadExecutor
import com.github.panpf.sketch.request.internal.LoadResultCacheInterceptor
import com.github.panpf.sketch.transform.internal.TransformationInterceptor
import com.github.panpf.sketch.util.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.File

class Sketch constructor(
    _context: Context,
    _logger: Logger?,
    _memoryCache: MemoryCache? = null,
    _diskCache: DiskCache? = null,
    _bitmapPool: BitmapPool? = null,
    _componentRegistry: ComponentRegistry? = null,
    _httpStack: HttpStack? = null,
    _downloadInterceptors: List<Interceptor<DownloadRequest, DownloadResult>>? = null,
    _loadInterceptors: List<Interceptor<LoadRequest, LoadResult>>? = null,
    _displayInterceptors: List<Interceptor<DisplayRequest, DisplayResult>>? = null,
) {
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            logger.e("scope", throwable, "exception")
        }
    )
    private val downloadExecutor = DownloadExecutor(this)
    private val loadExecutor = LoadExecutor(this)
    private val displayExecutor = DisplayExecutor(this)

    val appContext: Context = _context.applicationContext
    val logger = _logger ?: Logger()
    val httpStack = _httpStack ?: HurlStack.new()

    val diskCache = _diskCache ?: LruDiskCache(appContext, logger)
    val memoryCache: MemoryCache
    val bitmapPoolHelper: BitmapPoolHelper

    val componentRegistry: ComponentRegistry
    val downloadInterceptors: List<Interceptor<DownloadRequest, DownloadResult>>
    val loadInterceptors: List<Interceptor<LoadRequest, LoadResult>>
    val displayInterceptors: List<Interceptor<DisplayRequest, DisplayResult>>   // todo gif, svg, webpA

    val singleThreadTaskDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1)
    val networkTaskDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(10)
    val decodeTaskDispatcher: CoroutineDispatcher = Dispatchers.IO

    init {
        val memorySizeCalculator = MemorySizeCalculator(appContext, logger)
        memoryCache = _memoryCache
            ?: LruMemoryCache(appContext, memorySizeCalculator.memoryCacheSize, logger)
        val bitmapPool = _bitmapPool
            ?: LruBitmapPool(appContext, memorySizeCalculator.bitmapPoolSize, logger)
        bitmapPoolHelper = BitmapPoolHelper(_context, logger, bitmapPool)

        componentRegistry = (_componentRegistry ?: ComponentRegistry.new()).newBuilder().apply {
            addFetcher(HttpUriFetcher.Factory())
            addFetcher(FileUriFetcher.Factory())
            addFetcher(ContentUriFetcher.Factory())
            addFetcher(DrawableResUriFetcher.Factory())
            addFetcher(AndroidResUriFetcher.Factory())
            addFetcher(AssetUriFetcher.Factory())
            addFetcher(ApkIconUriFetcher.Factory())
            addFetcher(AppIconUriFetcher.Factory())
            addFetcher(Base64UriFetcher.Factory())
            addDecoder(BitmapFactoryDecoder.Factory())
        }.build()

        downloadInterceptors = (_downloadInterceptors ?: listOf()) + DownloadEngineInterceptor()
        loadInterceptors = (_loadInterceptors
            ?: listOf()) + LoadResultCacheInterceptor() + TransformationInterceptor() + LoadEngineInterceptor()
        displayInterceptors = (_displayInterceptors ?: listOf()) + DisplayEngineInterceptor()

        // todo 增加 defaultOptions
        if (diskCache is LruDiskCache) {
            val wrapperErrorCallback = diskCache.errorCallback
            diskCache.errorCallback =
                LruDiskCache.ErrorCallback { dir: File, throwable: Throwable ->
                    wrapperErrorCallback?.onInstallDiskCacheError(dir, throwable)
                    // todo
//                configuration.callback.onError(InstallDiskCacheException(e, cacheDir))
                }
        }
    }


    /***************************************** Display ********************************************/

    @AnyThread
    fun enqueueDisplay(request: DisplayRequest): Disposable<ExecuteResult<DisplayResult>> {
        // todo ViewTarget bind RequestManager，方尺重复加载，图片错乱、自动取消、自动重新请求，监听 lifecycler，延迟到确定了大小之后再发起请求
        val job = scope.async(singleThreadTaskDispatcher) {
            displayExecutor.execute(request)
        }
        return OneShotDisposable(job)
    }

    @AnyThread
    fun enqueueDisplay(
        uriString: String?,
        configBlock: (DisplayRequest.Builder.() -> Unit)? = null,
    ): Disposable<ExecuteResult<DisplayResult>> =
        enqueueDisplay(DisplayRequest.new(uriString, configBlock))

    @AnyThread
    fun enqueueDisplay(
        uri: Uri?,
        configBlock: (DisplayRequest.Builder.() -> Unit)? = null,
    ): Disposable<ExecuteResult<DisplayResult>> =
        enqueueDisplay(DisplayRequest.new(uri, configBlock))

    suspend fun executeDisplay(request: DisplayRequest): ExecuteResult<DisplayResult> =
        coroutineScope {
            val job = async(singleThreadTaskDispatcher) {
                displayExecutor.execute(request)
            }
            job.await()
        }

    suspend fun executeDisplay(
        uriString: String?,
        configBlock: (DisplayRequest.Builder.() -> Unit)? = null
    ): ExecuteResult<DisplayResult> =
        executeDisplay(DisplayRequest.new(uriString, configBlock))

    suspend fun executeDisplay(
        uri: Uri?,
        configBlock: (DisplayRequest.Builder.() -> Unit)? = null
    ): ExecuteResult<DisplayResult> =
        executeDisplay(DisplayRequest.new(uri, configBlock))


    /****************************************** Load **********************************************/

    @AnyThread
    fun enqueueLoad(request: LoadRequest): Disposable<ExecuteResult<LoadResult>> {
        val job = scope.async(singleThreadTaskDispatcher) {
            loadExecutor.execute(request)
        }
        return OneShotDisposable(job)
    }

    @AnyThread
    fun enqueueLoad(
        uriString: String,
        configBlock: (LoadRequest.Builder.() -> Unit)? = null,
    ): Disposable<ExecuteResult<LoadResult>> =
        enqueueLoad(LoadRequest.new(uriString, configBlock))

    @AnyThread
    fun enqueueLoad(
        uri: Uri,
        configBlock: (LoadRequest.Builder.() -> Unit)? = null,
    ): Disposable<ExecuteResult<LoadResult>> =
        enqueueLoad(LoadRequest.new(uri, configBlock))

    suspend fun executeLoad(request: LoadRequest): ExecuteResult<LoadResult> = coroutineScope {
        val job = async(singleThreadTaskDispatcher) {
            loadExecutor.execute(request)
        }
        job.await()
    }

    suspend fun executeLoad(
        uriString: String,
        configBlock: (LoadRequest.Builder.() -> Unit)? = null
    ): ExecuteResult<LoadResult> = executeLoad(LoadRequest.new(uriString, configBlock))

    suspend fun executeLoad(
        uri: Uri,
        configBlock: (LoadRequest.Builder.() -> Unit)? = null
    ): ExecuteResult<LoadResult> = executeLoad(LoadRequest.new(uri, configBlock))


    /**************************************** Download ********************************************/

    @AnyThread
    fun enqueueDownload(request: DownloadRequest): Disposable<ExecuteResult<DownloadResult>> {
        val job = scope.async(singleThreadTaskDispatcher) {
            downloadExecutor.execute(request)
        }
        return OneShotDisposable(job)
    }

    @AnyThread
    fun enqueueDownload(
        uriString: String,
        configBlock: (DownloadRequest.Builder.() -> Unit)? = null,
    ): Disposable<ExecuteResult<DownloadResult>> =
        enqueueDownload(DownloadRequest.new(uriString, configBlock))

    @AnyThread
    fun enqueueDownload(
        uri: Uri,
        configBlock: (DownloadRequest.Builder.() -> Unit)? = null,
    ): Disposable<ExecuteResult<DownloadResult>> =
        enqueueDownload(DownloadRequest.new(uri, configBlock))

    suspend fun executeDownload(request: DownloadRequest): ExecuteResult<DownloadResult> =
        coroutineScope {
            val job = async(singleThreadTaskDispatcher) {
                downloadExecutor.execute(request)
            }
            job.await()
        }

    suspend fun executeDownload(
        uriString: String,
        configBlock: (DownloadRequest.Builder.() -> Unit)? = null
    ): ExecuteResult<DownloadResult> =
        executeDownload(DownloadRequest.new(uriString, configBlock))

    suspend fun executeDownload(
        uri: Uri,
        configBlock: (DownloadRequest.Builder.() -> Unit)? = null
    ): ExecuteResult<DownloadResult> =
        executeDownload(DownloadRequest.new(uri, configBlock))


    companion object {
        fun new(
            context: Context,
            configBlock: (Builder.() -> Unit)? = null
        ): Sketch = Builder(context).apply {
            configBlock?.invoke(this)
        }.build()
    }

    class Builder(context: Context) {

        private val appContext: Context = context.applicationContext
        private var logger: Logger? = null
        private var memoryCache: MemoryCache? = null
        private var diskCache: DiskCache? = null
        private var bitmapPool: BitmapPool? = null
        private var componentRegistry: ComponentRegistry? = null
        private var httpStack: HttpStack? = null
        private var downloadInterceptors: MutableList<Interceptor<DownloadRequest, DownloadResult>>? =
            null
        private var loadInterceptors: MutableList<Interceptor<LoadRequest, LoadResult>>? =
            null
        private var displayInterceptors: MutableList<Interceptor<DisplayRequest, DisplayResult>>? =
            null

        fun logger(logger: Logger?): Builder = apply {
            this.logger = logger
        }

        fun memoryCache(memoryCache: MemoryCache?): Builder = apply {
            this.memoryCache = memoryCache
        }

        fun diskCache(diskCache: DiskCache?): Builder = apply {
            this.diskCache = diskCache
        }

        fun bitmapPool(bitmapPool: BitmapPool?): Builder = apply {
            this.bitmapPool = bitmapPool
        }

        fun components(components: ComponentRegistry?): Builder = apply {
            this.componentRegistry = components
        }

        fun componentsWithBuilder(block: ComponentRegistry.Builder.() -> Unit): Builder = apply {
            this.componentRegistry = ComponentRegistry.Builder().apply(block).build()
        }

        fun httpStack(httpStack: HttpStack?): Builder = apply {
            this.httpStack = httpStack
        }

        fun addDownloadInterceptor(interceptor: Interceptor<DownloadRequest, DownloadResult>): Builder =
            apply {
                this.downloadInterceptors = (downloadInterceptors ?: mutableListOf()).apply {
                    add(interceptor)
                }
            }

        fun addLoadInterceptor(interceptor: Interceptor<LoadRequest, LoadResult>): Builder =
            apply {
                this.loadInterceptors = (loadInterceptors ?: mutableListOf()).apply {
                    add(interceptor)
                }
            }

        fun addDisplayInterceptor(interceptor: Interceptor<DisplayRequest, DisplayResult>): Builder =
            apply {
                this.displayInterceptors = (displayInterceptors ?: mutableListOf()).apply {
                    add(interceptor)
                }
            }

        fun build(): Sketch = Sketch(
            _context = appContext,
            _logger = logger,
            _memoryCache = memoryCache,
            _diskCache = diskCache,
            _bitmapPool = bitmapPool,
            _componentRegistry = componentRegistry,
            _httpStack = httpStack,
            _downloadInterceptors = downloadInterceptors,
            _loadInterceptors = loadInterceptors,
            _displayInterceptors = displayInterceptors,
        )
    }
}