package com.recklessracoon.roman.audiocuttertest;

import android.Manifest;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.recklessracoon.roman.audiocuttertest.adapters.FileBrowserAdapter;
import com.recklessracoon.roman.audiocuttertest.decorators.SwipeableDeletableRecyclerViewDecorator;
import com.recklessracoon.roman.audiocuttertest.io.AudioDetector;
import com.recklessracoon.roman.audiocuttertest.io.AudioDetectorCallback;
import com.recklessracoon.roman.audiocuttertest.helpers.AudioFilesSingleton;
import com.recklessracoon.roman.audiocuttertest.io.Wrap;
import com.recklessracoon.roman.audiocuttertest.theming.BackgroundStyle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class FileBrowserActivity extends AppCompatActivity implements AudioDetectorCallback, SearchView.OnQueryTextListener {

    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 420;

    private static RecyclerView mRecyclerView;
    private FileBrowserAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean mergeModeOn;

    private ProgressBar mProgressBar;

    private FloatingActionButton mCombineButton;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private AudioDetector audioDetector;

    private Semaphore refreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.recklessracoon.roman.audiocuttertest.R.layout.activity_file_browser);

        refreshing = new Semaphore(1);
        handleMergeMode();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mProgressBar = (ProgressBar) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.progress_bar_recycle);

        mCombineButton = (FloatingActionButton) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.browse_floatingActionButton);
        if(!mergeModeOn)
            mCombineButton.setVisibility(View.GONE);

        // progress dialog is only confusing the user probably
        mProgressBar.setVisibility(View.GONE);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems(false);
            }
        });

        initRecyclerView();

        MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                onMediaPlayerThrowsError(mp, what, extra);
                return false;
            }
        };

        AudioFilesSingleton.applyOnErrorListener(onErrorListener); // hook-in this callbacks onError listener onto preloaded files
        refreshItems(false);

        handleIntent(getIntent());

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.activity_edit);
        coordinatorLayout.setBackground(BackgroundStyle.getBackgroundDrawable(this));

        mRecyclerView.setBackground(BackgroundStyle.getBackgroundDrawable(this));

        checkPermissions();
    }

    private void handleMergeMode(){
        mergeModeOn = getIntent().getBooleanExtra("MERGE", false);
        Log.d("MERGE",""+mergeModeOn);
    }

    private void refreshItems(boolean resetBeforeRefresh){
        if(refreshing.tryAcquire()) {
            mSwipeRefreshLayout.setRefreshing(true);
            File parent = EditActivity.getTemporarySavedFile(this).getParentFile();
            audioDetector = new AudioDetector(this, parent, this);
            if (resetBeforeRefresh)
                AudioFilesSingleton.resetAudioFiles();
            audioDetector.start();
        }
    }

    @Override
    public void onMediaPlayerThrowsError(MediaPlayer mp, int what, int flag){
        //Log.d("ONHEAPOVER", "refreshing..");
        makeSnackbar(getString(R.string.need_to_reload));
        refreshItems(true);
    }

    private void initRecyclerView(){
        mRecyclerView = (RecyclerView) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.recyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
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
    public void onAudioDetectorSuccess(final ArrayList<Wrap> audioFiles) {
        // specify an adapter (see also next example)
        final Context context = this;

        final boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mAdapter = new FileBrowserAdapter(audioFiles, mergeModeOn, mCombineButton);
                mRecyclerView.setAdapter(mAdapter);

                mAdapter.notifyDataSetChanged();
                mAdapter.notifyItemRangeChanged(0,mAdapter.getItemCount());

                if(mAdapter.getItemCount() == 0 && hasPermission)
                    makeSnackbar(getString(R.string.edit_no_files));

                mRecyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);

                Log.d("DATA",""+mAdapter.getItemCount());

                new SwipeableDeletableRecyclerViewDecorator().withContext(context).withRecyclerView(mRecyclerView).apply();

                if(mSwipeRefreshLayout.isRefreshing())
                    mSwipeRefreshLayout.setRefreshing(false);

                AudioFilesSingleton.setAudioFiles(audioFiles);
                refreshing.release();
            }
        });

        //AudioFilesSingleton.setAudioFiles(audioFiles);
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

    @Override
    public void onAudioDetectorFail(File audioFile, Exception e) {
        if(mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
        makeSnackbar(getString(R.string.search_file_load_fail));
        refreshing.release();
        // TODO decide on what to do with latestFile.dat and stuff
    }

    @Override
    public void onParticularAudioLoadFail(File audioFile, IOException e) {
        if(audioFile != null && !audioFile.getName().contains(".dat") && !audioFile.getName().contains("temp.mp3"))
            makeSnackbar(getString(R.string.search_particular_file_load_fail)+" : "+audioFile.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.recklessracoon.roman.audiocuttertest.R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        //SearchView searchView = (SearchView) menu.findItem(R.id.search_menu).getActionView();
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(com.recklessracoon.roman.audiocuttertest.R.id.search_menu_item));

        searchView.setOnQueryTextListener(this);

        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());

        searchView.setSearchableInfo(info);

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            if(mAdapter == null)
                return;

            String query = intent.getStringExtra(SearchManager.QUERY);

            FileBrowserAdapter adapter = (FileBrowserAdapter) mRecyclerView.getAdapter();

            adapter.updateWithSearchQuery(query);
            Log.d("QUERY2",""+query);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {
        if(mAdapter == null)
            return false;

        FileBrowserAdapter adapter = (FileBrowserAdapter) mRecyclerView.getAdapter();

        pauseAll();

        adapter.updateWithSearchQuery(newText);
        //Log.d("QUERY2",""+newText);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText){

        return false;
    }

    public void makeSnackbar(String text){
        Snackbar snackbar1 = Snackbar.make(mRecyclerView, text, Snackbar.LENGTH_LONG);
        snackbar1.show();
    }

    public static void makeSnackbarOnRecyclerView(String text){
        if(mRecyclerView == null)
            return;

        Snackbar snackbar1 = Snackbar.make(mRecyclerView, text, Snackbar.LENGTH_LONG);
        snackbar1.show();
    }

    @Override
    public void onPause(){
        super.onPause();
        pauseAll();
    }

    private void pauseAll(){
        if(mAdapter != null && mAdapter.getItemCount() > 0)
            mAdapter.pauseAll();
    }
}
