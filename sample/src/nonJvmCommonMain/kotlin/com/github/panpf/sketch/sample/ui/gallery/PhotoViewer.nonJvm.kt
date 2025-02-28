package com.github.panpf.sketch.sample.ui.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.ability.progressIndicator
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.sample.EventBus
import com.github.panpf.sketch.sample.appSettings
import com.github.panpf.sketch.sample.image.palette.PhotoPalette
import com.github.panpf.sketch.sample.resources.Res.drawable
import com.github.panpf.sketch.sample.resources.ic_info_baseline
import com.github.panpf.sketch.sample.resources.ic_rotate_right
import com.github.panpf.sketch.sample.resources.ic_save
import com.github.panpf.sketch.sample.resources.ic_share
import com.github.panpf.sketch.sample.resources.ic_zoom_in
import com.github.panpf.sketch.sample.ui.common.list.LoadState
import com.github.panpf.sketch.sample.ui.model.Photo
import com.github.panpf.sketch.sample.ui.util.rememberThemeSectorProgressPainter
import com.github.panpf.sketch.sample.ui.util.valueOf
import com.github.panpf.sketch.state.ThumbnailMemoryCacheStateImage
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
actual fun PhotoViewer(
    photo: Photo,
    photoPaletteState: MutableState<PhotoPalette>,
) {
    val context = LocalPlatformContext.current
    val appSettings = context.appSettings
    val coroutineScope = rememberCoroutineScope()
    val showOriginImage by appSettings.showOriginImage.collectAsState()
    val contentScaleName by appSettings.contentScale.collectAsState()
    val alignmentName by appSettings.alignment.collectAsState()
    val contentScale by remember {
        derivedStateOf {
            ContentScale.valueOf(contentScaleName)
        }
    }
    val alignment by remember {
        derivedStateOf {
            Alignment.valueOf(alignmentName)
        }
    }
    val imageUri by remember {
        derivedStateOf {
            if (showOriginImage) {
                photo.originalUrl
            } else {
                photo.mediumUrl ?: photo.originalUrl
            }
        }
    }
    val viewerSettings by appSettings.viewersCombinedFlow.collectAsState(Unit)
    val request = remember(imageUri, viewerSettings) {
        ImageRequest(context, imageUri) {
            merge(appSettings.buildViewerImageOptions())
            placeholder(ThumbnailMemoryCacheStateImage(photo.thumbnailUrl))
            crossfade(fadeStart = false)
        }
    }
    var photoInfoImageResult by remember { mutableStateOf<ImageResult?>(null) }
    Box(modifier = Modifier.fillMaxSize()) {
        // TODO ZoomAsyncImage
        val imageState = rememberAsyncImageState()
        val progressPainter = rememberThemeSectorProgressPainter()
        AsyncImage(
            request = request,
            sketch = SingletonSketch.get(context),
            state = imageState,
            contentDescription = "view image",
            modifier = Modifier
                .fillMaxSize()
                .progressIndicator(imageState, progressPainter),
            contentScale = contentScale,
            alignment = alignment,
        )

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                .padding(vertical = 30.dp),
        ) {
            val photoPalette by photoPaletteState
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        EventBus.sharePhotoFlow.emit(imageUri)
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = photoPalette.containerColor,
                    contentColor = photoPalette.contentColor
                )
            ) {
                Icon(
                    painter = painterResource(drawable.ic_share),
                    contentDescription = "share",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp),
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            IconButton(
                onClick = {
                    coroutineScope.launch {
                        EventBus.savePhotoFlow.emit(imageUri)
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = photoPalette.containerColor,
                    contentColor = photoPalette.contentColor
                )
            ) {
                Icon(
                    painter = painterResource(drawable.ic_save),
                    contentDescription = "save",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp),
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

//            val zoomIn by remember {
//                derivedStateOf {
//                    zoomState.zoomable.getNextStepScale() > zoomState.zoomable.transform.scaleX
//                }
//            }
            IconButton(
                onClick = {
                    coroutineScope.launch {
//                    val zoomable = zoomState.zoomable
//                    val nextStepScale = zoomable.getNextStepScale()
//                    zoomable.scale(nextStepScale, animated = true)
                        EventBus.toastFlow.emit("Not supported yet zoom")
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = photoPalette.containerColor,
                    contentColor = photoPalette.contentColor
                )
            ) {
                Icon(
//                    painter = if (zoomIn) {
//                        painterResource(drawable.ic_zoom_in)
//                    } else {
//                        painterResource(drawable.ic_zoom_out)
//                    },
                    painter = painterResource(drawable.ic_zoom_in),
                    contentDescription = "zoom",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp),
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            IconButton(
                onClick = {
                    coroutineScope.launch {
//                    val zoomable = zoomState.zoomable
//                    zoomable.rotate(zoomable.transform.rotation.roundToInt() + 90)
                        EventBus.toastFlow.emit("Not supported yet rotate")
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = photoPalette.containerColor,
                    contentColor = photoPalette.contentColor
                )
            ) {
                Icon(
                    painter = painterResource(drawable.ic_rotate_right),
                    contentDescription = "right rotate",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp),
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            IconButton(
                onClick = {
                    val imageResult = imageState.result
                    if (imageResult != null) {
                        photoInfoImageResult = imageResult
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = photoPalette.containerColor,
                    contentColor = photoPalette.contentColor
                )
            ) {
                Icon(
                    painter = painterResource(drawable.ic_info_baseline),
                    contentDescription = "info",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp),
                )
            }
        }

        LoadState(
            modifier = Modifier.align(Alignment.Center),
            imageState = imageState
        )
    }

    if (photoInfoImageResult != null) {
        PhotoInfoDialog(photoInfoImageResult) {
            photoInfoImageResult = null
        }
    }
}