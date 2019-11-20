package com.slamtec.slamware.uicommander.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import java.lang.ref.WeakReference;

import static java.lang.Thread.sleep;

public class LongClickButton extends Button {

    private LongClickRepeatListener mRepeatListener;

    private long mIntervalTime;

    private boolean mNeedHandle;

    private MyHandler mHandler;

    public LongClickButton(Context context) {
        super(context);
        init();
    }

    public LongClickButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LongClickButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mNeedHandle = false;
        mHandler = new MyHandler(this);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new Thread(new LongClickThread()).start();
                return true;
            }
        });
    }

    private class LongClickThread implements Runnable {

        @Override
        public void run() {
            while (LongClickButton.this.isPressed()) {
                mHandler.sendEmptyMessage(1);
                mNeedHandle = true;

                try {
                    sleep(mIntervalTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<LongClickButton> ref;

        MyHandler(LongClickButton button) {
            ref = new WeakReference<>(button);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            LongClickButton button = ref.get();
            if (button != null && button.mRepeatListener != null) {
                button.mRepeatListener.repeatAction(button);
            }
        }
    }

    public void setLongClickRepeatListener(LongClickRepeatListener listener, long intervalTime) {
        this.mRepeatListener = listener;
        this.mIntervalTime = intervalTime;
    }

    public void setLongClickRepeatListener(LongClickRepeatListener listener) {
        setLongClickRepeatListener(listener, 100);
    }

    public interface LongClickRepeatListener {
        void repeatAction(View view);
    }
}