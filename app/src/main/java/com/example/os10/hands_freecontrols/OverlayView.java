package com.example.os10.hands_freecontrols;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Class that creates user interface for tools such as: Dock Panel, Pointer, and Camera (?).
 */

public class OverlayView extends RelativeLayout {
    /**
     * constructor for Overlay View. Create a view which overlays the user screen.
     *
     * @param c context
     */
    OverlayView(Context c) {
        super(c);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        layoutParams.setTitle("LayoutOverlay");

        // Transparent background
        layoutParams.format = PixelFormat.TRANSLUCENT;

        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE |
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        //https://developer.android.com/reference/android/view/WindowManager.LayoutParams.html#TYPE_APPLICATION_OVERLAY
//        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;

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

        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        wm.addView(this, layoutParams);
    }

    /**
     * add the view v to the user screen overlay
     *
     * @param v view
     */
    public void addFullScreenLayer(View v) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(this.getWidth(), this.getHeight());
        lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;

        v.setLayoutParams(lp);
        this.addView(v);
    }

    public void terminate() {
        WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.removeViewImmediate(this);
    }
}



