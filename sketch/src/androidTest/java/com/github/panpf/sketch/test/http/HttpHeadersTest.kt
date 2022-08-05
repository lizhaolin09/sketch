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
package com.github.panpf.sketch.test.http

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.sketch.http.HttpHeaders
import com.github.panpf.sketch.http.isNotEmpty
import com.github.panpf.sketch.http.merged
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HttpHeadersTest {

    @Test
    fun testNewBuilder() {
        val httpHeaders = HttpHeaders.Builder().apply {
            set("key1", "value1")
            set("key2", "value2")
            add("key3", "value3")
            add("key3", "value31")
        }.build()

        Assert.assertEquals(httpHeaders, httpHeaders.newBuilder().build())
        Assert.assertNotEquals(httpHeaders, httpHeaders.newBuilder {
            add("key3", "value32")
        }.build())

        Assert.assertEquals(httpHeaders, httpHeaders.newHttpHeaders())
        Assert.assertNotEquals(httpHeaders, httpHeaders.newHttpHeaders() {
            add("key3", "value32")
        })
    }

    @Test
    fun testSizeAndCount() {
        HttpHeaders.Builder().build().apply {
            Assert.assertEquals(0, size)
            Assert.assertEquals(0, addSize)
            Assert.assertEquals(0, setSize)
        }

        HttpHeaders.Builder().apply {
            set("key1", "value1")
        }.build().apply {
            Assert.assertEquals(1, size)
            Assert.assertEquals(0, addSize)
            Assert.assertEquals(1, setSize)
        }

        HttpHeaders.Builder().apply {
            set("key1", "value1")
            set("key2", "value2")
            set("key1", "value11")
            add("key3", "value3")
            add("key3", "value31")
        }.build().apply {
            Assert.assertEquals(4, size)
            Assert.assertEquals(2, addSize)
            Assert.assertEquals(2, setSize)
        }
    }

    @Test
    fun testIsEmptyAndIsNotEmpty() {
        HttpHeaders.Builder().build().apply {
            Assert.assertTrue(isEmpty())
            Assert.assertFalse(isNotEmpty())
        }

        HttpHeaders.Builder().apply {
            set("key1", "value1")
        }.build().apply {
            Assert.assertFalse(isEmpty())
            Assert.assertTrue(isNotEmpty())
        }

        HttpHeaders.Builder().apply {
            add("key1", "value1")
        }.build().apply {
            Assert.assertFalse(isEmpty())
            Assert.assertTrue(isNotEmpty())
        }

        HttpHeaders.Builder().apply {
            set("key1", "value1")
            add("key2", "value2")
        }.build().apply {
            Assert.assertFalse(isEmpty())
            Assert.assertTrue(isNotEmpty())
        }
    }

    @Test
    fun testAddSetGetRemove() {
        HttpHeaders.Builder().build().apply {
            Assert.assertNull(getSet("key1"))
            Assert.assertNull(getAdd("key2"))
        }

        HttpHeaders.Builder().apply {
            set("key1", "value1")
        }.build().apply {
            Assert.assertEquals("value1", getSet("key1"))
            Assert.assertNull(getAdd("key2"))
        }

        HttpHeaders.Builder().apply {
            add("key2", "value2")
        }.build().apply {
            Assert.assertNull(getSet("key1"))
            Assert.assertEquals(listOf("value2"), getAdd("key2"))
        }

        HttpHeaders.Builder().apply {
            set("key1", "value1")
            add("key2", "value2")
        }.build().apply {
            Assert.assertEquals("value1", getSet("key1"))
            Assert.assertEquals(listOf("value2"), getAdd("key2"))
        }

        // key conflict
        HttpHeaders.Builder().apply {
            set("key1", "value1")
            set("key1", "value11")
            add("key2", "value2")
            add("key2", "value21")
        }.build().apply {
            Assert.assertEquals("value11", getSet("key1"))
            Assert.assertEquals(listOf("value2", "value21"), getAdd("key2"))
        }

        // key conflict on add set
        HttpHeaders.Builder().apply {
            set("key1", "value1")
            add("key1", "value11")
        }.build().apply {
            Assert.assertNull(getSet("key1"))
            Assert.assertEquals(listOf("value11"), getAdd("key1"))
        }
        HttpHeaders.Builder().apply {
            add("key1", "value11")
            set("key1", "value1")
        }.build().apply {
            Assert.assertEquals("value1", getSet("key1"))
            Assert.assertNull(getAdd("key1"))
        }

        // remove
        HttpHeaders.Builder().apply {
            set("key1", "value1")
            add("key2", "value2")
        }.build().apply {
            Assert.assertEquals("value1", getSet("key1"))
            Assert.assertEquals(listOf("value2"), getAdd("key2"))
        }.newHttpHeaders {
            removeAll("key1")
        }.apply {
            Assert.assertNull(getSet("key1"))
            Assert.assertEquals(listOf("value2"), getAdd("key2"))
        }
        HttpHeaders.Builder().apply {
            set("key1", "value1")
            add("key2", "value2")
        }.build().apply {
            Assert.assertEquals("value1", getSet("key1"))
            Assert.assertEquals(listOf("value2"), getAdd("key2"))
        }.newHttpHeaders {
            removeAll("key2")
        }.apply {
            Assert.assertEquals("value1", getSet("key1"))
            Assert.assertNull(getAdd("key2"))
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = HttpHeaders.Builder().apply {
            set("key1", "value1")
            add("key2", "value2")
        }.build()
        val element11 = HttpHeaders.Builder().apply {
            set("key1", "value1")
            add("key2", "value2")
        }.build()
        val element2 = HttpHeaders.Builder().apply {
            set("key1", "value1")
            add("key3", "value3")
        }.build()
        val element3 = HttpHeaders.Builder().apply {
            set("key3", "value3")
            add("key2", "value2")
        }.build()

        Assert.assertNotSame(element1, element11)
        Assert.assertNotSame(element1, element2)
        Assert.assertNotSame(element1, element3)
        Assert.assertNotSame(element2, element11)
        Assert.assertNotSame(element2, element3)

        Assert.assertEquals(element1, element1)
        Assert.assertEquals(element1, element11)
        Assert.assertNotEquals(element1, element2)
        Assert.assertNotEquals(element1, element3)
        Assert.assertNotEquals(element2, element11)
        Assert.assertNotEquals(element2, element3)
        Assert.assertNotEquals(element1, null)
        Assert.assertNotEquals(element1, Any())

        Assert.assertEquals(element1.hashCode(), element1.hashCode())
        Assert.assertEquals(element1.hashCode(), element11.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element2.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element3.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element11.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element3.hashCode())
    }

    @Test
    fun testToString() {
        HttpHeaders.Builder().build().apply {
            Assert.assertEquals("HttpHeaders(sets=[],adds=[])", toString())
        }

        HttpHeaders.Builder().apply {
            set("key1", "value1")
            add("key2", "value2")
        }.build().apply {
            Assert.assertEquals("HttpHeaders(sets=[key1:value1],adds=[key2:value2])", toString())
        }

        HttpHeaders.Builder().apply {
            set("key1", "value1")
            add("key2", "value2")
            add("key2", "value21")
        }.build().apply {
            Assert.assertEquals(
                "HttpHeaders(sets=[key1:value1],adds=[key2:value2,key2:value21])",
                toString()
            )
        }
    }

    @Test
    fun testMerged() {
        val httpHeaders0 = HttpHeaders.Builder().build().apply {
            Assert.assertEquals("HttpHeaders(sets=[],adds=[])", toString())
        }

        val httpHeaders1 = HttpHeaders.Builder().apply {
            set("set1", "setValue1")
            add("add1", "addValue1")
        }.build().apply {
            Assert.assertEquals(
                "HttpHeaders(sets=[set1:setValue1],adds=[add1:addValue1])",
                toString()
            )
        }

        val httpHeaders11 = HttpHeaders.Builder().apply {
            set("set1", "setValue11")
            add("add1", "addValue11")
        }.build().apply {
            Assert.assertEquals(
                "HttpHeaders(sets=[set1:setValue11],adds=[add1:addValue11])",
                toString()
            )
        }

        val httpHeaders2 = HttpHeaders.Builder().apply {
            set("set21", "setValue21")
            set("set22", "setValue22")
            add("add21", "addValue21")
            add("add22", "addValue22")
        }.build().apply {
            Assert.assertEquals(
                "HttpHeaders(sets=[set21:setValue21,set22:setValue22],adds=[add21:addValue21,add22:addValue22])",
                toString()
            )
        }

        httpHeaders0.merged(httpHeaders0).apply {
            Assert.assertEquals("HttpHeaders(sets=[],adds=[])", toString())
        }
        httpHeaders0.merged(httpHeaders1).apply {
            Assert.assertEquals(
                "HttpHeaders(sets=[set1:setValue1],adds=[add1:addValue1])",
                toString()
            )
        }
        httpHeaders0.merged(httpHeaders2).apply {
            Assert.assertEquals(
                "HttpHeaders(sets=[set21:setValue21,set22:setValue22],adds=[add21:addValue21,add22:addValue22])",
                toString()
            )
        }

        httpHeaders1.merged(httpHeaders2).apply {
            Assert.assertEquals(
                "HttpHeaders(sets=[set1:setValue1,set21:setValue21,set22:setValue22],adds=[add1:addValue1,add21:addValue21,add22:addValue22])",
                toString()
            )
        }

        httpHeaders1.merged(httpHeaders11).apply {
            Assert.assertEquals(
                "HttpHeaders(sets=[set1:setValue1],adds=[add1:addValue1,add1:addValue11])",
                toString()
            )
        }
        httpHeaders11.merged(httpHeaders1).apply {
            Assert.assertEquals(
                "HttpHeaders(sets=[set1:setValue11],adds=[add1:addValue11,add1:addValue1])",
                toString()
            )
        }

        httpHeaders1.merged(null).apply {
            Assert.assertSame(httpHeaders1, this)
        }
        null.merged(httpHeaders1).apply {
            Assert.assertSame(httpHeaders1, this)
        }
    }
}