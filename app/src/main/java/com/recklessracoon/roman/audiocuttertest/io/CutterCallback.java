package com.recklessracoon.roman.audiocuttertest.io;

import android.media.MediaPlayer;

import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;

/**
 * Created by Roman on 05.09.2017.
 */

public interface CutterCallback {
    void cutFinished(MediaPlayer mediaPlayer, File location);
    void cutFailed(Exception e);

    void conversionFinished(MediaPlayer mediaPlayer);
    void conversionFailed(Exception e);

    void onProgress(String message);

    void concatFinished(MediaPlayer mediaPlayer, File file);
    void concatFailed(Exception e);

    void ffmpegInitFailed(FFmpegNotSupportedException e);
}
