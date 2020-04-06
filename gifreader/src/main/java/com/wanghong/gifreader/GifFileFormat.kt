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

class GifFileFormat(private val gifFileBytes: ByteArray) {

    private var gifHeaderBlock : GifHeaderBlock
    internal var gifLocalScreenDescriptor : GifLocalScreenDescriptor
    internal var gifGlobalColorTable : ByteArray
    private var gifApplicationExtension: GifApplicationExtension? = null
    private val gifFrames = mutableListOf<GifFrame>()

    // private val gifFileBytes = File(gifFilePath).readBytes()
    private var currentLoopTimes = 0

    init {
        gifHeaderBlock = readHeaderBlock()
        gifLocalScreenDescriptor = readLocalScreenDescriptor()

        val globalColorTableStart = GIF_HEADER_BLOCK_LENGTH + GIF_LOCAL_SCREEN_DESCRIPTOR_LENGTH
        val globalColorTableLength = 1.shl(gifLocalScreenDescriptor.globalColorTableSize.plus(1)).times(3)
        gifGlobalColorTable = gifFileBytes.sliceArray(IntRange(globalColorTableStart, globalColorTableStart+globalColorTableLength-1))

        var indexInFileByte = GIF_HEADER_BLOCK_LENGTH + GIF_LOCAL_SCREEN_DESCRIPTOR_LENGTH + globalColorTableLength

        // there is a range check in kotlin 1.3, we read two bytes at a time
        // we need to change size to lastIndex
        while (indexInFileByte < gifFileBytes.lastIndex) {
            val prefix = gifFileBytes.sliceArray(IntRange(indexInFileByte, indexInFileByte + 1))

            when (prefix[0]) {
                GIF_EXTENSION_INTRODUCER -> {
                    if (isApplicationExtension(prefix)) {
                        gifApplicationExtension = GifApplicationExtension(gifFileBytes.sliceArray(IntRange(indexInFileByte, indexInFileByte + GIF_APPLICATION_EXTENSION_LENGTH - 1)))
                        indexInFileByte += GIF_APPLICATION_EXTENSION_LENGTH
                    } else if (isGraphicControlExtension(prefix)) {
                        // graphic control extension is optional
                        val blockSize = 2 /* prefix */ +
                                1 /* byte size */ +
                                gifFileBytes[indexInFileByte + 2].toPositiveInt() /* block size */ +
                                1 /* terminator */;

                        // graphic control extension
                        GifFrame(this).also {
                            gifFrames.add(it)
                            it.gifGraphicControlExtension = GifGraphicControlExtension(gifFileBytes.sliceArray(IntRange(indexInFileByte, indexInFileByte + blockSize - 1)))
                        }
                        indexInFileByte += blockSize
                    } else if (isPlainTextExtension(prefix)) {
                        // plain text extension
                        indexInFileByte += 2
                        var nextBlockSize = gifFileBytes[indexInFileByte].toPositiveInt()
                        while (nextBlockSize > 0) {
                            indexInFileByte++
                            // TODO: handle block size data
                            indexInFileByte += nextBlockSize
                            nextBlockSize = gifFileBytes[indexInFileByte].toPositiveInt()
                        }

                        // block terminator
                        indexInFileByte++
                    } else if (isCommentExtension(prefix)) {
                        indexInFileByte += 2
                        var nextBlockSize = gifFileBytes[indexInFileByte].toPositiveInt()
                        while (nextBlockSize > 0) {
                            indexInFileByte++
                            // TODO: handle comment extension sub-blocks
                            indexInFileByte += nextBlockSize
                            nextBlockSize = gifFileBytes[indexInFileByte].toPositiveInt()
                        }

                        // block terminator
                        indexInFileByte++
                    } else {
                    }
                }

                GIF_IMAGE_DESCRIPTOR_PREFIX -> {
                    val gifFrame = if (gifFrames.last().hasFilledImageData()) GifFrame(this).also { gifFrames.add(it) } else gifFrames.last()

                    // image descriptor
                    if (gifFileBytes[indexInFileByte] == GIF_IMAGE_DESCRIPTOR_PREFIX) {
                        gifFrame.gifImageDescriptor = GifImageDescriptor(gifFileBytes.sliceArray(IntRange(indexInFileByte, indexInFileByte + GIF_IMAGE_DESCRIPTOR_LENGTH - 1)))
                        indexInFileByte += GIF_IMAGE_DESCRIPTOR_LENGTH
                    }

                    // local color table
                    if (gifFrame.gifImageDescriptor.localColorTableFlag) {
                        val localColorTableSize = 1.shl(gifFrame.gifImageDescriptor.localColorTableSize.plus(1)).times(3)
                        gifFrame.gifLocalColorTable = gifFileBytes.sliceArray(IntRange(indexInFileByte, indexInFileByte + localColorTableSize - 1))
                        indexInFileByte += localColorTableSize
                    }

                    // image data
                    gifFrame.gifImageData = GifImageData()
                    gifFrame.gifImageData.gifLZWMinimumCodeSize = gifFileBytes[indexInFileByte].toPositiveInt()
                    indexInFileByte++

                    var nextSize = gifFileBytes[indexInFileByte].toPositiveInt()
                    while (nextSize > 0) {
                        indexInFileByte++

                        gifFrame.gifImageData.gifImageSubBlocks.add(gifFileBytes.sliceArray(IntRange(indexInFileByte, indexInFileByte + nextSize - 1)))

                        indexInFileByte += nextSize
                        nextSize = gifFileBytes[indexInFileByte].toPositiveInt()
                    }
                    // end of one frame image data
                    indexInFileByte++
                    // gifFrames.add(gifFrame)
                }

                GIF_TRAILER -> {
                    indexInFileByte++
                }

                else -> {
                    // indexInFileByte++
                }
            }

        }
    }

