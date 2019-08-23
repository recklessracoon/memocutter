package com.recklessracoon.roman.audiocuttertest.io;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import nl.bravobit.ffmpeg.FFcommandExecuteResponseHandler;
import nl.bravobit.ffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

/**
 * Created by Roman on 05.09.2017.
 */

public class CutterImpl extends Cutter {

    private int beginning, end;

    private MediaPlayer displayedMedia;
    private File fileOfDisplayedMedia;

    public CutterImpl(Context context, CutterCallback callback, MediaPlayer displayedMedia, File fileOfDisplayedMedia){
        super(context, callback);
        beginning = 0;
        end = 0;

        this.displayedMedia = displayedMedia;
        this.fileOfDisplayedMedia = fileOfDisplayedMedia;
    }

    @Override
    public int markBeginning(int beginning) {
        this.beginning = beginning;
        Log.d("MARK",""+beginning);
        return beginning;
    }

    @Override
    public int markEnd(int end) {
        this.end = end;
        Log.d("MARK",""+end);
        return end;
    }

    @Override
    public boolean cutAllowed() {
        return beginning != end && end > beginning;
    }

    @Override
    public void cutWithFFMPEGAsync() {

        /*
        try {
            Log.d("FFMPEGDEVICE",""+fFmpeg.getDeviceFFmpegVersion());
            Log.d("FFMPEGLIBRARY",""+fFmpeg.getLibraryFFmpegVersion());
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }


        String[] c = new String[1];
        c[0] = "-version";

        try {
            fFmpeg.execute(c, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    Log.d("VERSION",""+message);
                }

                @Override
                public void onProgress(String message) {
                    //Log.d("VERSION",""+message);
                    callback.onProgress(message);
                }

                @Override
                public void onFailure(String message) {
                    Log.d("VERSION",""+message);
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
        */

        final File resultLocation = getTemporaryCutFileLocationWithName();

        String[] cmd = new String[9];
        cmd[0] = "-i";
        cmd[1] = fileOfDisplayedMedia.getAbsolutePath();
        cmd[2] = "-ss";
        cmd[3] = formatDurationPrecise(beginning);
        cmd[4] = "-to";
        cmd[5] = formatDurationPrecise(end);
        cmd[6] = "-c";
        cmd[7] = "copy";
        cmd[8] = resultLocation.getAbsolutePath();

        try {
            fFmpeg.execute(cmd, new FFcommandExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    Log.d("FFMPEG",message);

                    MediaPlayer mediaPlayer;
                    Uri myUri = Uri.fromFile(resultLocation); // initialize Uri here
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        mediaPlayer.setDataSource(context, myUri);
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        callback.cutFailed(e);
                    }

                    callback.cutFinished(mediaPlayer, resultLocation);
                }

                @Override
                public void onProgress(String message) {
                    //Log.d("FFMPEG",message);
                    callback.onProgress(message);
                }

                @Override
                public void onFailure(String message) {
                    Log.d("FFMPEG",message);
                    callback.cutFailed(new Exception(message));
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void concatWithFFMPEGAsync(ArrayList<File> toConcat) {
        final File newDestination = getTemporaryCutFileLocationWithName();

        ArrayList<String> commands = new ArrayList<>();
        commands.add("-i");

        // build concat command with given files
        String allFiles = "concat:";

        for(File f : toConcat){
            allFiles = allFiles+f.getAbsolutePath()+"|";
        }

        // remove last "|"
        allFiles = allFiles.substring(0, allFiles.length()-1);

        commands.add(allFiles);

        commands.add("-acodec");
        commands.add("copy");
        commands.add(newDestination.getAbsolutePath());

        String[] cmd = commands.toArray(new String[commands.size()]);

        for(int i=0;i<cmd.length;i++)
            Log.d("TOCONCAT",""+cmd[i]);

        try {
            fFmpeg.execute(cmd, new FFcommandExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    fileOfDisplayedMedia = newDestination;

                    try {
                        displayedMedia = loadMediaPlayer(fileOfDisplayedMedia);
                        displayedMedia.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                callback.concatFinished(displayedMedia, fileOfDisplayedMedia);
                            }
                        });

                        displayedMedia.prepare();

                    } catch (IOException e) {
                        e.printStackTrace();
                        callback.concatFailed(e);
                        return;
                    }

                }

                @Override
                public void onProgress(String message) {
                    //Log.d("CONCAT",""+message);
                    callback.onProgress(message);
                }

                @Override
                public void onFailure(String message) {
                    callback.concatFailed(new Exception(message));
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void convertFromOpusToMp3Async(){

        final File newDestination = getTemporaryCutFileLocationWithName();

        // Conversion magic
        String[] cmd = new String[5];
        cmd[0] = "-i";
        cmd[1] = fileOfDisplayedMedia.getAbsolutePath();
        cmd[2] = "-acodec";
        cmd[3] = "libmp3lame";
        cmd[4] = newDestination.getAbsolutePath();

        try {
            fFmpeg.execute(cmd, new FFcommandExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    fileOfDisplayedMedia = newDestination;

                    // Create a mediaplayer from converted file
                    try {
                        displayedMedia = loadMediaPlayer(fileOfDisplayedMedia);
                        displayedMedia.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                callback.conversionFinished(mp);
                            }
                        });
                        displayedMedia.prepare();

                    } catch (IOException e) {
                        e.printStackTrace();
                        //callback.conversionFailed(e); // TODO check whether this solves problem on android 7 for not being able to load audios after converting
                        return;
                    }

                    //callback.conversionFinished(displayedMedia);
                }

                @Override
                public void onProgress(String message) {
                    callback.onProgress(message);
                }

                @Override
                public void onFailure(String message) {
                    callback.conversionFailed(new Exception(message));
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {

                }
            });
        } catch (FFmpegCommandAlreadyRunningException ex) {
            ex.printStackTrace();
        }

    }

    private File getTemporaryCutFileLocationWithName(){
        File directory = getMemoCutterLocation();

        if(!directory.exists())
            directory.mkdirs();

        File newPath = new File(directory,nextFreeFileName());

        while(newPath.exists()){
            newPath = new File(directory,nextFreeFileName());
        }

        /*
        // what is this
        if(mypath.getParentFile() != null && !mypath.getParentFile().exists())
            mypath.getParentFile().mkdirs();
        */

        return newPath;
    }

    private String nextFreeFileName(){
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sh.edit();

        long currentValue = sh.getLong("NAME", 0);
        String out = currentValue+".mp3";
        currentValue++;

        edit.putLong("NAME", currentValue);
        edit.apply();

        return out;
    }

    private MediaPlayer loadMediaPlayer(File fileOfDisplayedMedia) throws IOException {
        MediaPlayer mediaPlayer;
        Uri myUri = Uri.fromFile(fileOfDisplayedMedia); // initialize Uri here
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setDataSource(context, myUri);

        return mediaPlayer;
    }

}
