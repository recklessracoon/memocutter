package com.example.roman.audiocuttertest.theming;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import com.example.roman.audiocuttertest.R;

/**
 * Created by Roman on 13.09.2017.
 */

public class BackgroundStyle {

    public static int STANDARD_BACKGROUND = R.color.primaryBackground;
    public static int STANDARD_WHITE = R.color.colorWhite;
    public static int STANDARD_GLITTER_GOLD = R.drawable.glitter_gold;
    public static int STANDARD_GLITTER_PINK = R.drawable.glitter_pink;
    public static int STANDARD_GLITTER_BLUE = R.drawable.glitter_blue;

    public static Drawable getBackgroundDrawable(Context context){
        return ContextCompat.getDrawable(context, getCurrentBackground(context));
    }

    public static void setBackground(Context context, int id){
        setCurrentBackground(context, id);
    }

    private static int getCurrentBackground(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getInt("BGCOLOR", STANDARD_BACKGROUND);
    }

    private static void setCurrentBackground(Context context, int drawable) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sh.edit();

        edit.putInt("BGCOLOR", drawable);
        edit.apply();
    }


    public static int getCurrentMenuSelection(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getInt("BGCOLORSEL", 1);
    }

    public static void setCurrentMenuSelection(Context context, int number) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sh.edit();

        edit.putInt("BGCOLORSEL", number);
        edit.apply();
    }

}
