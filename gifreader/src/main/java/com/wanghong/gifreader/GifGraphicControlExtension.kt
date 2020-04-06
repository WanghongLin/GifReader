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

internal class GifGraphicControlExtension(private val byteData: ByteArray) {

    val extensionIntroducer : Byte = byteData[0]
    val graphicControlLabel : Byte = byteData[1]
    val byteSize = byteData[2].toPositiveInt()

    private val packedField = byteData[3]
    val transparentColorFlag = packedField.and(0x01.toByte()) == 0x01.toByte()
    val userInputFlag = packedField.and(0x02.toByte()) == 0x02.toByte()
    val disposalMethod = packedField.and(0x1C.toByte()).toPositiveInt().ushr(2)
    val reserved = 0

    val delayTime = byteData[5].toPositiveInt().shl(8).plus(byteData[4].toPositiveInt())
    val transparentColorIndex = byteData[6].toPositiveInt()
    val blockTerminator = 0

    override fun toString(): String {
        return "GifGraphicControlExtension(byteData=${byteData.toHexString()}, extensionIntroducer=$extensionIntroducer, graphicControlLabel=$graphicControlLabel, byteSize=$byteSize, packedField=$packedField, transparentColorFlag=$transparentColorFlag, userInputFlag=$userInputFlag, disposalMethod=$disposalMethod, reserved=$reserved, delayTime=$delayTime, transparentColorIndex=$transparentColorIndex, blockTerminator=$blockTerminator)"
    }

    fun delayTimeMillis(): Long = delayTime.times(10).toLong()
}