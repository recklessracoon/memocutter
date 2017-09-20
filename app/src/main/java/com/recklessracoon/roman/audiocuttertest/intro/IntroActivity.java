package com.recklessracoon.roman.audiocuttertest.intro;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.recklessracoon.roman.audiocuttertest.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by Roman on 07.09.2017.
 */

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide1_title), getString(R.string.intro_slide1_text), R.mipmap.ic_launcher_sci, ContextCompat.getColor(this, R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide2_title), getString(R.string.intro_slide2_text), R.drawable.share_slide, ContextCompat.getColor(this, R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide3_title), getString(R.string.intro_slide3_text), R.drawable.cut_slide, ContextCompat.getColor(this, R.color.colorPrimaryDark)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide4_title), getString(R.string.intro_slide4_text), R.drawable.directory_slide, ContextCompat.getColor(this, R.color.colorPrimaryDark)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide5_title), getString(R.string.intro_slide5_text), R.drawable.combine_slide, ContextCompat.getColor(this, R.color.colorPrimaryDark)));

        showSkipButton(true);
        setProgressButtonEnabled(true);

        setFadeAnimation();

        askForPermissions(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);

    }

    public void onDonePressed(Fragment currentFragment) {
        finish();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }
}
