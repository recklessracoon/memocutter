package com.recklessracoon.roman.audiocuttertest;

/**
 * Created by Roman on 11.12.2017.
 */

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.recklessracoon.roman.audiocuttertest.helpers.AudioFilesPreloader;
import com.recklessracoon.roman.audiocuttertest.io.Cutter;
import com.recklessracoon.roman.audiocuttertest.io.CutterCallback;
import com.recklessracoon.roman.audiocuttertest.io.CutterImpl;

import java.io.File;

/**
 * Created by ssaurel on 02/12/2016.
 */
public class SplashActivity extends AppCompatActivity {

    private Cutter unusedCutterReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFFMPEG();

        if(isPermissionGranted()) {
            AudioFilesPreloader preloader = new AudioFilesPreloader().withContext(this).withDirectory(EditActivity.getTemporarySavedFile(this).getParentFile());
            preloader.apply();
            Thread t = preloader.getThreadReference();

            // use splash screen to load audio files
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isPermissionGranted() {
        int result = checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void initFFMPEG(){
        unusedCutterReference = new CutterImpl(this, new CutterCallback() {
            @Override
            public void cutFinished(MediaPlayer mediaPlayer, File location) {

            }

            @Override
            public void cutFailed(Exception e) {

            }

            @Override
            public void conversionFinished(MediaPlayer mediaPlayer) {

            }

            @Override
            public void onProgress(String message){

            }

            @Override
            public void conversionFailed(Exception e) {

            }

            @Override
            public void concatFinished(MediaPlayer mediaPlayer, File file) {

            }

            @Override
            public void concatFailed(Exception e) {

            }

            @Override
            public void ffmpegInitFailed(FFmpegNotSupportedException e) {

            }
        }, null, null); // so the cutter will init ffmpeg TODO check whether this solves issue on android 7 of not being able to convert/cut on first appstart
    }

}