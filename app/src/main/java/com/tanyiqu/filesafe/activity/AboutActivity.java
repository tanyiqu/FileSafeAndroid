package com.tanyiqu.filesafe.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.utils.Util;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        init();
    }

    private void init() {
        initToolBar();
        randomColor();
    }

    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar_about);
        toolbar.setTitle("关于");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void randomColor() {
        TextView[] tvs = new TextView[]{findViewById(R.id.tv_aboutApp_content), findViewById(R.id.tv_aboutAuthor_content), findViewById(R.id.tv_thanks_content)};
        for (TextView tv : tvs) {
            int r = Util.RandomInt(1, 3);
            switch (r) {
                case 1:
                    tv.setBackgroundResource(R.drawable.shape_bg_about_text_01);
                    break;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_page_jump_3,R.anim.anim_page_jump_4);
    }
}
