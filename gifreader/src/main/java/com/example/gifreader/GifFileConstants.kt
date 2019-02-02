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


internal const val GIF_HEADER_BLOCK_LENGTH = 6
internal const val GIF_LOCAL_SCREEN_DESCRIPTOR_LENGTH = 7
internal const val GIF_GRAPHIC_CONTROL_EXTENSION_LENGTH = 8
internal const val GIF_IMAGE_DESCRIPTOR_LENGTH = 10
internal const val GIF_APPLICATION_EXTENSION_LENGTH = 19

private const val GIF_GRAPHIC_CONTROL_EXTENSION = 0xF9.toByte()
private const val GIF_PLAIN_TEXT_EXTENSION = 0x01.toByte()
private const val GIF_APPLICATION_EXTENSION = 0xFF.toByte()
private const val GIF_COMMENT_EXTENSION = 0xFE.toByte()

internal const val GIF_EXTENSION_INTRODUCER = 0x21.toByte()
internal const val GIF_TRAILER = 0x3B.toByte()
internal const val GIF_IMAGE_DESCRIPTOR_PREFIX = 0x2C.toByte()
internal const val GIF_LARGEST_CODE_SIZE = 12

internal const val GIF_DISPOSAL_METHOD_NONE = 0
internal const val GIF_DISPOSAL_METHOD_NOT_DISPOSE = 1
internal const val GIF_DISPOSAL_METHOD_RESTORE_TO_BACKGROUND = 2
internal const val GIF_DISPOSAL_METHOD_RESTORE_TO_CANVAS_PREVIOUS_STATE = 3

internal fun Byte.toPositiveInt(): Int = toInt() and 0xFF

internal fun isImageSeparator(prefix: ByteArray): Boolean = prefix[0] == GIF_IMAGE_DESCRIPTOR_PREFIX
internal fun isGraphicControlExtension(prefix: ByteArray): Boolean = prefix[0] == GIF_EXTENSION_INTRODUCER && prefix[1] == GIF_GRAPHIC_CONTROL_EXTENSION
internal fun isPlainTextExtension(prefix: ByteArray): Boolean = prefix[0] == GIF_EXTENSION_INTRODUCER && prefix[1] == GIF_PLAIN_TEXT_EXTENSION
internal fun isApplicationExtension(prefix: ByteArray): Boolean = prefix[0] == GIF_EXTENSION_INTRODUCER && prefix[1] == GIF_APPLICATION_EXTENSION
internal fun isCommentExtension(prefix: ByteArray): Boolean = prefix[0] == GIF_EXTENSION_INTRODUCER && prefix[1] == GIF_COMMENT_EXTENSION
internal fun isTrailer(prefix: ByteArray): Boolean = prefix[0] == GIF_TRAILER

/**
 * convert this byte array to char array, which can applied to multiple platform kotlin code to construct a [String]
 */
internal fun ByteArray.toCharArray() = CharArray(this.size) { this[it].toChar() }

private const val HEX = "0123456789ABCDEF"

internal fun Byte.toHexCharArray(): CharArray = charArrayOf('0', 'x',
        HEX[this.toInt().and(0xFF).ushr(4)],
        HEX[this.and(0x0F).toInt()])

internal fun ByteArray.toHexString(): String = StringBuilder().also { builder ->
    builder.append('[')
    var index = 0
    this.forEach { byte ->
        builder.append(byte.toHexCharArray())
        builder.append(if (index++ == this.lastIndex) ']' else ',')
    }
}.toString()
