/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.panpf.sketch.request

import androidx.annotation.Px
import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.decode.Decoder
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.http.HttpHeaders
import com.github.panpf.sketch.http.isNotEmpty
import com.github.panpf.sketch.http.merged
import com.github.panpf.sketch.merged
import com.github.panpf.sketch.resize.FixedSizeResolver
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.resize.PrecisionDecider
import com.github.panpf.sketch.resize.Scale
import com.github.panpf.sketch.resize.ScaleDecider
import com.github.panpf.sketch.resize.SizeApplyToDrawHelper
import com.github.panpf.sketch.resize.SizeResolver
import com.github.panpf.sketch.stateimage.ErrorStateImage
import com.github.panpf.sketch.stateimage.StateImage
import com.github.panpf.sketch.transform.Transformation
import com.github.panpf.sketch.transition.Transition
import com.github.panpf.sketch.util.Size

const val DEPTH_FROM_KEY = "sketch#depth_from"

/**
 * Build and set the [ImageOptions]
 */
fun ImageOptions(
    configBlock: (ImageOptions.Builder.() -> Unit)? = null
): ImageOptions = ImageOptions.Builder().apply {
    configBlock?.invoke(this)
}.build()

/**
 * Stores parameters required to download, load, display images
 */
interface ImageOptions {

    /**
     * The processing depth of the request.
     */
    val depth: Depth?

    /**
     * where does this depth come from
     */
    val depthFrom: String?
        get() = parameters?.value(DEPTH_FROM_KEY)

    /**
     * A map of generic values that can be used to pass custom data to [Fetcher] and [Decoder].
     */
    val parameters: Parameters?


    /**
     * Set headers for http requests
     *
     * @see com.github.panpf.sketch.http.HurlStack.getResponse
     */
    val httpHeaders: HttpHeaders?

    /**
     * Http download cache policy
     *
     * @see com.github.panpf.sketch.fetch.HttpUriFetcher
     */
    val downloadCachePolicy: CachePolicy?


    /**
     * Lazy calculation of resize size. If resize size is null at runtime, size is calculated and assigned to size
     */
    val sizeResolver: SizeResolver?

    /**
     * Decide what Precision to use with [sizeResolver] to calculate the size of the final Bitmap
     */
    val precisionDecider: PrecisionDecider?

    /**
     * Which part of the original image to keep when [precisionDecider] returns [Precision.EXACTLY] or [Precision.SAME_ASPECT_RATIO]
     */
    val scaleDecider: ScaleDecider?

    /**
     * The list of [Transformation]s to be applied to this request
     */
    val transformations: List<Transformation>?

    /**
     * Ignore Orientation property in file Exif info
     *
     * @see com.github.panpf.sketch.decode.internal.appliedExifOrientation
     */
    val ignoreExifOrientation: Boolean?

    /**
     * Disk caching policy for Bitmaps affected by [sizeResolver] or [transformations]
     *
     * @see com.github.panpf.sketch.cache.internal.ResultCacheDecodeInterceptor
     */
    val resultCachePolicy: CachePolicy?


    /**
     * Placeholder image when loading
     */
    val placeholder: StateImage?

    /**
     * Image to display when uri is empty
     */
    val uriEmpty: StateImage?

    /**
     * Image to display when loading fails
     */
    val error: ErrorStateImage?

    /**
     * How the current image and the new image transition
     */
    val transitionFactory: Transition.Factory?

    /**
     * Disallow decode animation image, animations such as gif will only decode their first frame and return BitmapDrawable
     */
    val disallowAnimatedImage: Boolean?

    /**
     * Wrap the final [Drawable] use [ResizeDrawable] and resize, the size of [ResizeDrawable] is the same as [sizeResolver]
     */
    val sizeApplyToDraw: SizeApplyToDrawHelper?

    /**
     * Bitmap memory caching policy
     *
     * @see com.github.panpf.sketch.request.internal.MemoryCacheRequestInterceptor
     */
    val memoryCachePolicy: CachePolicy?


    /**
     * Components that are only valid for the current request
     */
    val componentRegistry: ComponentRegistry?


