package com.github.panpf.sketch.request

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.graphics.drawable.Drawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.RequiresApi
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.decode.BitmapConfig
import com.github.panpf.sketch.drawable.CrossfadeDrawable
import com.github.panpf.sketch.http.HttpHeaders
import com.github.panpf.sketch.request.ImageOptions.Builder
import com.github.panpf.sketch.resize.FixedPrecisionDecider
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.resize.PrecisionDecider
import com.github.panpf.sketch.resize.Scale
import com.github.panpf.sketch.resize.SizeResolver
import com.github.panpf.sketch.stateimage.ErrorStateImage
import com.github.panpf.sketch.stateimage.StateImage
import com.github.panpf.sketch.transform.Transformation
import com.github.panpf.sketch.transition.CrossfadeTransition
import com.github.panpf.sketch.transition.Transition
import com.github.panpf.sketch.util.Size

fun ImageOptions(
    configBlock: (Builder.() -> Unit)? = null
): ImageOptions = Builder().apply {
    configBlock?.invoke(this)
}.build()

fun ImageOptionsBuilder(
    configBlock: (Builder.() -> Unit)? = null
): Builder = Builder().apply {
    configBlock?.invoke(this)
}

interface ImageOptions {

    val depth: RequestDepth?
    val parameters: Parameters?

    val httpHeaders: HttpHeaders?
    val networkContentDiskCachePolicy: CachePolicy?

    val bitmapConfig: BitmapConfig?
    val colorSpace: ColorSpace?

    @Deprecated("From Android N (API 24), this is ignored. The output will always be high quality.")
    val preferQualityOverSpeed: Boolean?
    val resizeSize: Size?
    val resizeSizeResolver: SizeResolver?
    val resizePrecisionDecider: PrecisionDecider?
    val resizeScale: Scale?
    val transformations: List<Transformation>?
    val disabledBitmapPool: Boolean?
    val ignoreExifOrientation: Boolean?
    val bitmapResultDiskCachePolicy: CachePolicy?
    val disabledAnimationDrawable: Boolean?
    val bitmapMemoryCachePolicy: CachePolicy?
    val placeholderImage: StateImage?
    val errorImage: StateImage?
    val transition: Transition.Factory?

    val depthFrom: String?
        get() = parameters?.value(ImageRequest.REQUEST_DEPTH_FROM)

    fun newBuilder(
        configBlock: (Builder.() -> Unit)? = null
    ): Builder = Builder(this).apply {
        configBlock?.invoke(this)
    }

    fun newOptions(
        configBlock: (Builder.() -> Unit)? = null
    ): ImageOptions = Builder(this).apply {
        configBlock?.invoke(this)
    }.build()

    @Suppress("DEPRECATION")
    fun isEmpty(): Boolean = depth == null
            && parameters?.isEmpty() != false
            && httpHeaders == null
            && networkContentDiskCachePolicy == null
            && bitmapConfig == null
            && (VERSION.SDK_INT < VERSION_CODES.O || colorSpace == null)
            && preferQualityOverSpeed == null
            && resizeSize == null
            && resizeSizeResolver == null
            && resizePrecisionDecider == null
            && resizeScale == null
            && transformations == null
            && disabledBitmapPool == null
            && ignoreExifOrientation == null
            && bitmapResultDiskCachePolicy == null
            && disabledAnimationDrawable == null
            && bitmapMemoryCachePolicy == null
            && placeholderImage == null
            && errorImage == null
            && transition == null

    class Builder {

        private var depth: RequestDepth? = null
        private var parametersBuilder: Parameters.Builder? = null

        private var httpHeaders: HttpHeaders.Builder? = null
        private var networkContentDiskCachePolicy: CachePolicy? = null

        private var bitmapConfig: BitmapConfig? = null

        private var colorSpace: ColorSpace? = null
        private var preferQualityOverSpeed: Boolean? = null
        private var resizeSize: Size? = null
        private var resizeSizeResolver: SizeResolver? = null
        private var resizePrecisionDecider: PrecisionDecider? = null
        private var resizeScale: Scale? = null
        private var transformations: List<Transformation>? = null
        private var disabledBitmapPool: Boolean? = null
        private var ignoreExifOrientation: Boolean? = null
        private var bitmapResultDiskCachePolicy: CachePolicy? = null

