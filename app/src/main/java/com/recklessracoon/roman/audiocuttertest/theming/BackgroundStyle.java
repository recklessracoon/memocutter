package com.recklessracoon.roman.audiocuttertest.theming;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import com.recklessracoon.roman.audiocuttertest.R;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Roman on 13.09.2017.
 */

public class BackgroundStyle {

    public static int STANDARD_BACKGROUND = R.color.primaryBackground;
    public static int STANDARD_WHITE = R.color.colorWhite;
    public static int STANDARD_GLITTER_GOLD = R.drawable.glitter_gold;
    public static int STANDARD_GLITTER_PINK = R.drawable.glitter_pink;
    public static int STANDARD_GLITTER_BLUE = R.drawable.glitter_blue;

    public static int CUSTOM_STYLE = 696969;
    private static Drawable CUSTOM_DRAWABLE;
    private static Uri CUSTOM_URI;

    public static Drawable getBackgroundDrawable(Context context){
        int currentBG = getCurrentBackground(context);

        if(currentBG == CUSTOM_STYLE){
            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
            Uri uri = Uri.parse(sh.getString("BGCUSTOM", ""));
            return BackgroundStyle.imageFromUri(context, uri);
        }

        return ContextCompat.getDrawable(context, currentBG);
    }

    private static Drawable imageFromUri(Context context, Uri yourUri) {
        if(yourUri.equals(CUSTOM_URI) && CUSTOM_DRAWABLE != null) {
            return CUSTOM_DRAWABLE;
        }

        Drawable yourDrawable;

        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(yourUri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            yourDrawable = new BitmapDrawable(context.getResources(), bitmap); // workaround

            CUSTOM_URI = yourUri;
            CUSTOM_DRAWABLE = yourDrawable;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            yourDrawable = ContextCompat.getDrawable(context, STANDARD_BACKGROUND);
        }

        return yourDrawable;
    }

    public static void setBackground(Context context, int id){
        setCurrentBackground(context, id);
    }

    public static void setBackground(Context context, Uri uri){
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sh.edit();

        edit.putString("BGCUSTOM", uri.toString());
        edit.apply();

        setCurrentBackground(context, CUSTOM_STYLE);
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
