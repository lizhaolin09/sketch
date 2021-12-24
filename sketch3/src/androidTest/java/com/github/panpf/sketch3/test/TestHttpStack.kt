package com.github.panpf.sketch3.test

import android.content.Context
import android.net.Uri
import com.github.panpf.sketch3.common.http.HttpStack
import com.github.panpf.sketch3.test.internal.SlowInputStream
import java.io.InputStream

class TestHttpStack(private val context: Context, val readDelayMillis: Long? = null) : HttpStack {

    companion object {
        val urls = arrayOf(
            TestUri(
                Uri.parse("http://5b0988e595225.cdn.sohucs.com/images/20171219/fd5717876ab046b8aa889c9aaac4b56c.jpeg"),
                540456
            )
        )
    }

    override fun getResponse(uri: String): HttpStack.Response {
        return TestResponse(context, uri.substring(uri.lastIndexOf("/") + 1), readDelayMillis)
    }

    class TestResponse(
        private val context: Context,
        private val assetFileName: String,
        private val readDelayMillis: Long? = null
    ) :
        HttpStack.Response {
        override val code: Int
            get() = 200
        override val message: String?
            get() = null
        override val contentLength: Long
            get() = 540456
        override val contentType: String
            get() = "image/jpeg"
        override val isContentChunked: Boolean
            get() = false
        override val contentEncoding: String?
            get() = null

        override fun getHeaderField(name: String): String? {
            return null
        }

        override fun getHeaderFieldInt(name: String, defaultValue: Int): Int {
            return defaultValue
        }

        override fun getHeaderFieldLong(name: String, defaultValue: Long): Long {
            return defaultValue
        }

        override val headersString: String?
            get() = null
        override val content: InputStream
            get() = context.assets.open(assetFileName).run {
                if (readDelayMillis != null) {
                    SlowInputStream(this, readDelayMillis)
                } else {
                    this
                }
            }

        override fun releaseConnection() {
        }
    }

    class TestUri(val uri: Uri, val contentLength: Long)
}