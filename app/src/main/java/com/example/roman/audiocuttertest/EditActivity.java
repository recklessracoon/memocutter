package com.example.roman.audiocuttertest;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roman.audiocuttertest.io.AudioLoader;
import com.example.roman.audiocuttertest.io.AudioLoaderCallback;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.Duration;

import rm.com.audiowave.AudioWaveView;
import rm.com.audiowave.OnProgressListener;
import rm.com.audiowave.OnSamplingListener;

public class EditActivity extends AppCompatActivity implements AudioLoaderCallback {

    private MediaPlayer mediaPlayer;
    private Handler mHandler;
    private SeekBar seekBar;

    private ImageButton rev1, pause1, play1;
    private TextView from1, now1, to1;

    int size;

    private Runnable updateBar = new Runnable() {

        @Override
        public void run() {
            if(mediaPlayer != null){
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                now1.setText(formatDuration(mediaPlayer.getCurrentPosition()));
                if(mediaPlayer.isPlaying())
                    mHandler.postDelayed(this, 100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mHandler = new Handler();

        initButtons();
        initTextViews();
        enqueueMediaPlayerInit();
    }

    private void initTextViews(){
        from1 = (TextView) findViewById(R.id.from_1);
        now1 = (TextView) findViewById(R.id.now_1);
        to1 = (TextView) findViewById(R.id.to_1);
    }

    private void initButtons(){
        rev1 = (ImageButton) findViewById(R.id.rev_1);
        pause1 = (ImageButton) findViewById(R.id.pause_1);
        play1 = (ImageButton) findViewById(R.id.play_1);

        play1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer != null){
                    mediaPlayer.start();
                    EditActivity.this.runOnUiThread(updateBar);
                }
            }
        });

        pause1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer != null){
                    mediaPlayer.pause();
                }
            }
        });
    }

    private void enqueueMediaPlayerInit(){
        Bundle extras = getIntent().getExtras();
        File file = (File) extras.getSerializable("theFile");
        new AudioLoader(this, file, this).start();
    }

    private void initAudioWave(){
        seekBar = (SeekBar) findViewById(R.id.seekBar_1);
        seekBar.setMax(mediaPlayer.getDuration());

        from1.setText(formatDuration(0));
        to1.setText(formatDuration(mediaPlayer.getDuration()));

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
                }
            }
        });
    }

    public void onPause(){
        super.onPause();
        if(mediaPlayer != null){
            mediaPlayer.pause();
        }
    }

    @Override
    public void audioLoadSuccess(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        initAudioWave();
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

    public void makeToast(String text){
        Toast.makeText(getApplicationContext(),
                text , Toast.LENGTH_LONG)
                .show();
    }

    public String formatDuration(int finalTime){
        return String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                finalTime)));
    }
}
