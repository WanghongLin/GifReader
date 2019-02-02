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

internal class GifCodeTable(private val lzwMinCodeSize: Int) {

    val table : MutableList<List<Int>> = mutableListOf()
    val clearCode = 1.shl(lzwMinCodeSize)
    val endOfInformationCode = clearCode.plus(1)

    init {
        buildCodeTable()
    }

    private fun buildCodeTable() {
        table.clear()
        val initialSize = 1.shl(lzwMinCodeSize).plus(2) // clear code and end of information code
        for (i in 0 until initialSize) {
            table.add(listOf(i))
        }
    }

    fun resetCodeTable() {
        buildCodeTable()
    }

    override fun toString(): String {
        return "GifCodeTable(lzwMinCodeSize=$lzwMinCodeSize, table=$table, clearCode=$clearCode, endOfInformationCode=$endOfInformationCode)"
    }
}