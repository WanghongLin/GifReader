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

/**
 * Created by wanghonglin on 2018/11/14 10:31 AM.
 */
private lateinit var currentDisplayRGBABytes: ByteArray
private lateinit var previousDisplayRGBABytes: ByteArray
private var internalDrawOverOnPreviousFrame: Boolean = true

class GifDisplayFrame(displaySize: Int) {

    init {
        if (!::currentDisplayRGBABytes.isInitialized) {
            currentDisplayRGBABytes = ByteArray(displaySize)
        }
        if (!::previousDisplayRGBABytes.isInitialized) {
            previousDisplayRGBABytes = ByteArray(displaySize)
        }
    }

    val displayRGBABytes: ByteArray
        get() = currentDisplayRGBABytes

    val oldDisplayRGBABytes: ByteArray
        get() = previousDisplayRGBABytes

    var drawOverOnPreviousFrame: Boolean
        get() = internalDrawOverOnPreviousFrame
        set(value) {
            internalDrawOverOnPreviousFrame = value
        }

    var isEmpty: Boolean = false
    // lateinit var rgbaBytes: ByteArray
    var delayTimeMillis: Long = 0
}