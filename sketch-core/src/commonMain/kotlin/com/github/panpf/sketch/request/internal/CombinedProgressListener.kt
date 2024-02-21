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

import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.Progress
import com.github.panpf.sketch.request.ProgressListener

class CombinedProgressListener(
    val fromTargetProgressListener: ProgressListener?,
    val fromBuilderProgressListener: ProgressListener?,
    val fromBuilderProgressListeners: List<ProgressListener>? = null,
) : ProgressListener {

    override fun onUpdateProgress(request: ImageRequest, progress: Progress) {
        fromTargetProgressListener?.onUpdateProgress(request, progress)
        fromBuilderProgressListener?.onUpdateProgress(request, progress)
        fromBuilderProgressListeners?.forEach {
            it.onUpdateProgress(request, progress)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CombinedProgressListener) return false
        if (fromTargetProgressListener != other.fromTargetProgressListener) return false
        if (fromBuilderProgressListener != other.fromBuilderProgressListener) return false
        if (fromBuilderProgressListeners != other.fromBuilderProgressListeners) return false
        return true
    }

    override fun hashCode(): Int {
        var result = fromTargetProgressListener.hashCode()
        result = 31 * result + (fromBuilderProgressListener?.hashCode() ?: 0)
        result = 31 * result + (fromBuilderProgressListeners?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "CombinedProgressListener(" +
                "fromProvider=$fromTargetProgressListener, " +
                "fromBuilder=$fromBuilderProgressListener, " +
                "fromBuilderProgressListeners=$fromBuilderProgressListeners" +
                ")"
    }
}