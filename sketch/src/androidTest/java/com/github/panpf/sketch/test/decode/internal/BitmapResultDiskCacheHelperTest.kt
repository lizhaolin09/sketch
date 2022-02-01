package com.github.panpf.sketch.test.decode.internal

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.CachePolicy.DISABLED
import com.github.panpf.sketch.cache.CachePolicy.ENABLED
import com.github.panpf.sketch.cache.CachePolicy.READ_ONLY
import com.github.panpf.sketch.cache.CachePolicy.WRITE_ONLY
import com.github.panpf.sketch.decode.BitmapDecodeResult
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.decode.internal.InSampledTransformed
import com.github.panpf.sketch.decode.internal.newBitmapResultDiskCacheHelper
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.sketch.request.DataFrom
import com.github.panpf.sketch.request.LoadRequest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BitmapResultDiskCacheHelperTest {

    @Test
    fun testNewBitmapResultCacheHelper() {
        val context = InstrumentationRegistry.getContext()
        val sketch = Sketch.new(context)
        val request = LoadRequest(newAssetUri("sample.jpeg"))

        Assert.assertNotNull(
            newBitmapResultDiskCacheHelper(sketch, request)
        )
        Assert.assertNotNull(
            newBitmapResultDiskCacheHelper(sketch, request.newLoadRequest {
                bitmapResultDiskCachePolicy(ENABLED)
            })
        )
        Assert.assertNull(
            newBitmapResultDiskCacheHelper(sketch, request.newLoadRequest {
                bitmapResultDiskCachePolicy(DISABLED)
            })
        )
        Assert.assertNotNull(
            newBitmapResultDiskCacheHelper(sketch, request.newLoadRequest {
                bitmapResultDiskCachePolicy(READ_ONLY)
            })
        )
        Assert.assertNotNull(
            newBitmapResultDiskCacheHelper(sketch, request.newLoadRequest {
                bitmapResultDiskCachePolicy(WRITE_ONLY)
            })
        )
    }

    @Test
    fun testRead() {
        val context = InstrumentationRegistry.getContext()
        val sketch = Sketch.new(context)
        val request = LoadRequest(newAssetUri("sample.jpeg"))

        sketch.diskCache.clear()

        // Is there really no
        val helper = newBitmapResultDiskCacheHelper(sketch, request)!!
        Assert.assertNull(helper.read())

        // There are the
        val bitmapDecodeResult = BitmapDecodeResult(
            Bitmap.createBitmap(100, 100, ARGB_8888),
            ImageInfo("image/jpeg", 1291, 1936, 0),
            DataFrom.LOCAL,
            listOf(InSampledTransformed(4))
        )
        helper.write(bitmapDecodeResult)
        Assert.assertNotNull(helper.read())
        Assert.assertNotNull(helper.read())

        Assert.assertNotNull(
            newBitmapResultDiskCacheHelper(sketch, request.newLoadRequest {
                bitmapResultDiskCachePolicy(ENABLED)
            })!!.read()
        )
        Assert.assertNotNull(
            newBitmapResultDiskCacheHelper(sketch, request.newLoadRequest {
                bitmapResultDiskCachePolicy(READ_ONLY)
            })!!.read()
        )
        Assert.assertNull(
            newBitmapResultDiskCacheHelper(sketch, request.newLoadRequest {
                bitmapResultDiskCachePolicy(WRITE_ONLY)
            })!!.read()
        )
    }

    @Test
    fun testWrite() {
        val context = InstrumentationRegistry.getContext()
        val sketch = Sketch.new(context)
        val request = LoadRequest(newAssetUri("sample.jpeg"))

        sketch.diskCache.clear()

        Assert.assertNull(newBitmapResultDiskCacheHelper(sketch, request)!!.read())

        // transformedList empty
        val bitmapDecodeResult = BitmapDecodeResult(
            Bitmap.createBitmap(100, 100, ARGB_8888),
            ImageInfo("image/jpeg", 1291, 1936, 0),
            DataFrom.LOCAL,
            null
        )
        Assert.assertFalse(
            newBitmapResultDiskCacheHelper(sketch, request)!!.write(bitmapDecodeResult)
        )
        Assert.assertNull(newBitmapResultDiskCacheHelper(sketch, request)!!.read())

        val bitmapDecodeResult1 = BitmapDecodeResult(
            Bitmap.createBitmap(100, 100, ARGB_8888),
            ImageInfo("image/jpeg", 1291, 1936, 0),
            DataFrom.LOCAL,
            listOf(InSampledTransformed(4))
        )
        Assert.assertTrue(
            newBitmapResultDiskCacheHelper(sketch, request)!!.write(bitmapDecodeResult1)
        )
        Assert.assertNotNull(newBitmapResultDiskCacheHelper(sketch, request)!!.read())

        Assert.assertTrue(
            newBitmapResultDiskCacheHelper(sketch, request.newLoadRequest {
                bitmapResultDiskCachePolicy(ENABLED)
            })!!.write(bitmapDecodeResult1)
        )
        Assert.assertFalse(
            newBitmapResultDiskCacheHelper(sketch, request.newLoadRequest {
                bitmapResultDiskCachePolicy(READ_ONLY)
            })!!.write(bitmapDecodeResult1)
        )
        Assert.assertTrue(
            newBitmapResultDiskCacheHelper(sketch, request.newLoadRequest {
                bitmapResultDiskCachePolicy(WRITE_ONLY)
            })!!.write(bitmapDecodeResult1)
        )

        Assert.assertNotNull(newBitmapResultDiskCacheHelper(sketch, request)!!.read())
    }
}