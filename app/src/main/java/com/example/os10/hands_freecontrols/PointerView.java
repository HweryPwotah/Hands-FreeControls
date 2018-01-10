package com.example.os10.hands_freecontrols;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;

/**
 * Class that handles user tools such as Dock Panel, Pointer, and Camera (?).
 */

public class PointerView extends View implements SharedPreferences.OnSharedPreferenceChangeListener {
    // Size of the long side of the pointer for normal size (in DIP)
    private static final float CURSOR_LONG_SIDE_DIP = 30;
    // bitmap of the (mouse) pointer
    private Bitmap mPointerBitmap;
    // cached paint box
    private final Paint mPaintBox;
    // the location where the pointer needs to be painted
    private PointF mPointerLocation;

    public static final String KEY_UI_ELEMENTS_SIZE= "ui_elements_size";
    public static final String KEY_GAMEPAD_TRANSPARENCY= "gamepad_transparency";

    /**
     *
     * Constructor for initializing Pointer
     * @param c context
     */
    public PointerView(Context c) {
        super(c);
        mPaintBox = new Paint();
        setWillNotDraw(false);
        mPointerLocation= new PointF();
        PointerInitialization();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equals(KEY_UI_ELEMENTS_SIZE) ||
                key.equals(KEY_GAMEPAD_TRANSPARENCY)) {
            PointerInitialization();
        }
    }

    private void PointerInitialization() {
        float size= 1; //Preferences.get().getUIElementsSize();
//        mAlphaPointer= (255 * Preferences.get().getGamepadTransparency()) / 100;

        // re-scale pointer accordingly
        BitmapDrawable bd = (BitmapDrawable)
                ContextCompat.getDrawable(getContext(), R.drawable.pointer);
        Bitmap origBitmap= bd.getBitmap();
        origBitmap.setDensity(Bitmap.DENSITY_NONE);

        // desired long side in pixels of the pointer for this screen density
        float longSide= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                CURSOR_LONG_SIDE_DIP, getResources().getDisplayMetrics()) * size;
        float scaling = longSide / (float) bd.getIntrinsicHeight();
        float shortSide= scaling * bd.getIntrinsicWidth();

        mPointerBitmap = Bitmap.createScaledBitmap(origBitmap, (int) shortSide, (int) longSide, true);
        mPointerBitmap.setDensity(Bitmap.DENSITY_NONE);

        // compute radius of progress indicator in px
//        mProgressIndicatorRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                PROGRESS_INDICATOR_RADIUS_DIP, getResources().getDisplayMetrics()) * size;
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        // draw progress indicator
//        if (mClickProgressPercent> 0) {
//            float radius= ((float)
//                    (100 - mClickProgressPercent) * mProgressIndicatorRadius) / 100.0f;
//
//            mPaintBox.setStyle(Paint.Style.FILL_AND_STROKE);
//            mPaintBox.setColor(0x80000000);
//            canvas.drawCircle(mPointerLocation.x, mPointerLocation.y, radius, mPaintBox);
//
//            mPaintBox.setStyle(Paint.Style.STROKE);
//            mPaintBox.setColor(0x80FFFFFF);
//            canvas.drawCircle(mPointerLocation.x, mPointerLocation.y, radius, mPaintBox);
//        }

        // draw pointer
        mPaintBox.setAlpha(255);
        canvas.drawBitmap(mPointerBitmap, mPointerLocation.x, mPointerLocation.y, mPaintBox);
    }

    public void updatePosition(PointF p) {
        mPointerLocation.x= p.x;
        mPointerLocation.y= p.y;
    }
}

