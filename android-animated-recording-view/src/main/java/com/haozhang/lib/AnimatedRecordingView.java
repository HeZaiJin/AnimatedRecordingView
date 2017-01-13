package com.haozhang.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Collections;

public class AnimatedRecordingView extends BaseSurfaceView {
    private static final String TAG = "SiriView";

    private static final int MAX_GAPS = 300;
    private static final float ANIM_SCALE_TIME = 400f;

    private static final int LOADING_WIDTH = 80;
    private static final int LOGO_LEFT_MARGIN = 60;

    private static int LOCATION_LEFT_RIGHT_OFFSET = 100;
    private static final int COLOR_RED = Color.parseColor("#f22b2b");

    private static final int COLOR_GREEN = Color.parseColor("#2bf3af");
    private static final int COLOR_BLUE = Color.parseColor("#2BAAF3");
    private static final int COLOR_LOADING = Color.parseColor("#007AFE");


    private static final int STATE_SLEEP = 0;
    private static final int STATE_WORK = 1;
    private static final int STATE_SEARCH = 2;

    static float mSamplingX[] = new float[MAX_GAPS + 1];
    static float mMapX[] = new float[MAX_GAPS + 1];

    Context mContext;
    ArrayList<SiriCursorView> mCursorList;
    float mWidth;
    float mHeight;
    float mWidthCenter;
    float mHeightCenter;
    static float mGap;
    int mState = STATE_SLEEP;
    Paint mPaint;

    Path mPathmBitmapLoadingTop;
    Path mPathmBitmapLoadingBottom;
    ArrayList<Integer> mShuffleList;
    Bitmap mBitmapLoading;
    Bitmap mBitmapLogo;
    long mCurentTime;
    Matrix mMatrix;
    RectF mBitmapLoadingRectf;
    RadialGradient mRadialGradient;
    RadialGradient mRadialGradientScale;
    RectF mRectFScale;
    boolean leftToRight = true;
    final float MAX_ANIM_STOP_INDEX = 20;
    final float MAX_ANIM_LOADING_INDEX = 45;
    float mStopAnimIndex = 0;
    boolean isStopLeft = false;

    float mLoadingAnimIndex = 0;
    int mControl = 0;

    public AnimatedRecordingView(Context context) {
        this(context, null);
    }

