package com.github.panpf.sketch.stateimage

import com.github.panpf.sketch.Image
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.sketch.decode.internal.isExifOrientationTransformed
import com.github.panpf.sketch.decode.internal.isInSampledTransformed
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.internal.getImageInfo
import com.github.panpf.sketch.request.internal.getTransformedList
import com.github.panpf.sketch.util.format
import kotlin.math.abs

/**
 * Find a Bitmap with the same aspect ratio and not modified by Transformation as a status image from memory
 * @param uri The uri of the image, if null use ImageRequest.uriString
 */
class ThumbnailMemoryCacheStateImage(
    private val uri: String? = null,
    private val defaultImage: StateImage? = null
) : StateImage {

    override fun getImage(
        sketch: Sketch,
        request: ImageRequest,
        throwable: Throwable?
    ): Image? {
        val uri = uri ?: request.uriString
        val keys = sketch.memoryCache.keys()
        var targetCachedValue: MemoryCache.Value? = null
        var count = 0
        for (key in keys) {
            // The key is spliced by uri and options. The options start with '_'. See RequestUtils.newKey() for details.
            var paramsStartFlagIndex = key.indexOf("?_")
            if (paramsStartFlagIndex == -1) {
                paramsStartFlagIndex = key.indexOf("&_")
            }
            val uriFromKey = if (paramsStartFlagIndex != -1) {
                key.substring(startIndex = 0, endIndex = paramsStartFlagIndex)
            } else {
                key
            }
            if (uri == uriFromKey) {
                val cachedValue = sketch.memoryCache[key]?.takeIf {
                    val image = it.image
                    val bitmapAspectRatio = (image.width.toFloat() / image.height).format(1)
                    val imageInfo = it.getImageInfo()!!
                    val imageAspectRatio =
                        (imageInfo.width.toFloat() / imageInfo.height).format(1)
                    val sizeSame = abs(bitmapAspectRatio - imageAspectRatio) <= 0.1f

                    val transformedList = it.getTransformedList()
                    val noOtherTransformed =
                        transformedList == null || transformedList.all { transformed ->
                            isInSampledTransformed(transformed) || isExifOrientationTransformed(
                                transformed
                            )
                        }

                    sizeSame && noOtherTransformed
                }
                if (cachedValue != null) {
                    targetCachedValue = cachedValue
                    break
                } else if (++count >= 3) {
                    break
                }
            }
        }
        return targetCachedValue?.image ?: defaultImage?.getImage(sketch, request, throwable)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ThumbnailMemoryCacheStateImage) return false
        if (uri != other.uri) return false
        if (defaultImage != other.defaultImage) return false
        return true
    }

    override fun hashCode(): Int {
        var result = uri?.hashCode() ?: 0
        result = 31 * result + (defaultImage?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ThumbnailMemoryCacheStateImage(uri=$uri, defaultImage=$defaultImage)"
    }
}