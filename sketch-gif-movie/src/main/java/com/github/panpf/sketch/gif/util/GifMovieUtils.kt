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
package com.github.panpf.sketch.gif.util

import android.graphics.Bitmap
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES


internal val Bitmap.allocationByteCountCompat: Int
    get() {
        return when {
            this.isRecycled -> 0
            VERSION.SDK_INT >= VERSION_CODES.KITKAT -> this.allocationByteCount
            else -> this.byteCount
        }
    }