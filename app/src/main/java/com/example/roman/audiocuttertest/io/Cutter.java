package com.example.roman.audiocuttertest.io;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by Roman on 06.08.2017.
 */

public class Cutter implements FFmpegExecuteResponseHandler {

    private FFmpeg fFmpeg;
    private MediaPlayer toCutMedia;
    private File toCutFile;

    private File afterCut; //saves afterCut here after cut

    private Context context;

    private int beginning, end;
    private boolean set1, set2;

    private Semaphore wait;

    public Cutter(Context context, FFmpeg fFmpeg, MediaPlayer toCutMedia, File toCutFile){

        wait = new Semaphore(0);

        this.fFmpeg = fFmpeg;
        this.toCutMedia = toCutMedia;
        this.toCutFile = toCutFile;
        this.context = context;

        beginning = 0;
        end = toCutMedia.getDuration();
    }

    public int markBeginning(){
        beginning = toCutMedia.getCurrentPosition();
        set1 = true;
        return beginning;
    }

    public int markEnd(){
        end = toCutMedia.getCurrentPosition();
        set2 = true;
        return end;
    }

    public boolean cutAllowed(){
        return set1 && set2 && (beginning != end);
    }

    public MediaPlayer cutWithFFMPEG(){

        File cut = getTemporaryCutFileLocation();

        if(!cut.getAbsolutePath().equals(toCutFile.getAbsolutePath())) {
            cut.delete();
        } else {
            do {
                cut = getTemporaryCutFileLocationWithName("recursive_"+randomInt(69,1337)+".mp3");
            } while(cut.exists());
        }

        if(beginning <= end) {
            String[] cmd = new String[9];
            cmd[0] = "-i";
            cmd[1] = toCutFile.getAbsolutePath();
            cmd[2] = "-ss";
            cmd[3] = formatDurationPrecise(beginning);
            cmd[4] = "-to";
            cmd[5] = formatDurationPrecise(end);
            cmd[6] = "-c";
            cmd[7] = "copy";
            cmd[8] = cut.getAbsolutePath();

            for(int i=0;i<cmd.length;i++){
                Log.d("CMD",cmd[i]);
            }

            try {
                fFmpeg.execute(cmd, this);

            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }

        } else {

            File tmp1 = getTemporaryCutFileLocationWithName("temp_cut1.mp3");
            File tmp2 = getTemporaryCutFileLocationWithName("temp_cut2.mp3");

            tmp1.delete();
            tmp2.delete();

            String[] cmd = new String[9];
            cmd[0] = "-i";
            cmd[1] = toCutFile.getAbsolutePath();
            cmd[2] = "-ss";
            cmd[3] = "0";
            cmd[4] = "-to";
            cmd[5] = formatDurationPrecise(end);
            cmd[6] = "-c";
            cmd[7] = "copy";
            cmd[8] = tmp1.getAbsolutePath();

            String[] cmd1 = new String[9];
            cmd1[0] = "-i";
            cmd1[1] = toCutFile.getAbsolutePath();
            cmd1[2] = "-ss";
            cmd1[3] = formatDurationPrecise(beginning);
            cmd1[4] = "-to";
            cmd1[5] = formatDurationPrecise(toCutMedia.getDuration());
            cmd1[6] = "-c";
            cmd1[7] = "copy";
            cmd1[8] = tmp2.getAbsolutePath();

            String[] cmd2 = new String[5];
            cmd2[0] = "-i";
            cmd2[1] = "concat:"+tmp1.getAbsolutePath()+"|"+tmp2.getAbsolutePath();
            cmd2[2] = "-acodec";
            cmd2[3] = "copy";
            cmd2[4] = cut.getAbsolutePath();

            try {
                fFmpeg.execute(cmd, null);
                fFmpeg.execute(cmd1, null);
                fFmpeg.execute(cmd2, this);

            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }

        }

        try {
            wait.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        afterCut = cut;
        MediaPlayer mediaPlayer = null;
        Uri myUri = Uri.fromFile(cut); // initialize Uri here

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(context, myUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mediaPlayer;
    }

    public File getTemporaryCutFileLocation(){
        return (afterCut == null) ? getTemporaryCutFileLocationWithName("temp_cut.mp3") : afterCut;
    }

    private File getTemporaryCutFileLocationWithName(String filename){

        //File directory = cw.getDir("AudioCutter", Context.MODE_PRIVATE);
        //File directory = new File(cw.getFilesDir(), "AudioCutter");
        File directory = new File(getExternalStorageDirectory().getAbsolutePath()+"/AudioCutter");

        if(!directory.exists())
            directory.mkdirs();

        final File mypath = new File(directory,filename);

        if(mypath.getParentFile() != null && !mypath.getParentFile().exists())
            mypath.getParentFile().mkdirs();

        return mypath;
    }

    public static String formatDuration(int finalTime){
        return String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                finalTime)));
    }

    public static String formatDurationPrecise(int l){
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
    }

    @Override
    public void onSuccess(String message) {
        Log.d("CUTT",message);
    }

    @Override
    public void onProgress(String message) {
        Log.d("CUTT",message);
    }

    @Override
    public void onFailure(String message) {
        Log.d("CUTT",message);
    }

    @Override
    public void onStart() {
        Log.d("CUTT","start");
    }

    @Override
    public void onFinish() {
        wait.release();
    }

    public static int randomInt(int min, int max) {
        return 1 + (int) (new Random().nextDouble() * (max-1 + (min)));
    }
}
