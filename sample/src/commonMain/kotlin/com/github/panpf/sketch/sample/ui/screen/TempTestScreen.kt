package com.github.panpf.sketch.sample.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.panpf.sketch.sample.ui.screen.base.BaseScreen
import com.github.panpf.sketch.sample.ui.screen.base.ToolbarScaffold

class TempTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold(title = "TempTest") {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("In development...")
            }
        }
    }
}