package com.homesoft.encoder;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

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

public class Mp4FrameMuxer implements FrameMuxer {
    private static final String TAG = Mp4FrameMuxer.class.getSimpleName();
    private final long frameUsec;
    private final MediaMuxer muxer;

    private boolean started;
    private int videoTrackIndex;
    private int audioTrackIndex;
    private int videoFrames;
    private long finalVideoTime;

    public Mp4FrameMuxer(final String path, final float fps) throws IOException {
        frameUsec = FrameEncoder.getFrameTime(fps);
        muxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void start(FrameEncoder frameEncoder) {
        MediaFormat newFormat = frameEncoder.getVideoMediaCodec().getOutputFormat();

        // now that we have the Magic Goodies, start the muxer
        videoTrackIndex = muxer.addTrack(newFormat);
        muxer.start();
        started = true;
    }

    @Override
    public void start(FrameEncoder frameEncoder, MediaExtractor audioExtractor) {
        MediaFormat videoFormat = frameEncoder.getVideoMediaCodec().getOutputFormat();
        audioExtractor.selectTrack(0);
        MediaFormat audioFormat = audioExtractor.getTrackFormat(0);

        videoTrackIndex = muxer.addTrack(videoFormat);
        audioTrackIndex = muxer.addTrack(audioFormat);
        Log.d("Video format: %s", videoFormat.toString());
        Log.e("Audio format: %s", audioFormat.toString());

        muxer.start();
        started = true;
    }

    @Override
    public void muxVideoFrame(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        // This code will break if the encoder supports B frames.
        // Ideally we would use set the value in the encoder,
        // don't know how to do that without using OpenGL
        finalVideoTime = frameUsec * videoFrames++;
        bufferInfo.presentationTimeUs = finalVideoTime;
        muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo);
    }

    @Override
    public void muxAudioFrame(ByteBuffer encodedData, MediaCodec.BufferInfo audioBufferInfo) {
        muxer.writeSampleData(audioTrackIndex, encodedData, audioBufferInfo);
    }

    @Override
    public void release() {
        muxer.stop();
        muxer.release();
    }

    @Override
    public long getVideoTime() {
        return finalVideoTime;
    }

}
