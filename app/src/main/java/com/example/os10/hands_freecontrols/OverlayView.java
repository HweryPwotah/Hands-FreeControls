package com.example.os10.hands_freecontrols;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Class that handles user interface such as Dock Panel, Pointer, and Camera (?).
 */

public class OverlayView {
    private static final String TAG = "OverlayView";
    FrameLayout mLayout;

    OverlayView(Context c) {
        mLayout = new FrameLayout(c);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        layoutParams.setTitle("LayoutOverlay");

        // Transparent background
        layoutParams.format = PixelFormat.TRANSLUCENT;

        //https://developer.android.com/reference/android/view/WindowManager.LayoutParams.html#TYPE_APPLICATION_OVERLAY
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;

        /*
         * Type of window. Whole screen is covered (including status bar)
         *
         * FLAG_NOT_FOCUSABLE: this window won't ever get key input focus, so the user can not
         *      send key or other button events to it. It can use the full screen for its content
         *      and cover the input method if needed
         *
         * FLAG_LAYOUT_IN_SCREEN: place the window within the entire screen, ignoring decorations
         *      around the border (such as the status bar)
         *
         */
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.gravity = Gravity.LEFT;

//        LayoutInflater inflater = LayoutInflater.from(c);
//        inflater.inflate(R.layout.dock_panel_layout, mLayout);

        WindowManager wm= (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        wm.addView(mLayout, layoutParams);
    }
//    void cleanup() {
//        WindowManager wm= (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
//        wm.removeViewImmediate(this);
//
//        Log.i(TAG, "OverlayView: finish destroyOverlay");
//    }

    public void addFullScreenLayer (View v) {
        RelativeLayout.LayoutParams lp= new RelativeLayout.LayoutParams(mLayout.getWidth(), mLayout.getHeight());
        lp.width= RelativeLayout.LayoutParams.MATCH_PARENT;
        lp.height= RelativeLayout.LayoutParams.MATCH_PARENT;

        v.setLayoutParams(lp);
        mLayout.addView(v);
    }
}



