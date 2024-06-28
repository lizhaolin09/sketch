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
package com.github.panpf.sketch.sample.ui.test

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.github.panpf.sketch.cache.CachePolicy.DISABLED
import com.github.panpf.sketch.loadImage
import com.github.panpf.sketch.images.ResourceImages
import com.github.panpf.sketch.sample.databinding.FragmentTestTempBinding
import com.github.panpf.sketch.sample.ui.base.BaseToolbarBindingFragment

class TempTestFragment : BaseToolbarBindingFragment<FragmentTestTempBinding>() {

    override fun getStatusBarInsetsView(binding: FragmentTestTempBinding): View {
        return binding.root
    }

    override fun getNavigationBarInsetsView(binding: FragmentTestTempBinding): View {
        return binding.root
    }

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: FragmentTestTempBinding,
        savedInstanceState: Bundle?
    ) {
        toolbar.title = "Temp"

        binding.myImage.apply {
            loadImage(ResourceImages.jpeg.uri) {
                memoryCachePolicy(DISABLED)
                resultCachePolicy(DISABLED)
            }
        }
    }
}