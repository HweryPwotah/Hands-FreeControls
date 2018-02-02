package com.example.os10.hands_freecontrols;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * Class that handles Pointer View.
 */

public class PointerView extends View implements SharedPreferences.OnSharedPreferenceChangeListener {
    // Size of the long side of the pointer for normal size (in DIP)
    private static final float CURSOR_LONG_SIDE_DIP = 30;
    private final Context context;
    // bitmap of the (mouse) pointer
    private Bitmap mPointerBitmap;
    // cached paint box
    private final Paint mPaintBox;
    // the location where the pointer needs to be painted
    private PointF mPointerLocation;

    public static final String KEY_UI_ELEMENTS_SIZE = "ui_elements_size";
    public static final String KEY_GAMEPAD_TRANSPARENCY = "gamepad_transparency";
    private int mClickProgressPercent = 0;
    private boolean mSwipemodeEnabled;

    /**
     * Constructor for initializing Pointer
     *
     * @param c context
     */
    public PointerView(Context c) {
        super(c);
        context = c;
        mPaintBox = new Paint();
        setWillNotDraw(false);
        mPointerLocation = new PointF();
        Log.i("PointerView", "Trying to initialize pointer");
        PointerInitialization();

        setCenter();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equals(KEY_UI_ELEMENTS_SIZE) ||
                key.equals(KEY_GAMEPAD_TRANSPARENCY)) {
            PointerInitialization();
        }
    }

    private void PointerInitialization() {
        float size = 1;

        // re-scale pointer accordingly
        BitmapDrawable bd = (BitmapDrawable)
                ContextCompat.getDrawable(getContext(), R.drawable.pointer);
        Bitmap origBitmap = bd.getBitmap();
        origBitmap.setDensity(Bitmap.DENSITY_NONE);

        // desired long side in pixels of the pointer for this screen density
        float longSide = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                CURSOR_LONG_SIDE_DIP, getResources().getDisplayMetrics()) * size;
        float scaling = longSide / (float) bd.getIntrinsicHeight();
        float shortSide = scaling * bd.getIntrinsicWidth();

        mPointerBitmap = Bitmap.createScaledBitmap(origBitmap, (int) shortSide, (int) longSide, true);
        mPointerBitmap.setDensity(Bitmap.DENSITY_NONE);
    }

    RectF progressCircle = new RectF();

    /**
     * called every time PostInvalidate() is called.
     * @param canvas user screen
     */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw pointer
        mPaintBox.setAlpha(255);
        canvas.drawBitmap(mPointerBitmap, mPointerLocation.x, mPointerLocation.y, mPaintBox);

        progressCircle.set(mPointerLocation.x + 30, mPointerLocation.y + 30, mPointerLocation.x + 30 + 20, mPointerLocation.y + 30 + 20);
        float mProgress = mClickProgressPercent * 3.6f;
        canvas.drawArc(progressCircle, 0, mProgress, true, mPaintBox);

        updatePosition(mPointerLocation);

        if (mSwipemodeEnabled) {
            canvas.drawLine(mSwipeLocation.x, mSwipeLocation.y, mPointerLocation.x, mPointerLocation.y, mPaintBox);
        }
    }

    public void updatePosition(PointF p) {
        mPointerLocation.x = p.x;
        mPointerLocation.y = p.y;
    }

    PointF mSwipeLocation = new PointF();

    public void saveSwipeLocation() {
        mSwipeLocation.x = mPointerLocation.x;
        mSwipeLocation.y = mPointerLocation.y;
        mSwipemodeEnabled = true;
    }

    public void MovePointer(PointF mMotion) {

        //adjust pointer speed
        float mXAxisBoost = 9;
        float mYAxisBoost = 9;

        mMotion.x *= mXAxisBoost;
        mMotion.y *= mYAxisBoost * 2.5f;

        // making sure pointer will not move out of the screen
        mPointerLocation.x += mMotion.x;
        if (mPointerLocation.x < 0) {
            mPointerLocation.x = 0;
        } else {
            int width = this.getWidth();
            if (mPointerLocation.x >= width)
                mPointerLocation.x = width - 1;
        }

        mPointerLocation.y += mMotion.y;
        if (mPointerLocation.y < 0) {
            mPointerLocation.y = 0;
        } else {
            int height = this.getHeight();
            if (mPointerLocation.y >= height)
                mPointerLocation.y = height - 1;
        }
        // update pointer location
    }


    public PointF getPointerLocation() {
        return mPointerLocation;
    }

    public void updateClickProgress(int percent) {
        mClickProgressPercent = percent;
    }

    public void setCenter() {
        //set pointer to center
        mPointerLocation.x = Resources.getSystem().getDisplayMetrics().widthPixels / 2;
        mPointerLocation.y = Resources.getSystem().getDisplayMetrics().heightPixels / 2;
        Log.i("PointerView", "Pointer location: " + mPointerLocation.x + ", " + mPointerLocation.y);
    }

    public void disableSwipeMode() {
        mSwipeLocation.x = 0;
        mSwipeLocation.y = 0;
        mSwipemodeEnabled = false;
    }
}

