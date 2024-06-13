@file:OptIn(InternalResourceApi::class)

package com.github.panpf.sketch.compose.core.test.painter

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.github.panpf.sketch.test.utils.SizeColorPainter
import com.github.panpf.sketch.painter.rememberIconAnimatablePainter
import com.github.panpf.sketch.painter.asEquality
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi

class IconAnimatablePainterTest {
    // TODO test

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    fun CreateFunctionTest() {
        val painterIcon = Color.Cyan.let { SizeColorPainter(it, Size(100f, 100f)).asEquality(it) }
        val resourceIcon = DrawableResource(
            "drawable:test_icon",
            setOf(
                org.jetbrains.compose.resources.ResourceItem(
                    setOf(),
                    "composeResources/sketch_root.sample.generated.resources/drawable/ic_info_baseline.xml",
                    -1,
                    -1
                ),
            )
        )
        val painterBackground = Color.Gray.let { SizeColorPainter(it, Size(100f, 100f)).asEquality(it) }
        val colorBackground = Color.DarkGray
        val resourceBackground = DrawableResource(
            "drawable:test_background",
            setOf(
                org.jetbrains.compose.resources.ResourceItem(
                    setOf(),
                    "composeResources/sketch_root.sample.generated.resources/drawable/ic_info_baseline.xml",
                    -1,
                    -1
                ),
            )
        )
        val iconSize = Size(200f, 200f)
        val iconTint = Color.Magenta

        // painter icon
        rememberIconAnimatablePainter(
            icon = painterIcon,
            background = painterBackground,
            iconSize = iconSize,
            iconTint = iconTint
        )
        rememberIconAnimatablePainter(
            icon = painterIcon,
            background = colorBackground,
            iconSize = iconSize,
            iconTint = iconTint
        )
        rememberIconAnimatablePainter(
            icon = painterIcon,
            background = resourceBackground,
            iconSize = iconSize,
            iconTint = iconTint
        )

        rememberIconAnimatablePainter(
            icon = painterIcon,
            background = painterBackground,
            iconSize = iconSize,
        )
        rememberIconAnimatablePainter(
            icon = painterIcon,
            background = colorBackground,
            iconSize = iconSize,
        )
        rememberIconAnimatablePainter(
            icon = painterIcon,
            background = resourceBackground,
            iconSize = iconSize,
        )

        rememberIconAnimatablePainter(
            icon = painterIcon,
            background = painterBackground,
            iconTint = iconTint
        )
        rememberIconAnimatablePainter(
            icon = painterIcon,
            background = colorBackground,
            iconTint = iconTint
        )
        rememberIconAnimatablePainter(
            icon = painterIcon,
            background = resourceBackground,
            iconTint = iconTint
        )

        rememberIconAnimatablePainter(
            icon = painterIcon,
            iconSize = iconSize,
            iconTint = iconTint
        )

        rememberIconAnimatablePainter(
            icon = painterIcon,
            background = painterBackground,
        )
        rememberIconAnimatablePainter(
            icon = painterIcon,
            background = colorBackground,
        )
        rememberIconAnimatablePainter(
            icon = painterIcon,
            background = resourceBackground,
        )

        rememberIconAnimatablePainter(
            icon = painterIcon,
            iconSize = iconSize,
        )
        rememberIconAnimatablePainter(
            icon = painterIcon,
            iconTint = iconTint
        )

        rememberIconAnimatablePainter(
            icon = painterIcon,
        )

        // resource icon
        rememberIconAnimatablePainter(
            icon = resourceIcon,
            background = painterBackground,
            iconSize = iconSize,
            iconTint = iconTint
        )
        rememberIconAnimatablePainter(
            icon = resourceIcon,
            background = colorBackground,
            iconSize = iconSize,
            iconTint = iconTint
        )
        rememberIconAnimatablePainter(
            icon = resourceIcon,
            background = resourceBackground,
            iconSize = iconSize,
            iconTint = iconTint
        )

        rememberIconAnimatablePainter(
            icon = resourceIcon,
            background = painterBackground,
            iconSize = iconSize,
        )
        rememberIconAnimatablePainter(
            icon = resourceIcon,
            background = colorBackground,
            iconSize = iconSize,
        )
        rememberIconAnimatablePainter(
            icon = resourceIcon,
            background = resourceBackground,
            iconSize = iconSize,
        )

        rememberIconAnimatablePainter(
            icon = resourceIcon,
            background = painterBackground,
            iconTint = iconTint
        )
        rememberIconAnimatablePainter(
            icon = resourceIcon,
            background = colorBackground,
            iconTint = iconTint
        )
        rememberIconAnimatablePainter(
            icon = resourceIcon,
            background = resourceBackground,
            iconTint = iconTint
        )

        rememberIconAnimatablePainter(
            icon = resourceIcon,
            iconSize = iconSize,
            iconTint = iconTint
        )

        rememberIconAnimatablePainter(
            icon = resourceIcon,
            background = painterBackground,
        )
        rememberIconAnimatablePainter(
            icon = resourceIcon,
            background = colorBackground,
        )
        rememberIconAnimatablePainter(
            icon = resourceIcon,
            background = resourceBackground,
        )

        rememberIconAnimatablePainter(
            icon = resourceIcon,
            iconSize = iconSize,
        )
        rememberIconAnimatablePainter(
            icon = resourceIcon,
            iconTint = iconTint
        )

        rememberIconAnimatablePainter(
            icon = resourceIcon,
        )
    }
}