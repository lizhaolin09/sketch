package com.github.panpf.sketch.sample.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.compose.LocalPlatformContext
import com.github.panpf.sketch.sample.appSettings
import com.github.panpf.sketch.sample.ui.model.LayoutMode
import com.github.panpf.sketch.sample.ui.rememberIconLayoutGridPainter
import com.github.panpf.sketch.sample.ui.rememberIconLayoutGridStaggeredPainter
import com.github.panpf.sketch.sample.ui.rememberIconPausePainter
import com.github.panpf.sketch.sample.ui.rememberIconPlayPainter
import com.github.panpf.sketch.sample.ui.rememberIconSettingsPainter

@Composable
fun MainMenu(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.background(
            color = colorScheme.tertiaryContainer,
            shape = RoundedCornerShape(50)
        )
    ) {
        val context = LocalPlatformContext.current
        val appSettings = context.appSettings
        val modifier1 = Modifier.size(40.dp).padding(10.dp)
        val disallowAnimatedImageInList by appSettings.disallowAnimatedImageInList.collectAsState()
        val photoListLayoutMode by appSettings.photoListLayoutMode.collectAsState()
        val playIcon = if (disallowAnimatedImageInList) {
            rememberIconPlayPainter()
        } else {
            rememberIconPausePainter()
        }
        val layoutModeIcon = if (photoListLayoutMode == LayoutMode.GRID.name) {
            rememberIconLayoutGridStaggeredPainter()
        } else {
            rememberIconLayoutGridPainter()
        }
        Icon(
            painter = playIcon,
            contentDescription = null,
            modifier = modifier1.clickable {
                appSettings.disallowAnimatedImageInList.value = !disallowAnimatedImageInList
            },
            tint = colorScheme.onTertiaryContainer
        )
        Icon(
            painter = layoutModeIcon,
            contentDescription = null,
            modifier = modifier1.clickable {
                appSettings.photoListLayoutMode.value =
                    if (photoListLayoutMode == LayoutMode.GRID.name) {
                        LayoutMode.STAGGERED_GRID.name
                    } else {
                        LayoutMode.GRID.name
                    }
            },
            tint = colorScheme.onTertiaryContainer
        )
        Icon(
            painter = rememberIconSettingsPainter(),
            contentDescription = null,
            modifier = modifier1.clickable {
                // TODO Open Settings dialog
            },
            tint = colorScheme.onTertiaryContainer
        )
    }
}