    private fun readHeaderBlock() : GifHeaderBlock {
        val headerBytes = gifFileBytes.sliceArray(IntRange(0, GIF_HEADER_BLOCK_LENGTH-1))
        return GifHeaderBlock(headerBytes)
    }

    private fun readLocalScreenDescriptor(): GifLocalScreenDescriptor {
        val dataBytes = gifFileBytes.sliceArray(IntRange(GIF_HEADER_BLOCK_LENGTH,
                GIF_HEADER_BLOCK_LENGTH+GIF_LOCAL_SCREEN_DESCRIPTOR_LENGTH-1))
        return GifLocalScreenDescriptor(dataBytes)
    }

    private var currentDisplayFrameIndex = 0

    fun nextDisplayFrame(): GifDisplayFrame {

        if (loopTimes() != 0 && currentLoopTimes >= loopTimes()) {
            return GifDisplayFrame(canvasDisplayByteSize()).apply { isEmpty = true }
        }

        val gifDisplayFrame = GifDisplayFrame(canvasDisplayByteSize()).also {
            it.delayTimeMillis = gifFrames[currentDisplayFrameIndex].gifGraphicControlExtension?.delayTimeMillis() ?: 0
        }

        if (currentDisplayFrameIndex == 0) {
            // first frame
            gifDisplayFrame.oldDisplayRGBABytes.directCopyFrom(wholeCanvasWithBackgroundColorDraw)
            gifDisplayFrame.drawOverOnPreviousFrame = true
        }

        val (rgbaByteBuffer, bufferSize) = gifFrames[currentDisplayFrameIndex].getRGBAByteBuffer()
        val shouldHandleAlpha = gifFrames[currentDisplayFrameIndex].gifGraphicControlExtension?.transparentColorFlag ?: false

        gifDisplayFrame.oldDisplayRGBABytes.let { previous ->

            if (previous.size == bufferSize && !shouldHandleAlpha) {
                gifDisplayFrame.displayRGBABytes.directCopyFrom(rgbaByteBuffer)
            } else {
                val width = gifFrames[currentDisplayFrameIndex].gifImageDescriptor.imageWidth
                val height = gifFrames[currentDisplayFrameIndex].gifImageDescriptor.imageHeight
                val top = gifFrames[currentDisplayFrameIndex].gifImageDescriptor.imageTop
                val left = gifFrames[currentDisplayFrameIndex].gifImageDescriptor.imageLeft

                if (!gifDisplayFrame.drawOverOnPreviousFrame) {
                    // previous frame is disposal and current is not a complete frame
                    if (top > 0 || left > 0) {
                        previous.directCopyFrom(wholeCanvasWithBackgroundColorDraw)
                    } else {
                        gifDisplayFrame.displayRGBABytes.directCopyFrom(rgbaByteBuffer)
                        return@let
                    }
                }

                for (h in 0 until height) {
                    for (w in 0 until width) {
                        val dstIndex = (top+h)*gifLocalScreenDescriptor.canvasWidth*4+(left+w)*4
                        val srcIndex = h*width*4 + w*4

                        // do not draw if it is transparent
                        if (rgbaByteBuffer[srcIndex+3] != 0x00.toByte()) {
                            previous[dstIndex+0] = rgbaByteBuffer[srcIndex+0]
                            previous[dstIndex+1] = rgbaByteBuffer[srcIndex+1]
                            previous[dstIndex+2] = rgbaByteBuffer[srcIndex+2]
                            previous[dstIndex+3] = rgbaByteBuffer[srcIndex+3]
                        }
                    }
                }
                gifDisplayFrame.displayRGBABytes.directCopyFrom(gifDisplayFrame.oldDisplayRGBABytes)
            }
        }

        val disposalMethod = gifFrames[currentDisplayFrameIndex].gifGraphicControlExtension?.disposalMethod ?: GIF_DISPOSAL_METHOD_NONE
        when (disposalMethod) {
            GIF_DISPOSAL_METHOD_NOT_DISPOSE -> {
                gifDisplayFrame.oldDisplayRGBABytes.directCopyFrom(gifDisplayFrame.displayRGBABytes)
                gifDisplayFrame.drawOverOnPreviousFrame = true
            }
            GIF_DISPOSAL_METHOD_RESTORE_TO_BACKGROUND -> {
                gifDisplayFrame.oldDisplayRGBABytes.directCopyFrom(wholeCanvasWithBackgroundColorDraw)
                gifDisplayFrame.drawOverOnPreviousFrame = true
            }
            GIF_DISPOSAL_METHOD_NONE -> gifDisplayFrame.drawOverOnPreviousFrame = false
            GIF_DISPOSAL_METHOD_RESTORE_TO_CANVAS_PREVIOUS_STATE -> {
                // do not update and use the latest not disposal frame
                // see http://webreference.com/content/studio/disposal.html
                gifDisplayFrame.drawOverOnPreviousFrame = true
            }
            else -> {
                println("unknown disposal method ${disposalMethod}")
            }
        }

        if (++currentDisplayFrameIndex >= gifFrames.size) {
            currentDisplayFrameIndex = 0
            currentLoopTimes++
        }

        return gifDisplayFrame
    }