    /**
     * Create a new [ImageOptions.Builder] based on the current [ImageOptions].
     *
     * You can extend it with a trailing lambda function [configBlock]
     */
    fun newBuilder(
        configBlock: (Builder.() -> Unit)? = null
    ): Builder = Builder(this).apply {
        configBlock?.invoke(this)
    }

    /**
     * Create a new [ImageOptions] based on the current [ImageOptions].
     *
     * You can extend it with a trailing lambda function [configBlock]
     */
    fun newOptions(
        configBlock: (Builder.() -> Unit)? = null
    ): ImageOptions = Builder(this).apply {
        configBlock?.invoke(this)
    }.build()

    /**
     * Merge the current [ImageOptions] and the new [ImageOptions] into a new [ImageOptions]. Currently [ImageOptions] takes precedence
     */
    fun merged(
        options: ImageOptions?
    ): ImageOptions {
        if (options == null) return this
        return Builder(this).apply {
            merge(options)
        }.build()
    }

    /**
     * Returns true if all properties are empty
     */
    fun isEmpty(): Boolean =
        depth == null
                && parameters?.isEmpty() != false
                && httpHeaders?.isEmpty() != false
                && downloadCachePolicy == null
                && sizeResolver == null
                && precisionDecider == null
                && scaleDecider == null
                && transformations == null
                && ignoreExifOrientation == null
                && resultCachePolicy == null
                && placeholder == null
                && uriEmpty == null
                && error == null
                && transitionFactory == null
                && disallowAnimatedImage == null
                && sizeApplyToDraw == null
                && memoryCachePolicy == null
                && componentRegistry == null

    class Builder {

        private var depth: Depth? = null
        private var parametersBuilder: Parameters.Builder? = null

        private var httpHeadersBuilder: HttpHeaders.Builder? = null
        private var downloadCachePolicy: CachePolicy? = null

        private var sizeResolver: SizeResolver? = null
        private var precisionDecider: PrecisionDecider? = null
        private var scaleDecider: ScaleDecider? = null
        private var transformations: MutableList<Transformation>? = null
        private var ignoreExifOrientation: Boolean? = null
        private var resultCachePolicy: CachePolicy? = null

        private var placeholder: StateImage? = null
        private var uriEmpty: StateImage? = null
        private var error: ErrorStateImage? = null
        private var transitionFactory: Transition.Factory? = null
        private var disallowAnimatedImage: Boolean? = null
        private var sizeApplyToDraw: SizeApplyToDrawHelper? = null
        private var memoryCachePolicy: CachePolicy? = null

        private var componentRegistry: ComponentRegistry? = null

        constructor()

        internal constructor(request: ImageOptions) {
            this.depth = request.depth
            this.parametersBuilder = request.parameters?.newBuilder()

            this.httpHeadersBuilder = request.httpHeaders?.newBuilder()
            this.downloadCachePolicy = request.downloadCachePolicy

            this.sizeResolver = request.sizeResolver
            this.precisionDecider = request.precisionDecider
            this.scaleDecider = request.scaleDecider
            this.transformations = request.transformations?.toMutableList()
            this.ignoreExifOrientation = request.ignoreExifOrientation
            this.resultCachePolicy = request.resultCachePolicy

            this.placeholder = request.placeholder
            this.uriEmpty = request.uriEmpty
            this.error = request.error
            this.transitionFactory = request.transitionFactory
            this.disallowAnimatedImage = request.disallowAnimatedImage
            this.sizeApplyToDraw = request.sizeApplyToDraw
            this.memoryCachePolicy = request.memoryCachePolicy

            this.componentRegistry = request.componentRegistry
        }


        /**
         * Set the requested depth
         */
        fun depth(depth: Depth?, depthFrom: String? = null): Builder = apply {
            this.depth = depth
            if (depth != null && depthFrom != null) {
                setParameter(DEPTH_FROM_KEY, depthFrom, null)
            } else {
                removeParameter(DEPTH_FROM_KEY)
            }
        }


        /**
         * Bulk set parameters for this request
         */
        fun parameters(parameters: Parameters?): Builder = apply {
            this.parametersBuilder = parameters?.newBuilder()
        }

