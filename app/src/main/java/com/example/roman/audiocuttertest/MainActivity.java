package com.example.roman.audiocuttertest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;
    private Button insert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        insert = (Button) findViewById(R.id.first_button);
        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

    }

    /*
    public void onStart(){
        super.onStart();

        makeToast("height: "+insert.getLayoutParams().height);
        makeToast("width: "+insert.getLayoutParams().width);

        insert.getLayoutParams().height = (insert.getLayoutParams().width);
    }
    */

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.first_select_file)),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
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
                if(data == null){
                    return;
                }

                Uri uri = data.getData();
                // Get the path
                String path = null;
                try {
                    path = getPath(this, uri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                Log.d("CHOOSE", "File Path: " + path);
                File file = new File(path);

                if(file.exists()){
                    Intent intent = new Intent(this, EditActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable("theFile", file);
                    intent.putExtras(b); //Put your id to your next Intent
                    startActivity(intent);
                } else {
                    makeToast("");
                }

                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public void makeToast(String text){
        Toast.makeText(getApplicationContext(),
                text , Toast.LENGTH_LONG)
                .show();
    }
}