        private var bitmapMemoryCachePolicy: CachePolicy? = null
        private var disabledAnimationDrawable: Boolean? = null
        private var placeholderImage: StateImage? = null
        private var errorImage: StateImage? = null
        private var transition: Transition.Factory? = null

        constructor()

        internal constructor(request: ImageOptions) {
            this.depth = request.depth
            this.parametersBuilder = request.parameters?.newBuilder()

            this.httpHeaders = request.httpHeaders?.newBuilder()
            this.networkContentDiskCachePolicy = request.networkContentDiskCachePolicy

            this.bitmapConfig = request.bitmapConfig
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                this.colorSpace = request.colorSpace
            }
            @Suppress("DEPRECATION")
            this.preferQualityOverSpeed = request.preferQualityOverSpeed
            this.resizeSize = request.resizeSize
            this.resizeSizeResolver = request.resizeSizeResolver
            this.resizePrecisionDecider = request.resizePrecisionDecider
            this.resizeScale = request.resizeScale
            this.transformations = request.transformations
            this.disabledBitmapPool = request.disabledBitmapPool
            this.ignoreExifOrientation = request.ignoreExifOrientation
            this.bitmapResultDiskCachePolicy = request.bitmapResultDiskCachePolicy

            this.bitmapMemoryCachePolicy = request.bitmapMemoryCachePolicy
            this.disabledAnimationDrawable = request.disabledAnimationDrawable
            this.placeholderImage = request.placeholderImage
            this.errorImage = request.errorImage
            this.transition = request.transition
        }

        fun depth(depth: RequestDepth?): Builder =
            apply {
                this.depth = depth
            }

        fun depthFrom(from: String?): Builder =
            apply {
                if (from != null) {
                    setParameter(ImageRequest.REQUEST_DEPTH_FROM, from, null)
                } else {
                    removeParameter(ImageRequest.REQUEST_DEPTH_FROM)
                }
            }

        fun parameters(parameters: Parameters?): Builder =
            apply {
                this.parametersBuilder = parameters?.newBuilder()
            }

        /**
         * Set a parameter for this request.
         *
         * @see Parameters.Builder.set
         */
        @JvmOverloads
        fun setParameter(
            key: String,
            value: Any?,
            cacheKey: String? = value?.toString()
        ): Builder =
            apply {
                this.parametersBuilder = (this.parametersBuilder ?: Parameters.Builder()).apply {
                    set(
                        key,
                        value,
                        cacheKey
                    )
                }
            }

        /**
         * Remove a parameter from this request.
         *
         * @see Parameters.Builder.remove
         */
        fun removeParameter(key: String): Builder =
            apply {
                this.parametersBuilder?.remove(key)
            }

        fun httpHeaders(httpHeaders: HttpHeaders?): Builder =
            apply {
                this.httpHeaders = httpHeaders?.newBuilder()
            }

        /**
         * Add a header for any network operations performed by this request.
         */
        fun addHttpHeader(
            name: String,
            value: String
        ): Builder = apply {
            this.httpHeaders = (this.httpHeaders ?: HttpHeaders.Builder()).apply {
                add(name, value)
            }
        }

        /**
         * Set a header for any network operations performed by this request.
         */
        fun setHttpHeader(
            name: String,
            value: String
        ): Builder = apply {
            this.httpHeaders = (this.httpHeaders ?: HttpHeaders.Builder()).apply {
                set(name, value)
            }
        }

        /**
         * Remove all network headers with the key [name].
         */
        fun removeHttpHeader(name: String): Builder =
            apply {
                this.httpHeaders?.removeAll(name)
            }

        fun networkContentDiskCachePolicy(networkContentDiskCachePolicy: CachePolicy?): Builder =
            apply {
                this.networkContentDiskCachePolicy = networkContentDiskCachePolicy
            }

        fun bitmapResultDiskCachePolicy(bitmapResultDiskCachePolicy: CachePolicy?): Builder =
            apply {
                this.bitmapResultDiskCachePolicy = bitmapResultDiskCachePolicy
            }

