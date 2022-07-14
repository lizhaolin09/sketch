package com.github.panpf.sketch.resize

import androidx.annotation.Keep
import com.github.panpf.sketch.decode.internal.ExifOrientationHelper
import com.github.panpf.sketch.util.JsonSerializable
import com.github.panpf.sketch.util.JsonSerializer
import com.github.panpf.sketch.util.Size
import org.json.JSONObject

fun fixedScale(precision: Scale): FixedScaleDecider = FixedScaleDecider(precision)

/**
 * Always return specified precision
 */
data class FixedScaleDecider(private val scale: Scale) : ScaleDecider {

    override val key: String by lazy { "Fixed($scale)" }

    override fun get(
        imageWidth: Int, imageHeight: Int, resizeWidth: Int, resizeHeight: Int
    ): Scale {
        return scale
    }

    override fun addExifOrientation(
        exifOrientationHelper: ExifOrientationHelper,
        imageSize: Size
    ): FixedScaleDecider {
        return FixedScaleDecider(exifOrientationHelper.addToScale(scale, imageSize))
    }

    override fun toString(): String {
        return "FixedScaleDecider(scale=$scale)"
    }

    override fun <T : JsonSerializable, T1 : JsonSerializer<T>> getSerializerClass(): Class<T1> {
        @Suppress("UNCHECKED_CAST")
        return Serializer::class.java as Class<T1>
    }

    @Keep
    class Serializer : JsonSerializer<FixedScaleDecider> {
        override fun toJson(t: FixedScaleDecider): JSONObject =
            JSONObject().apply {
                put("scale", t.scale.name)
            }

        override fun fromJson(jsonObject: JSONObject): FixedScaleDecider =
            FixedScaleDecider(
                Scale.valueOf(jsonObject.getString("scale"))
            )
    }
}