package com.example.roman.audiocuttertest.io;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

/**
 * Created by Roman on 05.08.2017.
 */

public class AudioLoader extends Thread {

    private AudioLoaderCallback callback;
    private Context context;
    private File audioFile;

    public AudioLoader(Context context, File audioFile, AudioLoaderCallback callback){
        this.callback = callback;
        this.context = context;
        this.audioFile = audioFile;
    }

    public void run(){
        MediaPlayer mediaPlayer;
        Uri myUri = Uri.fromFile(audioFile); // initialize Uri here
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(context, myUri);
            mediaPlayer.prepare();
            callback.audioLoadSuccess(mediaPlayer);
        } catch (IOException e) {
            callback.audioLoadFail(e);
        }
/*
        try {
            FileInputStream fis = new FileInputStream(audioFile);
            byte[] data = new byte[fis.available()];
            fis.read(data);
            fis.close();
            callback.bytesLoaded(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