        fun bitmapConfig(bitmapConfig: BitmapConfig?): Builder =
            apply {
                this.bitmapConfig = bitmapConfig
            }

        fun bitmapConfig(bitmapConfig: Bitmap.Config?): Builder =
            apply {
                this.bitmapConfig = if (bitmapConfig != null) BitmapConfig(bitmapConfig) else null
            }

        fun lowQualityBitmapConfig(): Builder =
            apply {
                this.bitmapConfig = BitmapConfig.LOW_QUALITY
            }

        fun middenQualityBitmapConfig(): Builder =
            apply {
                this.bitmapConfig = BitmapConfig.MIDDEN_QUALITY
            }

        fun highQualityBitmapConfig(): Builder =
            apply {
                this.bitmapConfig = BitmapConfig.HIGH_QUALITY
            }

        @RequiresApi(VERSION_CODES.O)
        fun colorSpace(colorSpace: ColorSpace?): Builder =
            apply {
                this.colorSpace = colorSpace
            }

        /**
         * From Android N (API 24), this is ignored.  The output will always be high quality.
         *
         * In {@link android.os.Build.VERSION_CODES#M} and below, if
         * inPreferQualityOverSpeed is set to true, the decoder will try to
         * decode the reconstructed image to a higher quality even at the
         * expense of the decoding speed. Currently the field only affects JPEG
         * decode, in the case of which a more accurate, but slightly slower,
         * IDCT method will be used instead.
         *
         * Applied to [android.graphics.BitmapFactory.Options.inPreferQualityOverSpeed]
         */
        @Deprecated("From Android N (API 24), this is ignored.  The output will always be high quality.")
        fun preferQualityOverSpeed(inPreferQualityOverSpeed: Boolean?): Builder =
            apply {
                if (VERSION.SDK_INT < VERSION_CODES.N) {
                    this.preferQualityOverSpeed = inPreferQualityOverSpeed
                }
            }

        fun resizeSize(size: Size?): Builder =
            apply {
                this.resizeSize = size
            }

        fun resizeSize(
            @Px width: Int,
            @Px height: Int
        ): Builder = apply {
            this.resizeSize = Size(width, height)
        }

        fun resizeSizeResolver(sizeResolver: SizeResolver?): Builder =
            apply {
                this.resizeSizeResolver = sizeResolver
            }

        fun resizePrecision(precisionDecider: PrecisionDecider?): Builder =
            apply {
                this.resizePrecisionDecider = precisionDecider
            }

        fun resizePrecision(precision: Precision?): Builder =
            apply {
                this.resizePrecisionDecider = precision?.let { FixedPrecisionDecider(it) }
            }

        fun resizeScale(scale: Scale?): Builder =
            apply {
                this.resizeScale = scale
            }

        fun transformations(transformations: List<Transformation>?): Builder =
            apply {
                this.transformations = transformations
            }

        fun transformations(vararg transformations: Transformation): Builder =
            apply {
                this.transformations = transformations.toList()
            }

        fun disabledBitmapPool(disabledBitmapPool: Boolean? = true): Builder =
            apply {
                this.disabledBitmapPool = disabledBitmapPool
            }

        fun ignoreExifOrientation(ignoreExifOrientation: Boolean? = true): Builder =
            apply {
                this.ignoreExifOrientation = ignoreExifOrientation
            }

        fun bitmapMemoryCachePolicy(bitmapMemoryCachePolicy: CachePolicy?): Builder =
            apply {
                this.bitmapMemoryCachePolicy = bitmapMemoryCachePolicy
            }

        fun disabledAnimationDrawable(disabledAnimationDrawable: Boolean? = true): Builder =
            apply {
                this.disabledAnimationDrawable = disabledAnimationDrawable
            }

        fun placeholder(placeholderImage: StateImage?): Builder =
            apply {
                this.placeholderImage = placeholderImage
            }

        fun placeholder(placeholderDrawable: Drawable?): Builder =
            apply {
                this.placeholderImage =
                    if (placeholderDrawable != null) StateImage.drawable(placeholderDrawable) else null
            }