        /**
         * Set a parameter for this request.
         */
        fun setParameter(
            key: String, value: Any?, cacheKey: String? = value?.toString()
        ): Builder = apply {
            this.parametersBuilder = (this.parametersBuilder ?: Parameters.Builder()).apply {
                set(key, value, cacheKey)
            }
        }

        /**
         * Remove a parameter from this request.
         */
        fun removeParameter(key: String): Builder = apply {
            this.parametersBuilder?.remove(key)
        }


        /**
         * Bulk set headers for any network request for this request
         */
        fun httpHeaders(httpHeaders: HttpHeaders?): Builder = apply {
            this.httpHeadersBuilder = httpHeaders?.newBuilder()
        }

        /**
         * Add a header for any network operations performed by this request.
         */
        fun addHttpHeader(name: String, value: String): Builder = apply {
            this.httpHeadersBuilder = (this.httpHeadersBuilder ?: HttpHeaders.Builder()).apply {
                add(name, value)
            }
        }

        /**
         * Set a header for any network operations performed by this request.
         */
        fun setHttpHeader(name: String, value: String): Builder = apply {
            this.httpHeadersBuilder = (this.httpHeadersBuilder ?: HttpHeaders.Builder()).apply {
                set(name, value)
            }
        }

        /**
         * Remove all network headers with the key [name].
         */
        fun removeHttpHeader(name: String): Builder = apply {
            this.httpHeadersBuilder?.removeAll(name)
        }

        /**
         * Set http download cache policy
         */
        fun downloadCachePolicy(cachePolicy: CachePolicy?): Builder = apply {
            this.downloadCachePolicy = cachePolicy
        }

        /**
         * Set how to resize image
         *
         * @param size Expected Bitmap size Resolver
         * @param precision precision of size, default is [Precision.LESS_PIXELS]
         * @param scale Which part of the original image to keep when [precision] is
         * [Precision.EXACTLY] or [Precision.SAME_ASPECT_RATIO], default is [Scale.CENTER_CROP]
         */
        fun resize(
            size: SizeResolver?,
            precision: PrecisionDecider? = null,
            scale: ScaleDecider? = null
        ): Builder = apply {
            this.sizeResolver = size
            this.precisionDecider = precision
            this.scaleDecider = scale
        }

        /**
         * Set how to resize image
         *
         * @param size Expected Bitmap size
         * @param precision precision of size, default is [Precision.LESS_PIXELS]
         * @param scale Which part of the original image to keep when [precision] is
         * [Precision.EXACTLY] or [Precision.SAME_ASPECT_RATIO], default is [Scale.CENTER_CROP]
         */
        fun resize(
            size: Size,
            precision: Precision? = null,
            scale: Scale? = null
        ): Builder = resize(
            SizeResolver(size),
            precision?.let { PrecisionDecider(it) },
            scale?.let { ScaleDecider(it) }
        )

        /**
         * Set how to resize image
         *
         * @param width Expected Bitmap width
         * @param height Expected Bitmap height
         * @param precision precision of size, default is [Precision.LESS_PIXELS]
         * @param scale Which part of the original image to keep when [precision] is
         * [Precision.EXACTLY] or [Precision.SAME_ASPECT_RATIO], default is [Scale.CENTER_CROP]
         */
        fun resize(
            @Px width: Int,
            @Px height: Int,
            precision: Precision? = null,
            scale: Scale? = null
        ): Builder = resize(
            FixedSizeResolver(width, height),
            precision?.let { PrecisionDecider(it) },
            scale?.let { ScaleDecider(it) }
        )

        /**
         * Set the [SizeResolver] to lazy resolve the requested size.
         */
        fun size(sizeResolver: SizeResolver?): Builder = apply {
            this.sizeResolver = sizeResolver
        }

        /**
         * Set the resize size
         */
        fun size(size: Size): Builder = size(SizeResolver(size))

        /**
         * Set the resize size
         */
        fun size(@Px width: Int, @Px height: Int): Builder =
            size(FixedSizeResolver(width, height))

        /**
         * Set the resize precision, default is [Precision.LESS_PIXELS]
         */
        fun precision(precisionDecider: PrecisionDecider?): Builder = apply {
            this.precisionDecider = precisionDecider
        }

