package com.recklessracoon.roman.audiocuttertest.helpers;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.recklessracoon.roman.audiocuttertest.io.AudioDetector;
import com.recklessracoon.roman.audiocuttertest.io.AudioDetectorCallback;
import com.recklessracoon.roman.audiocuttertest.io.Wrap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Roman on 07.09.2017.
 */

public class AudioFilesPreloader implements AudioDetectorCallback {

    private AudioDetector audioDetector;
    private Context context;
    private File directory;

    public AudioFilesPreloader(){

    }

    public AudioFilesPreloader withContext(Context context){
        this.context = context;
        return this;
    }

    public AudioFilesPreloader withDirectory(File directory){
        this.directory = directory;
        return this;
    }

    public void apply(){
        if(context != null && directory != null) {
            audioDetector = new AudioDetector(context, directory, this);
            audioDetector.start();
        } else {
            // TODO handle wrong call?
        }
    }

    public Thread getThreadReference(){
        return audioDetector;
    }

    @Override
    public void onAudioDetectorSuccess(ArrayList<Wrap> audioFiles) {
        AudioFilesSingleton.setAudioFiles(audioFiles);
    }

    @Override
    public void onAudioDetectorFail(File audioFile, Exception e) {
        // TODO handle preload fail. Maybe just not visible for user?
        //Log.d("PRELOAD","fail: "+audioFile.getAbsolutePath()+e.toString());
    }

    @Override
    public void onParticularAudioLoadFail(File audioFile, IOException e) {
        //Log.d("PRELOAD","fail: "+audioFile.getAbsolutePath()+e.toString());
    }

    @Override
    public void onMediaPlayerThrowsError(MediaPlayer mp, int what, int flag) {

    }
}