        fun placeholder(@DrawableRes placeholderDrawableResId: Int?): Builder =
            apply {
                this.placeholderImage = if (placeholderDrawableResId != null) {
                    StateImage.drawableRes(placeholderDrawableResId)
                } else null
            }

        fun error(
            errorImage: StateImage?,
            configBlock: (ErrorStateImage.Builder.() -> Unit)? = null
        ): Builder = apply {
            this.errorImage = errorImage?.let {
                if (configBlock != null) {
                    ErrorStateImage.new(it, configBlock)
                } else {
                    it
                }
            }
        }

        fun error(
            errorDrawable: Drawable?,
            configBlock: (ErrorStateImage.Builder.() -> Unit)? = null
        ): Builder = apply {
            this.errorImage = errorDrawable?.let {
                if (configBlock != null) {
                    ErrorStateImage.new(StateImage.drawable(it), configBlock)
                } else {
                    StateImage.drawable(it)
                }
            }
        }

        fun error(
            errorDrawableResId: Int?,
            configBlock: (ErrorStateImage.Builder.() -> Unit)? = null
        ): Builder = apply {
            this.errorImage = errorDrawableResId?.let {
                if (configBlock != null) {
                    ErrorStateImage.new(StateImage.drawableRes(it), configBlock)
                } else {
                    StateImage.drawableRes(it)
                }
            }
        }

        fun transition(transition: Transition.Factory?): Builder =
            apply {
                this.transition = transition
            }

        fun crossfade(
            durationMillis: Int = CrossfadeDrawable.DEFAULT_DURATION,
            preferExactIntrinsicSize: Boolean = false
        ): Builder = apply {
            transition(CrossfadeTransition.Factory(durationMillis, preferExactIntrinsicSize))
        }

        @SuppressLint("NewApi")
        fun build(): ImageOptions = ImageOptionsImpl(
            depth = depth,
            parameters = parametersBuilder?.build(),
            httpHeaders = httpHeaders?.build(),
            networkContentDiskCachePolicy = networkContentDiskCachePolicy,
            bitmapResultDiskCachePolicy = bitmapResultDiskCachePolicy,
            bitmapConfig = bitmapConfig,
            colorSpace = if (VERSION.SDK_INT >= VERSION_CODES.O) colorSpace else null,
            preferQualityOverSpeed = preferQualityOverSpeed,
            resizeSize = resizeSize,
            resizeSizeResolver = resizeSizeResolver,
            resizePrecisionDecider = resizePrecisionDecider,
            resizeScale = resizeScale,
            transformations = transformations,
            disabledBitmapPool = disabledBitmapPool,
            ignoreExifOrientation = ignoreExifOrientation,
            bitmapMemoryCachePolicy = bitmapMemoryCachePolicy,
            disabledAnimationDrawable = disabledAnimationDrawable,
            placeholderImage = placeholderImage,
            errorImage = errorImage,
            transition = transition,
        )
    }

    class ImageOptionsImpl(
        override val depth: RequestDepth?,
        override val parameters: Parameters?,

        override val httpHeaders: HttpHeaders?,
        override val networkContentDiskCachePolicy: CachePolicy?,

        override val bitmapConfig: BitmapConfig?,
        override val colorSpace: ColorSpace?,
        @Deprecated("From Android N (API 24), this is ignored. The output will always be high quality.")
        override val preferQualityOverSpeed: Boolean?,
        override val resizeSize: Size?,
        override val resizeSizeResolver: SizeResolver?,
        override val resizePrecisionDecider: PrecisionDecider?,
        override val resizeScale: Scale?,
        override val transformations: List<Transformation>?,
        override val disabledBitmapPool: Boolean?,
        override val ignoreExifOrientation: Boolean?,
        override val bitmapResultDiskCachePolicy: CachePolicy?,
        override val disabledAnimationDrawable: Boolean?,
        override val bitmapMemoryCachePolicy: CachePolicy?,
        override val placeholderImage: StateImage?,
        override val errorImage: StateImage?,
        override val transition: Transition.Factory?,
    ) : ImageOptions
}