        /**
         * Set the resize precision, default is [Precision.LESS_PIXELS]
         */
        fun precision(precision: Precision): Builder =
            precision(PrecisionDecider(precision))

        /**
         * Set the resize scale, default is [Scale.CENTER_CROP]
         */
        fun scale(scaleDecider: ScaleDecider?): Builder = apply {
            this.scaleDecider = scaleDecider
        }

        /**
         * Set the resize scale, default is [Scale.CENTER_CROP]
         */
        fun scale(scale: Scale): Builder = scale(ScaleDecider(scale))

        /**
         * Set the list of [Transformation]s to be applied to this request.
         */
        fun transformations(transformations: List<Transformation>?): Builder = apply {
            this.transformations = transformations?.toMutableList()
        }

        /**
         * Set the list of [Transformation]s to be applied to this request.
         */
        fun transformations(vararg transformations: Transformation): Builder =
            transformations(transformations.toList())

        /**
         * Append the list of [Transformation]s to be applied to this request.
         */
        fun addTransformations(transformations: List<Transformation>): Builder = apply {
            val filterTransformations = transformations.filter { newTransformation ->
                this.transformations?.find { it.key == newTransformation.key } == null
            }
            this.transformations = (this.transformations ?: mutableListOf()).apply {
                addAll(filterTransformations)
            }
        }

        /**
         * Append the list of [Transformation]s to be applied to this request.
         */
        fun addTransformations(vararg transformations: Transformation): Builder =
            addTransformations(transformations.toList())

        /**
         * Bulk remove from current [Transformation] list
         */
        fun removeTransformations(removeTransformations: List<Transformation>): Builder =
            apply {
                this.transformations = this.transformations?.filter { oldTransformation ->
                    removeTransformations.find { it.key == oldTransformation.key } == null
                }?.toMutableList()
            }

        /**
         * Bulk remove from current [Transformation] list
         */
        fun removeTransformations(vararg removeTransformations: Transformation): Builder =
            removeTransformations(removeTransformations.toList())

        /**
         * Set ignore Orientation property in file Exif info
         */
        fun ignoreExifOrientation(ignore: Boolean? = true): Builder = apply {
            this.ignoreExifOrientation = ignore
        }

        /**
         * Set disk caching policy for Bitmaps affected by [size] or [transformations]
         */
        fun resultCachePolicy(cachePolicy: CachePolicy?): Builder =
            apply {
                this.resultCachePolicy = cachePolicy
            }


        /**
         * Set placeholder image when loading
         */
        fun placeholder(stateImage: StateImage?): Builder = apply {
            this.placeholder = stateImage
        }

        /**
         * Set placeholder image when uri is empty
         */
        fun uriEmpty(stateImage: StateImage?): Builder = apply {
            this.uriEmpty = stateImage
        }

        /**
         * Set image to display when loading fails.
         *
         * You can also set image of different error types via the trailing lambda function
         */
        fun error(
            defaultStateImage: StateImage?,
            configBlock: (ErrorStateImage.Builder.() -> Unit)? = null
        ): Builder = apply {
            this.error = ErrorStateImage(defaultStateImage, configBlock)
                .takeIf { it.stateList.isNotEmpty() }
        }

        /**
         * Set image to display when loading fails.
         *
         * You can also set image of different error types via the trailing lambda function
         */
        fun error(
            configBlock: (ErrorStateImage.Builder.() -> Unit)? = null
        ): Builder = error(null, configBlock)

        /**
         * Set the transition between the current image and the new image
         */
        fun transitionFactory(transitionFactory: Transition.Factory?): Builder = apply {
            this.transitionFactory = transitionFactory
        }

        /**
         * Set disallow decode animation image, animations such as gif will only decode their first frame and return BitmapDrawable
         */
        fun disallowAnimatedImage(disabled: Boolean? = true): Builder = apply {
            this.disallowAnimatedImage = disabled
        }

        /**
         * TODO
         * Set wrap the final [Drawable] use [ResizeDrawable] and resize, the size of [ResizeDrawable] is the same as [size]
         */
        fun sizeApplyToDraw(apply: SizeApplyToDrawHelper?): Builder = apply {
            this.sizeApplyToDraw = apply
        }

