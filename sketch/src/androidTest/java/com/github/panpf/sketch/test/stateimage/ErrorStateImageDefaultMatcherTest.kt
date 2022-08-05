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
package com.github.panpf.sketch.test.stateimage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.stateimage.ColorStateImage
import com.github.panpf.sketch.stateimage.ErrorStateImage.DefaultMatcher
import com.github.panpf.sketch.test.utils.TestAssets
import com.github.panpf.sketch.test.utils.getTestContext
import com.github.panpf.sketch.test.utils.getTestContextAndNewSketch
import com.github.panpf.sketch.util.asOrThrow
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorStateImageDefaultMatcherTest {

    @Test
    fun testMatch() {
        val context = getTestContext()
        val request = DisplayRequest(context, TestAssets.SAMPLE_JPEG_URI)

        DefaultMatcher(ColorStateImage(Color.RED)).apply {
            Assert.assertTrue(match(request, null))
        }

        DefaultMatcher(ColorStateImage(Color.GREEN)).apply {
            Assert.assertTrue(match(request, null))
        }
    }

    @Test
    fun testGetDrawable() {
        val (context, sketch) = getTestContextAndNewSketch()
        val request = DisplayRequest(context, TestAssets.SAMPLE_JPEG_URI)

        DefaultMatcher(ColorStateImage(Color.RED)).apply {
            Assert.assertEquals(
                Color.RED,
                getDrawable(sketch, request, null)!!.asOrThrow<ColorDrawable>().color
            )
        }

        DefaultMatcher(ColorStateImage(Color.GREEN)).apply {
            Assert.assertEquals(
                Color.GREEN,
                getDrawable(sketch, request, null)!!.asOrThrow<ColorDrawable>().color
            )
        }
    }

    @Test
    fun testToString() {
        DefaultMatcher(ColorStateImage(Color.RED)).apply {
            Assert.assertEquals(
                "DefaultMatcher(ColorStateImage(IntColor(${Color.RED})))",
                toString()
            )
        }

        DefaultMatcher(ColorStateImage(Color.GREEN)).apply {
            Assert.assertEquals(
                "DefaultMatcher(ColorStateImage(IntColor(${Color.GREEN})))",
                toString()
            )
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = DefaultMatcher(ColorStateImage(Color.RED))
        val element11 = DefaultMatcher(ColorStateImage(Color.RED))
        val element2 = DefaultMatcher(ColorStateImage(Color.GREEN))
        val element3 = DefaultMatcher(ColorStateImage(Color.BLUE))

        Assert.assertNotSame(element1, element11)
        Assert.assertNotSame(element1, element2)
        Assert.assertNotSame(element1, element3)
        Assert.assertNotSame(element2, element11)
        Assert.assertNotSame(element2, element3)

        Assert.assertEquals(element1, element1)
        Assert.assertEquals(element1, element11)
        Assert.assertNotEquals(element1, element2)
        Assert.assertNotEquals(element1, element3)
        Assert.assertNotEquals(element2, element11)
        Assert.assertNotEquals(element2, element3)
        Assert.assertNotEquals(element1, null)
        Assert.assertNotEquals(element1, Any())

        Assert.assertEquals(element1.hashCode(), element1.hashCode())
        Assert.assertEquals(element1.hashCode(), element11.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element2.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element3.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element11.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element3.hashCode())
    }
}