package com.example.roman.audiocuttertest.io;

import android.media.MediaPlayer;

import java.io.File;

/**
 * Created by Roman on 05.09.2017.
 */

public class Wrap {
    public MediaPlayer mediaPlayer;
    public String name;
    public File actualFile;
    public boolean isSelected;

    public Wrap(File actualFile, String name, MediaPlayer mediaPlayer){
        this.mediaPlayer = mediaPlayer;
        this.name = name;
        this.actualFile = actualFile;
    }
}