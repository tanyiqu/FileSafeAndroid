package com.tanyiqu.filesafe.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.utils.TypefaceUtil;
import com.tanyiqu.filesafe.utils.Util;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        init();
        TypefaceUtil.replaceFont(this,"fonts/MiLan.ttf");
    }

    /**
     * 初始化
     */
    private void init() {
        initToolBar();
        randomColor();
        initText();
    }

    /**
     * 初始化超链接文本
     */
    private void initText() {
        TextView[] tvs = new TextView[]{findViewById(R.id.tv_aboutApp_content), findViewById(R.id.tv_aboutAuthor_content), findViewById(R.id.tv_thanks_content)};
        tvs[0].setMovementMethod(LinkMovementMethod.getInstance());
        tvs[1].setMovementMethod(LinkMovementMethod.getInstance());
        tvs[2].setMovementMethod(LinkMovementMethod.getInstance());
        SpannableString s1 = getS1();
        SpannableString s2 = getS2();
        SpannableString s3 = getS3();
        tvs[0].setText(s1);
        tvs[1].setText(s2);
        tvs[2].setText(s3);
    }

    /**
     * 超链接文本1
     * @return 超链接文本1
     */
    private SpannableString getS1() {
        String text = getString(R.string.about_app_content);
        final String url = "https://github.com/Tanyiqu/FileSafe";
        SpannableString spanStr = new SpannableString(text);
        int start = text.indexOf(url);
        int end = start + url.length();
        //设置下划线
        spanStr.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置点击
        spanStr.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        },start,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置前景色
        spanStr.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanStr;
    }

    /**
     * 超链接文本2
     * @return 超链接文本2
     */
    private SpannableString getS2() {
        String text = getString(R.string.about_author_content);
        final String url1 = "https://tanyiqu.github.io";
        final String url2 = "https://github.com/Tanyiqu";
        SpannableString spanStr = new SpannableString(text);
        int start1 = text.indexOf(url1);
        int start2 = text.indexOf(url2);
        int end1 = start1 + url1.length();
        int end2 = start2 + url2.length();
        spanStr.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url1));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        },start1,end1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url2));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        },start2,end2,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new ForegroundColorSpan(Color.BLUE), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new ForegroundColorSpan(Color.BLUE), start2, end2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanStr;
    }

    /**
     * 超链接文本3
     * @return 超链接文本3
     */
    private SpannableString getS3() {
        String text = getString(R.string.thanks_content);
        final String[] url = new String[]{
                "https://www.baidu.com",
                "https://www.csdn.net",
                "https://www.runoob.com/w3cnote/android-tutorial-intro.html",
                "http://guolin.tech",
                "https://www.iconfont.cn",
                "https://github.com"
        };
        SpannableString spanStr = new SpannableString(text);
        for(int i=0;i<url.length;i++){
            int start = text.indexOf(url[i]);
            int end = start + url[i].length();
            final int ii = i;
            spanStr.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url[ii]));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            },start,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanStr.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spanStr;
    }

    /**
     * 初始化Toolbar
     */
    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar_about);
        toolbar.setTitle("关于");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    /**
     * 生成随机颜色的背景
     */
    private void randomColor() {
        TextView[] tvs = new TextView[]{findViewById(R.id.tv_aboutApp_content), findViewById(R.id.tv_aboutAuthor_content), findViewById(R.id.tv_thanks_content)};
        for (TextView tv : tvs) {
            int r = Util.RandomInt(1, 3);
            switch (r) {
                case 2:
                    tv.setBackgroundResource(R.drawable.shape_bg_about_text_02);
                    break;
                case 3:
                    tv.setBackgroundResource(R.drawable.shape_bg_about_text_03);
                    break;
                default:
                    tv.setBackgroundResource(R.drawable.shape_bg_about_text_01);
                    break;
            }
        }
    }

    /**
     * 重写返回事件
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_page_jump_3,R.anim.anim_page_jump_4);
    }

}
