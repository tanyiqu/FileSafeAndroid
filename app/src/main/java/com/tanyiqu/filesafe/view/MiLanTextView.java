package com.tanyiqu.filesafe.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


public class MiLanTextView extends TextView {
    public MiLanTextView(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    @Override
    public void setTypeface(Typeface tf) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/MiLan.ttf");
        super.setTypeface(tf);
    }
}

