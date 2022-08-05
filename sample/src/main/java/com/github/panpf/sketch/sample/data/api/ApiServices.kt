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
package com.github.panpf.sketch.sample.data.api

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class ApiServices(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val jsonConverterFactory = json.asConverterFactory(MediaType.get("application/json"))

    val giphy = Retrofit.Builder()
        .baseUrl("https://api.giphy.com")
        .addConverterFactory(jsonConverterFactory)
        .build()
        .create(GiphyService::class.java)

    val pexels = Retrofit.Builder()
        .baseUrl("https://api.pexels.com")
        .addConverterFactory(jsonConverterFactory)
        .client(OkHttpClient.Builder().addInterceptor {
            val newRequest = it.request()
                .newBuilder()
                .addHeader(
                    "Authorization",
                    "563492ad6f917000010000011ee9732fe9a644d0a798296f68b93c4e"
                ).build()
            it.proceed(newRequest)
        }.build())
        .build()
        .create(PexelsService::class.java)
}