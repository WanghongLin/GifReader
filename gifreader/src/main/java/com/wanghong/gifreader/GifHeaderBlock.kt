/*
 * Copyright 2018 wanghonglin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanghong.gifreader

internal class GifHeaderBlock(private val headerData: ByteArray) {

    init {
        if (headerData.size != GIF_HEADER_BLOCK_LENGTH) {
            throw RuntimeException("Gif header block length not match")
        }
    }

    val signature = String(headerData.sliceArray(IntRange(0, 2)).toCharArray())
    val version = String(headerData.sliceArray(IntRange(3, headerData.size-1)).toCharArray())
    override fun toString(): String {
        return "GifHeaderBlock(headerData=${headerData.toHexString()}, signature='$signature', version='$version')"
    }
}