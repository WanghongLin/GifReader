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

package com.example.gifreaderexample

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AppCompatActivity
import com.example.gifreader.GifFileFormat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private lateinit var gifFileFormat: GifFileFormat
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            playGif()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }

    private fun playGif() {
        val gifFile = filesDir.absoluteFile.toString() + File.separator + "beno.gif"
        val gifFileOutputStream = FileOutputStream(gifFile)

        gifFileOutputStream.use { output ->
            assets.open("beno.gif").use { input ->
                input.copyTo(output)
            }
        }

        File(Environment.getExternalStorageDirectory().absolutePath + File.separator + "gif").mkdirs()

        gifFileFormat = GifFileFormat(File(gifFile).readBytes());

        imageView.post(runnable)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            playGif()
        }
    }

    private val runnable : Runnable = object : Runnable {
        override fun run() {
            val gifFrame = gifFileFormat.nextDisplayFrame()

            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(gifFileFormat.canvasWidth, gifFileFormat.canvasHeight, Bitmap.Config.ARGB_8888)
            }

            bitmap?.copyPixelsFromBuffer(ByteBuffer.wrap(gifFrame.displayRGBABytes))

            imageView.setImageBitmap(bitmap)
            imageView.postDelayed(this, gifFrame.delayTimeMillis)
        }
    }
}
