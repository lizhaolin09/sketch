package com.github.panpf.sketch.decode.internal

import androidx.annotation.WorkerThread
import com.github.panpf.sketch.Image
import com.github.panpf.sketch.datasource.DataFrom
import com.github.panpf.sketch.decode.DecodeResult
import com.github.panpf.sketch.decode.ExifOrientation
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.request.internal.RequestContext
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.resize.Precision.LESS_PIXELS
import com.github.panpf.sketch.resize.Resize
import com.github.panpf.sketch.resize.internal.calculateResizeMapping
import com.github.panpf.sketch.size
import com.github.panpf.sketch.util.Rect
import com.github.panpf.sketch.util.Size
import com.github.panpf.sketch.util.requiredWorkThread


/* ************************************** sampling ********************************************** */

expect fun getMaxBitmapSize(targetSize: Size): Size

/**
 * Calculate the size of the sampled Bitmap, support for BitmapFactory or ImageDecoder
 */
expect fun calculateSampledBitmapSize(
    imageSize: Size,
    sampleSize: Int,
    mimeType: String? = null
): Size

/**
 * Calculate the size of the sampled Bitmap, support for BitmapRegionDecoder
 */
expect fun calculateSampledBitmapSizeForRegion(
    regionSize: Size,
    sampleSize: Int,
    mimeType: String? = null,
    imageSize: Size? = null
): Size


/**
 * Calculate the sample size, support for BitmapFactory or ImageDecoder
 */
fun calculateSampleSize(
    imageSize: Size,
    targetSize: Size,
    smallerSizeMode: Boolean,
    mimeType: String? = null,
): Int {
    if (imageSize.isEmpty || targetSize.isEmpty) {
        return 1
    }
    var sampleSize = 1
    var accepted = false
    val maxBitmapSize = getMaxBitmapSize(targetSize)
    while (!accepted) {
        val sampledBitmapSize = calculateSampledBitmapSize(
            imageSize = imageSize,
            sampleSize = sampleSize,
            mimeType = mimeType
        )
        accepted = checkSampledBitmapSize(
            sampledBitmapSize = sampledBitmapSize,
            targetSize = targetSize,
            smallerSizeMode = smallerSizeMode,
            maxBitmapSize = maxBitmapSize,
        )
        if (!accepted) {
            sampleSize *= 2
        }
    }
    return sampleSize
}

/**
 * Calculate the sample size, support for BitmapFactory or ImageDecoder
 */
fun calculateSampleSize(
    imageSize: Size,
    targetSize: Size,
    mimeType: String? = null
): Int = calculateSampleSize(
    imageSize = imageSize,
    targetSize = targetSize,
    smallerSizeMode = false,
    mimeType = mimeType
)

/**
 * Calculate the sample size, support for BitmapRegionDecoder
 */
fun calculateSampleSizeForRegion(
    regionSize: Size,
    targetSize: Size,
    smallerSizeMode: Boolean,
    mimeType: String? = null,
    imageSize: Size? = null,
): Int {
    if (regionSize.isEmpty || targetSize.isEmpty) {
        return 1
    }
    var sampleSize = 1
    var accepted = false
    val maxBitmapSize = getMaxBitmapSize(targetSize)
    while (!accepted) {
        val sampledBitmapSize = calculateSampledBitmapSizeForRegion(
            regionSize = regionSize,
            sampleSize = sampleSize,
            mimeType = mimeType,
            imageSize = imageSize
        )
        accepted = checkSampledBitmapSize(
            sampledBitmapSize = sampledBitmapSize,
            targetSize = targetSize,
            smallerSizeMode = smallerSizeMode,
            maxBitmapSize = maxBitmapSize,
        )
        if (!accepted) {
            sampleSize *= 2
        }
    }
    return sampleSize
}

/**
 * Calculate the sample size, support for BitmapRegionDecoder
 */
fun calculateSampleSizeForRegion(
    regionSize: Size,
    targetSize: Size,
    mimeType: String? = null,
    imageSize: Size? = null
): Int = calculateSampleSizeForRegion(
    regionSize = regionSize,
    targetSize = targetSize,
    smallerSizeMode = false,
    mimeType = mimeType,
    imageSize = imageSize
)

fun checkSampledBitmapSize(
    sampledBitmapSize: Size,
    targetSize: Size,
    smallerSizeMode: Boolean,
    maxBitmapSize: Size? = null
): Boolean {
    var accept = if (smallerSizeMode) {
        sampledBitmapSize.width <= targetSize.width && sampledBitmapSize.height <= targetSize.height
    } else {
        sampledBitmapSize.width * sampledBitmapSize.height <= targetSize.width * targetSize.height
    }
    if (accept && maxBitmapSize != null) {
        accept =
            sampledBitmapSize.width <= maxBitmapSize.width && sampledBitmapSize.height <= maxBitmapSize.height
    }
    return accept
}


/* **************************************** decode ********************************************* */

