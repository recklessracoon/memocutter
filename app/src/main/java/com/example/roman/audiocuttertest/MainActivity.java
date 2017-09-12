package com.example.roman.audiocuttertest;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.roman.audiocuttertest.helpers.AudioFilesPreloader;
import com.example.roman.audiocuttertest.intro.IntroActivity;
import com.example.roman.audiocuttertest.io.AudioLoader;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private static final int FILE_SELECT_CODE = 0;

    private Button insert, last, another, merge;
    private CheckBox checkBox;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        initButtons();
        //initCheckbox();

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        new AudioFilesPreloader().withContext(this).withDirectory(EditActivity.getTemporarySavedFile(this).getParentFile()).apply();
        handleIntro();
    }

    /*
    private void initCheckbox(){
        checkBox = (CheckBox) findViewById(R.id.first_checkbox);
        final Activity activity = this;

        checkBox.setChecked(isIntroActivated(activity));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setIntroActivated(activity, isChecked);
            }
        });
    }
    */

    private void initButtons(){
        insert = (Button) findViewById(R.id.first_button);
        last = (Button) findViewById(R.id.first_button_last);
        another = (Button) findViewById(R.id.first_button_other_app);
        merge = (Button) findViewById(R.id.first_button_merge);

        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileBrowserIntent();
            }
        });
        another.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otherAppIntent();
            }
        });
        merge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mergeBrowserIntent();
            }
        });
    }

    private void handleIntro(){
        if(isIntroActivated(this)) {
            Intent intent = new Intent(this, IntroActivity.class);
            startActivity(intent);
        }
    }

    //opens filebrowser-like activity FileBrowserActivity
    private void fileBrowserIntent(){
        Intent intent = new Intent(this, FileBrowserActivity.class);
        startActivity(intent);
    }

    private void mergeBrowserIntent(){
        Intent intent = new Intent(this, FileBrowserActivity.class);
        intent.putExtra("MERGE",true);
        startActivity(intent);
    }

    private void otherAppIntent(){
        /*
        Intent intent = Intent.createChooser(new Intent(Intent.), getString(R.string.first_button_other_app));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        */
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void showFileChooser() {

        Intent intent;

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
                String path;

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

    public static boolean isIntroActivated(Activity activity) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        return sh.getBoolean("INTRO", true);
    }

    public static void setIntroActivated(Activity activity, boolean value) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        SharedPreferences.Editor edit = sh.edit();

        edit.putBoolean("INTRO", value);
        edit.apply();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings_button) {

            PopupMenu popup = new PopupMenu(this, this.findViewById(R.id.settings_button));
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.settings_menu_items, popup.getMenu());
            popup.setOnMenuItemClickListener(this);
            popup.getMenu().getItem(0).setChecked(isIntroActivated(this));
            popup.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.first_checkbox:
                boolean newState = !isIntroActivated(this);
                setIntroActivated(this, newState);
                item.setChecked(newState);
                return true;
            case R.id.do_stuff1:
                makeToast("stuff1");
                return true;
            case R.id.do_stuff2:
                makeToast("stuff2");
                return true;
            default:
                return false;
        }
    }

}
