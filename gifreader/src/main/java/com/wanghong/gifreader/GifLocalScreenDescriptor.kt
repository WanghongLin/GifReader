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

import kotlin.experimental.and

internal class GifLocalScreenDescriptor(private val byteData: ByteArray) {

    private val canvasWidthBytes : ByteArray = byteData.sliceArray(IntRange(0, 1))
    private val canvasHeightBytes : ByteArray = byteData.sliceArray(IntRange(2, 3))

    val packedField = byteData[4]

    val globalColorTableFlag = packedField.and(0x80.toByte()) == 0x80.toByte()
    val colorResolution = packedField.and(0x70.toByte()).toPositiveInt().ushr(4)
    val sortFlag = packedField.and(0x08.toByte()) == 0x08.toByte()
    val globalColorTableSize = packedField.and(0x07.toByte()).toPositiveInt()

    val backgroundColorIndex = byteData[5].toPositiveInt()
    val pixelAspectRatio = byteData[6].toPositiveInt()

    val canvasWidth = canvasWidthBytes[1].toPositiveInt().shl(8).plus(canvasWidthBytes[0].toPositiveInt())
    val canvasHeight = canvasHeightBytes[1].toPositiveInt().shl(8).plus(canvasHeightBytes[0].toPositiveInt())

    override fun toString(): String {
        return "GifLocalScreenDescriptor(byteData=${byteData.toHexString()}, canvasWidthBytes=${canvasWidthBytes.toHexString()}, canvasHeightBytes=${canvasHeightBytes.toHexString()}, packedField=$packedField, globalColorTableFlag=$globalColorTableFlag, colorResolution=$colorResolution, sortFlag=$sortFlag, globalColorTableSize=$globalColorTableSize, backgroundColorIndex=$backgroundColorIndex, pixelAspectRatio=$pixelAspectRatio, canvasWidth=$canvasWidth, canvasHeight=$canvasHeight)"
    }

}