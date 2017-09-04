package com.example.roman.audiocuttertest.io;

import android.media.MediaPlayer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Roman on 03.09.2017.
 */

public interface AudioDetectorCallback {
    void onSuccess(ArrayList<AudioDetector.Wrap> audioFiles);
    void onFail(Exception e);
}
