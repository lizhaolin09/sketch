/*
 * Copyright (C) 2019 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.sketch.sample.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.SparseArray
import androidx.annotation.IntDef
import com.github.panpf.sketch.display.TransitionImageDisplayer
import com.github.panpf.sketch.process.GaussianBlurImageProcessor
import com.github.panpf.sketch.request.DisplayOptions
import com.github.panpf.sketch.request.DownloadOptions
import com.github.panpf.sketch.request.LoadOptions
import com.github.panpf.sketch.request.ShapeSize
import com.github.panpf.sketch.sample.R
import com.github.panpf.sketch.shaper.CircleImageShaper
import com.github.panpf.sketch.shaper.RoundRectImageShaper
import com.github.panpf.sketch.util.SketchUtils

object ImageOptions {
    /**
     * 通用矩形
     */
    const val RECT = 101

    /**
     * 带描边的圆形
     */
    const val CIRCULAR_STROKE = 102

    /**
     * 窗口背景
     */
    const val WINDOW_BACKGROUND = 103

    /**
     * 圆角矩形
     */
    const val ROUND_RECT = 104

    /**
     * 充满列表
     */
    const val LIST_FULL = 105

    @JvmStatic
    private val OPTIONS_ARRAY = SparseArray<OptionsHolder>()

    init {
        val transitionImageDisplayer = TransitionImageDisplayer()

        OPTIONS_ARRAY.append(RECT, object : OptionsHolder() {
            override fun onCreateOptions(context: Context): DownloadOptions {
                return DisplayOptions().apply {
                    loadingImage(R.drawable.image_loading)
                    errorImage(R.drawable.image_error)
                    pauseDownloadImage(R.drawable.image_pause_download)
                    displayer = transitionImageDisplayer
                    shapeSize = ShapeSize.byViewFixedSize()
                }
            }
        })

        OPTIONS_ARRAY.append(CIRCULAR_STROKE, object : OptionsHolder() {
            override fun onCreateOptions(context: Context): DownloadOptions {
                return DisplayOptions().apply {
                    loadingImage(R.drawable.image_loading)
                    errorImage(R.drawable.image_error)
                    pauseDownloadImage(R.drawable.image_pause_download)
                    displayer = transitionImageDisplayer
                    shaper =
                        CircleImageShaper().setStroke(Color.WHITE, SketchUtils.dp2px(context, 1))
                    shapeSize = ShapeSize.byViewFixedSize()
                }
            }
        })

        OPTIONS_ARRAY.append(WINDOW_BACKGROUND, object : OptionsHolder() {
            override fun onCreateOptions(context: Context): DownloadOptions {
                return DisplayOptions().apply {
                    processor =
                        GaussianBlurImageProcessor.makeLayerColor(Color.parseColor("#66000000"))
                    isCacheProcessedImageInDisk = true
                    bitmapConfig = Bitmap.Config.ARGB_8888   // 效果比较重要
                    shapeSize = ShapeSize.byViewFixedSize()
                    maxSize(
                        context.resources.displayMetrics.widthPixels / 4,
                        context.resources.displayMetrics.heightPixels / 4
                    )
                    displayer = TransitionImageDisplayer(true)
                }
            }
        })

        OPTIONS_ARRAY.append(ROUND_RECT, object : OptionsHolder() {
            override fun onCreateOptions(context: Context): DownloadOptions {
                return DisplayOptions().apply {
                    loadingImage(R.drawable.image_loading)
                    errorImage(R.drawable.image_error)
                    pauseDownloadImage(R.drawable.image_pause_download)
                    shaper = RoundRectImageShaper(SketchUtils.dp2px(context, 6).toFloat())
                    displayer = transitionImageDisplayer
                    shapeSize = ShapeSize.byViewFixedSize()
                }
            }
        })

        OPTIONS_ARRAY.append(LIST_FULL, object : OptionsHolder() {
            override fun onCreateOptions(context: Context): DownloadOptions {
                val displayMetrics = context.resources.displayMetrics
                return DisplayOptions().apply {
                    loadingImage(R.drawable.image_loading)
                    errorImage(R.drawable.image_error)
                    pauseDownloadImage(R.drawable.image_pause_download)
                    maxSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
                    displayer = transitionImageDisplayer
                }
            }
        })
    }

    fun getDisplayOptions(context: Context, @Type optionsId: Int): DisplayOptions {
        return OPTIONS_ARRAY.get(optionsId).getOptions(context) as DisplayOptions
    }

    fun getLoadOptions(context: Context, @Type optionsId: Int): LoadOptions {
        return OPTIONS_ARRAY.get(optionsId).getOptions(context) as LoadOptions
    }

    fun getDownloadOptions(context: Context, @Type optionsId: Int): DownloadOptions {
        return OPTIONS_ARRAY.get(optionsId).getOptions(context)
    }

    private abstract class OptionsHolder {
        private var options: DownloadOptions? = null

        fun getOptions(context: Context): DownloadOptions {
            if (options == null) {
                synchronized(this) {
                    if (options == null) {
                        options = onCreateOptions(context)
                    }
                }
            }
            return options!!
        }

        protected abstract fun onCreateOptions(context: Context): DownloadOptions
    }

    @IntDef(RECT, CIRCULAR_STROKE, WINDOW_BACKGROUND, ROUND_RECT, LIST_FULL)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type
}
