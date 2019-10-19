package com.tanyiqu.filesafe.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenSizeUtil {

    public static float dp_2_px(Context context, int dp) {
        //获取屏蔽的像素密度系数
        float density = context.getResources().getDisplayMetrics().density;
        return dp * density;
    }

    /**
     * px to dp
     * @param context 上下文
     * @param px px
     * @return dp
     */
    public static float px_2_dp(Context context, int px) {
        //获取屏蔽的像素密度系数
        float density = context.getResources().getDisplayMetrics().density;
        return px / density;
    }

    public static int getScreenWidth(Context context){
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (manager != null) {
            manager.getDefaultDisplay().getMetrics(dm);
        }
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context){
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (manager != null) {
            manager.getDefaultDisplay().getMetrics(dm);
        }
        return dm.heightPixels;
    }

}