@WorkerThread
fun realDecode(
    requestContext: RequestContext,
    dataFrom: DataFrom,
    imageInfo: ImageInfo,
    decodeFull: (sampleSize: Int) -> Image,
    decodeRegion: ((srcRect: Rect, sampleSize: Int) -> Image)?
): DecodeResult {
    requiredWorkThread()
    val request = requestContext.request
    val size = requestContext.size!!
    val exifOrientationHelper = ExifOrientationHelper(imageInfo.exifOrientation)
    val imageSize = Size(imageInfo.width, imageInfo.height)
    val appliedImageSize = exifOrientationHelper?.applyToSize(imageSize) ?: imageSize
    val precision = request.precisionDecider.get(imageSize = appliedImageSize, targetSize = size)
    val scale = request.scaleDecider.get(imageSize = appliedImageSize, targetSize = size)
    val resize = Resize(
        width = size.width,
        height = size.height,
        precision = precision,
        scale = scale
    )
    val addedResize = exifOrientationHelper?.addToResize(resize, appliedImageSize) ?: resize
    val transformedList = mutableListOf<String>()
    val resizeMapping = calculateResizeMapping(
        imageSize = imageInfo.size,
        resizeSize = addedResize.size,
        precision = addedResize.precision,
        scale = addedResize.scale,
    )
    val bitmap = if (
        addedResize.shouldClip(imageInfo.size)
        && addedResize.precision != LESS_PIXELS
        && decodeRegion != null
        && resizeMapping != null
    ) {
        val sampleSize = calculateSampleSizeForRegion(
            regionSize = Size(resizeMapping.srcRect.width(), resizeMapping.srcRect.height()),
            targetSize = Size(resizeMapping.destRect.width(), resizeMapping.destRect.height()),
            smallerSizeMode = addedResize.precision.isSmallerSizeMode(),
            mimeType = imageInfo.mimeType,
            imageSize = imageSize
        )
        if (sampleSize > 1) {
            transformedList.add(createInSampledTransformed(sampleSize))
        }
        transformedList.add(createSubsamplingTransformed(resizeMapping.srcRect))
        decodeRegion(resizeMapping.srcRect, sampleSize)
    } else {
        val sampleSize = calculateSampleSize(
            imageSize = imageSize,
            targetSize = addedResize.size,
            smallerSizeMode = addedResize.precision.isSmallerSizeMode(),
            mimeType = imageInfo.mimeType
        )
        if (sampleSize > 1) {
            transformedList.add(0, createInSampledTransformed(sampleSize))
        }
        decodeFull(sampleSize)
    }
    return DecodeResult(
        image = bitmap,
        imageInfo = imageInfo,
        dataFrom = dataFrom,
        transformedList = transformedList.takeIf { it.isNotEmpty() }?.toList(),
        extras = null,
    )
}

@WorkerThread
fun DecodeResult.appliedExifOrientation(requestContext: RequestContext): DecodeResult {
    requiredWorkThread()
    if (transformedList?.getExifOrientationTransformed() != null
        || imageInfo.exifOrientation == ExifOrientation.UNDEFINED
        || imageInfo.exifOrientation == ExifOrientation.NORMAL
    ) {
        return this
    }
    val exifOrientationHelper = ExifOrientationHelper(imageInfo.exifOrientation) ?: return this
    val newImage = exifOrientationHelper.applyToImage(image) ?: return this
    val newSize = exifOrientationHelper.applyToSize(
        Size(imageInfo.width, imageInfo.height)
    )
    requestContext.sketch.logger.d("appliedExifOrientation") {
        "appliedExifOrientation. successful. ${newImage}. ${imageInfo}. '${requestContext.logKey}'"
    }
    return newResult(
        image = newImage,
        imageInfo = imageInfo.copy(size = newSize)
    ) {
        addTransformed(createExifOrientationTransformed(imageInfo.exifOrientation))
    }
}

@WorkerThread
fun DecodeResult.appliedResize(requestContext: RequestContext): DecodeResult {
    requiredWorkThread()
    val imageTransformer = image.transformer() ?: return this
    val request = requestContext.request
    val size = requestContext.size!!
    if (size.isEmpty) {
        return this
    }
    val resize = Resize(
        width = size.width,
        height = size.height,
        precision = request.precisionDecider.get(
            imageSize = Size(imageInfo.width, imageInfo.height),
            targetSize = size,
        ),
        scale = request.scaleDecider.get(
            imageSize = Size(imageInfo.width, imageInfo.height),
            targetSize = size,
        )
    )
    val newImage = if (resize.precision == LESS_PIXELS) {
        val sampleSize = calculateSampleSize(
            imageSize = image.size,
            targetSize = resize.size,
            smallerSizeMode = resize.precision.isSmallerSizeMode()
        )
        if (sampleSize != 1) {
            imageTransformer.scale(image = image, scaleFactor = 1 / sampleSize.toFloat())
        } else {
            null
        }
    } else if (resize.shouldClip(image.size)) {
        val mapping = calculateResizeMapping(
            imageSize = image.size,
            resizeSize = resize.size,
            precision = resize.precision,
            scale = resize.scale,
        )
        if (mapping != null) {
            imageTransformer.mapping(image, mapping)
        } else {
            null
        }
    } else {
        null
    }
    return if (newImage != null) {
        requestContext.sketch.logger.d("appliedResize") {
            "appliedResize. successful. ${newImage}. ${imageInfo}. '${requestContext.logKey}'"
        }
        newResult(image = newImage) {
            addTransformed(createResizeTransformed(resize))
        }
    } else {
        this
    }
}

fun Precision.isSmallerSizeMode(): Boolean {
    return this == Precision.SMALLER_SIZE
}