    fun dumpGifInfo() {
        println("GifFileFormat.dumpGifInfo $gifHeaderBlock")
        println("GifFileFormat.dumpGifInfo $gifLocalScreenDescriptor")
        println("GifFileFormat.dumpGifInfo $gifApplicationExtension")
        println("GifFileFormat.dumpGifInfo global table size ${gifGlobalColorTable.size}")
        println("GifFileFormat.dumpGifInfo total frames ${gifFrames.size}")
        println("GifFileFormat.dumpGifInfo loop times ${loopTimes()}")
    }

    fun loopTimes(): Int = this.gifApplicationExtension?.loopingTimes ?: -1
    val canvasWidth = this.gifLocalScreenDescriptor.canvasWidth
    val canvasHeight = this.gifLocalScreenDescriptor.canvasHeight

    internal fun canvasDisplaySize() = gifLocalScreenDescriptor.canvasWidth * gifLocalScreenDescriptor.canvasHeight
    internal fun canvasDisplayByteSize() = canvasDisplaySize().times(4)

    private val wholeCanvasWithBackgroundColorDraw: ByteArray by lazy {
        ByteArray(canvasDisplayByteSize()).also {
            var j = 0
            val bgColorIndex = gifLocalScreenDescriptor.backgroundColorIndex.times(3)
            while (j < it.size) {
                it[j + 0] = gifGlobalColorTable[bgColorIndex + 0]
                it[j + 1] = gifGlobalColorTable[bgColorIndex + 1]
                it[j + 2] = gifGlobalColorTable[bgColorIndex + 2]
                it[j + 3] = 0xFF.toByte()
                j += 4
            }
        }
    }
}

private fun ByteArray.directCopyFrom(srcByteArray: ByteArray) = srcByteArray.copyInto(this)

