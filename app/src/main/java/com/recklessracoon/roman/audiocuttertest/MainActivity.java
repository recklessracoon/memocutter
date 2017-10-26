package com.recklessracoon.roman.audiocuttertest;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.recklessracoon.roman.audiocuttertest.helpers.AudioFilesPreloader;
import com.recklessracoon.roman.audiocuttertest.intro.IntroActivity;
import com.recklessracoon.roman.audiocuttertest.io.AudioLoader;
import com.recklessracoon.roman.audiocuttertest.theming.BackgroundStyle;

import java.io.File;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private static final int FILE_SELECT_CODE = 0;
    private static final int IMAGE_PICK_CODE = 1;

    private Button insert, last, another, merge;

    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.recklessracoon.roman.audiocuttertest.R.layout.activity_main_new);

        relativeLayout = (RelativeLayout) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.activity_main);
        handleNewStyle();

        initButtons();
        //initCheckbox();

        if(isPermissionGranted())
            new AudioFilesPreloader().withContext(this).withDirectory(EditActivity.getTemporarySavedFile(this).getParentFile()).apply();

        handleIntro();
    }

    private boolean isPermissionGranted() {
        int result = checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void handleNewStyle(){
        relativeLayout.setBackground(BackgroundStyle.getBackgroundDrawable(this));
    }

    private void initButtons(){
        insert = (Button) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.first_button);
        last = (Button) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.first_button_last);
        another = (Button) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.first_button_other_app);
        merge = (Button) findViewById(com.recklessracoon.roman.audiocuttertest.R.id.first_button_merge);

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
                    Intent.createChooser(intent, getString(com.recklessracoon.roman.audiocuttertest.R.string.first_select_file)),
                    FILE_SELECT_CODE);
        } catch (ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, getString(com.recklessracoon.roman.audiocuttertest.R.string.first_no_file_manager),
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

                path = AudioLoader.getRealPathFromURI(this, uri, data.getFlags());

                Log.d("CHOOSE", "File Path: " + path);
                File file = new File(path);

                if (file.exists()) {
                    intentWithFile(file);
                } else {
                    makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.edit_load_fail));
                }

                break;

            case IMAGE_PICK_CODE:
                if(data == null)
                    return;

                Uri img = data.getData();

                BackgroundStyle.setCurrentMenuSelection(this, 7);
                BackgroundStyle.setBackground(this, img);
                handleNewStyle();
                if(isPatterActivated(this))
                    makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.background_custom_txt));

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

    public void makeSnackbar(String text) {
        Snackbar snackbar1 = Snackbar.make(relativeLayout, text, Snackbar.LENGTH_LONG);
        snackbar1.show();
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

    public static boolean isPatterActivated(Activity activity) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        return sh.getBoolean("PATTER", true);
    }

    public static void setPatterActivated(Activity activity, boolean value) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        SharedPreferences.Editor edit = sh.edit();

        edit.putBoolean("PATTER", value);
        edit.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.recklessracoon.roman.audiocuttertest.R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == com.recklessracoon.roman.audiocuttertest.R.id.settings_button) {

            PopupMenu popup = new PopupMenu(this, this.findViewById(com.recklessracoon.roman.audiocuttertest.R.id.settings_button));
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(com.recklessracoon.roman.audiocuttertest.R.menu.settings_menu_items, popup.getMenu());
            popup.setOnMenuItemClickListener(this);
            popup.getMenu().getItem(0).setChecked(isIntroActivated(this));
            popup.getMenu().getItem(1).setChecked(isPatterActivated(this));

            int current = BackgroundStyle.getCurrentMenuSelection(this);
            popup.getMenu().getItem(current).setChecked(true);

            popup.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case com.recklessracoon.roman.audiocuttertest.R.id.first_checkbox:
                boolean newState = !isIntroActivated(this);
                setIntroActivated(this, newState);
                item.setChecked(newState);
                return true;

            case com.recklessracoon.roman.audiocuttertest.R.id.first_patterbox:
                boolean newState2 = !isPatterActivated(this);
                setPatterActivated(this, newState2);
                item.setChecked(newState2);
                return true;

            case com.recklessracoon.roman.audiocuttertest.R.id.background_standard:
                BackgroundStyle.setBackground(this, BackgroundStyle.STANDARD_BACKGROUND);
                BackgroundStyle.setCurrentMenuSelection(this, 2);
                handleNewStyle();
                if(isPatterActivated(this))
                    makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.background_standard_sel));
                return true;

            case com.recklessracoon.roman.audiocuttertest.R.id.background_white:
                BackgroundStyle.setBackground(this, BackgroundStyle.STANDARD_WHITE);
                BackgroundStyle.setCurrentMenuSelection(this, 3);
                handleNewStyle();
                if(isPatterActivated(this))
                    makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.background_white_sel));
                return true;

            case com.recklessracoon.roman.audiocuttertest.R.id.background_glitter_gold:
                BackgroundStyle.setBackground(this, BackgroundStyle.STANDARD_GLITTER_GOLD);
                BackgroundStyle.setCurrentMenuSelection(this, 4);
                handleNewStyle();
                if(isPatterActivated(this))
                    makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.background_glitter_gold_sel));
                return true;

            case com.recklessracoon.roman.audiocuttertest.R.id.background_glitter_pink:
                BackgroundStyle.setBackground(this, BackgroundStyle.STANDARD_GLITTER_PINK);
                BackgroundStyle.setCurrentMenuSelection(this, 5);
                handleNewStyle();
                if(isPatterActivated(this))
                    makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.background_glitter_pink_sel));
                return true;

            case com.recklessracoon.roman.audiocuttertest.R.id.background_glitter_blue:
                BackgroundStyle.setBackground(this, BackgroundStyle.STANDARD_GLITTER_BLUE);
                BackgroundStyle.setCurrentMenuSelection(this, 6);
                handleNewStyle();
                if(isPatterActivated(this))
                    makeSnackbar(getString(com.recklessracoon.roman.audiocuttertest.R.string.background_glitter_blue_sel));
                return true;

            case com.recklessracoon.roman.audiocuttertest.R.id.background_custom:
                sendImagePickIntent();
                return true;

            case com.recklessracoon.roman.audiocuttertest.R.id.about_page:
                handleAboutPageCall();
                return true;

            default:
                return false;
        }
    }

    private void sendImagePickIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(com.recklessracoon.roman.audiocuttertest.R.string.background_custom)), IMAGE_PICK_CODE);
    }

    private void handleAboutPageCall(){
        Intent intent = new Intent(this, AboutPageActivity.class);
        startActivity(intent);
    }

}
