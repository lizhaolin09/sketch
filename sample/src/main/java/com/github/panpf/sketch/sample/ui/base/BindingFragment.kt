/*
 * Copyright (C) 2021 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.panpf.sketch.sample.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.github.panpf.sketch.sample.R
import com.github.panpf.sketch.sample.util.instanceViewBinding

abstract class BindingFragment<VIEW_BINDING : ViewBinding> : BaseFragment() {

    private var binding: VIEW_BINDING? = null

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = (this@BindingFragment::class.java
        .instanceViewBinding(0, inflater, container) as VIEW_BINDING).apply {
        this@BindingFragment.binding = this
        if (root.background == null) {
            root.setBackgroundResource(R.color.windowBackground)
        }
    }.root

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewCreated(this.binding!!, savedInstanceState)
    }

    abstract fun onViewCreated(binding: VIEW_BINDING, savedInstanceState: Bundle?)

    override fun onDestroyView() {
        this.binding = null
        super.onDestroyView()
    }
}