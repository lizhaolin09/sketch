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
package com.github.panpf.sketch.sample.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import com.github.panpf.sketch.sample.databinding.ActivityMainBinding
import com.github.panpf.sketch.sample.service.NotificationService
import com.github.panpf.sketch.sample.ui.base.BaseBindingActivity
import com.google.android.material.internal.EdgeToEdgeUtils

class MainActivity : BaseBindingActivity<ActivityMainBinding>() {

    @SuppressLint("RestrictedApi")
    override fun onCreate(binding: ActivityMainBinding, savedInstanceState: Bundle?) {
        EdgeToEdgeUtils.applyEdgeToEdge(/* window = */ window,/* edgeToEdgeEnabled = */ true)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor("#60000000")
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.TRANSPARENT
        }
    }

    override fun onFirstResume() {
        super.onFirstResume()
        // It can only be executed here, not in onCreate.
        // Because when the app is started when the phone is locked, the app is in the background state in the onCreate method, so the app will crash when the service is started.
        startService(Intent(this@MainActivity, NotificationService::class.java))
    }
}
