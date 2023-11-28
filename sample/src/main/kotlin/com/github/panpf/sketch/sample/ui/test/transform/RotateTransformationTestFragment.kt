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
package com.github.panpf.sketch.sample.ui.test.transform

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.github.panpf.sketch.cache.CachePolicy.DISABLED
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.sample.AssetImages
import com.github.panpf.sketch.sample.databinding.RotateTransformationTestFragmentBinding
import com.github.panpf.sketch.sample.ui.base.BindingFragment
import com.github.panpf.sketch.transform.RotateTransformation

class RotateTransformationTestFragment :
    BindingFragment<RotateTransformationTestFragmentBinding>() {

    private val viewModel by viewModels<RotateTransformationTestViewModel>()

    override fun onViewCreated(
        binding: RotateTransformationTestFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        binding.rotateTransformationTestButton.setOnClickListener {
            viewModel.changeRotate(viewModel.rotateData.value!! + 45)
        }

        viewModel.rotateData.observe(viewLifecycleOwner) {
            binding.rotateTransformationTestImage.displayImage(AssetImages.STATICS.first()) {
                memoryCachePolicy(DISABLED)
                resultCachePolicy(DISABLED)
                addTransformations(RotateTransformation(it))
            }
        }
    }
}