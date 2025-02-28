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
package com.github.panpf.sketch.fetch

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.Resources
import android.net.Uri
import android.util.TypedValue
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.drawable.ResDrawable
import com.github.panpf.sketch.fetch.ResourceUriFetcher.Companion.SCHEME
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.source.DataFrom
import com.github.panpf.sketch.source.DrawableDataSource
import com.github.panpf.sketch.source.ResourceDataSource
import com.github.panpf.sketch.util.MimeTypeMap
import com.github.panpf.sketch.util.ifOrNull

/**
 * Sample: 'android.resource://resource?resType=drawable&resName=ic_launcher'
 * <br>
 * Use ImageRequest.context.packageName to get Resources by default
 */
fun newResourceUri(resType: String, resName: String): String =
    "$SCHEME://resource?resType=$resType&resName=$resName"

/**
 * Sample: 'android.resource://resource?resId=1031232'
 * <br>
 * Use ImageRequest.context.packageName to get Resources by default
 */
fun newResourceUri(resId: Int): String =
    "$SCHEME://resource?resId=$resId"

/**
 * Sample: 'android.resource://resource?packageName=com.github.panpf.sketch.sample&resType=drawable&resName=ic_launcher'
 * <br>
 * Use [packageName] to get Resources
 */
fun newResourceUri(packageName: String, resType: String, resName: String): String =
    "$SCHEME://resource?packageName=$packageName&resType=$resType&resName=$resName"

/**
 * Sample: 'android.resource://resource?packageName=com.github.panpf.sketch.sample&resId=1031232'
 * <br>
 * Use [packageName] to get Resources
 */
fun newResourceUri(packageName: String, resId: Int): String =
    "$SCHEME://resource?packageName=$packageName&resId=$resId"

/**
 * Sample: 'android.resource://resource?packageName=com.github.panpf.sketch.sample&resType=drawable&resName=ic_launcher'
 * <br>
 * Use current Context's packageName to get Resources
 */
fun Context.newResourceUri(resType: String, resName: String): String =
    newResourceUri(packageName, resType, resName)

/**
 * Sample: 'android.resource://resource?packageName=com.github.panpf.sketch.sample&resId=1031232'
 * <br>
 * Use current Context's packageName to get Resources
 */
fun Context.newResourceUri(resId: Int): String = newResourceUri(packageName, resId)

/**
 * Support the following uri:
 *
 * 'android.resource://resource?resType=drawable&resName=ic_launcher'
 * 'android.resource://resource?resId=1031232'
 * 'android.resource://resource?packageName=com.github.panpf.sketch.sample&resType=drawable&resName=ic_launcher'
 * 'android.resource://resource?packageName=com.github.panpf.sketch.sample&resId=1031232'
 */
class ResourceUriFetcher(
    val sketch: Sketch,
    val request: ImageRequest,
    val contentUri: Uri,
) : Fetcher {

    companion object {
        const val SCHEME = "android.resource"
    }

    @SuppressLint("DiscouragedApi")
    @WorkerThread
    override suspend fun fetch(): Result<FetchResult> = kotlin.runCatching {
        val packageName = contentUri.getQueryParameters("packageName")
            .firstOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?: request.context.packageName
        val resources: Resources = try {
            request.context.packageManager.getResourcesForApplication(packageName)
        } catch (ex: NameNotFoundException) {
            throw Resources.NotFoundException("Not found Resources by packageName: $contentUri")
        }

        val resId = contentUri.getQueryParameters("resId").firstOrNull()?.toIntOrNull()
        val finalResId = if (resId != null) {
            resId
        } else {
            val resType =
                contentUri.getQueryParameters("resType").firstOrNull()
                    ?.takeIf { it.isNotEmpty() }
            val resName =
                contentUri.getQueryParameters("resName").firstOrNull()
                    ?.takeIf { it.isNotEmpty() }
            if (resType == null || resName == null) {
                throw Resources.NotFoundException("Invalid resource uri: $contentUri")
            }
            resources.getIdentifier(resName, resType, packageName).takeIf { it != 0 }
                ?: throw Resources.NotFoundException("No found resource identifier by resType, resName: $contentUri")
        }

        val path =
            TypedValue().apply { resources.getValue(finalResId, this, true) }.string ?: ""
        val entryName = path.lastIndexOf('/').takeIf { it != -1 }
            ?.let { path.substring(it + 1) }
            ?: path.toString()
        val mimeType = MimeTypeMap.getMimeTypeFromUrl(entryName)
        val dataSource = if (resources.getResourceTypeName(finalResId) == "raw") {
            ResourceDataSource(
                sketch = sketch,
                request = request,
                packageName = packageName,
                resources = resources,
                resId = finalResId,
            )
        } else {
            DrawableDataSource(
                sketch = sketch,
                request = request,
                dataFrom = DataFrom.LOCAL,
                drawableFetcher = ResDrawable(packageName, resources, finalResId)
            )
        }
        FetchResult(dataSource, mimeType)
    }

    class Factory : Fetcher.Factory {

        override fun create(sketch: Sketch, request: ImageRequest): ResourceUriFetcher? {
            val uri = request.uri.toUri()
            return ifOrNull(SCHEME.equals(uri.scheme, ignoreCase = true)) {
                ResourceUriFetcher(sketch, request, uri)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Factory
        }

        override fun hashCode(): Int {
            return this@Factory::class.hashCode()
        }

        override fun toString(): String = "ResourceUriFetcher"
    }
}