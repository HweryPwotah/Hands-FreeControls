package com.example.os10.hands_freecontrols;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import org.opencv.core.Rect;

/**
 * Class that handles Pointer View.
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
    private float mXAxisBoost, mYAxisBoost;

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
        Log.i("PointerView", "Trying to initialize pointer");
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
        Log.i("PointerView", "declaring pointer");
        BitmapDrawable bd = (BitmapDrawable)
                ContextCompat.getDrawable(getContext(), R.drawable.pointer);
        Bitmap origBitmap= bd.getBitmap();
        origBitmap.setDensity(Bitmap.DENSITY_NONE);

        // desired long side in pixels of the pointer for this screen density
        Log.i("PointerView", "declaring variables");
        float longSide= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                CURSOR_LONG_SIDE_DIP, getResources().getDisplayMetrics()) * size;
        float scaling = longSide / (float) bd.getIntrinsicHeight();
        float shortSide= scaling * bd.getIntrinsicWidth();

        Log.i("PointerView", "other settings");
        mPointerBitmap = Bitmap.createScaledBitmap(origBitmap, (int) shortSide, (int) longSide, true);
        mPointerBitmap.setDensity(Bitmap.DENSITY_NONE);

        // compute radius of progress indicator in px
//        mProgressIndicatorRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                PROGRESS_INDICATOR_RADIUS_DIP, getResources().getDisplayMetrics()) * size;
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        Log.i("PointerView", "onDraw");
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
        Log.i("PointerView", "draw pointer");
        canvas.drawBitmap(mPointerBitmap, mPointerLocation.x, mPointerLocation.y, mPaintBox);
        updatePosition(mPointerLocation);


    }

    public void updatePosition(PointF p) {
        mPointerLocation.x= p.x;
        mPointerLocation.y= p.y;
    }

    PointF prevPointer = new PointF();
    PointF currPointer = new PointF();

    PointF mMotion = new PointF();
    public void processMotion(Rect prevFace, Rect currFace) {

        if (null == prevFace) return;

        mXAxisBoost = 6;
        mYAxisBoost = 6;

        //get middle point of prevFace
        prevPointer.x = (float) (prevFace.tl().x + prevFace.br().x) / 2;
        prevPointer.y = (float) (prevFace.tl().y + prevFace.br().y) / 2;

        //get middle point of currFace
        currPointer.x = (float) (currFace.tl().x + currFace.br().x) / 2;
        currPointer.y = (float) (currFace.tl().y + currFace.br().y) / 2;

        //calculate vector
        mMotion.x = currPointer.x - prevPointer.x;
        mMotion.y = currPointer.y - prevPointer.y;

        //multiplied to ensure pointer can reach any point in the screen
        mMotion.x *= mXAxisBoost;
        mMotion.y *= mYAxisBoost;

        // making sure pointer will not move out from the screen
        mPointerLocation.x+= mMotion.x;
        if (mPointerLocation.x< 0) {
            mPointerLocation.x= 0;
        }
        else {
            int width= this.getWidth();
            if (mPointerLocation.x>= width)
                mPointerLocation.x= width - 1;
        }

        mPointerLocation.y+= mMotion.y;
        if (mPointerLocation.y< 0) {
            mPointerLocation.y= 0;
        }
        else {
            int height= this.getHeight();
            if (mPointerLocation.y>= height)
                mPointerLocation.y= height - 1;
        }

        // update pointer location
        updatePosition(mPointerLocation);
    }
}

