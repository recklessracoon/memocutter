package com.example.roman.audiocuttertest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roman.audiocuttertest.io.AudioLoader;
import com.example.roman.audiocuttertest.io.AudioLoaderCallback;
import com.example.roman.audiocuttertest.io.Cutter;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.IOException;

import static android.os.Environment.getExternalStorageDirectory;

public class EditActivity extends AppCompatActivity implements AudioLoaderCallback {

    private FFmpeg ffmpeg;

    private MediaPlayer mediaPlayer, mediaPlayerFromCut;
    private Handler mHandler;
    private SeekBar seekBar, seekBarFromCut;

    private ProgressDialog progressConvert, progressCut;

    private ImageButton playpause1, playpause2;
    private TextView from1, now1, to1, name1, from2, now2, to2;


    private Button markBeginning, markEnd, cut;
    private TextView beginningTxt, endTxt;
    private Cutter cutter;

    private Runnable updateBar = new Runnable() {

        @Override
        public void run() {
            if(mediaPlayer != null){
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                now1.setText(Cutter.formatDurationPrecise(mediaPlayer.getCurrentPosition()));
                if(mediaPlayer.isPlaying())
                    mHandler.postDelayed(this, 100);
            }
        }
    };

    private Runnable updateBarFromCut = new Runnable() {

        @Override
        public void run() {
            if(mediaPlayerFromCut != null){
                seekBarFromCut.setProgress(mediaPlayerFromCut.getCurrentPosition());
                now2.setText(Cutter.formatDurationPrecise(mediaPlayerFromCut.getCurrentPosition()));
                if(mediaPlayerFromCut.isPlaying())
                    mHandler.postDelayed(this, 100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mHandler = new Handler();

        initProgressDialog();
        initFFMPEG();

        initButtons();
        initTextViews();
        enqueueMediaPlayerInit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initProgressDialog(){
        progressConvert = new ProgressDialog(this);
        progressConvert.setTitle(getString(R.string.edit_wait));
        progressConvert.setMessage(getString(R.string.edit_convert));
        progressConvert.setCancelable(false); // disable dismiss by tapping outside of the dialog

        progressCut = new ProgressDialog(this);
        progressCut.setTitle(getString(R.string.edit_wait));
        progressCut.setMessage(getString(R.string.edit_cut));
        progressCut.setCancelable(false);
    }

    private void initTextViews(){
        from1 = (TextView) findViewById(R.id.from_1);
        now1 = (TextView) findViewById(R.id.now_1);
        to1 = (TextView) findViewById(R.id.to_1);
        name1 = (TextView) findViewById(R.id.name_1);


        from2 = (TextView) findViewById(R.id.from_2);
        now2 = (TextView) findViewById(R.id.now_2);
        to2 = (TextView) findViewById(R.id.to_2);

        beginningTxt = (TextView) findViewById(R.id.edit_begin_txt);
        endTxt = (TextView) findViewById(R.id.edit_end_txt);
    }

    private void initButtons(){

        playpause1 = (ImageButton) findViewById(R.id.playpause_1);
        playpause2 = (ImageButton) findViewById(R.id.playpause_2);

        markBeginning = (Button) findViewById(R.id.edit_begin_btn);
        markEnd = (Button) findViewById(R.id.edit_end_btn);

        cut = (Button) findViewById(R.id.edit_btn_cut);

        playpause1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer != null){
                    if(!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        EditActivity.this.runOnUiThread(updateBar);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playpause1.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                            }
                        });
                    } else {
                        mediaPlayer.pause();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playpause1.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                            }
                        });
                    }
                }
            }
        });

        playpause2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayerFromCut != null){
                    if(!mediaPlayerFromCut.isPlaying()) {
                        mediaPlayerFromCut.start();
                        EditActivity.this.runOnUiThread(updateBarFromCut);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playpause2.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                            }
                        });
                    } else {
                        mediaPlayerFromCut.pause();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playpause2.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                            }
                        });
                    }
                }
            }
        });

        markBeginning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginningTxt.setText(Cutter.formatDurationPrecise(cutter.markBeginning()));
            }
        });

        markEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTxt.setText(Cutter.formatDurationPrecise(cutter.markEnd()));
            }
        });

        cut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cutter.cutAllowed()){
                    progressCut.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mediaPlayerFromCutFileArrives(cutter.cutWithFFMPEG());
                        }
                    }).start();
                } else {
                    makeToast(getString(R.string.edit_cut_not_allowed));
                }
            }
        });
    }

    private void enqueueMediaPlayerInit(){

        Bundle extras = getIntent().getExtras();
        final File file = (File) extras.getSerializable("theFile");
        //Log.d("OPUS","contains opus?"+file.getName()+" "+file.getName().contains(".opus"));

        if(file.getName().contains(".opus")){ // convert to mp3 first

            progressConvert.show();

            String[] cmd = new String[5];
            cmd[0] = "-i";
            cmd[1] = file.getAbsolutePath();
            cmd[2] = "-acodec";
            cmd[3] = "libmp3lame";

            final File mypath = EditActivity.getTemporarySavedFile(this);

            cmd[4] = mypath.getAbsolutePath();

            final AudioLoaderCallback tmp = this;
            final Activity context = this;

            try {
                if(mypath.exists())
                    mypath.delete();

                ffmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
                    @Override
                    public void onSuccess(String message) {
                        name1.setText(file.getName());
                        new AudioLoader(context, mypath, tmp).start();
                        MainActivity.saveLastFile(context, mypath.getAbsolutePath());
                    }

                    @Override
                    public void onProgress(String message) {
                        Log.d("FFMPEG",message);
                    }

                    @Override
                    public void onFailure(String message) {
                        name1.setText(file.getName());
                        new AudioLoader(context, file, tmp).start();
                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onFinish() {
                        progressConvert.dismiss();
                    }
                });

                return;

            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
                makeToast("Still running exception");
            }
        }

        name1.setText(file.getName());
        new AudioLoader(this, file, this).start();
        MainActivity.saveLastFile(this, file.getAbsolutePath());
    }

    private void initSeekBar(){
        seekBar = (SeekBar) findViewById(R.id.seekBar_1);
        seekBarFromCut = (SeekBar) findViewById(R.id.seekBar_2);

        seekBar.setMax(mediaPlayer.getDuration());

        from1.setText(Cutter.formatDuration(0));
        now1.setText(Cutter.formatDurationPrecise(0));
        to1.setText(Cutter.formatDuration(mediaPlayer.getDuration()));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer != null && fromUser){
                    mediaPlayer.seekTo(progress);
                    now1.setText(Cutter.formatDurationPrecise(progress));
                }
            }
        });

        seekBarFromCut.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayerFromCut != null && fromUser){
                    mediaPlayerFromCut.seekTo(progress);
                    now2.setText(Cutter.formatDurationPrecise(progress));
                }
            }
        });
    }

    public void onPause(){
        super.onPause();
        if(mediaPlayer != null){
            mediaPlayer.pause();
        }
        if(mediaPlayerFromCut != null){
            mediaPlayerFromCut.pause();
        }
    }

    @Override
    public void audioLoadSuccess(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playpause1.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                    }
                });
            }
        });
        Bundle extras = getIntent().getExtras();
        final File file = (File) extras.getSerializable("theFile");
        cutter = new Cutter(this, ffmpeg, mediaPlayer, file);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initSeekBar();
            }
        });
    }

    @Override
    public void audioLoadFail(IOException e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                makeToast(getString(R.string.edit_load_fail));
            }
        });
    }

    private void mediaPlayerFromCutFileArrives(final MediaPlayer mediaPlayerFromCut){
        this.mediaPlayerFromCut = mediaPlayerFromCut;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playpause2.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                    }
                });
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                from2.setText(Cutter.formatDuration(0));
                now2.setText(Cutter.formatDurationPrecise(0));
                to2.setText(Cutter.formatDuration(mediaPlayerFromCut.getDuration()));

                seekBarFromCut.setMax(mediaPlayerFromCut.getDuration());

                progressCut.dismiss();
            }
        });
    }

    public void makeToast(String text){
        Toast.makeText(getApplicationContext(),
                text , Toast.LENGTH_LONG)
                .show();
    }

    private void initFFMPEG() {
        ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {

                }

                @Override
                public void onFailure() {
                    //makeToast("Could not instantiate FFMPEG");
                }

                @Override
                public void onSuccess() {
                    //makeToast("FFPEG instantiating successful");
                }

                @Override
                public void onFinish() {

                }

            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
        }
    }

    // Returns path to the last temporary saved file
    private static File getTemporarySavedFile(Context context){
        ContextWrapper cw = new ContextWrapper(context);

        //File directory = cw.getDir("AudioCutter", Context.MODE_PRIVATE);
        //File directory = new File(cw.getFilesDir(), "AudioCutter");
        File directory = new File(getExternalStorageDirectory().getAbsolutePath()+"/AudioCutter");

        if(!directory.exists())
            directory.mkdirs();

        final File mypath = new File(directory,"temp.mp3");

        if(mypath.getParentFile() != null && !mypath.getParentFile().exists())
            mypath.getParentFile().mkdirs();

        try {
            mypath.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mypath;
    }

    // Handles pressing the system back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //Log.d(this.getClass().getName(), "back button pressed");
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    // Handles pressing the back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            finish();
            return true;
        }

        if (id == R.id.mybutton) {

            if(mediaPlayerFromCut == null){
                makeToast(getString(R.string.other_share_nothing));
                return true;
            }

            String sharePath = cutter.getTemporaryCutFileLocation().getAbsolutePath();
            Uri uri = Uri.parse(sharePath);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/*");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(share, getString(R.string.other_share)));
        }

        return super.onOptionsItemSelected(item);
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
