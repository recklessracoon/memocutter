package com.example.roman.audiocuttertest.io;

import android.content.Context;
import android.util.Log;

import com.example.roman.audiocuttertest.R;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by Roman on 05.09.2017.
 */

public abstract class Cutter {

    protected Context context;
    protected FFmpeg fFmpeg;
    protected CutterCallback callback;

    public Cutter(Context context, CutterCallback callback){
        this.context = context;
        this.callback = callback;
        initFFMPEG();
    }

    public abstract int markBeginning(int beg);
    public abstract int markEnd(int end);
    public abstract boolean cutAllowed();

    public abstract void cutWithFFMPEGAsync();
    public abstract void concatWithFFMPEGAsync(ArrayList<File> toConcat);
    public abstract void convertFromOpusToMp3Async();

    private void initFFMPEG() {
        fFmpeg = FFmpeg.getInstance(context);
        try {
            fFmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {

                }

                @Override
                public void onFailure() {
                    //makeSnackbar("Could not instantiate FFMPEG");
                }

                @Override
                public void onSuccess() {
                    //makeSnackbar("FFPEG instantiating successful");
                }

                @Override
                public void onFinish() {

                }

            });
        } catch (FFmpegNotSupportedException e) {
            callback.ffmpegInitFailed(e);
        }
    }

    protected File getMemoCutterLocation(){
        return new File(getExternalStorageDirectory().getAbsolutePath()+"/"+context.getString(R.string.folder_name));
    }

    public static String formatDuration(int finalTime){
        return String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                finalTime)));
    }

    public static String formatDurationPrecise(long l){
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
    }

    public static int randomInt(int min, int max) {
        return 1 + (int) (new Random().nextDouble() * (max-1 + (min)));
    }

    public static File getTemporaryCutFileLocationWithName(String filename){
        Log.d("STOOPID","sjdfiskdk");
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
}
