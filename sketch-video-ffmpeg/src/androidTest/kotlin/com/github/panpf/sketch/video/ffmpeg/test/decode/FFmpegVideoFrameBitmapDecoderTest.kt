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
package com.github.panpf.sketch.video.ffmpeg.test.decode

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.cache.CachePolicy.DISABLED
import com.github.panpf.sketch.datasource.AssetDataSource
import com.github.panpf.sketch.datasource.DataFrom.LOCAL
import com.github.panpf.sketch.decode.FFmpegVideoFrameBitmapDecoder
import com.github.panpf.sketch.decode.internal.createInSampledTransformed
import com.github.panpf.sketch.decode.supportFFmpegVideoFrame
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.request.internal.RequestContext
import com.github.panpf.sketch.request.videoFrameMillis
import com.github.panpf.sketch.request.videoFrameOption
import com.github.panpf.sketch.request.videoFramePercent
import com.github.panpf.sketch.resize.Precision.LESS_PIXELS
import com.github.panpf.sketch.resources.AssetImages
import com.github.panpf.sketch.test.singleton.sketch
import com.github.panpf.sketch.util.Size
import com.github.panpf.sketch.video.ffmpeg.test.utils.corners
import com.github.panpf.tools4a.device.Devicex
import com.github.panpf.tools4j.test.ktx.assertThrow
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FFmpegVideoFrameBitmapDecoderTest {

    @Test
    fun testSupportApkIcon() {
        ComponentRegistry.Builder().apply {
            build().apply {
                Assert.assertEquals(
                    "ComponentRegistry(" +
                            "fetcherFactoryList=[]," +
                            "bitmapDecoderFactoryList=[]," +
                            "drawableDecoderFactoryList=[]," +
                            "requestInterceptorList=[]," +
                            "bitmapDecodeInterceptorList=[]," +
                            "drawableDecodeInterceptorList=[]" +
                            ")",
                    toString()
                )
            }

            supportFFmpegVideoFrame()
            build().apply {
                Assert.assertEquals(
                    "ComponentRegistry(" +
                            "fetcherFactoryList=[]," +
                            "bitmapDecoderFactoryList=[FFmpegVideoFrameBitmapDecoder]," +
                            "drawableDecoderFactoryList=[]," +
                            "requestInterceptorList=[]," +
                            "bitmapDecodeInterceptorList=[]," +
                            "drawableDecodeInterceptorList=[]" +
                            ")",
                    toString()
                )
            }

            supportFFmpegVideoFrame()
            build().apply {
                Assert.assertEquals(
                    "ComponentRegistry(" +
                            "fetcherFactoryList=[]," +
                            "bitmapDecoderFactoryList=[FFmpegVideoFrameBitmapDecoder,FFmpegVideoFrameBitmapDecoder]," +
                            "drawableDecoderFactoryList=[]," +
                            "requestInterceptorList=[]," +
                            "bitmapDecodeInterceptorList=[]," +
                            "drawableDecodeInterceptorList=[]" +
                            ")",
                    toString()
                )
            }
        }
    }

    @Test
    fun testFactory() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = context.sketch
        val factory = FFmpegVideoFrameBitmapDecoder.Factory()

        Assert.assertEquals("FFmpegVideoFrameBitmapDecoder", factory.toString())

        // normal
        LoadRequest(context, AssetImages.mp4.uri).let {
            val fetchResult =
                FetchResult(AssetDataSource(sketch, it, AssetImages.mp4.fileName), null)
            factory.create(sketch, it.toRequestContext(), fetchResult)
        }.apply {
            Assert.assertNull(this)
        }

        LoadRequest(context, AssetImages.mp4.uri).let {
            val fetchResult =
                FetchResult(AssetDataSource(sketch, it, AssetImages.mp4.fileName), "video/mp4")
            factory.create(sketch, it.toRequestContext(), fetchResult)
        }.apply {
            Assert.assertNotNull(this)
        }

        // data error
        LoadRequest(context, AssetImages.png.uri).let {
            val fetchResult =
                FetchResult(AssetDataSource(sketch, it, AssetImages.png.fileName), "video/mp4")
            factory.create(sketch, it.toRequestContext(), fetchResult)
        }.apply {
            Assert.assertNotNull(this)
        }

        // mimeType error
        LoadRequest(context, AssetImages.mp4.uri).let {
            val fetchResult =
                FetchResult(AssetDataSource(sketch, it, AssetImages.mp4.fileName), "image/png")
            factory.create(sketch, it.toRequestContext(), fetchResult)
        }.apply {
            Assert.assertNull(this)
        }
    }

    @Test
    fun testFactoryEqualsAndHashCode() {
        val element1 = FFmpegVideoFrameBitmapDecoder.Factory()
        val element11 = FFmpegVideoFrameBitmapDecoder.Factory()

        Assert.assertNotSame(element1, element11)

        Assert.assertEquals(element1, element1)
        Assert.assertEquals(element1, element11)

        Assert.assertNotEquals(element1, Any())
        Assert.assertNotEquals(element1, null)

        Assert.assertEquals(element1.hashCode(), element1.hashCode())
        Assert.assertEquals(element1.hashCode(), element11.hashCode())
    }

    @Test
    fun testDecode() {
        if (Build.VERSION.SDK_INT < 24 && Devicex.isEmulator()) {
            // UnsatisfiedLinkError /data/app/com.github.panpf.sketch.video.ffmpeg.test-1/lib/arm64/libssl.so
            return
        }

        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = context.sketch
        val factory = FFmpegVideoFrameBitmapDecoder.Factory()

        LoadRequest(context, AssetImages.mp4.uri).run {
            val fetcher = sketch.components.newFetcherOrThrow(this)
            val fetchResult = runBlocking { fetcher.fetch() }.getOrThrow()
            runBlocking {
                factory.create(sketch, this@run.toRequestContext(), fetchResult)!!.decode()
            }.getOrThrow()
        }.apply {
            Assert.assertEquals("Bitmap(500x250,ARGB_8888)", bitmap.toShortInfoString())
            Assert.assertEquals(
                "ImageInfo(500x250,'video/mp4',UNDEFINED)",
                imageInfo.toShortString()
            )
            Assert.assertEquals(LOCAL, dataFrom)
            Assert.assertNull(transformedList)
        }

        LoadRequest(context, AssetImages.mp4.uri) {
            resize(300, 300, LESS_PIXELS)
        }.run {
            val fetcher = sketch.components.newFetcherOrThrow(this)
            val fetchResult = runBlocking { fetcher.fetch() }.getOrThrow()
            runBlocking {
                factory.create(sketch, this@run.toRequestContext(), fetchResult)!!.decode()
            }.getOrThrow()
        }.apply {
            Assert.assertEquals("Bitmap(250x125,ARGB_8888)", bitmap.toShortInfoString())
            Assert.assertEquals(
                "ImageInfo(500x250,'video/mp4',UNDEFINED)",
                imageInfo.toShortString()
            )
            Assert.assertEquals(LOCAL, dataFrom)
            Assert.assertEquals(listOf(createInSampledTransformed(2)), transformedList)
        }

        LoadRequest(context, AssetImages.png.uri).run {
            val fetcher = sketch.components.newFetcherOrThrow(this)
            val fetchResult = runBlocking { fetcher.fetch() }.getOrThrow()
            assertThrow(NullPointerException::class) {
                runBlocking {
                    factory.create(sketch, this@run.toRequestContext(), fetchResult)!!
                        .decode()
                }.getOrThrow()
            }
        }
    }

    @Test
    fun testDecodeVideoFrameMicros() {
        if (Build.VERSION.SDK_INT < 24 && Devicex.isEmulator()) {
            // UnsatisfiedLinkError /data/app/com.github.panpf.sketch.video.ffmpeg.test-1/lib/arm64/libssl.so
            return
        }

        val context = InstrumentationRegistry.getInstrumentation().context

        val sketch = context.sketch
        val factory = FFmpegVideoFrameBitmapDecoder.Factory()
        val bitmap1 = LoadRequest(context, AssetImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
        }.run {
            val fetcher = sketch.components.newFetcherOrThrow(this)
            val fetchResult = runBlocking { fetcher.fetch() }.getOrThrow()
            runBlocking {
                factory.create(sketch, this@run.toRequestContext(), fetchResult)!!.decode()
            }.getOrThrow()
        }.bitmap
        val bitmap11 = LoadRequest(context, AssetImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
        }.run {
            val fetcher = sketch.components.newFetcherOrThrow(this)
            val fetchResult = runBlocking { fetcher.fetch() }.getOrThrow()
            runBlocking {
                factory.create(sketch, this@run.toRequestContext(), fetchResult)!!.decode()
            }.getOrThrow()
        }.bitmap
        val bitmap2 = LoadRequest(context, AssetImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
            videoFrameMillis(500)
        }.run {
            val fetcher = sketch.components.newFetcherOrThrow(this)
            val fetchResult = runBlocking { fetcher.fetch() }.getOrThrow()
            runBlocking {
                factory.create(sketch, this@run.toRequestContext(), fetchResult)!!.decode()
            }.getOrThrow()
        }.bitmap
        Assert.assertEquals(bitmap1.corners(), bitmap11.corners())
        Assert.assertNotEquals(bitmap1.corners(), bitmap2.corners())
    }

    @Test
    fun testDecodeVideoFramePercent() {
        if (Build.VERSION.SDK_INT < 24 && Devicex.isEmulator()) {
            // UnsatisfiedLinkError /data/app/com.github.panpf.sketch.video.ffmpeg.test-1/lib/arm64/libssl.so
            return
        }

        val context = InstrumentationRegistry.getInstrumentation().context

        val sketch = context.sketch
        val factory = FFmpegVideoFrameBitmapDecoder.Factory()
        val bitmap1 = LoadRequest(context, AssetImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
        }.run {
            val fetcher = sketch.components.newFetcherOrThrow(this)
            val fetchResult = runBlocking { fetcher.fetch() }.getOrThrow()
            runBlocking {
                factory.create(sketch, this@run.toRequestContext(), fetchResult)!!.decode()
            }.getOrThrow()
        }.bitmap
        val bitmap11 = LoadRequest(context, AssetImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
        }.run {
            val fetcher = sketch.components.newFetcherOrThrow(this)
            val fetchResult = runBlocking { fetcher.fetch() }.getOrThrow()
            runBlocking {
                factory.create(sketch, this@run.toRequestContext(), fetchResult)!!.decode()
            }.getOrThrow()
        }.bitmap
        val bitmap2 = LoadRequest(context, AssetImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
            videoFramePercent(0.45f)
        }.run {
            val fetcher = sketch.components.newFetcherOrThrow(this)
            val fetchResult = runBlocking { fetcher.fetch() }.getOrThrow()
            runBlocking {
                factory.create(sketch, this@run.toRequestContext(), fetchResult)!!.decode()
            }.getOrThrow()
        }.bitmap
        Assert.assertEquals(bitmap1.corners(), bitmap11.corners())
        Assert.assertNotEquals(bitmap1.corners(), bitmap2.corners())
    }

    @Test
    fun testDecodeVideoOption() {
        if (Build.VERSION.SDK_INT < 24 && Devicex.isEmulator()) {
            // UnsatisfiedLinkError /data/app/com.github.panpf.sketch.video.ffmpeg.test-1/lib/arm64/libssl.so
            return
        }

        val context = InstrumentationRegistry.getInstrumentation().context

        val sketch = context.sketch
        val factory = FFmpegVideoFrameBitmapDecoder.Factory()
        val bitmap1 = LoadRequest(context, AssetImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFramePercent(0.5f)
        }.run {
            val fetcher = sketch.components.newFetcherOrThrow(this)
            val fetchResult = runBlocking { fetcher.fetch() }.getOrThrow()
            runBlocking {
                factory.create(sketch, this@run.toRequestContext(), fetchResult)!!.decode()
            }.getOrNull()
        }?.bitmap
        val bitmap2 = LoadRequest(context, AssetImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFramePercent(0.5f)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
        }.run {
            val fetcher = sketch.components.newFetcherOrThrow(this)
            val fetchResult = runBlocking { fetcher.fetch() }.getOrThrow()
            runBlocking {
                factory.create(sketch, this@run.toRequestContext(), fetchResult)!!.decode()
            }.getOrThrow()
        }.bitmap
        Assert.assertNotEquals(bitmap1?.corners(), bitmap2.corners())
    }

    private fun Bitmap.toShortInfoString(): String = "Bitmap(${width}x${height},$config)"
}

fun ImageRequest.toRequestContext(resizeSize: Size? = null): RequestContext {
    return RequestContext(this, resizeSize ?: runBlocking { resizeSizeResolver.size() })
}