package com.example.roman.audiocuttertest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
                //.addPlayStore("com.ideashower.readitlater.pro")
                .addGroup(getString(R.string.about_page_connect)) // my contact details
                //.addYoutube("UCdPQtdWIsg7_pi4mrRu46vA")
                //.addGitHub(getString(R.string.about_page_connect_git_name))
                .addEmail(getString(R.string.about_page_email))
                .addItem(getGithubContact())
                .addItem(getLicenseGPL3())
                .addGroup(getString(R.string.about_page_credits)) // credits
                .addItem(getIconCredit())
                .addItem(getBackgroundCredit())
                .addGroup(getString(R.string.about_page_credits_libraries)) // used libraries
                .addItem(getFFMPEGCredit())
                .addItem(getLicenseGPL3())
                .addItem(getAppIntroCredit())
                .addItem(getLicenseApache())
                .addItem(getAboutPageCredit())
                .addItem(getLicenseApache())
                .addItem(getRangeBarCredit())
                .addItem(getLicenseApache())
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

    private Element getIconCredit(){
        Element credit = new Element();
        credit.setTitle(getString(R.string.about_page_icons));
        //credit.setIconDrawable(R.drawable.about_icon_github);
        credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebIntent(getString(R.string.about_page_icons_link));
            }
        });

        return credit;
    }

    private Element getGithubContact(){
        Element credit = new Element();
        credit.setTitle(getString(R.string.about_page_git_project));
        credit.setIconDrawable(R.drawable.about_icon_github);
        credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebIntent(getString(R.string.about_page_git_project_link));
            }
        });

        return credit;
    }

    private Element getLicenseApache(){
        Element credit = new Element();
        credit.setTitle(getString(R.string.about_page_license));
        credit.setIconDrawable(R.drawable.ic_format_list_numbered_black_24dp);
        credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLicenseIntent(getApacheLicenseFromText());
            }
        });

        return credit;
    }

    private Element getLicenseGPL3(){
        Element credit = new Element();
        credit.setTitle(getString(R.string.about_page_license));
        credit.setIconDrawable(R.drawable.ic_format_list_numbered_black_24dp);
        credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLicenseIntent(getGPL3LicenseFromText());
            }
        });

        return credit;
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

    private void startLicenseIntent(String license){
        Intent intent = new Intent(this, LicenseActivity.class);
        intent.putExtra("LICENSE", license);
        startActivity(intent);
    }

    private String getApacheLicenseFromText(){
        return getTextFromRaw(R.raw.licenseapache);
    }

    private String getGPL3LicenseFromText(){
        return getTextFromRaw(R.raw.licensegpl);
    }

    private String getTextFromRaw(int resId){
        BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(resId)));
        String line;
        StringBuilder text = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text.toString();
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
