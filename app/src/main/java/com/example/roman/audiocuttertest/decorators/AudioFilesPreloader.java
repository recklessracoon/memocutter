package com.example.roman.audiocuttertest.decorators;

import android.content.Context;
import android.util.Log;

import com.example.roman.audiocuttertest.io.AudioDetector;
import com.example.roman.audiocuttertest.io.AudioDetectorCallback;
import com.example.roman.audiocuttertest.io.Wrap;

import java.io.File;
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

    @Override
    public void onAudioDetectorSuccess(ArrayList<Wrap> audioFiles) {
        AudioFilesSingleton.setAudioFiles(audioFiles);
    }

    @Override
    public void onAudioDetectorFail(File audioFile, Exception e) {
        // TODO handle preload fail. Maybe just not visible for user?
        Log.d("PRELOAD","fail: "+audioFile.getAbsolutePath()+e.toString());
    }
}
