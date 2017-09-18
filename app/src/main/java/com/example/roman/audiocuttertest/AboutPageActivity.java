package com.example.roman.audiocuttertest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

/**
 * Created by Roman on 18.09.2017.
 */

public class AboutPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription(getString(R.string.about_page_description))
                .setImage(R.mipmap.ic_launcher_sci)
                .addItem(getVersionElement())
                //.addWebsite("http://medyo.github.io/")
                //.addFacebook("the.medy")
                //.addTwitter("medyo80")
                //.addYoutube("UCdPQtdWIsg7_pi4mrRu46vA")
                //.addPlayStore("com.ideashower.readitlater.pro")
                //.addEmail("elmehdi.sakout@gmail.com")
                .addGroup(getString(R.string.about_page_connect)) // my contact details
                .addGitHub(getString(R.string.about_page_connect_git_name))
                .addGroup(getString(R.string.about_page_credits)) // credits
                .addItem(getBackgroundCredit())
                .addGroup(getString(R.string.about_page_credits_libraries)) // used libraries
                .addItem(getFFMPEGCredit())
                .addItem(getAppIntroCredit())
                .addItem(getAboutPageCredit())
                .addItem(getRangeBarCredit())
                //.addItem(getCopyRightsElement())
                .create();

        setContentView(aboutPage);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        //final String copyrights = String.format(getString("copyrightsText"), Calendar.getInstance().get(Calendar.YEAR));
        final String copyrights = "copyrightsText @2017";
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setIconDrawable(R.drawable.about_icon_copy_right);
        copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
        copyRightsElement.setIconNightTint(android.R.color.white);
        copyRightsElement.setGravity(Gravity.CENTER);
        copyRightsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AboutPageActivity.this, copyrights, Toast.LENGTH_SHORT).show();
            }
        });
        return copyRightsElement;
    }

    private Element getVersionElement(){
        Element versionElement = new Element();
        versionElement.setTitle(getString(R.string.about_page_version));

        return versionElement;
    }

    private Element getBackgroundCredit(){
        Element credit = new Element();
        credit.setTitle(getString(R.string.about_page_bg));
        //credit.setIconDrawable(R.drawable.about_icon_github);
        credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebIntent(getString(R.string.about_page_bg_link));
            }
        });

        return credit;
    }

    private Element getFFMPEGCredit(){
        Element credit = new Element();
        credit.setTitle(getString(R.string.about_page_lib_ffmpeg));
        credit.setIconDrawable(R.drawable.about_icon_github);
        credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebIntent(getString(R.string.about_page_lib_ffmpeg_link));
            }
        });

        return credit;
    }

    private Element getAppIntroCredit(){
        Element credit = new Element();
        credit.setTitle(getString(R.string.about_page_lib_appintro));
        credit.setIconDrawable(R.drawable.about_icon_github);
        credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebIntent(getString(R.string.about_page_lib_appintro_link));
            }
        });

        return credit;
    }

    private Element getAboutPageCredit(){
        Element credit = new Element();
        credit.setTitle(getString(R.string.about_page_lib_aboutpage));
        credit.setIconDrawable(R.drawable.about_icon_github);
        credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebIntent(getString(R.string.about_page_lib_aboutpage_link));
            }
        });

        return credit;
    }

    private Element getRangeBarCredit(){
        Element credit = new Element();
        credit.setTitle(getString(R.string.about_page_lib_rangebar));
        credit.setIconDrawable(R.drawable.about_icon_github);
        credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebIntent(getString(R.string.about_page_lib_rangebar_link));
            }
        });

        return credit;
    }

    private void startWebIntent(String url){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
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
