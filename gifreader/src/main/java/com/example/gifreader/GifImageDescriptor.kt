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

package com.example.gifreader

import kotlin.experimental.and

internal class GifImageDescriptor(private val byteData: ByteArray) {


    val imageSeparator: Byte = byteData[0]
    val imageLeft = byteData[2].toPositiveInt().shl(4).plus(byteData[1].toPositiveInt())
    val imageTop = byteData[4].toPositiveInt().shl(4).plus(byteData[3].toPositiveInt())
    val imageWidth = byteData[6].toPositiveInt().shl(4).plus(byteData[5].toPositiveInt())
    val imageHeight = byteData[8].toPositiveInt().shl(4).plus(byteData[7].toPositiveInt())

    val localColorTableFlag = byteData[9].and(0x80.toByte()) == 0x80.toByte()
    val interlaceFlag = byteData[9].and(0x40.toByte()) == 0x40.toByte()
    val sortFlag = byteData[9].and(0x20.toByte()) == 0x20.toByte()
    val reserved = 0
    val localColorTableSize = byteData[9].and(0x07).toPositiveInt()

    override fun toString(): String {
        return "GifImageDescriptor(byteData=${byteData.toHexString()}, imageSeparator=$imageSeparator, imageLeft=$imageLeft, imageTop=$imageTop, imageWidth=$imageWidth, imageHeight=$imageHeight, localColorTableFlag=$localColorTableFlag, interlaceFlag=$interlaceFlag, sortFlag=$sortFlag, reserved=$reserved, localColorTableSize=$localColorTableSize)"
    }


}