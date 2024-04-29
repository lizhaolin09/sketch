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
package com.github.panpf.sketch.core.android.test.request

import android.R.color
import android.R.drawable
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.Config.RGB_565
import android.graphics.Color
import android.graphics.ColorSpace
import android.graphics.ColorSpace.Named.ACES
import android.graphics.ColorSpace.Named.ADOBE_RGB
import android.graphics.ColorSpace.Named.BT709
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.cache.CachePolicy.DISABLED
import com.github.panpf.sketch.cache.CachePolicy.ENABLED
import com.github.panpf.sketch.cache.CachePolicy.READ_ONLY
import com.github.panpf.sketch.cache.CachePolicy.WRITE_ONLY
import com.github.panpf.sketch.decode.BitmapConfig
import com.github.panpf.sketch.decode.internal.BitmapFactoryDecoder
import com.github.panpf.sketch.fetch.HttpUriFetcher
import com.github.panpf.sketch.http.HttpHeaders
import com.github.panpf.sketch.images.MyImages
import com.github.panpf.sketch.request.DefaultLifecycleResolver
import com.github.panpf.sketch.request.Depth.LOCAL
import com.github.panpf.sketch.request.Depth.NETWORK
import com.github.panpf.sketch.request.GlobalTargetLifecycle
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.LifecycleResolver
import com.github.panpf.sketch.request.Listener
import com.github.panpf.sketch.request.Parameters
import com.github.panpf.sketch.request.ProgressListener
import com.github.panpf.sketch.request.bitmapConfig
import com.github.panpf.sketch.request.colorSpace
import com.github.panpf.sketch.request.error
import com.github.panpf.sketch.request.get
import com.github.panpf.sketch.request.internal.CombinedListener
import com.github.panpf.sketch.request.internal.CombinedProgressListener
import com.github.panpf.sketch.request.placeholder
import com.github.panpf.sketch.request.preferQualityOverSpeed
import com.github.panpf.sketch.request.resizeOnDraw
import com.github.panpf.sketch.request.uriEmpty
import com.github.panpf.sketch.resize.FixedPrecisionDecider
import com.github.panpf.sketch.resize.FixedScaleDecider
import com.github.panpf.sketch.resize.FixedSizeResolver
import com.github.panpf.sketch.resize.LongImageClipPrecisionDecider
import com.github.panpf.sketch.resize.LongImageStartCropScaleDecider
import com.github.panpf.sketch.resize.Precision.EXACTLY
import com.github.panpf.sketch.resize.Precision.LESS_PIXELS
import com.github.panpf.sketch.resize.Precision.SAME_ASPECT_RATIO
import com.github.panpf.sketch.resize.Scale.CENTER_CROP
import com.github.panpf.sketch.resize.Scale.END_CROP
import com.github.panpf.sketch.resize.Scale.FILL
import com.github.panpf.sketch.resize.Scale.START_CROP
import com.github.panpf.sketch.resize.internal.DisplaySizeResolver
import com.github.panpf.sketch.resize.internal.ViewSizeResolver
import com.github.panpf.sketch.state.ColorStateImage
import com.github.panpf.sketch.state.CurrentStateImage
import com.github.panpf.sketch.state.DrawableStateImage
import com.github.panpf.sketch.state.ErrorStateImage
import com.github.panpf.sketch.state.IconStateImage
import com.github.panpf.sketch.state.MemoryCacheStateImage
import com.github.panpf.sketch.state.ThumbnailMemoryCacheStateImage
import com.github.panpf.sketch.state.uriEmptyError
import com.github.panpf.sketch.test.utils.TestDecodeInterceptor
import com.github.panpf.sketch.test.utils.TestDecoder
import com.github.panpf.sketch.test.utils.TestFetcher
import com.github.panpf.sketch.test.utils.TestListenerImageView
import com.github.panpf.sketch.test.utils.TestOptionsImageView
import com.github.panpf.sketch.test.utils.TestRequestInterceptor
import com.github.panpf.sketch.test.utils.TestTarget
import com.github.panpf.sketch.test.utils.getTestContext
import com.github.panpf.sketch.test.utils.getTestContextAndNewSketch
import com.github.panpf.sketch.test.utils.target
import com.github.panpf.sketch.transform.BlurTransformation
import com.github.panpf.sketch.transform.CircleCropTransformation
import com.github.panpf.sketch.transform.RotateTransformation
import com.github.panpf.sketch.transform.RoundedCornersTransformation
import com.github.panpf.sketch.transition.CrossfadeTransition
import com.github.panpf.sketch.util.ColorDrawableEqualizer
import com.github.panpf.sketch.util.IntColor
import com.github.panpf.sketch.util.ResColor
import com.github.panpf.sketch.util.Size
import com.github.panpf.sketch.util.asOrThrow
import com.github.panpf.sketch.util.getEqualityDrawableCompat
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageRequestTest {

    @Test
    fun testFun() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest(context1, uriString1).apply {
            Assert.assertSame(context1, this.context)
            Assert.assertEquals("asset://sample.jpeg", uriString)
            Assert.assertNull(this.listener)
            Assert.assertNull(this.progressListener)
            Assert.assertNull(this.target)
            Assert.assertEquals(
                DefaultLifecycleResolver(LifecycleResolver(GlobalTargetLifecycle)),
                this.lifecycleResolver
            )

            Assert.assertEquals(NETWORK, this.depth)
            Assert.assertNull(this.parameters)
            Assert.assertNull(this.httpHeaders)
            Assert.assertEquals(ENABLED, this.downloadCachePolicy)
            Assert.assertNull(this.bitmapConfig)
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                Assert.assertNull(this.colorSpace)
            }
            Assert.assertFalse(this.preferQualityOverSpeed)
            Assert.assertEquals(DisplaySizeResolver(context1), this.sizeResolver)
            Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), this.precisionDecider)
            Assert.assertEquals(FixedScaleDecider(CENTER_CROP), this.scaleDecider)
            Assert.assertNull(this.transformations)
            Assert.assertEquals(ENABLED, this.resultCachePolicy)
            Assert.assertNull(this.placeholder)
            Assert.assertNull(this.uriEmpty)
            Assert.assertNull(this.error)
            Assert.assertNull(this.transitionFactory)
            Assert.assertFalse(this.disallowAnimatedImage)
            Assert.assertFalse(this.resizeOnDraw)
            Assert.assertEquals(ENABLED, this.memoryCachePolicy)
        }
    }

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    @Test
    fun testNewBuilder() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri

        ImageRequest(context1, uriString1).newBuilder().build().apply {
            Assert.assertEquals(NETWORK, depth)
        }
        ImageRequest(context1, uriString1).newBuilder {
            depth(LOCAL)
        }.build().apply {
            Assert.assertEquals(LOCAL, depth)
        }
        ImageRequest(context1, uriString1).newBuilder {
            depth(LOCAL)
        }.build().apply {
            Assert.assertEquals(LOCAL, depth)
        }

        ImageRequest(context1, uriString1).newRequest().apply {
            Assert.assertEquals(NETWORK, depth)
        }
        ImageRequest(context1, uriString1).newRequest {
            depth(LOCAL)
        }.apply {
            Assert.assertEquals(LOCAL, depth)
        }
        ImageRequest(context1, uriString1).newRequest {
            depth(LOCAL)
        }.apply {
            Assert.assertEquals(LOCAL, depth)
        }

        ImageRequest(context1, uriString1).newBuilder().build().apply {
            Assert.assertEquals(NETWORK, depth)
            Assert.assertNull(listener)
            Assert.assertNull(progressListener)
        }
        ImageRequest(context1, uriString1).newBuilder {
            depth(LOCAL)
            listener(
                onStart = { request: ImageRequest ->

                },
                onCancel = { request: ImageRequest ->

                },
                onError = { request: ImageRequest, result: ImageResult.Error ->

                },
                onSuccess = { request: ImageRequest, result: ImageResult.Success ->

                },
            )
            progressListener { _, _ ->

            }
        }.build().apply {
            Assert.assertEquals(LOCAL, depth)
            Assert.assertNotNull(listener)
            Assert.assertNotNull(progressListener)
        }

        ImageRequest(context1, uriString1).newRequest().apply {
            Assert.assertEquals(NETWORK, depth)
            Assert.assertNull(listener)
            Assert.assertNull(progressListener)
        }
        ImageRequest(context1, uriString1).newRequest {
            depth(LOCAL)
            listener(
                onStart = { request: ImageRequest ->

                },
                onCancel = { request: ImageRequest ->

                },
                onError = { request: ImageRequest, result: ImageResult.Error ->

                },
                onSuccess = { request: ImageRequest, result: ImageResult.Success ->

                },
            )
            progressListener { _, _ ->

            }
        }.apply {
            Assert.assertEquals(LOCAL, depth)
            Assert.assertNotNull(listener)
            Assert.assertNotNull(progressListener)
        }
    }

    @Test
    fun testContext() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest(context1, uriString1).apply {
            Assert.assertEquals(context1, context)
            Assert.assertNotEquals(context1, context.applicationContext)
        }
    }

    @Test
    fun testTarget() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        val imageView = TestOptionsImageView(context1)

        ImageRequest(context1, uriString1).apply {
            Assert.assertNull(target)
        }

        ImageRequest(context1, uriString1) {
            target(TestTarget())
        }.apply {
            Assert.assertTrue(target is TestTarget)
        }

        ImageRequest(imageView, uriString1) {
            target(TestTarget())
            target(null)
        }.apply {
            Assert.assertNull(target)
        }

        ImageRequest(context1, uriString1) {
            target(onStart = { _, _ -> }, onSuccess = { _, _ -> }, onError = { _, _ -> })
        }.apply {
            Assert.assertNotNull(target)
            Assert.assertEquals(ENABLED, memoryCachePolicy)
        }
        ImageRequest(context1, uriString1) {
            target(onStart = { _, _ -> })
        }.apply {
            Assert.assertNotNull(target)
            Assert.assertEquals(ENABLED, memoryCachePolicy)
        }
        ImageRequest(context1, uriString1) {
            target(onSuccess = { _, _ -> })
        }.apply {
            Assert.assertNotNull(target)
            Assert.assertEquals(ENABLED, memoryCachePolicy)
        }
        ImageRequest(context1, uriString1) {
            target(onError = { _, _ -> })
        }.apply {
            Assert.assertNotNull(target)
            Assert.assertEquals(ENABLED, memoryCachePolicy)
        }
    }

    @Test
    fun testLifecycle() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        var lifecycle1: Lifecycle? = null
        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle
                get() = lifecycle1!!
        }
        lifecycle1 = LifecycleRegistry(lifecycleOwner)

        ImageRequest(context1, uriString1).apply {
            Assert.assertEquals(
                DefaultLifecycleResolver(LifecycleResolver(GlobalTargetLifecycle)),
                this.lifecycleResolver
            )
        }

        // TODO test lifecycle
    }

    @Test
    fun testDefinedOptions() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri

        ImageRequest(context1, uriString1).apply {
            Assert.assertEquals(ImageOptions(), definedOptions)
        }

        ImageRequest(context1, uriString1) {
            size(100, 50)
            addTransformations(CircleCropTransformation())
            crossfade()
        }.apply {
            Assert.assertEquals(ImageOptions {
                size(100, 50)
                addTransformations(CircleCropTransformation())
                crossfade()
            }, definedOptions)
        }
    }

    @Test
    fun testDefault() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri

        ImageRequest(context1, uriString1).apply {
            Assert.assertNull(defaultOptions)
        }

        val options = ImageOptions {
            size(100, 50)
            addTransformations(CircleCropTransformation())
            crossfade()
        }
        ImageRequest(context1, uriString1) {
            default(options)
        }.apply {
            Assert.assertSame(options, defaultOptions)
        }
    }

    @Test
    fun testMerge() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertEquals(NETWORK, depth)
                Assert.assertNull(parameters)
            }

            merge(ImageOptions {
                size(100, 50)
                memoryCachePolicy(DISABLED)
                addTransformations(CircleCropTransformation())
                crossfade()
            })
            build().apply {
                Assert.assertEquals(FixedSizeResolver(100, 50), sizeResolver)
                Assert.assertEquals(DISABLED, memoryCachePolicy)
                Assert.assertEquals(listOf(CircleCropTransformation()), transformations)
                Assert.assertEquals(CrossfadeTransition.Factory(), transitionFactory)
            }

            merge(ImageOptions {
                memoryCachePolicy(READ_ONLY)
            })
            build().apply {
                Assert.assertEquals(FixedSizeResolver(100, 50), sizeResolver)
                Assert.assertEquals(DISABLED, memoryCachePolicy)
                Assert.assertEquals(listOf(CircleCropTransformation()), transformations)
                Assert.assertEquals(CrossfadeTransition.Factory(), transitionFactory)
            }
        }
    }

    @Test
    fun testDepth() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest(context1, uriString1).apply {
            Assert.assertEquals(NETWORK, depth)
            Assert.assertNull(depthFrom)
        }

        ImageRequest(context1, uriString1) {
            depth(LOCAL)
        }.apply {
            Assert.assertEquals(LOCAL, depth)
            Assert.assertNull(depthFrom)
        }

        ImageRequest(context1, uriString1) {
            depth(null)
        }.apply {
            Assert.assertEquals(NETWORK, depth)
            Assert.assertNull(depthFrom)
        }

        ImageRequest(context1, uriString1) {
            depth(LOCAL, null)
        }.apply {
            Assert.assertEquals(LOCAL, depth)
            Assert.assertNull(depthFrom)
        }

        ImageRequest(context1, uriString1) {
            depth(null, "TestDepthFrom")
        }.apply {
            Assert.assertEquals(NETWORK, depth)
            Assert.assertNull(depthFrom)
        }

        ImageRequest(context1, uriString1) {
            depth(LOCAL, "TestDepthFrom")
        }.apply {
            Assert.assertEquals(LOCAL, depth)
            Assert.assertEquals("TestDepthFrom", depthFrom)
        }
    }

    @Test
    fun testParameters() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertNull(parameters)
            }

            /* parameters() */
            parameters(Parameters())
            build().apply {
                Assert.assertNull(parameters)
            }

            parameters(Parameters.Builder().set("key1", "value1").build())
            build().apply {
                Assert.assertEquals(1, parameters?.size)
                Assert.assertEquals("value1", parameters?.get("key1"))
            }

            parameters(null)
            build().apply {
                Assert.assertNull(parameters)
            }

            /* setParameter(), removeParameter() */
            setParameter("key1", "value1")
            setParameter("key2", "value2", "value2")
            build().apply {
                Assert.assertEquals(2, parameters?.size)
                Assert.assertEquals("value1", parameters?.get("key1"))
                Assert.assertEquals("value2", parameters?.get("key2"))
            }

            setParameter("key2", "value2.1", null)
            build().apply {
                Assert.assertEquals(2, parameters?.size)
                Assert.assertEquals("value1", parameters?.get("key1"))
                Assert.assertEquals("value2.1", parameters?.get("key2"))
            }

            removeParameter("key2")
            build().apply {
                Assert.assertEquals(1, parameters?.size)
                Assert.assertEquals("value1", parameters?.get("key1"))
            }

            removeParameter("key1")
            build().apply {
                Assert.assertNull(parameters)
            }
        }
    }

    @Test
    fun testHttpHeaders() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertNull(httpHeaders)
            }

            /* httpHeaders() */
            httpHeaders(HttpHeaders())
            build().apply {
                Assert.assertNull(httpHeaders)
            }

            httpHeaders(HttpHeaders.Builder().set("key1", "value1").build())
            build().apply {
                Assert.assertEquals(1, httpHeaders?.size)
                Assert.assertEquals("value1", httpHeaders?.getSet("key1"))
            }

            httpHeaders(null)
            build().apply {
                Assert.assertNull(httpHeaders)
            }

            /* setHttpHeader(), addHttpHeader(), removeHttpHeader() */
            setHttpHeader("key1", "value1")
            setHttpHeader("key2", "value2")
            addHttpHeader("key3", "value3")
            addHttpHeader("key3", "value3.1")
            build().apply {
                Assert.assertEquals(4, httpHeaders?.size)
                Assert.assertEquals(2, httpHeaders?.setSize)
                Assert.assertEquals(2, httpHeaders?.addSize)
                Assert.assertEquals("value1", httpHeaders?.getSet("key1"))
                Assert.assertEquals("value2", httpHeaders?.getSet("key2"))
                Assert.assertEquals(listOf("value3", "value3.1"), httpHeaders?.getAdd("key3"))
            }

            setHttpHeader("key2", "value2.1")
            build().apply {
                Assert.assertEquals(4, httpHeaders?.size)
                Assert.assertEquals(2, httpHeaders?.setSize)
                Assert.assertEquals(2, httpHeaders?.addSize)
                Assert.assertEquals("value1", httpHeaders?.getSet("key1"))
                Assert.assertEquals("value2.1", httpHeaders?.getSet("key2"))
                Assert.assertEquals(listOf("value3", "value3.1"), httpHeaders?.getAdd("key3"))
            }

            removeHttpHeader("key3")
            build().apply {
                Assert.assertEquals(2, httpHeaders?.size)
                Assert.assertEquals("value1", httpHeaders?.getSet("key1"))
                Assert.assertEquals("value2.1", httpHeaders?.getSet("key2"))
            }

            removeHttpHeader("key2")
            build().apply {
                Assert.assertEquals(1, httpHeaders?.size)
                Assert.assertEquals("value1", httpHeaders?.getSet("key1"))
            }

            removeHttpHeader("key1")
            build().apply {
                Assert.assertNull(httpHeaders)
            }
        }
    }

    @Test
    fun testDownloadCachePolicy() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertEquals(ENABLED, downloadCachePolicy)
            }

            downloadCachePolicy(READ_ONLY)
            build().apply {
                Assert.assertEquals(READ_ONLY, downloadCachePolicy)
            }

            downloadCachePolicy(DISABLED)
            build().apply {
                Assert.assertEquals(DISABLED, downloadCachePolicy)
            }

            downloadCachePolicy(null)
            build().apply {
                Assert.assertEquals(ENABLED, downloadCachePolicy)
            }
        }
    }

    @Test
    fun testBitmapConfig() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertNull(bitmapConfig)
            }

            bitmapConfig(BitmapConfig(RGB_565))
            build().apply {
                Assert.assertEquals(BitmapConfig(RGB_565), bitmapConfig)
            }

            bitmapConfig(ARGB_8888)
            build().apply {
                Assert.assertEquals(BitmapConfig(ARGB_8888), bitmapConfig)
            }

            bitmapConfig(BitmapConfig.LowQuality)
            build().apply {
                Assert.assertEquals(BitmapConfig.LowQuality, bitmapConfig)
            }

            bitmapConfig(BitmapConfig.HighQuality)
            build().apply {
                Assert.assertEquals(BitmapConfig.HighQuality, bitmapConfig)
            }

            bitmapConfig(null)
            build().apply {
                Assert.assertNull(bitmapConfig)
            }
        }
    }

    @Test
    fun testColorSpace() {
        if (VERSION.SDK_INT < VERSION_CODES.O) return

        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertNull(colorSpace)
            }

            colorSpace(ACES)
            build().apply {
                Assert.assertEquals(ColorSpace.get(ACES), colorSpace)
            }

            colorSpace(BT709)
            build().apply {
                Assert.assertEquals(ColorSpace.get(BT709), colorSpace)
            }

            colorSpace(null)
            build().apply {
                Assert.assertNull(colorSpace)
            }
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun testPreferQualityOverSpeed() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertFalse(preferQualityOverSpeed)
            }

            preferQualityOverSpeed()
            build().apply {
                Assert.assertEquals(true, preferQualityOverSpeed)
            }

            preferQualityOverSpeed(false)
            build().apply {
                Assert.assertEquals(false, preferQualityOverSpeed)
            }

            preferQualityOverSpeed(null)
            build().apply {
                Assert.assertFalse(preferQualityOverSpeed)
            }
        }
    }

    @Test
    fun testResize() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertNull(definedOptions.sizeResolver)
                Assert.assertNull(definedOptions.precisionDecider)
                Assert.assertNull(definedOptions.scaleDecider)
                Assert.assertEquals(DisplaySizeResolver(context1), sizeResolver)
                Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), precisionDecider)
                Assert.assertEquals(FixedScaleDecider(CENTER_CROP), scaleDecider)
            }

            resize(100, 100, SAME_ASPECT_RATIO, START_CROP)
            build().apply {
                Assert.assertEquals(FixedSizeResolver(100, 100), definedOptions.sizeResolver)
                Assert.assertEquals(
                    FixedPrecisionDecider(SAME_ASPECT_RATIO),
                    definedOptions.precisionDecider
                )
                Assert.assertEquals(
                    FixedScaleDecider(START_CROP),
                    definedOptions.scaleDecider
                )
                Assert.assertEquals(FixedSizeResolver(100, 100), sizeResolver)
                Assert.assertEquals(
                    FixedPrecisionDecider(SAME_ASPECT_RATIO),
                    precisionDecider
                )
                Assert.assertEquals(FixedScaleDecider(START_CROP), scaleDecider)
            }

            resize(100, 100)
            build().apply {
                Assert.assertEquals(FixedSizeResolver(100, 100), definedOptions.sizeResolver)
                Assert.assertNull(definedOptions.precisionDecider)
                Assert.assertNull(definedOptions.scaleDecider)
                Assert.assertEquals(FixedSizeResolver(100, 100), sizeResolver)
                Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), precisionDecider)
                Assert.assertEquals(FixedScaleDecider(CENTER_CROP), scaleDecider)
            }

            resize(100, 100, SAME_ASPECT_RATIO, START_CROP)
            resize(100, 100, EXACTLY)
            build().apply {
                Assert.assertEquals(FixedSizeResolver(100, 100), definedOptions.sizeResolver)
                Assert.assertEquals(
                    FixedPrecisionDecider(EXACTLY),
                    definedOptions.precisionDecider
                )
                Assert.assertNull(definedOptions.scaleDecider)
                Assert.assertEquals(FixedSizeResolver(100, 100), sizeResolver)
                Assert.assertEquals(FixedPrecisionDecider(EXACTLY), precisionDecider)
                Assert.assertEquals(FixedScaleDecider(CENTER_CROP), scaleDecider)
            }

            resize(100, 100, SAME_ASPECT_RATIO, START_CROP)
            resize(100, 100, scale = END_CROP)
            build().apply {
                Assert.assertEquals(FixedSizeResolver(100, 100), definedOptions.sizeResolver)
                Assert.assertNull(definedOptions.precisionDecider)
                Assert.assertEquals(FixedScaleDecider(END_CROP), definedOptions.scaleDecider)
                Assert.assertEquals(FixedSizeResolver(100, 100), sizeResolver)
                Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), precisionDecider)
                Assert.assertEquals(FixedScaleDecider(END_CROP), scaleDecider)
            }

            resize(100, 100, SAME_ASPECT_RATIO, START_CROP)
            resize(null)
            build().apply {
                Assert.assertNull(definedOptions.sizeResolver)
                Assert.assertNull(definedOptions.precisionDecider)
                Assert.assertNull(definedOptions.scaleDecider)
                Assert.assertEquals(DisplaySizeResolver(context1), sizeResolver)
                Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), precisionDecider)
                Assert.assertEquals(FixedScaleDecider(CENTER_CROP), scaleDecider)
            }

            resize(Size(100, 100), SAME_ASPECT_RATIO, START_CROP)
            build().apply {
                Assert.assertEquals(
                    FixedSizeResolver(Size(100, 100)),
                    definedOptions.sizeResolver
                )
                Assert.assertEquals(
                    FixedPrecisionDecider(SAME_ASPECT_RATIO),
                    definedOptions.precisionDecider
                )
                Assert.assertEquals(
                    FixedScaleDecider(START_CROP),
                    definedOptions.scaleDecider
                )
                Assert.assertEquals(FixedSizeResolver(Size(100, 100)), sizeResolver)
                Assert.assertEquals(
                    FixedPrecisionDecider(SAME_ASPECT_RATIO),
                    precisionDecider
                )
                Assert.assertEquals(FixedScaleDecider(START_CROP), scaleDecider)
            }

            resize(Size(100, 100))
            build().apply {
                Assert.assertEquals(
                    FixedSizeResolver(Size(100, 100)),
                    definedOptions.sizeResolver
                )
                Assert.assertNull(definedOptions.precisionDecider)
                Assert.assertNull(definedOptions.scaleDecider)
                Assert.assertEquals(FixedSizeResolver(Size(100, 100)), sizeResolver)
                Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), precisionDecider)
                Assert.assertEquals(FixedScaleDecider(CENTER_CROP), scaleDecider)
            }

            resize(Size(100, 100), SAME_ASPECT_RATIO, START_CROP)
            resize(Size(100, 100), EXACTLY)
            build().apply {
                Assert.assertEquals(
                    FixedSizeResolver(Size(100, 100)),
                    definedOptions.sizeResolver
                )
                Assert.assertEquals(
                    FixedPrecisionDecider(EXACTLY),
                    definedOptions.precisionDecider
                )
                Assert.assertNull(definedOptions.scaleDecider)
                Assert.assertEquals(FixedSizeResolver(Size(100, 100)), sizeResolver)
                Assert.assertEquals(FixedPrecisionDecider(EXACTLY), precisionDecider)
                Assert.assertEquals(FixedScaleDecider(CENTER_CROP), scaleDecider)
            }

            resize(Size(100, 100), SAME_ASPECT_RATIO, START_CROP)
            resize(Size(100, 100), scale = END_CROP)
            build().apply {
                Assert.assertEquals(
                    FixedSizeResolver(Size(100, 100)),
                    definedOptions.sizeResolver
                )
                Assert.assertNull(definedOptions.precisionDecider)
                Assert.assertEquals(FixedScaleDecider(END_CROP), definedOptions.scaleDecider)
                Assert.assertEquals(FixedSizeResolver(Size(100, 100)), sizeResolver)
                Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), precisionDecider)
                Assert.assertEquals(FixedScaleDecider(END_CROP), scaleDecider)
            }

            resize(Size(100, 100), SAME_ASPECT_RATIO, START_CROP)
            resize(null)
            build().apply {
                Assert.assertNull(definedOptions.sizeResolver)
                Assert.assertNull(definedOptions.precisionDecider)
                Assert.assertNull(definedOptions.scaleDecider)
                Assert.assertEquals(DisplaySizeResolver(context1), sizeResolver)
                Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), precisionDecider)
                Assert.assertEquals(FixedScaleDecider(CENTER_CROP), scaleDecider)
            }
        }
    }

    @Test
    fun testResizeSize() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertNull(definedOptions.sizeResolver)
                Assert.assertEquals(DisplaySizeResolver(context1), sizeResolver)
            }

            size(Size(100, 100))
            build().apply {
                Assert.assertEquals(FixedSizeResolver(100, 100), definedOptions.sizeResolver)
                Assert.assertEquals(FixedSizeResolver(100, 100), sizeResolver)
            }

            size(200, 200)
            build().apply {
                Assert.assertEquals(FixedSizeResolver(200, 200), definedOptions.sizeResolver)
                Assert.assertEquals(FixedSizeResolver(200, 200), sizeResolver)
            }

            size(FixedSizeResolver(300, 200))
            build().apply {
                Assert.assertEquals(FixedSizeResolver(300, 200), definedOptions.sizeResolver)
                Assert.assertEquals(FixedSizeResolver(300, 200), sizeResolver)
            }

            size(null)
            build().apply {
                Assert.assertNull(definedOptions.sizeResolver)
                Assert.assertEquals(DisplaySizeResolver(context1), sizeResolver)
            }
        }
    }

    @Test
    fun testResizePrecision() {
        val (context, sketch) = getTestContextAndNewSketch()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context, uriString1).apply {
            build().apply {
                Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), precisionDecider)
            }

            precision(LongImageClipPrecisionDecider(EXACTLY))
            build().apply {
                Assert.assertEquals(LongImageClipPrecisionDecider(EXACTLY), precisionDecider)
            }

            precision(SAME_ASPECT_RATIO)
            build().apply {
                Assert.assertEquals(
                    FixedPrecisionDecider(SAME_ASPECT_RATIO),
                    precisionDecider
                )
            }

            precision(null)
            build().apply {
                Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), precisionDecider)
            }
        }

        val request = ImageRequest(context, uriString1).apply {
            Assert.assertEquals(DisplaySizeResolver(context), sizeResolver)
            Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), precisionDecider)
        }
        val size = runBlocking {
            DisplaySizeResolver(context).size()
        }
        val request1 = request.newRequest {
            size(size)
        }.apply {
            Assert.assertEquals(FixedSizeResolver(size), sizeResolver)
            Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), precisionDecider)
        }
        request1.newRequest().apply {
            Assert.assertEquals(FixedSizeResolver(size), sizeResolver)
            Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), precisionDecider)
        }

        request.apply {
            Assert.assertEquals(FixedPrecisionDecider(LESS_PIXELS), precisionDecider)
        }
        runBlocking { sketch.execute(request) }.apply {
            Assert.assertEquals(
                FixedPrecisionDecider(LESS_PIXELS),
                this.request.precisionDecider
            )
        }
    }

    @Test
    fun testResizeScale() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertEquals(FixedScaleDecider(CENTER_CROP), scaleDecider)
            }

            scale(LongImageStartCropScaleDecider(START_CROP, END_CROP))
            build().apply {
                Assert.assertEquals(
                    LongImageStartCropScaleDecider(START_CROP, END_CROP),
                    scaleDecider
                )
            }

            scale(FILL)
            build().apply {
                Assert.assertEquals(FixedScaleDecider(FILL), scaleDecider)
            }

            scale(null)
            build().apply {
                Assert.assertEquals(FixedScaleDecider(CENTER_CROP), scaleDecider)
            }
        }
    }

    @Test
    fun testTransformations() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertNull(transformations)
            }

            /* transformations() */
            transformations(listOf(CircleCropTransformation()))
            build().apply {
                Assert.assertEquals(
                    listOf(CircleCropTransformation()),
                    transformations
                )
            }

            transformations(RoundedCornersTransformation(), RotateTransformation(40))
            build().apply {
                Assert.assertEquals(
                    listOf(RoundedCornersTransformation(), RotateTransformation(40)),
                    transformations
                )
            }

            transformations(null)
            build().apply {
                Assert.assertNull(transformations)
            }

            /* addTransformations(List), removeTransformations(List) */
            addTransformations(listOf(CircleCropTransformation()))
            build().apply {
                Assert.assertEquals(
                    listOf(CircleCropTransformation()),
                    transformations
                )
            }
            addTransformations(listOf(CircleCropTransformation(), RotateTransformation(40)))
            build().apply {
                Assert.assertEquals(
                    listOf(CircleCropTransformation(), RotateTransformation(40)),
                    transformations
                )
            }
            removeTransformations(listOf(RotateTransformation(40)))
            build().apply {
                Assert.assertEquals(
                    listOf(CircleCropTransformation()),
                    transformations
                )
            }
            removeTransformations(listOf(CircleCropTransformation()))
            build().apply {
                Assert.assertNull(transformations)
            }

            /* addTransformations(vararg), removeTransformations(vararg) */
            addTransformations(CircleCropTransformation())
            build().apply {
                Assert.assertEquals(
                    listOf(CircleCropTransformation()),
                    transformations
                )
            }
            addTransformations(CircleCropTransformation(), RotateTransformation(40))
            build().apply {
                Assert.assertEquals(
                    listOf(CircleCropTransformation(), RotateTransformation(40)),
                    transformations
                )
            }
            removeTransformations(RotateTransformation(40))
            build().apply {
                Assert.assertEquals(
                    listOf(CircleCropTransformation()),
                    transformations
                )
            }
            removeTransformations(CircleCropTransformation())
            build().apply {
                Assert.assertNull(transformations)
            }
        }
    }

    @Test
    fun testResultCachePolicy() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertEquals(ENABLED, resultCachePolicy)
            }

            resultCachePolicy(READ_ONLY)
            build().apply {
                Assert.assertEquals(READ_ONLY, resultCachePolicy)
            }

            resultCachePolicy(DISABLED)
            build().apply {
                Assert.assertEquals(DISABLED, resultCachePolicy)
            }

            resultCachePolicy(null)
            build().apply {
                Assert.assertEquals(ENABLED, resultCachePolicy)
            }
        }
    }

    @Test
    fun testDisallowAnimatedImage() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertFalse(disallowAnimatedImage)
            }

            disallowAnimatedImage(true)
            build().apply {
                Assert.assertEquals(true, disallowAnimatedImage)
            }

            disallowAnimatedImage(false)
            build().apply {
                Assert.assertEquals(false, disallowAnimatedImage)
            }

            disallowAnimatedImage(null)
            build().apply {
                Assert.assertFalse(disallowAnimatedImage)
            }
        }
    }

    @Test
    fun testPlaceholder() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertNull(placeholder)
            }

            placeholder(ColorStateImage(IntColor(Color.BLUE)))
            build().apply {
                Assert.assertEquals(ColorStateImage(IntColor(Color.BLUE)), placeholder)
            }

            placeholder(ColorDrawableEqualizer(Color.GREEN))
            build().apply {
                Assert.assertEquals(true, placeholder is DrawableStateImage)
            }

            placeholder(drawable.bottom_bar)
            build().apply {
                Assert.assertEquals(
                    DrawableStateImage(drawable.bottom_bar),
                    placeholder
                )
            }

            placeholder(null)
            build().apply {
                Assert.assertNull(placeholder)
            }
        }
    }

    @Test
    fun testUriEmpty() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertNull(uriEmpty)
            }

            uriEmpty(ColorStateImage(IntColor(Color.BLUE)))
            build().apply {
                Assert.assertEquals(ColorStateImage(IntColor(Color.BLUE)), uriEmpty)
            }

            uriEmpty(ColorDrawableEqualizer(Color.GREEN))
            build().apply {
                Assert.assertEquals(true, uriEmpty is DrawableStateImage)
            }

            uriEmpty(drawable.bottom_bar)
            build().apply {
                Assert.assertEquals(
                    DrawableStateImage(drawable.bottom_bar),
                    uriEmpty
                )
            }

            uriEmpty(null)
            build().apply {
                Assert.assertNull(uriEmpty)
            }
        }
    }

    @Test
    fun testError() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertNull(error)
            }

            error(ColorStateImage(IntColor(Color.BLUE)))
            build().apply {
                Assert.assertEquals(
                    ErrorStateImage(ColorStateImage(IntColor(Color.BLUE))),
                    error
                )
            }

            error(ColorDrawableEqualizer(Color.GREEN))
            build().apply {
                Assert.assertEquals(true, error is ErrorStateImage)
            }

            error(drawable.bottom_bar)
            build().apply {
                Assert.assertEquals(
                    ErrorStateImage(DrawableStateImage(drawable.bottom_bar)),
                    error
                )
            }

            error(drawable.bottom_bar) {
                uriEmptyError(drawable.alert_dark_frame)
            }
            build().apply {
                Assert.assertEquals(
                    ErrorStateImage(DrawableStateImage(drawable.bottom_bar)) {
                        uriEmptyError(drawable.alert_dark_frame)
                    },
                    error
                )
            }

            error()
            build().apply {
                Assert.assertNull(error)
            }

            error {
                uriEmptyError(drawable.btn_dialog)
            }
            build().apply {
                Assert.assertNotNull(error)
            }
        }
    }

    @Test
    fun testTransitionFactory() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertNull(transitionFactory)
            }

            transitionFactory(CrossfadeTransition.Factory())
            build().apply {
                Assert.assertEquals(CrossfadeTransition.Factory(), transitionFactory)
            }

            transitionFactory(null)
            build().apply {
                Assert.assertNull(transitionFactory)
            }
        }
    }

    @Test
    fun testResizeOnDraw() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertFalse(resizeOnDraw)
            }

            resizeOnDraw()
            build().apply {
                Assert.assertEquals(true, resizeOnDraw)
            }

            resizeOnDraw(false)
            build().apply {
                Assert.assertEquals(false, resizeOnDraw)
            }

            resizeOnDraw(null)
            build().apply {
                Assert.assertFalse(resizeOnDraw)
            }
        }
    }

    @Test
    fun testMemoryCachePolicy() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertEquals(ENABLED, memoryCachePolicy)
            }

            memoryCachePolicy(READ_ONLY)
            build().apply {
                Assert.assertEquals(READ_ONLY, memoryCachePolicy)
            }

            memoryCachePolicy(DISABLED)
            build().apply {
                Assert.assertEquals(DISABLED, memoryCachePolicy)
            }

            memoryCachePolicy(null)
            build().apply {
                Assert.assertEquals(ENABLED, memoryCachePolicy)
            }
        }
    }

    @Test
    fun testListener() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertNull(listener)
                Assert.assertNull(target)
            }

            listener(onStart = {}, onCancel = {}, onError = { _, _ -> }, onSuccess = { _, _ -> })
            build().apply {
                Assert.assertNotNull(listener)
                Assert.assertTrue(listener !is CombinedListener)
            }
            build().newRequest().apply {
                Assert.assertNotNull(listener)
                Assert.assertTrue(listener !is CombinedListener)
            }

            listener(onStart = {})
            build().apply {
                Assert.assertNotNull(listener)
                Assert.assertTrue(listener !is CombinedListener)
            }

            listener(onCancel = {})
            build().apply {
                Assert.assertNotNull(listener)
                Assert.assertTrue(listener !is CombinedListener)
            }

            listener(onError = { _, _ -> })
            build().apply {
                Assert.assertNotNull(listener)
                Assert.assertTrue(listener !is CombinedListener)
            }

            listener(onSuccess = { _, _ -> })
            build().apply {
                Assert.assertNotNull(listener)
                Assert.assertTrue(listener !is CombinedListener)
            }

            listener(null)
            target(null)
            build().apply {
                Assert.assertNull(listener)
            }

            listener(onSuccess = { _, _ -> })
            build().apply {
                Assert.assertNotNull(listener)
                Assert.assertTrue(listener !is CombinedListener)
            }

            listener(null)
            target(null)
            build().apply {
                Assert.assertNull(listener)
            }

            listener(onSuccess = { _, _ -> })
            build().listener!!.asOrThrow<CombinedListener>().apply {
                Assert.assertNotNull(fromBuilderListener)
                Assert.assertNull(fromBuilderListeners)
                Assert.assertTrue(fromBuilderListener !is CombinedListener)
                Assert.assertNotNull(fromTargetListener)
                Assert.assertTrue(fromTargetListener !is CombinedListener)
            }
            build().newRequest().listener!!.asOrThrow<CombinedListener>().apply {
                Assert.assertNotNull(fromBuilderListener)
                Assert.assertNull(fromBuilderListeners)
                Assert.assertTrue(fromBuilderListener !is CombinedListener)
                Assert.assertNotNull(fromTargetListener)
                Assert.assertTrue(fromTargetListener !is CombinedListener)
            }

            val listener2 = object : Listener {}
            val listener3 = object : Listener {}
            addListener(listener2)
            addListener(listener3)
            addListener(listener2)
            build().listener!!.asOrThrow<CombinedListener>().apply {
                Assert.assertNotNull(fromBuilderListener)
                Assert.assertNotNull(fromBuilderListeners)
                Assert.assertTrue(fromBuilderListeners!!.size == 2)
                Assert.assertTrue(fromBuilderListener !is CombinedListener)
                Assert.assertNotNull(fromTargetListener)
                Assert.assertTrue(fromTargetListener !is CombinedListener)
            }
            build().newRequest().listener!!.asOrThrow<CombinedListener>().apply {
                Assert.assertNotNull(fromBuilderListener)
                Assert.assertNotNull(fromBuilderListeners)
                Assert.assertTrue(fromBuilderListeners!!.size == 2)
                Assert.assertTrue(fromBuilderListener !is CombinedListener)
                Assert.assertNotNull(fromTargetListener)
                Assert.assertTrue(fromTargetListener !is CombinedListener)
            }

            removeListener(listener2)
            build().listener!!.asOrThrow<CombinedListener>().apply {
                Assert.assertNotNull(fromBuilderListener)
                Assert.assertNotNull(fromBuilderListeners)
                Assert.assertTrue(fromBuilderListeners!!.size == 1)
                Assert.assertTrue(fromBuilderListener !is CombinedListener)
                Assert.assertNotNull(fromTargetListener)
                Assert.assertTrue(fromTargetListener !is CombinedListener)
            }
            build().newRequest().listener!!.asOrThrow<CombinedListener>().apply {
                Assert.assertNotNull(fromBuilderListener)
                Assert.assertNotNull(fromBuilderListeners)
                Assert.assertTrue(fromBuilderListeners!!.size == 1)
                Assert.assertTrue(fromBuilderListener !is CombinedListener)
                Assert.assertNotNull(fromTargetListener)
                Assert.assertTrue(fromTargetListener !is CombinedListener)
            }
        }
    }

    @Test
    fun testProgressListener() {
        val context1 = getTestContext()
        val uriString1 = MyImages.jpeg.uri
        ImageRequest.Builder(context1, uriString1).apply {
            build().apply {
                Assert.assertNull(progressListener)
                Assert.assertNull(target)
            }

            progressListener { _, _ -> }
            build().apply {
                Assert.assertNotNull(progressListener)
                Assert.assertTrue(progressListener !is CombinedProgressListener)
            }
            build().newRequest().apply {
                Assert.assertNotNull(progressListener)
                Assert.assertTrue(progressListener !is CombinedProgressListener)
            }

            progressListener(null)
            target(null)
            build().apply {
                Assert.assertNull(progressListener)
            }

            progressListener { _, _ -> }
            build().apply {
                Assert.assertNotNull(progressListener)
                Assert.assertTrue(progressListener !is CombinedProgressListener)
            }

            progressListener(null)
            target(null)
            build().apply {
                Assert.assertNull(progressListener)
            }

            progressListener { _, _ -> }
            build().progressListener!!.asOrThrow<CombinedProgressListener>().apply {
                Assert.assertNotNull(fromBuilderProgressListener)
                Assert.assertNull(fromBuilderProgressListeners)
                Assert.assertTrue(fromBuilderProgressListener !is CombinedProgressListener)
                Assert.assertNotNull(fromTargetProgressListener)
                Assert.assertTrue(fromTargetProgressListener !is CombinedProgressListener)
            }
            build().newRequest().progressListener!!.asOrThrow<CombinedProgressListener>()
                .apply {
                    Assert.assertNotNull(fromBuilderProgressListener)
                    Assert.assertNull(fromBuilderProgressListeners)
                    Assert.assertTrue(fromBuilderProgressListener !is CombinedProgressListener)
                    Assert.assertNotNull(fromTargetProgressListener)
                    Assert.assertTrue(fromTargetProgressListener !is CombinedProgressListener)
                }

            val progressListener2 = ProgressListener { _, _ -> }
            val progressListener3 = ProgressListener { _, _ -> }
            addProgressListener(progressListener2)
            addProgressListener(progressListener3)
            addProgressListener(progressListener2)
            build().progressListener!!.asOrThrow<CombinedProgressListener>().apply {
                Assert.assertNotNull(fromBuilderProgressListener)
                Assert.assertNotNull(fromBuilderProgressListeners)
                Assert.assertTrue(fromBuilderProgressListeners!!.size == 2)
                Assert.assertTrue(fromBuilderProgressListener !is CombinedProgressListener)
                Assert.assertNotNull(fromTargetProgressListener)
                Assert.assertTrue(fromTargetProgressListener !is CombinedProgressListener)
            }
            build().newRequest().progressListener!!.asOrThrow<CombinedProgressListener>()
                .apply {
                    Assert.assertNotNull(fromBuilderProgressListener)
                    Assert.assertNotNull(fromBuilderProgressListeners)
                    Assert.assertTrue(fromBuilderProgressListeners!!.size == 2)
                    Assert.assertTrue(fromBuilderProgressListener !is CombinedProgressListener)
                    Assert.assertNotNull(fromTargetProgressListener)
                    Assert.assertTrue(fromTargetProgressListener !is CombinedProgressListener)
                }

            removeProgressListener(progressListener2)
            build().progressListener!!.asOrThrow<CombinedProgressListener>().apply {
                Assert.assertNotNull(fromBuilderProgressListener)
                Assert.assertNotNull(fromBuilderProgressListeners)
                Assert.assertTrue(fromBuilderProgressListeners!!.size == 1)
                Assert.assertTrue(fromBuilderProgressListener !is CombinedProgressListener)
                Assert.assertNotNull(fromTargetProgressListener)
                Assert.assertTrue(fromTargetProgressListener !is CombinedProgressListener)
            }
            build().newRequest().progressListener!!.asOrThrow<CombinedProgressListener>()
                .apply {
                    Assert.assertNotNull(fromBuilderProgressListener)
                    Assert.assertNotNull(fromBuilderProgressListeners)
                    Assert.assertTrue(fromBuilderProgressListeners!!.size == 1)
                    Assert.assertTrue(fromBuilderProgressListener !is CombinedProgressListener)
                    Assert.assertNotNull(fromTargetProgressListener)
                    Assert.assertTrue(fromTargetProgressListener !is CombinedProgressListener)
                }
        }
    }

    @Test
    fun testComponents() {
        val context = getTestContext()
        ImageRequest(context, MyImages.jpeg.uri).apply {
            Assert.assertNull(componentRegistry)
        }

        ImageRequest(context, MyImages.jpeg.uri) {
            components {
                addFetcher(HttpUriFetcher.Factory())
                addFetcher(TestFetcher.Factory())
                addDecoder(BitmapFactoryDecoder.Factory())
                addDecoder(TestDecoder.Factory())
                addRequestInterceptor(TestRequestInterceptor())
                addDecodeInterceptor(TestDecodeInterceptor())
            }
        }.apply {
            Assert.assertEquals(
                ComponentRegistry.Builder().apply {
                    addFetcher(HttpUriFetcher.Factory())
                    addFetcher(TestFetcher.Factory())
                    addDecoder(BitmapFactoryDecoder.Factory())
                    addDecoder(TestDecoder.Factory())
                    addRequestInterceptor(TestRequestInterceptor())
                    addDecodeInterceptor(TestDecodeInterceptor())
                }.build(),
                componentRegistry
            )
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val context = getTestContext()
        val element1 = ImageRequest(context, MyImages.jpeg.uri)
        val element11 = ImageRequest(context, MyImages.jpeg.uri)
        val element2 = ImageRequest(context, MyImages.png.uri)

        Assert.assertNotSame(element1, element11)
        Assert.assertNotSame(element1, element2)
        Assert.assertNotSame(element2, element11)

        Assert.assertEquals(element1, element1)
        Assert.assertEquals(element1, element11)
        Assert.assertNotEquals(element1, element2)
        Assert.assertNotEquals(element2, element11)
        Assert.assertNotEquals(element1, null)
        Assert.assertNotEquals(element1, Any())

        Assert.assertEquals(element1.hashCode(), element1.hashCode())
        Assert.assertEquals(element1.hashCode(), element11.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element2.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element11.hashCode())

        val imageView = TestListenerImageView(context)
        ImageRequest(context, MyImages.jpeg.uri).apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            listener(onStart = {})
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            progressListener { _, _ -> }
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            default(ImageOptions())
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            depth(LOCAL, "test")
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            setParameter("type", "list")
            setParameter("big", "true")
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            setHttpHeader("from", "china")
            setHttpHeader("job", "Programmer")
            addHttpHeader("Host", "www.google.com")
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            downloadCachePolicy(READ_ONLY)
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            bitmapConfig(RGB_565)
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                colorSpace(ADOBE_RGB)
            }
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            @Suppress("DEPRECATION")
            preferQualityOverSpeed(true)
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            size(300, 200)
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            size(ViewSizeResolver(imageView))
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            precision(EXACTLY)
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            precision(LongImageClipPrecisionDecider())
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            scale(END_CROP)
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            scale(LongImageStartCropScaleDecider())
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            transformations(CircleCropTransformation(), BlurTransformation())
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            resultCachePolicy(WRITE_ONLY)
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            placeholder(
                IconStateImage(
                    icon = drawable.ic_delete,
                    background = color.background_dark
                )
            )
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            placeholder(ColorStateImage(Color.BLUE))
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            placeholder(ColorStateImage(ResColor(color.background_dark)))
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            val drawable = context.resources.getEqualityDrawableCompat(drawable.ic_delete, null)
            placeholder(DrawableStateImage(drawable))
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            placeholder(DrawableStateImage(drawable.ic_delete))
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            placeholder(CurrentStateImage(drawable.ic_delete))
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            placeholder(MemoryCacheStateImage("uri", ColorStateImage(Color.BLUE)))
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            placeholder(ThumbnailMemoryCacheStateImage("uri", ColorStateImage(Color.BLUE)))
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            error(DrawableStateImage(drawable.ic_delete))
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            error(DrawableStateImage(drawable.ic_delete)) {
                uriEmptyError(ColorStateImage(Color.BLUE))
            }
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            transitionFactory(CrossfadeTransition.Factory())
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            transitionFactory(CrossfadeTransition.Factory(fadeStart = false, alwaysUse = true))
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            disallowAnimatedImage(true)
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            resizeOnDraw(true)
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            memoryCachePolicy(WRITE_ONLY)
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }.newRequest {
            components {
                addFetcher(TestFetcher.Factory())
                addRequestInterceptor(TestRequestInterceptor())
                addDecodeInterceptor(TestDecodeInterceptor())
                addDecoder(TestDecoder.Factory())
            }
        }.apply {
            Assert.assertEquals(this, this.newRequest())
        }
    }

    @Test
    fun testMergeComponents() {
        // TODO test mergeComponents
    }

    @Test
    fun testSizeMultiplier() {
        // TODO test sizeMultiplier
    }
}