package com.homesoft.encoder;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/*
 * Copyright (C) 2019 Homesoft, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class AvcEncoderConfig extends EncoderConfig {
    public static final String MIME_TYPE = "video/avc";
    private static final int IFRAME_INTERVAL = 0;          // 10 seconds between I-frames

    //Defaults from the BigFlake Sample
    public static final int DEFAULT_WIDTH = 320;
    public static final int DEFAULT_HEIGHT = 240;
    public static final String DEFAULT_PATH = new File(Environment.getExternalStorageDirectory(),
            "test." + MIME_TYPE.split("/")[1] + '.' + DEFAULT_WIDTH + "x" + DEFAULT_HEIGHT + ".mp4").getAbsolutePath();

    public AvcEncoderConfig() {
        this(DEFAULT_PATH ,DEFAULT_WIDTH, DEFAULT_HEIGHT,15,2000000, "");
    }

    public AvcEncoderConfig(final String path, final int width, final int height, final float framesPerSecond, final int bitRate, final String audioPath) {
        super(path, width, height, framesPerSecond, bitRate, audioPath);
    }


    @Override
    public MediaFormat getVideoMediaFormat() {
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, getWidth(), getHeight());

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, getBitRate());
        format.setFloat(MediaFormat.KEY_FRAME_RATE, getFramePerSecond());
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        return format;
    }

    @Override
    public FrameMuxer getFrameMuxer() throws IOException {
        return new Mp4FrameMuxer(getPath(), getAudioPath(), getFramePerSecond());
    }
}
