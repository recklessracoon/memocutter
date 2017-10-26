package com.recklessracoon.roman.audiocuttertest.io;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Roman on 03.09.2017.
 */

public interface AudioDetectorCallback {
    void onAudioDetectorSuccess(ArrayList<Wrap> audioFiles);
    void onAudioDetectorFail(File audioFile, Exception e);

    void onParticularAudioLoadFail(File audioFile, IOException e);
}
