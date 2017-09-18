package com.example.roman.audiocuttertest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Roman on 18.09.2017.
 */

public class LicenseActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setVerticalScrollBarEnabled(true);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        textView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);

        textView.setText(getIntent().getStringExtra("LICENSE"));
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);

        setContentView(textView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Handles pressing the system back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
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

}
