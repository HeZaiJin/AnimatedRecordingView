package com.haozhang.lib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class BaseSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private final Object surfaceLock = new Object();
    private DrawThread drawThread;
    public float mVolume;

    public BaseSurfaceView(Context context) {
        this(context, null);
    }

    public BaseSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
    }

    public void setVolume(float volume) {
        volume = volume * volume;
        if (mVolume > volume) {
            float diff = mVolume - volume;
            float v = diff / 8f;
            this.mVolume -= v;
        } else {
            this.mVolume = volume;
        }
    }

    private class DrawThread extends Thread {

        private static final long SLEEP_TIME = 16;

        private SurfaceHolder surfaceHolder;
        private boolean running = true;

        public DrawThread(SurfaceHolder holder) {
            super("RenderThread");
            surfaceHolder = holder;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (surfaceLock) {
                    if (!running) {
                        return;
                    }
                    Canvas canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        render(canvas, mVolume);  //这里做真正绘制的事情
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setRun(boolean isRun) {
            this.running = isRun;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(holder);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized (surfaceLock) {
            drawThread.setRun(false);
        }
    }

    private void render(Canvas canvas, float volume) {
        onRender(canvas, volume);
    }

    protected void onRender(Canvas canvas, float volume) {
    }
}
