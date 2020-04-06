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

internal class GifImageData {

    var gifLZWMinimumCodeSize: Int = 0
    val gifImageSubBlocks : MutableList<ByteArray> = mutableListOf()

    fun codeStream2ColorIndexStream() : List<Int> {

        val codeTable = GifCodeTable(gifLZWMinimumCodeSize)

        var currentCodeSize = gifLZWMinimumCodeSize.plus(1)
        var currentReadBits = 0
        var currentCodeValue = 0
        var currentSetBitIndexInCodeValue = 0
        var previousCodeValue = 0

        val indexStream = mutableListOf<Int>()

        for (gifImageSubBlock in gifImageSubBlocks) {

            for (i in 0 until gifImageSubBlock.size) {
                val value = gifImageSubBlock[i].toInt() and 0xFF

                for (shift in 0..7) {
                    if (value.shr(shift) and 0x1 == 0x1) {
                        currentCodeValue = currentCodeValue.or(1.shl(currentSetBitIndexInCodeValue))
                    }
                    currentReadBits++
                    currentSetBitIndexInCodeValue++

                    if (currentReadBits == currentCodeSize) {
                        // we got one code from code stream

                        if (currentCodeValue == codeTable.clearCode) {
                            codeTable.resetCodeTable()
                            currentCodeSize = gifLZWMinimumCodeSize.plus(1)
                        } else if (currentCodeValue == codeTable.endOfInformationCode) {
                            println("reach end of information code")
                        } else {
                            if (currentCodeValue < codeTable.table.size && previousCodeValue < codeTable.table.size) {
                                // in code table
                                indexStream.addAll(codeTable.table[currentCodeValue])

                                if (previousCodeValue != codeTable.clearCode) {
                                    val K = codeTable.table[currentCodeValue][0]
                                    val toAdded = mutableListOf<Int>()
                                    toAdded.addAll(codeTable.table[previousCodeValue])
                                    toAdded.add(K)

                                    codeTable.table.add(toAdded)
                                }

                            } else {
                                // not in code table
                                if (previousCodeValue != codeTable.clearCode && previousCodeValue < codeTable.table.size) {

                                    val K = codeTable.table[previousCodeValue][0]
                                    val toAdded = mutableListOf<Int>()
                                    toAdded.addAll(codeTable.table[previousCodeValue])
                                    toAdded.add(K)

                                    indexStream.addAll(toAdded)
                                    codeTable.table.add(toAdded)
                                }
                            }
                        }

                        previousCodeValue = currentCodeValue
                        currentCodeValue = 0
                        currentReadBits = 0
                        currentSetBitIndexInCodeValue = 0
                    }

                    if (codeTable.table.size == 1.shl(currentCodeSize) && currentCodeSize < GIF_LARGEST_CODE_SIZE) {
                        currentCodeSize++
                        // if (currentCodeSize > GIF_LARGEST_CODE_SIZE) {
                        //    codeTable.resetCodeTable()
                        //    currentCodeSize = gifLZWMinimumCodeSize.plus(1)
                        // }
                    }
                }
            }
        }
        return indexStream
    }
}