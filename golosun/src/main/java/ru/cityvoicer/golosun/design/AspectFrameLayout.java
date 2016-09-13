package ru.cityvoicer.golosun.design;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import ru.cityvoicer.golosun.R;

public class AspectFrameLayout extends FrameLayout {
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AspectFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyAttributes(context, attrs);
    }

    public AspectFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        applyAttributes(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        if (mMode == MODE_VARIABLE_HEIGHT) {
            h = (int)((float)w / mAspect);
        } else {
            w = (int)((float)h * mAspect);
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
    }

    private float mAspect = 1.0f;
    private int mMode = MODE_VARIABLE_HEIGHT;
    public static final int MODE_VARIABLE_HEIGHT = 0;
    public static final int MODE_VARIABLE_WIDTH = 1;

    private void applyAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AspectFrameLayout);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.AspectFrameLayout_aspect:
                    try {
                        mAspect = a.getFloat(attr, 1.0f);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.styleable.AspectFrameLayout_mode:
                    try {
                        mMode = a.getInt(attr, MODE_VARIABLE_HEIGHT);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public float getAspect() {
        return mAspect;
    }

    public void setAspect(float aspect) {
        mAspect = aspect;
        requestLayout();
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(int mode) {
        mMode = mode;
        requestLayout();
    }
}
