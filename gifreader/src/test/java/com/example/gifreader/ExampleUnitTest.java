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

package com.example.gifreader;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void readGifFile() {
        InputStream inputStream = null;
        byte[] fileBytes = null;
        try {
            inputStream = new FileInputStream("/Users/wanghonglin/temp/test.gif");
            fileBytes = new byte[inputStream.available()];
            if (inputStream.read(fileBytes) < 0) {
                System.err.println("Read file error");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (fileBytes == null) {
            return;
        }

        GifFileFormat gifFileFormat = new GifFileFormat(fileBytes);
        gifFileFormat.dumpGifInfo();

        while (true) {
            GifDisplayFrame gifDisplayFrame = gifFileFormat.nextDisplayFrame();

            if (gifDisplayFrame.isEmpty()) {
                break;
            }

            System.out.println("next frame " + gifDisplayFrame.getDisplayRGBABytes().length);
            try {
                Thread.sleep(gifDisplayFrame.getDelayTimeMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}