        /**
         * Set bitmap memory caching policy
         */
        fun memoryCachePolicy(cachePolicy: CachePolicy?): Builder = apply {
            this.memoryCachePolicy = cachePolicy
        }

        /**
         * Set the [ComponentRegistry]
         */
        fun components(components: ComponentRegistry?): Builder = apply {
            this.componentRegistry = components
        }

        /**
         * Build and set the [ComponentRegistry]
         */
        fun components(configBlock: (ComponentRegistry.Builder.() -> Unit)): Builder =
            components(ComponentRegistry.Builder().apply(configBlock).build())


        /**
         * Merge the specified [ImageOptions] into the current [Builder]. Currently [Builder] takes precedence
         */
        fun merge(options: ImageOptions?): Builder = apply {
            if (options == null) return@apply

            if (this.depth == null) {
                this.depth = options.depth
            }
            options.parameters?.let {
                parametersBuilder = parametersBuilder?.build().merged(it)?.newBuilder()
            }

            options.httpHeaders?.let {
                httpHeadersBuilder = httpHeadersBuilder?.build().merged(it)?.newBuilder()
            }
            if (this.downloadCachePolicy == null) {
                this.downloadCachePolicy = options.downloadCachePolicy
            }

            if (this.sizeResolver == null) {
                this.sizeResolver = options.sizeResolver
            }
            if (this.precisionDecider == null) {
                this.precisionDecider = options.precisionDecider
            }
            if (this.scaleDecider == null) {
                this.scaleDecider = options.scaleDecider
            }
            options.transformations?.takeIf { it.isNotEmpty() }?.let {
                addTransformations(it)
            }
            if (this.ignoreExifOrientation == null) {
                this.ignoreExifOrientation = options.ignoreExifOrientation
            }
            if (this.resultCachePolicy == null) {
                this.resultCachePolicy = options.resultCachePolicy
            }

            if (this.placeholder == null) {
                this.placeholder = options.placeholder
            }
            if (this.uriEmpty == null) {
                this.uriEmpty = options.uriEmpty
            }
            if (this.error == null) {
                this.error = options.error
            }
            if (this.transitionFactory == null) {
                this.transitionFactory = options.transitionFactory
            }
            if (this.disallowAnimatedImage == null) {
                this.disallowAnimatedImage = options.disallowAnimatedImage
            }
            if (this.sizeApplyToDraw == null) {
                this.sizeApplyToDraw = options.sizeApplyToDraw
            }
            if (this.memoryCachePolicy == null) {
                this.memoryCachePolicy = options.memoryCachePolicy
            }

            componentRegistry = componentRegistry.merged(options.componentRegistry)
        }


        fun build(): ImageOptions = ImageOptionsImpl(
            depth = depth,
            parameters = parametersBuilder?.build()?.takeIf { it.isNotEmpty() },
            httpHeaders = httpHeadersBuilder?.build()?.takeIf { it.isNotEmpty() },
            downloadCachePolicy = downloadCachePolicy,
            resultCachePolicy = resultCachePolicy,
            sizeResolver = sizeResolver,
            precisionDecider = precisionDecider,
            scaleDecider = scaleDecider,
            transformations = transformations?.takeIf { it.isNotEmpty() },
            ignoreExifOrientation = ignoreExifOrientation,
            placeholder = placeholder,
            uriEmpty = uriEmpty,
            error = error,
            transitionFactory = transitionFactory,
            disallowAnimatedImage = disallowAnimatedImage,
            sizeApplyToDraw = sizeApplyToDraw,
            memoryCachePolicy = memoryCachePolicy,
            componentRegistry = componentRegistry,
        )
    }

