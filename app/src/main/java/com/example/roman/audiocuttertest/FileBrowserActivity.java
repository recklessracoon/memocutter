package com.example.roman.audiocuttertest;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ProgressBar;

import com.example.roman.audiocuttertest.adapters.FileBrowserAdapter;
import com.example.roman.audiocuttertest.decorators.SwipeableDeletableRecyclerViewDecorator;
import com.example.roman.audiocuttertest.io.AudioDetector;
import com.example.roman.audiocuttertest.io.AudioDetectorCallback;
import com.example.roman.audiocuttertest.decorators.AudioFilesSingleton;
import com.example.roman.audiocuttertest.io.Wrap;

import java.io.File;
import java.util.ArrayList;

public class FileBrowserActivity extends AppCompatActivity implements AudioDetectorCallback, SearchView.OnQueryTextListener {

    private RecyclerView mRecyclerView;
    private FileBrowserAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ProgressBar mProgressBar;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private AudioDetector audioDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_recycle);

        // progress dialog is only confusing the user probably
        mProgressBar.setVisibility(View.GONE);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

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
        }
    }

    private void initRecyclerView(){
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
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
                mAdapter = new FileBrowserAdapter(audioFiles);
                mRecyclerView.setAdapter(mAdapter);

                mAdapter.notifyDataSetChanged();
                mRecyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);

                mSwipeRefreshLayout.setRefreshing(false);

                new SwipeableDeletableRecyclerViewDecorator().withContext(context).withRecyclerView(mRecyclerView).withRecyclerViewAdapterWithRemoveOption(mAdapter).apply();
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
        inflater.inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        //SearchView searchView = (SearchView) menu.findItem(R.id.search_menu).getActionView();
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search_menu_item));

        searchView.setOnQueryTextListener(this);

        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
        if(searchView != null)
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
        /*
        Toast.makeText(getApplicationContext(),
                text , Toast.LENGTH_LONG)
                .show();
                */
        Snackbar snackbar1 = Snackbar.make(mRecyclerView, text, Snackbar.LENGTH_SHORT);
        snackbar1.show();
    }
}
