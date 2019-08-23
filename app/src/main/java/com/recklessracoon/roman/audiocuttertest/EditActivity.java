package com.recklessracoon.roman.audiocuttertest;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.roman.thesimplerangebar.SimpleRangeBar;
import com.example.roman.thesimplerangebar.SimpleRangeBarOnChangeListener;
import com.recklessracoon.roman.audiocuttertest.adapters.EditMemoAdapter;
import com.recklessracoon.roman.audiocuttertest.adapters.EditMemoAdapterShareCallback;
import com.recklessracoon.roman.audiocuttertest.decorators.SwipeableDeletableRecyclerViewDecorator;
import com.recklessracoon.roman.audiocuttertest.io.AudioLoader;
import com.recklessracoon.roman.audiocuttertest.io.Cutter;
import com.recklessracoon.roman.audiocuttertest.io.CutterCallback;
import com.recklessracoon.roman.audiocuttertest.io.CutterImpl;
import com.recklessracoon.roman.audiocuttertest.io.Wrap;
import com.recklessracoon.roman.audiocuttertest.theming.BackgroundStyle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.os.Environment.getExternalStorageDirectory;

public class EditActivity extends AppCompatActivity implements EditMemoAdapterShareCallback, CutterCallback {

    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 420;

    private Handler mHandler;

    private ProgressDialog progressConvert, progressConcat;

    private MediaPlayer mediaPlayer;
    private Cutter cutter;
    private File audioFile;

    private TextView leftTime, rightTime;
    private ImageButton playPauseButton;
    private FloatingActionButton floatingCutButton;

    private RecyclerView mRecyclerView;
    private EditMemoAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SimpleRangeBar rangeBar;

    private LinearLayout mLinear;