    public AnimatedRecordingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatedRecordingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    @Override
    protected void onRender(Canvas canvas, float volume) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        initCanvas(canvas);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (mState == STATE_WORK) {
            drawWave(canvas, volume);
        } else if (mState == STATE_SEARCH) {
            drawSearch(canvas);
        } else if (mState == STATE_SLEEP) {
            drawStopLogo(canvas);
        }
    }

    public boolean isWorking() {
        return mState != STATE_SLEEP;
    }

    void initCanvas(Canvas canvas) {
        if (mWidth == 0) {
            mWidth = canvas.getWidth();
            mWidthCenter = mWidth / 2.0f;
            mHeight = canvas.getHeight();
            mHeightCenter = mHeight / 2.0f;
            mGap = mWidth / MAX_GAPS;
            mRadialGradient = new RadialGradient(mWidthCenter, mHeightCenter, mWidthCenter, new int[]{Color.WHITE, COLOR_BLUE, COLOR_LOADING}, null, Shader.TileMode.CLAMP);
            mRadialGradientScale = new RadialGradient(mWidthCenter, mHeightCenter, mWidthCenter, new int[]{COLOR_LOADING, COLOR_BLUE, Color.WHITE}, null, Shader.TileMode.CLAMP);
            mBitmapLoadingRectf = new RectF(LOCATION_LEFT_RIGHT_OFFSET, mHeightCenter - 3f, mWidth - LOCATION_LEFT_RIGHT_OFFSET, mHeightCenter + 3f);
            float x;
            for (int i = 0; i <= MAX_GAPS; i++) {
                x = i * mGap;
                mSamplingX[i] = x;
                mMapX[i] = (x / mWidth) * 20f - 10f;
            }
            caculateLocations();
        }
    }

    void drawWave(Canvas canvas, float volume) {
        mPaint.setShader(mRadialGradient);
        canvas.drawOval(mBitmapLoadingRectf, mPaint);
        mPaint.setShader(null);
        canvas.clipRect(LOCATION_LEFT_RIGHT_OFFSET, 0, (int) (mWidth - LOCATION_LEFT_RIGHT_OFFSET), mHeight);
        for (int i = 0; i < 9; i++) {
            Integer index = mShuffleList.get(i);
            SiriCursorView view = mCursorList.get(index);
            view.drawWave(canvas, mPaint, volume, mHeightCenter, 0, mWidth);
        }
    }

    void drawStopLogo(Canvas canvas) {
        canvas.save();
        if (MAX_ANIM_STOP_INDEX >= mStopAnimIndex) {
            float i = getInterpolation(mStopAnimIndex / MAX_ANIM_STOP_INDEX);
            mPaint.setAlpha((int) (160 + 95 * i));
            mStopAnimIndex++;
        }
        float left = mWidthCenter - mBitmapLogo.getWidth() / 2;
        if (isStopLeft) {
            left = LOGO_LEFT_MARGIN;
        }
        canvas.drawBitmap(mBitmapLogo, left, mHeightCenter - mBitmapLogo.getHeight() / 2f, mPaint);
        canvas.restore();
    }

    void drawSearch(Canvas canvas) {
        if (0 == mCurentTime) {
            mCurentTime = System.currentTimeMillis();
        } else {
            long time_offset = System.currentTimeMillis() - mCurentTime;
            if (time_offset <= ANIM_SCALE_TIME) {
                mPaint.setShader(mRadialGradientScale);
                float input = time_offset / ANIM_SCALE_TIME;
                canvas.save();
                float interpolation = 1.00f - getInterpolation(input);
                float left = mBitmapLoadingRectf.left + (mWidthCenter - mBitmapLoadingRectf.left - LOADING_WIDTH / 2f) * (1 - interpolation);
                float right = mWidthCenter + LOADING_WIDTH / 2f + (mBitmapLoadingRectf.right - mWidthCenter - LOADING_WIDTH / 2f) * interpolation;
                float top = mBitmapLoadingRectf.top;
                float bottom = mBitmapLoadingRectf.bottom;
                mRectFScale.set(left, top, right, bottom);
                canvas.translate(-(mWidthCenter - LOCATION_LEFT_RIGHT_OFFSET) * (1.00f - interpolation), 0);
                canvas.drawOval(mRectFScale, mPaint);
                canvas.restore();
            } else {
                if (mLoadingAnimIndex < MAX_ANIM_LOADING_INDEX) {

                } else if (mLoadingAnimIndex == MAX_ANIM_LOADING_INDEX) {
                    leftToRight = !leftToRight;
                    mLoadingAnimIndex = 0;
                }
                mLoadingAnimIndex++;

                float progress = mLoadingAnimIndex / MAX_ANIM_LOADING_INDEX;

                canvas.save();
                float interpolation = getInterpolation(progress);

                float dx = interpolation * (mWidth - 2 * LOCATION_LEFT_RIGHT_OFFSET);

                if (!leftToRight) {
                    canvas.rotate(180, mWidthCenter, mHeightCenter);
                }
                canvas.translate(dx, 0);
                if (interpolation > 0.5f) {
                    mPaint.setAlpha((int) (255 - 255 * (interpolation - 0.5) * 2));
                } else {
                    mPaint.setAlpha((int) (255 * interpolation * 2));
                }
                canvas.drawBitmap(mBitmapLoading, LOCATION_LEFT_RIGHT_OFFSET, mHeightCenter - mBitmapLoading.getHeight() / 2f, mPaint);
                canvas.restore();
            }
        }
    }

    void init(Context context) {
        mMatrix = new Matrix();
        mContext = context;
        mPaint = new Paint();
        mBitmapLoading = BitmapFactory.decodeResource(getResources(), R.drawable.loading);
        mBitmapLogo = BitmapFactory.decodeResource(getResources(), R.drawable.icon_logo);
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));

        mRectFScale = new RectF();
        mPathmBitmapLoadingTop = new Path();
        mPathmBitmapLoadingBottom = new Path();

        mCursorList = new ArrayList<>();
        SiriCursorView red_base = new SiriCursorView(COLOR_RED, SiriCursorView.TYPE_CENTER);
        mCursorList.add(red_base);
        SiriCursorView blue_base = new SiriCursorView(COLOR_BLUE, SiriCursorView.TYPE_CENTER);

        mCursorList.add(blue_base);
        SiriCursorView green_base = new SiriCursorView(COLOR_GREEN, SiriCursorView.TYPE_CENTER);
        mCursorList.add(green_base);

        SiriCursorView red_small = new SiriCursorView(COLOR_RED, SiriCursorView.TYPE_MIDDLE);
        mCursorList.add(red_small);
        SiriCursorView blue_small = new SiriCursorView(COLOR_BLUE, SiriCursorView.TYPE_MIDDLE);
        mCursorList.add(blue_small);
        SiriCursorView green_small = new SiriCursorView(COLOR_GREEN, SiriCursorView.TYPE_MIDDLE);
        mCursorList.add(green_small);

        SiriCursorView red_small_s = new SiriCursorView(COLOR_RED, SiriCursorView.TYPE_OUTER);
        mCursorList.add(red_small_s);
        SiriCursorView blue_small_s = new SiriCursorView(COLOR_BLUE, SiriCursorView.TYPE_OUTER);
        mCursorList.add(blue_small_s);
        SiriCursorView green_small_s = new SiriCursorView(COLOR_GREEN, SiriCursorView.TYPE_OUTER);
        mCursorList.add(green_small_s);
        mShuffleList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            mShuffleList.add(i);
        }
        Collections.shuffle(mShuffleList);
        // init locations
        reset();
    }

    void caculateLocations() {
        if (0 != mWidth) {
            for (int i = 0; i < 9; i++) {
                SiriCursorView view = mCursorList.get(i);
                view.randomLocationAndOffset();
            }
        }
    }

    void caculateDitherLocations() {
        if (0 != mWidth) {
            for (int i = 0; i < 9; i++) {
                SiriCursorView view = mCursorList.get(i);
                view.dither();
            }
        }
    }

    @Override
    public void setVolume(float volume) {
        super.setVolume(volume);
        if (mControl == 40) {
            Collections.shuffle(mShuffleList);
            mControl = 0;
        } else if (mControl < 80) {
            mControl++;
            if (mControl % 20 == 0) {
                caculateDitherLocations();
            }
        }
    }

    public void loading() {
        mLoadingAnimIndex = 0;
        leftToRight = true;
        mMatrix.reset();
        mCurentTime = System.currentTimeMillis();
        mState = STATE_SEARCH;
    }

    public void start() {
        mLoadingAnimIndex = 0;
        isStopLeft = false;
        leftToRight = true;
        mState = STATE_WORK;
        reset();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            SiriCursorView view = mCursorList.get(i);
            sb.append("type : [" + view.type + "] ,  location: [" + view.location + "] ,  offset: [" + view.offset + "]").append("\r\n");
        }
        return sb.toString();
    }

    public void stop() {
        isStopLeft = false;
        mStopAnimIndex = 0;
        mState = STATE_SLEEP;
    }

    public void stop(boolean isLeft) {
        isStopLeft = isLeft;
        mStopAnimIndex = 0;
        mState = STATE_SLEEP;
    }

    public void reset() {
        caculateLocations();
    }

    public static float getInterpolation(float input) {
        return (float) (Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
    }


    static class SiriCursorView {
        public static final int TYPE_CENTER = 1;
        public static final int TYPE_MIDDLE = 2;
        public static final int TYPE_OUTER = 3;
        public static final int CENTER_LOCATION_MAX = 240;
        public static final int CENTER_LOCATION_MIN = -120;
        public static final int MIDDLE_LOCATION_MAX = 360;
        public static final int MIDDLE_LOCATION_MIN = -180;
        public static final int OUTER_LOCATION_MAX = 440;
        public static final int OUTER_LOCATION_MIN = -220;


        public static final float CENTER_OFFSET_MAX = 0.95f;
        public static final float CENTER_OFFSET_MIN = 0.40f;
        public static final float MIDDLE_OFFSET_MAX = 0.6f;
        public static final float MIDDLE_OFFSET_MIN = 0.15f;
        public static final float OUTER_OFFSET_MAX = 0.30f;
        public static final float OUTER_OFFSET_MIN = 0.05f;

        Path pathTop;
        Path pathBottom;
        int color;

        float offset = 0.3f;
        int type;
        float location;

        public SiriCursorView(int center_color, int type) {
            pathTop = new Path();
            pathBottom = new Path();
            this.type = type;
            this.color = center_color;
        }

        public void dither() {
            float offset = this.offset;
            float location = this.location;
            switch (type) {
                case TYPE_CENTER:
                    float c_location = getRadomValue(60, -30);
                    if ((location + c_location > CENTER_LOCATION_MAX) || (location + c_location < CENTER_LOCATION_MIN)) {
                        location -= c_location;
                    } else {
                        location += c_location;
                    }

                    float c_offset = getRadomValue(0.3f, -0.15f);
                    if ((offset + c_offset > CENTER_OFFSET_MAX) || (offset + c_offset < CENTER_OFFSET_MIN)) {
                        offset -= c_offset;
                    } else {
                        offset += c_offset;
                    }


                    break;
                case TYPE_MIDDLE:
                    float m_location = getRadomValue(60, -30);
                    if ((location + m_location > MIDDLE_LOCATION_MAX) || (location + m_location < MIDDLE_LOCATION_MIN)) {
                        location -= m_location;
                    } else {
                        location += m_location;
                    }

                    float m_offset = getRadomValue(0.3f, -0.15f);
                    if ((offset + m_offset > MIDDLE_OFFSET_MAX) || (offset + m_offset < MIDDLE_OFFSET_MIN)) {
                        offset -= m_offset;
                    } else {
                        offset += m_offset;
                    }

                    break;
                case TYPE_OUTER:
                    float o_location = getRadomValue(60, -30);
                    if ((location + o_location > MIDDLE_LOCATION_MAX) || (location + o_location < MIDDLE_LOCATION_MIN)) {
                        location -= o_location;
                    } else {
                        location += o_location;
                    }

                    float o_offset = getRadomValue(0.3f, -0.15f);
                    if ((offset + o_offset > OUTER_OFFSET_MAX) || (offset + o_offset < OUTER_OFFSET_MIN)) {
                        offset -= o_offset;
                    } else {
                        offset += o_offset;
                    }
                    break;
            }
            setLocation(location);
            setOffset(offset);
        }

        public void randomLocationAndOffset() {
            float offset = 0f;
            float location = 0f;
            switch (type) {
                case TYPE_CENTER:
                    location = getRadomValue(CENTER_LOCATION_MAX, CENTER_LOCATION_MIN);
                    offset = getRadomValue(CENTER_OFFSET_MAX - CENTER_OFFSET_MIN, CENTER_OFFSET_MIN);
                    break;
                case TYPE_MIDDLE:
                    location = getRadomValue(MIDDLE_LOCATION_MAX, MIDDLE_LOCATION_MIN);
                    offset = getRadomValue(MIDDLE_OFFSET_MAX - MIDDLE_OFFSET_MIN, MIDDLE_OFFSET_MIN);
                    break;
                case TYPE_OUTER:
                    location = getRadomValue(OUTER_LOCATION_MAX, OUTER_LOCATION_MIN);
                    offset = getRadomValue(OUTER_OFFSET_MAX - OUTER_OFFSET_MIN, OUTER_OFFSET_MIN);
                    break;
            }
            setLocation(location);
            setOffset(offset);
        }

        public float getRadomValue(float max, float min) {
            return (float) (Math.random() * max + min);
        }

        public void setOffset(float offset) {
            if (offset > 1.0f) {
                offset = 1.0f;
            } else if (offset < 0.01f) {
                offset = 0.01f;
            }
            this.offset = offset;
        }

        public void setLocation(float location) {
            if (location > 220) {
                location = 200;
            } else if (location < -220) {
                location = -220;
            }
            this.location = location;
        }


        public void drawWave(Canvas canvas, Paint paint, float volume, float center_height, float start_x, float end_x) {
            if (mGap == 0) {
                return;
            }
            canvas.save();
            paint.setColor(color);

            pathTop.rewind();
            pathTop.moveTo(start_x, center_height);
            pathBottom.rewind();
            pathBottom.moveTo(start_x, center_height);
            canvas.translate(location, 0);
            float x;
            float y_top, y_bottom;
            // draw top path
            for (int i = 0; i <= MAX_GAPS; i++) {
                x = mSamplingX[i];
                y_top = center_height - (float) caculateValue(mMapX[i], volume * offset, true) * center_height / 2f;
                pathTop.lineTo(x, y_top);
                y_bottom = center_height - (float) caculateValue(mMapX[i], volume * offset, false) * center_height / 2f;
                pathBottom.lineTo(x, y_bottom);
            }

            pathTop.lineTo(end_x, center_height);
            pathBottom.lineTo(end_x, center_height);
            canvas.drawPath(pathTop, paint);
            canvas.drawPath(pathBottom, paint);
            canvas.restore();
        }

        double caculateValue(float mapX, float volume, boolean isTop) {
            float offset = (volume * 1.95f / 10000f) + 0.05f;
            offset = isTop ? offset : (-offset);
            return offset / Math.pow((1 + Math.pow(mapX, 2)), 2);
        }

    }
}
