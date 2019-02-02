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

internal class GifApplicationExtension(private val byteData: ByteArray) {

    val extensionIntroducer : Byte = byteData[0]
    val applicationExtensionLabel: Byte = byteData[1]
    val blockSize = byteData[2].toPositiveInt()
    val applicationIdentifier = String(byteData.sliceArray(IntRange(3, 10)).toCharArray())
    val applicationAuthenticationCode = String(byteData.sliceArray(IntRange(11, 13)).toCharArray())
    val loopingTimes = byteData[17].toPositiveInt().shl(4).plus(byteData[16].toPositiveInt())

    override fun toString(): String {
        return "GifApplicationExtension(byteData=${byteData.toHexString()}, extensionIntroducer=$extensionIntroducer, applicationExtensionLabel=$applicationExtensionLabel, blockSize=$blockSize, applicationIdentifier='$applicationIdentifier', applicationAuthenticationCode='$applicationAuthenticationCode', loopingTimes=$loopingTimes)"
    }

}