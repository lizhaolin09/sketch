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
package com.github.panpf.sketch.view.core.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.sketch.cache.CachePolicy.DISABLED
import com.github.panpf.sketch.disposeLoad
import com.github.panpf.sketch.imageResult
import com.github.panpf.sketch.images.ResourceImages
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.test.singleton.loadImage
import com.github.panpf.sketch.test.utils.DelayTransformation
import com.github.panpf.sketch.test.utils.MediumImageViewTestActivity
import com.github.panpf.tools4a.test.ktx.getActivitySync
import com.github.panpf.tools4a.test.ktx.launchActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageViewExtensionsTest {

    @Test
    fun testDisposeLoad() {
        val activity = MediumImageViewTestActivity::class.launchActivity().getActivitySync()
        val imageView = activity.imageView

        Assert.assertNull(imageView.drawable)
        runBlocking {
            imageView.loadImage(ResourceImages.jpeg.uri).job.join()
        }
        Assert.assertNotNull(imageView.drawable)

        runBlocking(Dispatchers.Main) {
            imageView.setImageDrawable(null)
        }
        Assert.assertNull(imageView.drawable)
        runBlocking {
            imageView.loadImage(ResourceImages.png.uri) {
                resultCachePolicy(DISABLED)
                memoryCachePolicy(DISABLED)
                addTransformations(DelayTransformation {
                    imageView.disposeLoad()
                })
            }.job.join()
        }
        Assert.assertNull(imageView.drawable)
    }

    @Test
    fun testResult() {
        val activity = MediumImageViewTestActivity::class.launchActivity().getActivitySync()
        val imageView = activity.imageView

        Assert.assertNull(imageView.imageResult)

        runBlocking {
            imageView.loadImage(ResourceImages.jpeg.uri).job.join()
        }
        Assert.assertTrue(imageView.imageResult is ImageResult.Success)

        runBlocking {
            imageView.loadImage("asset://fake.jpeg").job.join()
        }
        Assert.assertTrue(imageView.imageResult is ImageResult.Error)

        runBlocking {
            imageView.loadImage(ResourceImages.png.uri) {
                resultCachePolicy(DISABLED)
                memoryCachePolicy(DISABLED)
                addTransformations(DelayTransformation {
                    imageView.disposeLoad()
                })
            }.job.join()
        }
        Assert.assertNull(imageView.imageResult)
    }
}