    class ImageOptionsImpl(
        override val depth: Depth?,
        override val parameters: Parameters?,

        override val httpHeaders: HttpHeaders?,
        override val downloadCachePolicy: CachePolicy?,

        override val sizeResolver: SizeResolver?,
        override val precisionDecider: PrecisionDecider?,
        override val scaleDecider: ScaleDecider?,
        override val transformations: List<Transformation>?,
        override val ignoreExifOrientation: Boolean?,
        override val resultCachePolicy: CachePolicy?,
        override val placeholder: StateImage?,
        override val uriEmpty: StateImage?,
        override val error: ErrorStateImage?,
        override val transitionFactory: Transition.Factory?,
        override val disallowAnimatedImage: Boolean?,
        override val sizeApplyToDraw: SizeApplyToDrawHelper?,
        override val memoryCachePolicy: CachePolicy?,
        override val componentRegistry: ComponentRegistry?,
    ) : ImageOptions {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ImageOptionsImpl
            if (depth != other.depth) return false
            if (parameters != other.parameters) return false
            if (httpHeaders != other.httpHeaders) return false
            if (downloadCachePolicy != other.downloadCachePolicy) return false
            if (sizeResolver != other.sizeResolver) return false
            if (precisionDecider != other.precisionDecider) return false
            if (scaleDecider != other.scaleDecider) return false
            if (transformations != other.transformations) return false
            if (ignoreExifOrientation != other.ignoreExifOrientation) return false
            if (resultCachePolicy != other.resultCachePolicy) return false
            if (placeholder != other.placeholder) return false
            if (uriEmpty != other.uriEmpty) return false
            if (error != other.error) return false
            if (transitionFactory != other.transitionFactory) return false
            if (disallowAnimatedImage != other.disallowAnimatedImage) return false
            if (sizeApplyToDraw != other.sizeApplyToDraw) return false
            if (memoryCachePolicy != other.memoryCachePolicy) return false
            if (componentRegistry != other.componentRegistry) return false
            return true
        }

        override fun hashCode(): Int {
            var result = depth?.hashCode() ?: 0
            result = 31 * result + (parameters?.hashCode() ?: 0)
            result = 31 * result + (httpHeaders?.hashCode() ?: 0)
            result = 31 * result + (downloadCachePolicy?.hashCode() ?: 0)
            result = 31 * result + (sizeResolver?.hashCode() ?: 0)
            result = 31 * result + (precisionDecider?.hashCode() ?: 0)
            result = 31 * result + (scaleDecider?.hashCode() ?: 0)
            result = 31 * result + (transformations?.hashCode() ?: 0)
            result = 31 * result + (ignoreExifOrientation?.hashCode() ?: 0)
            result = 31 * result + (resultCachePolicy?.hashCode() ?: 0)
            result = 31 * result + (placeholder?.hashCode() ?: 0)
            result = 31 * result + (uriEmpty?.hashCode() ?: 0)
            result = 31 * result + (error?.hashCode() ?: 0)
            result = 31 * result + (transitionFactory?.hashCode() ?: 0)
            result = 31 * result + (disallowAnimatedImage?.hashCode() ?: 0)
            result = 31 * result + (sizeApplyToDraw?.hashCode() ?: 0)
            result = 31 * result + (memoryCachePolicy?.hashCode() ?: 0)
            result = 31 * result + (componentRegistry?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String {
            return buildString {
                append("ImageOptionsImpl(")
                append("depth=$depth, ")
                append("parameters=$parameters, ")
                append("httpHeaders=$httpHeaders, ")
                append("downloadCachePolicy=$downloadCachePolicy, ")
                append("sizeResolver=$sizeResolver, ")
                append("precisionDecider=$precisionDecider, ")
                append("scaleDecider=$scaleDecider, ")
                append("transformations=$transformations, ")
                append("ignoreExifOrientation=$ignoreExifOrientation, ")
                append("resultCachePolicy=$resultCachePolicy, ")
                append("placeholder=$placeholder, ")
                append("uriEmpty=$uriEmpty, ")
                append("error=$error, ")
                append("transition=$transitionFactory, ")
                append("disallowAnimatedImage=$disallowAnimatedImage, ")
                append("sizeApplyToDraw=$sizeApplyToDraw")
                append("memoryCachePolicy=$memoryCachePolicy, ")
                append("componentRegistry=$componentRegistry, ")
                append(")")
            }
        }
    }
}

/**
 * Returns true as long as any property is not empty
 */
fun ImageOptions.isNotEmpty(): Boolean = !isEmpty()