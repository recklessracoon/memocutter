package com.example.roman.audiocuttertest.io;

import android.media.MediaPlayer;

import java.io.File;
import java.io.IOException;

/**
 * Created by Roman on 05.08.2017.
 */

public interface AudioLoaderCallback {
    public void audioLoadSuccess(File audioFile, MediaPlayer mediaPlayer);
    public void audioLoadFail(IOException e);
}
