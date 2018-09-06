package com.renyu.nimlibrary.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.facebook.drawee.span.SimpleDraweeSpanTextView;

public class AutoLinkNewSimpleDraweeSpanTextView extends SimpleDraweeSpanTextView {
    public AutoLinkNewSimpleDraweeSpanTextView(Context context) {
        super(context);
    }

    public AutoLinkNewSimpleDraweeSpanTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoLinkNewSimpleDraweeSpanTextView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    long time;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            time = System.currentTimeMillis();
        } else if (event.getAction() == MotionEvent.ACTION_UP)
            if (System.currentTimeMillis() - time > 500)
                return true;
        return super.onTouchEvent(event);
    }
}
