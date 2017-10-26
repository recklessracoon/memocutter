package com.recklessracoon.roman.audiocuttertest.io;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
            callback.audioLoadSuccess(audioFile, mediaPlayer);
        } catch (IOException e) {
            callback.audioLoadFail(audioFile, e);
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

    public static void copyInputStreamToFile(InputStream in, File file) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if ( out != null ) {
                    out.close();
                }

                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                in.close();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    public static String getRealPathFromURI(Context context, Uri contentUri, int flags) {

        InputStream inputStream;
        String finalPath = contentUri.getPath();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                context.grantUriPermission(context.getPackageName(), contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                final int takeFlags = flags & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.getContentResolver().takePersistableUriPermission(contentUri, takeFlags);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String bufferPath = "lastEditedFile.dat"; // .dat? .mp3?
        Log.d("CURI",""+contentUri.getPath());

        try {
            if(contentUri.getPath().contains("mp3") || contentUri.getPath().contains("audio/"))
                bufferPath = "lastEditedFile.mp3";
        } catch (Exception e){

        }

        try {
            inputStream = context.getContentResolver().openInputStream(contentUri);
            File bufferHere = Cutter.getTemporaryCutFileLocationWithName(bufferPath);
            copyInputStreamToFile(inputStream, bufferHere);
            finalPath = bufferHere.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace(); // fnfe: no content provider. ends up here
        }

        return finalPath;
    }
}
