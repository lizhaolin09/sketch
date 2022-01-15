package com.github.panpf.sketch.decode

import android.graphics.BitmapFactory.Options
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.datasource.AssetsDataSource
import com.github.panpf.sketch.datasource.ByteArrayDataSource
import com.github.panpf.sketch.datasource.ContentDataSource
import com.github.panpf.sketch.datasource.DataSource
import com.github.panpf.sketch.datasource.DiskCacheDataSource
import com.github.panpf.sketch.datasource.DrawableResDataSource
import com.github.panpf.sketch.datasource.FileDataSource
import com.github.panpf.sketch.decode.internal.decodeBitmap
import com.github.panpf.sketch.decode.internal.readImageInfo
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.drawable.SketchGifDrawableImpl
import com.github.panpf.sketch.request.DisplayRequest

class GifDrawableDecoder(
    private val sketch: Sketch,
    private val request: DisplayRequest,
    private val dataSource: DataSource
) : DrawableDecoder {

    // todo 参考 coil 的 gif 实现

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun decodeDrawable(): DrawableDecodeResult {
        val request = request
        val imageInfo = dataSource.readImageInfo(request)
        val gifDrawable = when (val source = dataSource) {
            is ByteArrayDataSource -> {
                SketchGifDrawableImpl(
                    request.key,
                    request.uriString,
                    imageInfo,
                    source.from,
                    sketch.bitmapPoolHelper,
                    source.data
                )
            }
            is DiskCacheDataSource -> {
                SketchGifDrawableImpl(
                    request.key,
                    request.uriString,
                    imageInfo,
                    source.from,
                    sketch.bitmapPoolHelper,
                    source.diskCacheEntry.file
                )
            }
            is DrawableResDataSource -> {
                SketchGifDrawableImpl(
                    request.key,
                    request.uriString,
                    imageInfo,
                    source.from,
                    sketch.bitmapPoolHelper,
                    source.context.resources,
                    source.drawableId
                )
            }
            is ContentDataSource -> {
                SketchGifDrawableImpl(
                    request.key,
                    request.uriString,
                    imageInfo,
                    source.from,
                    sketch.bitmapPoolHelper,
                    source.context.contentResolver,
                    source.contentUri
                )
            }
            is FileDataSource -> {
                SketchGifDrawableImpl(
                    request.key,
                    request.uriString,
                    imageInfo,
                    source.from,
                    sketch.bitmapPoolHelper,
                    source.file
                )
            }
            is AssetsDataSource -> {
                SketchGifDrawableImpl(
                    request.key,
                    request.uriString,
                    imageInfo,
                    source.from,
                    sketch.bitmapPoolHelper,
                    source.context.assets,
                    source.assetsFilePath
                )
            }
            else -> {
                throw Exception("Unsupported DataSource: ${source::class.qualifiedName}")
            }
        }
        return DrawableDecodeResult(gifDrawable, imageInfo, dataSource.from)
    }

    override fun close() {

    }

    class Factory : com.github.panpf.sketch.decode.DrawableDecoder.Factory {

        override fun create(
            sketch: Sketch,
            request: DisplayRequest,
            fetchResult: FetchResult
        ): GifDrawableDecoder? {
            if (request.disabledAnimationDrawable != true) {
                // todo 改进判断方式，参考 coil 当 mimeType 判断不出来时用文件头标识判断
                val mimeType = fetchResult.mimeType ?: Options().apply {
                    inJustDecodeBounds = true
                    fetchResult.dataSource.decodeBitmap(this)
                }.outMimeType.orEmpty()
                if (mimeType == "image/gif") {
                    return GifDrawableDecoder(sketch, request, fetchResult.dataSource)
                }
            }
            return null
        }
    }
}