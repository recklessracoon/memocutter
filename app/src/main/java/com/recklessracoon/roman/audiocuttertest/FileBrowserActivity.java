package com.recklessracoon.roman.audiocuttertest;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import java.util.ArrayList;

public class FileBrowserActivity extends AppCompatActivity implements AudioDetectorCallback, SearchView.OnQueryTextListener {

    private static RecyclerView mRecyclerView;
    private FileBrowserAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean mergeModeOn;

    private ProgressBar mProgressBar;

    private FloatingActionButton mCombineButton;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private AudioDetector audioDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.recklessracoon.roman.audiocuttertest.R.layout.activity_file_browser);

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
                refreshItems();
            }
        });

        initRecyclerView();
        refreshItems();

        handleIntent(getIntent());

        FrameLayout frameLayout = (FrameLayout) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.activity_edit);
        frameLayout.setBackground(BackgroundStyle.getBackgroundDrawable(this));

        mRecyclerView.setBackground(BackgroundStyle.getBackgroundDrawable(this));
    }

    private void handleMergeMode(){
        mergeModeOn = getIntent().getBooleanExtra("MERGE", false);
        Log.d("MERGE",""+mergeModeOn);
    }

    private void refreshItems(){
        ArrayList<Wrap> preloaded = AudioFilesSingleton.getAudioFiles();

        if(preloaded.size() == 0) { // Either not finished or no files
            File parent = EditActivity.getTemporarySavedFile(this).getParentFile();
            audioDetector = new AudioDetector(this, parent, this);
            audioDetector.start();
        } else { // Directly call success method with preloaded data
            AudioFilesSingleton.setAudioFiles(new ArrayList<Wrap>());
            onAudioDetectorSuccess(preloaded);
            AudioFilesSingleton.resetAudioFiles();
        }
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter = new FileBrowserAdapter(audioFiles, mergeModeOn, mCombineButton);
                mRecyclerView.setAdapter(mAdapter);

                mAdapter.notifyDataSetChanged();
                mAdapter.notifyItemRangeChanged(0,mAdapter.getItemCount());

                mRecyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);

                mSwipeRefreshLayout.setRefreshing(false);

                Log.d("DATA",""+mAdapter.getItemCount());

                new SwipeableDeletableRecyclerViewDecorator().withContext(context).withRecyclerView(mRecyclerView).apply();
            }
        });

    }

    @Override
    public void onAudioDetectorFail(File audioFile, Exception e) {
        //makeSnackbar(getString(R.string.search_file_load_fail));
        // TODO decide on what to do with latestFile.dat and stuff
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
            String query = intent.getStringExtra(SearchManager.QUERY);

            FileBrowserAdapter adapter = (FileBrowserAdapter) mRecyclerView.getAdapter();

            adapter.updateWithSearchQuery(query);
            Log.d("QUERY2",""+query);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {
        FileBrowserAdapter adapter = (FileBrowserAdapter) mRecyclerView.getAdapter();
        adapter.updateWithSearchQuery(newText);
        Log.d("QUERY2",""+newText);
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
        mAdapter.pauseAll();
    }
}