    private final Runnable updateBar = new Runnable() {
        @Override
        public void run() {
            if(mediaPlayer != null){
                try {
                    if (mediaPlayer.isPlaying()) {

                        //long time = SystemClock.currentThreadTimeMillis();
                        rangeBar.setThumbValues(rangeBar.getLeftThumbValue(), mediaPlayer.getCurrentPosition());
                        //time = SystemClock.currentThreadTimeMillis() - time;
                        //Log.d("TIME", "" + Cutter.formatDurationPrecise((int) time));

                        rightTime.setText(Cutter.formatDurationPrecise(mediaPlayer.getCurrentPosition()));

                        mHandler.postDelayed(this, 50);
                    }
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.recklessracoon.roman.audiocuttertest.R.layout.activity_edit_new);

        mHandler = new Handler();

        initProgressDialog();

        initTextViews();
        initButtons();
        initRangeBar();
        initRecyclerView();
        initMediaPlayerAndCutter();

        android.support.v7.app.ActionBar bar = getSupportActionBar();

        if(bar != null) {

            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);

            if (audioFile != null && audioFile.getName() != null)
                bar.setTitle(audioFile.getName());

        }

        mLinear = (LinearLayout) findViewById(R.id.activity_edit_linear);
        mLinear.setBackground(BackgroundStyle.getBackgroundDrawable(this));

        checkPermissions();
    }

    private void checkPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            makeSnackbar(getString(R.string.permission_explain));

                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeSnackbar(getString(R.string.permission_granted));
                } else {
                    makeSnackbar(getString(R.string.permission_denied));
                }
                return;
            }

        }
    }

    private void initRecyclerView(){
        mRecyclerView = (RecyclerView) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.edit_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new EditMemoAdapter(new ArrayList<Wrap>());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        new SwipeableDeletableRecyclerViewDecorator().withContext(this).withRecyclerView(mRecyclerView).apply();
    }

    private void initProgressDialog(){
        progressConvert = new ProgressDialog(this);
        progressConvert.setTitle(getString(com.recklessracoon.roman.audiocuttertest.R.string.edit_wait));
        progressConvert.setMessage(getString(com.recklessracoon.roman.audiocuttertest.R.string.edit_convert));
        progressConvert.setCancelable(false); // disable dismiss by tapping outside of the dialog

        progressConcat = new ProgressDialog(this);
        progressConcat.setTitle(getString(com.recklessracoon.roman.audiocuttertest.R.string.edit_wait));
        progressConcat.setMessage(getString(com.recklessracoon.roman.audiocuttertest.R.string.edit_concat));
        progressConcat.setCancelable(false); // disable dismiss by tapping outside of the dialog
    }

    private void initTextViews(){
        leftTime = (TextView) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.edit_lefttime);
        rightTime = (TextView) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.edit_righttime);

        leftTime.setText(Cutter.formatDurationPrecise(0));
        rightTime.setText(Cutter.formatDurationPrecise(0));
    }

    private void initButtons() {
        playPauseButton = (ImageButton) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.edit_playpause);
        floatingCutButton = (FloatingActionButton) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.edit_floatingActionButton);

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mediaPlayer != null) {
                        if (!mediaPlayer.isPlaying()) {

                            mediaPlayer.seekTo((int) rangeBar.getRightThumbValue());

                            mediaPlayer.start();
                            mHandler.post(updateBar);
                            playPauseButton.setImageResource(com.recklessracoon.roman.audiocuttertest.R.drawable.ic_pause_circle_outline_black_24dp);
                        } else {
                            mediaPlayer.pause();
                            playPauseButton.setImageResource(com.recklessracoon.roman.audiocuttertest.R.drawable.ic_play_circle_outline_black_24dp);
                        }
                    }
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }
        });

        floatingCutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cutter.markBeginning((int)rangeBar.getLeftThumbValue());
                cutter.markEnd((int)rangeBar.getRightThumbValue());

                if(cutter.cutAllowed()){
                    makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.edit_cut));
                    cutter.cutWithFFMPEGAsync();
                } else {
                    makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.edit_cut_not_allowed));
                }

            }
        });
    }

    private void initRangeBar(){
        rangeBar = (SimpleRangeBar) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.edit_seekbar);

        rangeBar.setOnSimpleRangeBarChangeListener(new SimpleRangeBarOnChangeListener() {
            @Override
            public void leftThumbValueChanged(long l) {
                leftTime.setText(Cutter.formatDurationPrecise(l));
            }

            @Override
            public void rightThumbValueChanged(long l) {
                rightTime.setText(Cutter.formatDurationPrecise(l));
            }
        });
    }

    private void handleConcatIntent(ArrayList<File> toConcat){
        if(toConcat == null) // nothing to do
            return;

        progressConcat.show();
        cutter.concatWithFFMPEGAsync(toConcat);
    }

    private void initMediaPlayerAndCutter(){
        Bundle extras = getIntent().getExtras();

        File audioFile = null;

        try {
            audioFile = (extras.getSerializable("theFile") == null) ?
                    new File((AudioLoader.getRealPathFromURI(this, ((Uri) extras.get("android.intent.extra.STREAM")), getIntent().getFlags()))) :
                    ((File) extras.getSerializable("theFile"));
        } catch (NullPointerException e){
            makeSnackbar(getString(R.string.edit_load_fail));
            return;
        }

        Uri myUri = Uri.fromFile(audioFile); // initialize Uri here
        this.audioFile = audioFile;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playPauseButton.setImageResource(com.recklessracoon.roman.audiocuttertest.R.drawable.ic_play_circle_outline_black_24dp);

                long min = rangeBar.getLeftThumbValue();
                rangeBar.setThumbValues(min, min); // let the mediaplayer start from the front
                //mediaPlayer.seekTo(rangeBar.getSelectedMinValue().intValue());
            }
        });

        cutter = new CutterImpl(this, this, mediaPlayer, audioFile);

        Log.d("CURI2",""+audioFile.getAbsolutePath());

        //Log.d("OPUS",audioFile.getName());

        if(!audioFile.getName().contains(".mp3")) { // not sure which extension, bc received just the outputstream of the file via share function
            //Log.d("OPUS","im here");
            progressConvert.show();
            cutter.convertFromOpusToMp3Async();
        }

        try {
            mediaPlayer.setDataSource(this, myUri);
            mediaPlayer.prepare();
        } catch (IOException e) { // could not load, no supported format, try converting to mp3
            e.printStackTrace();
            //progressConvert.show();
            //cutter.convertFromOpusToMp3Async();
        }

        rangeBar.setRanges(0, mediaPlayer.getDuration());
        rangeBar.setThumbValues(0,0);

        ArrayList<File> toConcat = (ArrayList<File>) extras.getSerializable("filesList");
        handleConcatIntent(toConcat);
    }

    public void onPause(){
        super.onPause();
        if(mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.pause();
        mAdapter.pauseAll();
    }

    public void makeSnackbar(String text){
        /*
        Toast.makeText(getApplicationContext(),
                text , Toast.LENGTH_LONG)
                .show();
                */
        Snackbar snackbar1 = Snackbar.make(mRecyclerView, text, Snackbar.LENGTH_LONG);
        snackbar1.show();
    }

    // Returns path to the last temporary saved file, used as buffer for conversion
    public static File getTemporarySavedFile(Context context){
        //File directory = cw.getDir("AudioCutter", Context.MODE_PRIVATE);
        //File directory = new File(cw.getFilesDir(), "AudioCutter");
        File directory = new File(getExternalStorageDirectory().getAbsolutePath()+"/"+context.getString(com.recklessracoon.roman.audiocuttertest.R.string.folder_name));

        if(!directory.exists())
            directory.mkdirs();

        final File mypath = new File(directory,"temp.mp3");

        if(mypath.getParentFile() != null && !mypath.getParentFile().exists())
            Log.d("CREATEFILE",""+mypath.getParentFile().mkdirs());

        try {
            mypath.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            //mypath.mkdirs();
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

        if (id == com.recklessracoon.roman.audiocuttertest.R.id.mybutton) {
            share(audioFile);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void share(File actualFile) {
        shareFromContentUri(actualFile);
    }

    private void shareFromContentUri(File file) {
        MediaScannerConnection.scanFile(getBaseContext(), new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("audio/*");
                share.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(share, getString(com.recklessracoon.roman.audiocuttertest.R.string.other_share)));
            }
        });
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.recklessracoon.roman.audiocuttertest.R.menu.share_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void cutFinished(MediaPlayer mediaPlayer, File location) {
        mAdapter.addCutFile(new Wrap(location, location.getName(), mediaPlayer));
    }

    @Override
    public void cutFailed(Exception e) {
        makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.edit_cut_fail));
    }

    @Override
    public void onProgress(String message){
        if(progressConvert != null && progressConvert.isShowing()){
            progressConvert.setMessage(message);
        }

        if(progressConcat != null && progressConcat.isShowing()){
            progressConcat.setMessage(message);
        }
    }

    @Override
    public void conversionFinished(MediaPlayer mediaPlayer){
        handleNewMediaPlayerArrival(mediaPlayer);

        if (EditActivity.this.isDestroyed()) {
            return;
        }

        if (progressConvert != null && progressConvert.isShowing()) {
            progressConvert.dismiss();
        }
    }

    @Override
    public void conversionFailed(Exception e) {
        makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.edit_load_fail));

        if (EditActivity.this.isDestroyed()) {
            return;
        }

        if (progressConvert != null && progressConvert.isShowing()) {
            progressConvert.dismiss();
        }
    }

    @Override
    public void concatFinished(MediaPlayer mediaPlayer, File file) {
        handleNewMediaPlayerArrival(mediaPlayer);
        if (EditActivity.this.isDestroyed()) {
            return;
        }

        if (progressConcat != null && progressConcat.isShowing()) {
            progressConcat.dismiss();
        }
        makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.edit_concat_success));

        audioFile = file;
        getSupportActionBar().setTitle(audioFile.getName());
    }

    @Override
    public void concatFailed(Exception e) {
        if (EditActivity.this.isDestroyed()) {
            return;
        }

        if (progressConcat != null && progressConcat.isShowing()) {
            progressConcat.dismiss();
        }
        makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.edit_concat_fail));
    }

    @Override
    public void ffmpegInitFailed() {
        //makeSnackbar(getString(R.string.edit_cut_ffmpeg_fail));
        makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.edit_cut_fail));
    }

    private void handleNewMediaPlayerArrival(final MediaPlayer mediaPlayer){
        this.mediaPlayer = mediaPlayer;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playPauseButton.setImageResource(com.recklessracoon.roman.audiocuttertest.R.drawable.ic_play_circle_outline_black_24dp);

                long min = rangeBar.getLeftThumbValue();
                rangeBar.setThumbValues(min, min); // let the mediaplayer start from the front
                //EditActivity.this.mediaPlayer.seekTo(rangeBar.getSelectedMinValue().intValue());
                mediaPlayer.seekTo((int)min);
            }
        });

        rangeBar.setRanges(0, mediaPlayer.getDuration());
        rangeBar.setThumbValues(0,0);
        mediaPlayer.seekTo(0);
        Log.d("VALUES","(handled new mediaplayer)"+rangeBar.getLeftThumbValue()+" "+rangeBar.getRightThumbValue());
    }

}