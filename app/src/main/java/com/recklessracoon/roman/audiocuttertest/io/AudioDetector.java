package com.recklessracoon.roman.audiocuttertest.io;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.recklessracoon.roman.audiocuttertest.helpers.AudioFilesPreloader;
import com.recklessracoon.roman.audiocuttertest.helpers.AudioFilesSingleton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

/**
 * Created by Roman on 03.09.2017.
 */

public class AudioDetector extends Thread {

    private Context context;
    private File audioCutterDirectory;
    private AudioDetectorCallback callback;
    
    public AudioDetector(Context context, File audioCutterDirectory, AudioDetectorCallback callback){
        this.context = context;
        this.audioCutterDirectory = audioCutterDirectory;
        this.callback = callback;
    }

    @Override
    public void run(){

        ArrayList<Wrap> audioFiles = AudioFilesSingleton.getAudioFiles();
        File[] directory = audioCutterDirectory.listFiles();

        if(directory == null)
            directory = new File[0];

        if(audioFiles.size() == directory.length){ // directory stayed the same, no updates
            callback.onAudioDetectorSuccess(audioFiles);
            return;
        }

        final File first = directory[0];

        audioFiles = new ArrayList<>();
        HashMap<String, Wrap> oldAudioFiles = new HashMap<>();

        MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                callback.onMediaPlayerThrowsError(mp, what, extra);
                return false;
            }
        };

        for(Wrap wrap : AudioFilesSingleton.getAudioFiles()){
            oldAudioFiles.put(wrap.actualFile.getAbsolutePath(), wrap);
        }

        try {
            for (File audioFile : directory) {
                if (audioFile.getName().contains("mp3")) {

                    if(oldAudioFiles.containsKey(audioFile.getAbsolutePath())){ // no need to reload old audio; just retrieve mediaplayer

                        Wrap putIn = new Wrap(audioFile, audioFile.getName(), oldAudioFiles.get(audioFile.getAbsolutePath()).mediaPlayer);
                        audioFiles.add(putIn);
                        //Log.d("BROWSE", "containerino ABS: " + audioFile.getAbsolutePath());

                    } else {

                        //Log.d("BROWSE", "no containerino ABS: " + audioFile.getAbsolutePath());

                        MediaPlayer mediaPlayer;
                        Uri myUri = Uri.fromFile(audioFile); // initialize Uri here
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setOnErrorListener(onErrorListener);
                        try {
                            mediaPlayer.setDataSource(context, myUri);
                            mediaPlayer.prepare();

                            Wrap putIn = new Wrap(audioFile, audioFile.getName(), mediaPlayer);
                            audioFiles.add(putIn);

                        } catch (IOException e) {
                            callback.onParticularAudioLoadFail(audioFile, e);
                            //Log.d("BROWSE", "FAIL" + e.toString());
                        }

                    }



                } //endif
            } //endfor

            callback.onAudioDetectorSuccess(audioFiles);

        } catch (NullPointerException e){
            callback.onAudioDetectorFail(audioCutterDirectory, e);
        }

    }

}
