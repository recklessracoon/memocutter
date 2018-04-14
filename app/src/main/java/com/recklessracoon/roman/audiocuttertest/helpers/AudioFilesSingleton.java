package com.recklessracoon.roman.audiocuttertest.helpers;

import android.media.MediaPlayer;

import com.recklessracoon.roman.audiocuttertest.io.Wrap;

import java.util.ArrayList;

/**
 * Created by Roman on 07.09.2017.
 */

public class AudioFilesSingleton {
    private static ArrayList<Wrap> audioFiles;

    public static void setAudioFiles(ArrayList<Wrap> audioFiles){
            AudioFilesSingleton.audioFiles = audioFiles;
    }

    public static ArrayList<Wrap> getAudioFiles(){
        if(audioFiles == null)
            audioFiles = new ArrayList<>();

        return audioFiles;
    }

    public static void applyOnErrorListener(MediaPlayer.OnErrorListener onErrorListener){
        ArrayList<Wrap> audioFiles = AudioFilesSingleton.getAudioFiles();
        try {
            for (Wrap w : audioFiles) {
                w.mediaPlayer.setOnErrorListener(onErrorListener);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void resetAudioFiles(){
        if(audioFiles != null) {
            for (Wrap w : audioFiles) {
                try {
                    w.mediaPlayer.pause();
                    w.mediaPlayer.stop();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            for (Wrap w : audioFiles) {
                try {
                    w.mediaPlayer.release();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
        audioFiles = new ArrayList<>();
    }
}
