package com.example.roman.audiocuttertest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.example.roman.audiocuttertest.adapters.EditMemoAdapter;
import com.example.roman.audiocuttertest.adapters.EditMemoAdapterShareCallback;
import com.example.roman.audiocuttertest.decorators.SwipeableDeletableRecyclerViewDecorator;
import com.example.roman.audiocuttertest.io.AudioLoader;
import com.example.roman.audiocuttertest.io.Cutter;
import com.example.roman.audiocuttertest.io.CutterCallback;
import com.example.roman.audiocuttertest.io.CutterImpl;
import com.example.roman.audiocuttertest.io.Wrap;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.os.Environment.getExternalStorageDirectory;

public class EditActivity extends AppCompatActivity implements EditMemoAdapterShareCallback, CutterCallback {

    private Handler mHandler;

    private ProgressDialog progressConvert;

    private MediaPlayer mediaPlayer;
    private Cutter cutter;

    private TextView leftTime, rightTime;
    private ImageButton playPauseButton;
    private FloatingActionButton floatingCutButton;

    private RecyclerView mRecyclerView;
    private EditMemoAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private CrystalRangeSeekbar rangeBar;

    private final Runnable updateBar = new Runnable() {
        @Override
        public void run() {
            if(mediaPlayer != null){
                rangeBar.setMinStartValue(rangeBar.getSelectedMinValue().floatValue()).setMaxStartValue(mediaPlayer.getCurrentPosition()).apply();
                rightTime.setText(Cutter.formatDurationPrecise(mediaPlayer.getCurrentPosition()));
                if(mediaPlayer.isPlaying())
                    mHandler.postDelayed(this, 100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_new);

        mHandler = new Handler();

        initProgressDialog();

        initTextViews();
        initButtons();
        initRangeBar();
        initMediaPlayerAndCutter();
        initRecyclerView();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initRecyclerView(){
        mRecyclerView = (RecyclerView) findViewById(R.id.edit_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new EditMemoAdapter(new ArrayList<Wrap>());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        new SwipeableDeletableRecyclerViewDecorator().withContext(this).withRecyclerView(mRecyclerView).withRecyclerViewAdapterWithRemoveOption(mAdapter).apply();
    }

    private void initProgressDialog(){
        progressConvert = new ProgressDialog(this);
        progressConvert.setTitle(getString(R.string.edit_wait));
        progressConvert.setMessage(getString(R.string.edit_convert));
        progressConvert.setCancelable(false); // disable dismiss by tapping outside of the dialog
    }

    private void initTextViews(){
        leftTime = (TextView) findViewById(R.id.edit_lefttime);
        rightTime = (TextView) findViewById(R.id.edit_righttime);

        leftTime.setText(Cutter.formatDurationPrecise(0));
        rightTime.setText(Cutter.formatDurationPrecise(0));
    }

    private void initButtons() {
        playPauseButton = (ImageButton) findViewById(R.id.edit_playpause);
        floatingCutButton = (FloatingActionButton) findViewById(R.id.edit_floatingActionButton);

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer != null){
                    if(!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        mHandler.post(updateBar);
                        playPauseButton.setImageResource(R.drawable.ic_pause_circle_outline_white_24dp);
                    } else {
                        mediaPlayer.pause();
                        playPauseButton.setImageResource(R.drawable.ic_play_circle_outline_white_24dp);
                    }
                }
            }
        });

        floatingCutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cutter.markBeginning(rangeBar.getSelectedMinValue().intValue());
                cutter.markEnd(rangeBar.getSelectedMaxValue().intValue());

                if(cutter.cutAllowed()){
                    makeSnackbar(getString(R.string.edit_cut));
                    cutter.cutWithFFMPEGAsync();
                } else {
                    makeSnackbar(getString(R.string.edit_cut_not_allowed));
                }

            }
        });
    }

    private void initRangeBar(){
        rangeBar = (CrystalRangeSeekbar) findViewById(R.id.edit_seekbar);

        rangeBar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                leftTime.setText(Cutter.formatDurationPrecise(minValue.intValue()));
                rightTime.setText(Cutter.formatDurationPrecise(maxValue.intValue()));
                if(mediaPlayer != null)
                    mediaPlayer.seekTo(maxValue.intValue());
            }
        });

    }

    private void initMediaPlayerAndCutter(){
        Bundle extras = getIntent().getExtras();

        final File audioFile = (extras.getSerializable("theFile") == null) ?
                new File((AudioLoader.getRealPathFromURI(this, ((Uri)extras.get("android.intent.extra.STREAM"))))) :
                ((File) extras.getSerializable("theFile"));

        Uri myUri = Uri.fromFile(audioFile); // initialize Uri here
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playPauseButton.setImageResource(R.drawable.ic_play_circle_outline_white_24dp);
            }
        });

        cutter = new CutterImpl(this, this, mediaPlayer, audioFile);

        try {
            mediaPlayer.setDataSource(this, myUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            //e.printStackTrace();
            cutter.convertFromOpusToMp3Async();
            progressConvert.show();
        }

        rangeBar.setMaxValue((float)(mediaPlayer.getDuration()));
        rangeBar.setMinStartValue(0).setMaxStartValue(0).apply();
    }

    public void onPause(){
        super.onPause();
        if(mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.pause();
    }

    public void makeSnackbar(String text){
        /*
        Toast.makeText(getApplicationContext(),
                text , Toast.LENGTH_LONG)
                .show();
                */
        Snackbar snackbar1 = Snackbar.make(mRecyclerView, text, Snackbar.LENGTH_SHORT);
        snackbar1.show();
    }

    // Returns path to the last temporary saved file, used as buffer for conversion
    public static File getTemporarySavedFile(Context context){
        //File directory = cw.getDir("AudioCutter", Context.MODE_PRIVATE);
        //File directory = new File(cw.getFilesDir(), "AudioCutter");
        File directory = new File(getExternalStorageDirectory().getAbsolutePath()+"/"+context.getString(R.string.folder_name));

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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void share(File actualFile) {
        String sharePath = actualFile.getAbsolutePath();
        Uri uri = Uri.parse(sharePath);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/*");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, getString(R.string.other_share)));
    }

    @Override
    public void cutFinished(MediaPlayer mediaPlayer, File location) {
        mAdapter.addCutFile(new Wrap(location, location.getName(), mediaPlayer));
    }

    @Override
    public void cutFailed(Exception e) {
        makeSnackbar(getString(R.string.edit_cut_fail));
    }

    @Override
    public void conversionFinished(MediaPlayer mediaPlayer){
        this.mediaPlayer = mediaPlayer;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playPauseButton.setImageResource(R.drawable.ic_play_circle_outline_white_24dp);
            }
        });

        rangeBar.setMaxValue((float)(mediaPlayer.getDuration()));
        rangeBar.setMinStartValue(0).setMaxStartValue(0).apply();

        progressConvert.dismiss();
    }

    @Override
    public void conversionFailed(Exception e) {
        makeSnackbar(getString(R.string.edit_load_fail));
    }

    @Override
    public void concatFinished(MediaPlayer mediaPlayer) {
        //TODO
    }

    @Override
    public void concatFailed(Exception e) {
        //TODO
    }

    @Override
    public void ffmpegInitFailed(FFmpegNotSupportedException e) {
        makeSnackbar(getString(R.string.edit_cut_ffmpeg_fail));
    }

}
