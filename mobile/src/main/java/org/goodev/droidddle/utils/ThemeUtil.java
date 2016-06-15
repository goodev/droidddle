package org.goodev.droidddle.utils;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import org.goodev.droidddle.App;
import org.goodev.droidddle.R;
import org.goodev.droidddle.ThemeActivity;

public final class ThemeUtil {

    private static final int DEFAULT_COLOR = Color.parseColor("#C2185B");

    //@f:off
    public static int[] THEMES = new int[]{R.style.AppTheme_Default,
            R.style.AppTheme_Dark,
            R.style.AppTheme_Purple,
            R.style.AppTheme_Amber,
            R.style.AppTheme_Blue,
            R.style.AppTheme_Green,
            R.style.AppTheme_Teal,
    };
    //@f:on
    //@f:off
    public static int[] THEMES_TOOLBAR = new int[]{R.style.AppTheme_Default_Toolbar,
            R.style.AppTheme_Dark_Toolbar,
            R.style.AppTheme_Purple_Toolbar,
            R.style.AppTheme_Amber_Toolbar,
            R.style.AppTheme_Blue_Toolbar,
            R.style.AppTheme_Green_Toolbar,
            R.style.AppTheme_Teal_Toolbar,
    };
    //@f:on

    private ThemeUtil() {
    }

    public static int getSelectTheme() {
        int theme = Pref.getTheme(App.getContext());
        return (theme >= 0 && theme < THEMES.length) ? theme : Pref.DEFAULT_THEME;
    }

    public static boolean isTransTheme() {
        int theme = getSelectTheme();
        return theme > 1 && theme < THEMES.length - 1;
    }

    public static void setTheme(ThemeActivity activity) {
        int theme = getSelectTheme();
        activity.setMyTheme(theme);
        //        activity.setTheme(THEMES[theme]);
    }

    public static void reloadTheme(ThemeActivity activity) {
        int theme = getSelectTheme();
        if (theme != activity.getMyTheme())
            activity.recreate();
    }

    public static void reloadTheme(ThemeActivity activity, int theme) {
        if (theme != activity.getMyTheme())
            activity.recreate();
    }

    public static int getThemeColor(Context context, int id) {
        Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[]{id});
        int result = a.getColor(0, DEFAULT_COLOR);
        a.recycle();
        return result;
    }

    public static boolean getThemeDark(Context context, int id) {
        Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[]{id});
        boolean result = a.getBoolean(0, false);
        a.recycle();
        return result;
    }

    //    public static Drawable getThemeButtonBackground(Context context) {
    //        Theme theme = context.getTheme();
    //        TypedArray a = theme.obtainStyledAttributes(new int[] {
    //            R.attr.button_background
    //        });
    //        Drawable result = a.getDrawable(0);
    //        a.recycle();
    //        return result;
    //    }

    public static Drawable getThemeDrawable(Context context, int res) {
        Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[]{res});
        Drawable result = a.getDrawable(0);
        a.recycle();
        return result;
    }

    // public static int getActivatiedRes(Context context) {
    // int theme = getSelectTheme();
    // switch (theme) {
    // case 0:
    // return R.drawable.fonter_activated_background_holo_light;
    // case 2:
    // return R.drawable.purple_activated_background_holo_light;
    // case 3:
    // return R.drawable.amber_activated_background_holo_light;
    // case 4:
    // return R.drawable.blue_activated_background_holo_light;
    //
    // default:
    // break;
    // }
    // return R.drawable.fonter_activated_background_holo_light;
    //
    // Theme theme = context.getApplicationContext().getTheme();
    // TypedArray a = theme.obtainStyledAttributes(new int[]
    // {R.attr.fonter_activated_indicator});
    //
    // int result = a.getResourceId(0, 0);
    // a.recycle();
    // System.out.println("r........... "+ result
    // +"   "+R.drawable.fonter_activated_background_holo_light);
    // return result;
    // }
}
