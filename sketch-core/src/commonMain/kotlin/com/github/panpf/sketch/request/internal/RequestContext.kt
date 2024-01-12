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
package com.github.panpf.sketch.request.internal

import androidx.annotation.MainThread
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.drawable.SketchCountBitmapDrawable
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.util.Size
import com.github.panpf.sketch.util.requiredMainThread

class RequestContext constructor(val sketch: Sketch, val initialRequest: ImageRequest) {

    private var pendingCountDrawable: SketchCountBitmapDrawable? = null
    private val _requestList = mutableListOf(initialRequest)
    private var _request: ImageRequest = initialRequest
    private var _cacheKey: String? = null

    val requestList: List<ImageRequest>
        get() = _requestList.toList()

    val request: ImageRequest
        get() = _request

    val logKey: String = initialRequest.key

    /** Used to cache bitmaps in memory and on disk */
    @get:Synchronized
    val cacheKey: String
        get() = _cacheKey
            ?: request.newCacheKey(resizeSize!!).apply {
                _cacheKey = this
            }

    var resizeSize: Size? = null

    internal suspend fun setNewRequest(request: ImageRequest) {
        val lastRequest = this.request
        if (lastRequest != request) {
            _requestList.add(request)
            _request = request
            if (lastRequest.resizeSizeResolver != request.resizeSizeResolver) {
                resizeSize = request.resizeSizeResolver.size()
            }
            _cacheKey = null
        }
    }

    @MainThread
    fun pendingCountDrawable(drawable: SketchCountBitmapDrawable, caller: String) {
        requiredMainThread()
        completeCountDrawable(caller)
        pendingCountDrawable = drawable.apply {
            countBitmap.setIsPending(true, caller)
        }
    }

    @MainThread
    fun completeCountDrawable(caller: String) {
        requiredMainThread()
        pendingCountDrawable?.apply {
            countBitmap.setIsPending(false, caller)
        }
    }
}