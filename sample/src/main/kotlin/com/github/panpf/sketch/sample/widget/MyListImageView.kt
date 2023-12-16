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
package com.github.panpf.sketch.sample.widget

import android.content.Context
import android.util.AttributeSet
import com.github.panpf.sketch.sample.appSettingsService
import com.github.panpf.sketch.sample.ui.common.createMimeTypeLogoMap
import com.github.panpf.sketch.sample.util.collectWithLifecycle
import com.github.panpf.sketch.sample.util.lifecycleOwner
import com.github.panpf.sketch.viewability.removeDataFromLogo
import com.github.panpf.sketch.viewability.removeMimeTypeLogo
import com.github.panpf.sketch.viewability.removeProgressIndicator
import com.github.panpf.sketch.viewability.showDataFromLogo
import com.github.panpf.sketch.viewability.showMimeTypeLogoWithDrawable
import com.github.panpf.sketch.viewability.showSectorProgressIndicator
import com.github.panpf.tools4a.dimen.ktx.dp2px

class MyListImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : MyImageView(context, attrs, defStyle) {

    private val mimeTypeLogoMap by lazy { createMimeTypeLogoMap() }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        appSettingsService.showProgressIndicatorInList.stateFlow.collectWithLifecycle(lifecycleOwner) { show ->
            setShowProgressIndicator(show = show)
        }
        appSettingsService.showMimeTypeLogoInLIst.stateFlow.collectWithLifecycle(lifecycleOwner) { show ->
            setShowMimeTypeLogo(show = show)
        }
        appSettingsService.showDataFromLogo.stateFlow.collectWithLifecycle(lifecycleOwner) { show ->
            setShowDataFromLogo(show = show)
        }
    }

    private fun setShowProgressIndicator(show: Boolean) {
        if (show) {
            showSectorProgressIndicator(hiddenWhenIndeterminate = true)
        } else {
            removeProgressIndicator()
        }
    }

    private fun setShowMimeTypeLogo(show: Boolean) {
        if (show) {
            showMimeTypeLogoWithDrawable(mimeTypeLogoMap, 4.dp2px)
        } else {
            removeMimeTypeLogo()
        }
    }

    private fun setShowDataFromLogo(show: Boolean) {
        if (show) {
            showDataFromLogo()
        } else {
            removeDataFromLogo()
        }
    }
}