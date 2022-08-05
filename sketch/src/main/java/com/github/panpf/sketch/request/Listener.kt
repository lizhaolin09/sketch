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
package com.github.panpf.sketch.request

import androidx.annotation.MainThread

/**
 * A set of callbacks for an [ImageRequest].
 */
interface Listener<REQUEST : ImageRequest, SUCCESS : ImageResult.Success, ERROR : ImageResult.Error> {

    /**
     * Called if the request is started.
     */
    @MainThread
    fun onStart(request: REQUEST) {
    }

    /**
     * Called if the request completes successfully.
     */
    @MainThread
    fun onSuccess(request: REQUEST, result: SUCCESS) {
    }

    /**
     * Called if an error occurs while executing the request.
     */
    @MainThread
    fun onError(request: REQUEST, result: ERROR) {
    }

    /**
     * Called if the request is cancelled.
     */
    @MainThread
    fun onCancel(request: REQUEST) {
    }
}