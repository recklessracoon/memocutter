package com.recklessracoon.roman.audiocuttertest.io;

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

    public String toString(){
        return actualFile.getAbsolutePath();
    }

    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;

        if (object != null && object instanceof Wrap) {
            sameSame = this.actualFile.getAbsolutePath().equals(((Wrap) object).actualFile.getAbsolutePath());
        }

        return sameSame;
    }
}