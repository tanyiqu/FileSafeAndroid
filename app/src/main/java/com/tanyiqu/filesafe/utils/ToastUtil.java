package com.tanyiqu.filesafe.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tanyiqu.filesafe.R;

public class ToastUtil {

    /**
     * 自定义Toast
     * @param context Context
     * @param text 文本
     */
    public static void myToast(Context context, String text){
        View toastView = LayoutInflater.from(context).inflate(R.layout.layout_toast, null);
        LinearLayout relativeLayout = (LinearLayout)toastView.findViewById(R.id.toast_linear);
        //动态设置toast控件的宽高度，宽高分别是130dp
        //这里用了一个将dp转换为px的工具类Util
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) ScreenSizeUtil.dp_2_px(context, 130), (int)ScreenSizeUtil.dp_2_px(context, 130));
        relativeLayout.setLayoutParams(layoutParams);
        TextView textView = (TextView)toastView.findViewById(R.id.tv_toast_clear);
        textView.setText(text);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(toastView);
        toast.show();
    }

    /**
     * 错误Toast
     * @param context Context
     * @param text 文本
     */
    public static void errorToast(Context context, String text){
        View toastView = LayoutInflater.from(context).inflate(R.layout.layout_toast_error, null);
        LinearLayout relativeLayout = (LinearLayout)toastView.findViewById(R.id.toast_linear);
        //动态设置toast控件的宽高度，宽高分别是130dp
        //这里用了一个将dp转换为px的工具类Util
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) ScreenSizeUtil.dp_2_px(context, 130), (int)ScreenSizeUtil.dp_2_px(context, 130));
        relativeLayout.setLayoutParams(layoutParams);
        TextView textView = (TextView)toastView.findViewById(R.id.tv_toast_clear);
        textView.setText(text);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(toastView);
        toast.show();
    }

}
