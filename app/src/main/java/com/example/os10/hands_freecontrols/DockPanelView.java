package com.example.os10.hands_freecontrols;

import android.content.Context;
import android.graphics.Point;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Class that handles Dock Panel
 */

public class DockPanelView extends RelativeLayout {
    // the docking panel
    private LinearLayout mDockPanelView;

    /**
     * Constructor : for initialization purposes, drawing dock panel.
     *
     * @param c Context
     */
    public DockPanelView(Context c) {
        super(c);
        int gravity = Gravity.START;
        float size = 1;

        if (mDockPanelView != null) {
            removeView(mDockPanelView);
        }

        //Container view
        mDockPanelView = new LinearLayout(c);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        lp.addRule(RelativeLayout.CENTER_VERTICAL);

        mDockPanelView.setLayoutParams(lp);

        //Panel Button
        LayoutInflater inflater = LayoutInflater.from(c);
        LinearLayout contents = (LinearLayout)
                inflater.inflate(R.layout.dock_panel_layout, mDockPanelView, false);
        contents.setOrientation(LinearLayout.VERTICAL);

        mDockPanelView.addView(contents);

        addView(mDockPanelView);
    }

    /**
     * get dock panel button below the point "point" and returns the button
     *
     * @param point pointer location
     * @return return button found below point. Null for not found
     */
    public int getViewID(Point point) {
        View result = getConcernedView0(point, mDockPanelView);
        if (result == null) return View.NO_ID;

        return result.getId();
    }

    /**
     * go through the view recursively to find the node below point
     *
     * @param p pointer location
     * @param v view
     * @return return button flow below point. Null for not found
     */
    public static View getConcernedView0(Point p, View v) {
        if (v.getVisibility() != View.VISIBLE) return null;
        if (!isPointInsideView(p, v)) return null;
        if (!(v instanceof ViewGroup)) {
            if (v.getId() != View.NO_ID) return v;
            return null;
        }

        // is a ViewGroup, iterate children
        ViewGroup vg = (ViewGroup) v;

        int childCount = vg.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View result = getConcernedView0(p, vg.getChildAt(i));
            if (result != null) return result;
        }

        return null;
    }

    /**
     * check whether or not the point p is inside the view v
     *
     * @param p    point
     * @param view view
     * @return boolean
     */
    public static boolean isPointInsideView(Point p, View view) {
        if (view == null) return false;

//        int[] location = new int[2];
        int[] location = {0, 0};

        view.getLocationOnScreen(location);

        return !(p.x < location[0] || p.y < location[1]) &&
                !(location[0] + view.getWidth() < p.x ||
                        location[1] + view.getHeight() < p.y);
    }

    /**
     * handles dock panel swipe button mode (off, on once, on always)
     *
     * @param SwipeMode 0 for off, 1 for once, 2 for always
     */
    public void updateSwipeButton(int SwipeMode) {
        ImageButton ib = (ImageButton) mDockPanelView.findViewById(R.id.toggle_swipe_mode);
        switch (SwipeMode) {
            case 0:
                ib.setImageResource(R.drawable.ic_swipe_off);
                Toast.makeText(getContext(), "Swipe Mode off.", Toast.LENGTH_SHORT).show();
//                Log.i("DockPanelView", "updateSwipeButton: swipe = off");
                break;
            case 1:
                ib.setImageResource(R.drawable.ic_swipe_once);
                Toast.makeText(getContext(), "Swipe Mode on.", Toast.LENGTH_SHORT).show();
//                Log.i("DockPanelView", "updateSwipeButton: swipe = on once");
                break;
            case 2:
                ib.setImageResource(R.drawable.ic_swipe_always);
                Toast.makeText(getContext(), "Swipe Mode always.", Toast.LENGTH_SHORT).show();
//                Log.i("DockPanelView", "updateSwipeButton: swipe = on always");
                break;
        }
    }

}
