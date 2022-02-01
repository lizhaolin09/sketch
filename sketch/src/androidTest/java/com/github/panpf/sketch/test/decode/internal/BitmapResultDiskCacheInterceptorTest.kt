package com.github.panpf.sketch.test.decode.internal

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.internal.BitmapDecodeEngineInterceptor
import com.github.panpf.sketch.decode.internal.BitmapDecodeInterceptorChain
import com.github.panpf.sketch.decode.internal.BitmapResultDiskCacheInterceptor
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.sketch.request.DataFrom
import com.github.panpf.sketch.request.LoadRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BitmapResultDiskCacheInterceptorTest {

    @Test
    fun testIntercept() {
        val context = InstrumentationRegistry.getContext()
        val sketch = Sketch.new(context)
        val interceptors = listOf(BitmapResultDiskCacheInterceptor(), BitmapDecodeEngineInterceptor())
        val loadRequest = LoadRequest(newAssetUri("sample.jpeg")) {
            maxSize(500, 500)
        }
        val chain =
            BitmapDecodeInterceptorChain(loadRequest, interceptors, 0, sketch, loadRequest, null)

        sketch.diskCache.clear()

        val result = runBlocking {
            chain.proceed(loadRequest)
        }
        Assert.assertEquals(323, result.bitmap.width)
        Assert.assertEquals(484, result.bitmap.height)
        Assert.assertEquals(
            "ImageInfo(mimeType='image/jpeg',width=1291,height=1936,exifOrientation=NORMAL)",
            result.imageInfo.toString()
        )
        Assert.assertEquals(DataFrom.LOCAL, result.dataFrom)
        Assert.assertEquals("InSampledTransformed(4)", result.transformedList?.joinToString())

        val result1 = runBlocking {
            chain.proceed(loadRequest)
        }
        Assert.assertEquals(323, result1.bitmap.width)
        Assert.assertEquals(484, result1.bitmap.height)
        Assert.assertEquals(
            "ImageInfo(mimeType='image/jpeg',width=1291,height=1936,exifOrientation=NORMAL)",
            result1.imageInfo.toString()
        )
        Assert.assertEquals(DataFrom.RESULT_DISK_CACHE, result1.dataFrom)
        Assert.assertEquals("InSampledTransformed(4)", result.transformedList?.joinToString())
    }

    @Test
    fun testToString() {
        Assert.assertEquals(
            "BitmapResultDiskCacheInterceptor",
            BitmapResultDiskCacheInterceptor().toString()
        )
    }
}