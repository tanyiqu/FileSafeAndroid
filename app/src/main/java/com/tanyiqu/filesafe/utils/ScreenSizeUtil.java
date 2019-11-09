package com.tanyiqu.filesafe.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenSizeUtil {

    /**
     * dp to px
     * @param context Context
     * @param dp dp
     * @return px
     */
    public static float dp_2_px(Context context, int dp) {
        //获取屏蔽的像素密度系数
        float density = context.getResources().getDisplayMetrics().density;
        return dp * density;
    }

    /**
     * px to dp
     * @param context Context
     * @param px px
     * @return dp
     */
    public static float px_2_dp(Context context, int px) {
        //获取屏蔽的像素密度系数
        float density = context.getResources().getDisplayMetrics().density;
        return px / density;
    }

    /**
     * 获取屏幕宽度
     * @param context Context
     * @return 宽度
     */
    public static int getScreenWidth(Context context){
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (manager != null) {
            manager.getDefaultDisplay().getMetrics(dm);
        }
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     * @param context Context
     * @return 高度
     */
    public static int getScreenHeight(Context context){
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (manager != null) {
            manager.getDefaultDisplay().getMetrics(dm);
        }
        return dm.heightPixels;
    }

}
