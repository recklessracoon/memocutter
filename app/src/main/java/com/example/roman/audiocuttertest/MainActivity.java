package com.example.roman.audiocuttertest;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.roman.audiocuttertest.io.AudioLoader;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;
    private Button insert, last;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        insert = (Button) findViewById(R.id.first_button);
        last = (Button) findViewById(R.id.first_button_last);

        File lastConverted = new File(MainActivity.getLastFile(this));
        if (!lastConverted.exists()) {
            last.setVisibility(View.GONE);
        }

        final Activity activity = this;

        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                File lastConverted = new File(MainActivity.getLastFile(activity));
                if(lastConverted.exists()){
                    intentWithFile(lastConverted);
                }
                */

                fileBrowserIntent();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void fileBrowserIntent(){
        Intent intent = new Intent(this, FileBrowserActivity.class);
        startActivity(intent);
    }

    private void showFileChooser() {

        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType("*/*");
        //intent.addCategory(Intent.CATEGORY_OPENABLE);

        //Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        //intent.putExtra("CONTENT_TYPE", "*/*");
        //intent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent intent = null;

        if (Build.MODEL.contains("amsung") || Build.MANUFACTURER.contains("amsung")) {

            intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
            intent.putExtra("CONTENT_TYPE", "*/*");
            intent.addCategory(Intent.CATEGORY_DEFAULT);

        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
        }

        try {
            startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.first_select_file)),
                    FILE_SELECT_CODE);
        } catch (ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, getString(R.string.first_no_file_manager),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                //if (resultCode == RESULT_OK) {
                // Get the Uri of the selected file
                if (data == null) {
                    return;
                }

                Uri uri = data.getData();

                //intentWithUri(uri);

                // Get the path
                String path = null;

                path = AudioLoader.getRealPathFromURI(this, uri);

                Log.d("CHOOSE", "File Path: " + path);
                File file = new File(path);

                if (file.exists()) {
                    intentWithFile(file);
                } else {
                    makeToast("");
                }

                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void intentWithFile(File file) {
        Intent intent = new Intent(this, EditActivity.class);
        Bundle b = new Bundle();
        b.putSerializable("theFile", file);
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
    }

    public void makeToast(String text) {
        Toast.makeText(getApplicationContext(),
                text, Toast.LENGTH_LONG)
                .show();
    }

    public static void saveLastFile(Activity activity, String lastFile) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        SharedPreferences.Editor edit = sh.edit();

        edit.putString(activity.getString(R.string.other_prefs_last), lastFile);
        Log.d("SAVE", lastFile);
        edit.apply();
    }

    public static String getLastFile(Activity activity) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String out = sh.getString(activity.getString(R.string.other_prefs_last), "");
        Log.d("LOAD", out);
        return out;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
