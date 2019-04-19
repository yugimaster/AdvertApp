package com.grandartisans.advert.view;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;

public class MarqueeTextView extends AppCompatTextView {

    /**
     * 是否停止滚动
     */
    private boolean mStopMarquee;

    /**
     * 文本内容
     */
    private String mText;

    /**
     * 当前滚动位置X
     */
    private float mCoordinateX = 800;

    /**
     * 当前滚动位置Y
     */
    private float mCoordinateY = 150;

    /**
     * 文本宽度
     */
    private float mTextWidth;

    /**
     * 滚动区域宽度
     */
    private int mScrollWidth = 800;

    /**
     * 滚动速度
     */
    private int mSpeed = 1;

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context) {
        super(context);
    }

    public void setCoordinateX(float coordinateX) {
        this.mCoordinateX = coordinateX;
    }

    public void setStopMarquee(boolean stopMarquee) {
        this.mStopMarquee = stopMarquee;
    }

    public boolean getStopMarquee() {
        return mStopMarquee;
    }

    public float getCoordinateX() {
        return mCoordinateX;
    }

    public void setCoordinateY(float coordinateY) {
        this.mCoordinateY = coordinateY;
    }

    public float getCoordinateY() {
        return mCoordinateY;
    }

    public void setScrollWidth(int scrollWidth) {
        this.mScrollWidth = scrollWidth;
    }

    public int getScrollWidth() {
        return mScrollWidth;
    }

    public void setSpeed(int speed) {
        this.mSpeed = speed;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public void setText(String text) {
        this.mText = text;
        mTextWidth = getPaint().measureText(mText);
        if (mHandler.hasMessages(0))
            mHandler.removeMessages(0);
        mHandler.sendEmptyMessageDelayed(0, 10);
    }

    @Override
    protected void onAttachedToWindow() {
        mStopMarquee = false;
        if (!isEmpty(mText))
            mHandler.sendEmptyMessageDelayed(0, 2000);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        mStopMarquee = true;
        if (mHandler.hasMessages(0))
            mHandler.removeMessages(0);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isEmpty(mText))
            canvas.drawText(mText, mCoordinateX, mCoordinateY, getPaint());
    }

    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (mCoordinateX < (-mTextWidth)) {
                        // 文字滚动完了，从滚动区域右边出来
                        mCoordinateX = mScrollWidth;
                        invalidate();
                        if (!mStopMarquee) {
                            sendEmptyMessageDelayed(0, 500);
                        }
                    } else {
                        mCoordinateX -= mSpeed;
                        invalidate();
                        if (!mStopMarquee) {
                            sendEmptyMessageDelayed(0, 30);
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
