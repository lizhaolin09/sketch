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
package com.github.panpf.sketch.test.decode.internal

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.sketch.datasource.DataFrom
import com.github.panpf.sketch.decode.internal.BitmapDecodeInterceptorChain
import com.github.panpf.sketch.decode.internal.BitmapEngineDecodeInterceptor
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.request.internal.RequestContext
import com.github.panpf.sketch.test.utils.getTestContextAndNewSketch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BitmapEngineDecodeInterceptorTest {

    @Test
    fun testIntercept() {
        val (context, sketch) = getTestContextAndNewSketch()
        val interceptors = listOf(BitmapEngineDecodeInterceptor())
        val loadRequest = LoadRequest(context, newAssetUri("sample.jpeg"))
        val requestContext = RequestContext(loadRequest)
        val chain =
            BitmapDecodeInterceptorChain(sketch, loadRequest, requestContext, null, interceptors, 0)
        val result = runBlocking {
            chain.proceed()
        }
        Assert.assertEquals(1291, result.bitmap.width)
        Assert.assertEquals(1936, result.bitmap.height)
        Assert.assertEquals(
            "ImageInfo(width=1291, height=1936, mimeType='image/jpeg', exifOrientation=NORMAL)",
            result.imageInfo.toString()
        )
        Assert.assertEquals(DataFrom.LOCAL, result.dataFrom)
        Assert.assertNull(result.transformedList)
    }

    @Test
    fun testFactoryEqualsAndHashCode() {
        val element1 = BitmapEngineDecodeInterceptor()
        val element11 = BitmapEngineDecodeInterceptor()
        val element2 = BitmapEngineDecodeInterceptor()

        Assert.assertNotSame(element1, element11)
        Assert.assertNotSame(element1, element2)
        Assert.assertNotSame(element2, element11)

        Assert.assertEquals(element1, element1)
        Assert.assertEquals(element1, element11)
        Assert.assertEquals(element1, element2)
        Assert.assertEquals(element2, element11)
        Assert.assertNotEquals(element1, null)
        Assert.assertNotEquals(element1, Any())

        Assert.assertEquals(element1.hashCode(), element1.hashCode())
        Assert.assertEquals(element1.hashCode(), element11.hashCode())
        Assert.assertEquals(element1.hashCode(), element2.hashCode())
        Assert.assertEquals(element2.hashCode(), element11.hashCode())
    }

    @Test
    fun testToString() {
        Assert.assertEquals(
            "BitmapEngineDecodeInterceptor",
            BitmapEngineDecodeInterceptor().toString()
        )
    }
}