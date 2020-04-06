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

private lateinit var rgba: ByteArray

internal class GifFrame(private val gifFileFormat: GifFileFormat) {

    internal var gifGraphicControlExtension : GifGraphicControlExtension? = null
    internal lateinit var gifImageDescriptor : GifImageDescriptor
    internal var gifLocalColorTable : ByteArray? = null
    internal lateinit var gifImageData: GifImageData

    fun hasFilledImageData(): Boolean = this::gifImageDescriptor.isInitialized && this::gifImageData.isInitialized

    init {
        if (!::rgba.isInitialized) {
            rgba = ByteArray(gifFileFormat.canvasDisplayByteSize())
        }
    }


    /**
     * Get rgba byte array and it's size pair
     */
    fun getRGBAByteBuffer(): Pair<ByteArray, Int> {
        val colorIndexStream = gifImageData.codeStream2ColorIndexStream()

        val colorTable = gifLocalColorTable ?: gifFileFormat.gifGlobalColorTable

        val transparentColorFlag = gifGraphicControlExtension?.transparentColorFlag ?: false
        val transparentColorIndex = gifGraphicControlExtension?.transparentColorIndex ?: 0

        colorTable.let { ct ->
            var toIndex = 0
            var fromIndex = 0
            while (fromIndex < colorIndexStream.size) {

                val colorIndex = colorIndexStream[fromIndex].times(3)

                rgba[toIndex + 0] = ct[colorIndex+0]
                rgba[toIndex + 1] = ct[colorIndex+1]
                rgba[toIndex + 2] = ct[colorIndex+2]
                rgba[toIndex + 3] = if (transparentColorFlag && transparentColorIndex == colorIndexStream[fromIndex]) 0x00.toByte() else 0xFF.toByte()

                toIndex += 4
                fromIndex++
            }
        }

        return Pair(rgba, colorIndexStream.size.times(4))
    }

    override fun toString(): String {
        return "GifFrame(gifGraphicControlExtension=$gifGraphicControlExtension, gifImageDescriptor=$gifImageDescriptor, gifLocalColorTable=${gifLocalColorTable?.size})"
    }
}