package com.recklessracoon.roman.audiocuttertest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.recklessracoon.roman.audiocuttertest.adapters.ListFileAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roman on 01.03.2018.
 */

public class ListFileActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 420;

    private ListView mListView;
    private ListFileAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mListView = (ListView) findViewById(R.id.filesListView);
        mListView.setOnItemClickListener(this);

        if(!checkPermissions()) {
            makeSnackbar(getString(R.string.list_files_cannot_browse_no_permission));
            return;
        }

        ArrayList<File> data = new ArrayList<>();
        data.add(Environment.getExternalStorageDirectory());

        getSupportActionBar().setTitle(data.get(0).getName());

        mAdapter = new ListFileAdapter(this, data);
        mListView.setAdapter(mAdapter);
    }

    private void folderOpened(File folder){
        mAdapter.doClearData();
        for(File f : folder.listFiles()){
            if(isAllowedFolder(f) || isAllowedAudio(f))
                mAdapter.addFile(f);
        }
        mAdapter.notifyDataSetChanged();
        getSupportActionBar().setTitle(folder.getName());
    }

    private void audioOpened(File audio){
        Intent intent = new Intent(this, EditActivity.class);
        Bundle b = new Bundle();
        b.putSerializable("theFile", audio);
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
    }

    private boolean checkPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            makeSnackbar(getString(R.string.permission_explain));
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    private void makeSnackbar(String text){
        Snackbar snackbar1 = Snackbar.make(mListView, text, Snackbar.LENGTH_LONG);
        snackbar1.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final File toHandle = (File) mAdapter.getItem(position);

        if (toHandle.isDirectory() && toHandle.exists()) {
            try {
                folderOpened(toHandle);
            } catch (Exception e){
                makeSnackbar(getString(R.string.list_files_cannot_browse_no_permission_given));
                mAdapter.undoClearData();
            }
        } else {
            audioOpened(toHandle);
        }
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

            if(mAdapter == null) {
                onBackPressed();
                finish();
                return true;
            }

            boolean resultUndo = mAdapter.undoClearData();

            if(resultUndo){
                mAdapter.notifyDataSetChanged();
            } else {
                onBackPressed();
                finish();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isAllowedFolder(File folder){
        if(folder.isDirectory() && folder.exists())
            return true;

        return false;
    }

    private boolean isAllowedAudio(File audio){
        if(getFileExtension(audio).contains("mp3"))
            return true;

        if(getFileExtension(audio).contains("mp4"))
            return true;

        if(getFileExtension(audio).contains("opus"))
            return true;

        return false;
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }
}