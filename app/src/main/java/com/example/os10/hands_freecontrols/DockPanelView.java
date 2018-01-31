package com.example.os10.hands_freecontrols;

import android.content.Context;
import android.graphics.Point;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Class that handles Dock Panel
 */

public class DockPanelView extends RelativeLayout {
    // the docking panel
    private LinearLayout mDockPanelView;

    /**
     *
     * Constructor : for initialization purposes, drawing dock panel.
     * @param c Context
     */
    public DockPanelView(Context c) {
        super(c);
//        SharedPreferences sp= Preferences.get().getSharedPreferences();
//        sp.registerOnSharedPreferenceChangeListener(this);
        int gravity= Gravity.START;
        float size = 1;//Preferences.get().getUIElementsSize();

        if (mDockPanelView != null) {
            removeView(mDockPanelView);
        }

        //Container view
        mDockPanelView = new LinearLayout(c);
        RelativeLayout.LayoutParams lp= new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        lp.addRule(RelativeLayout.CENTER_VERTICAL);

        mDockPanelView.setLayoutParams(lp);

        //Panel Button
        LayoutInflater inflater = LayoutInflater.from(c);
        LinearLayout contents= (LinearLayout)
                inflater.inflate(R.layout.dock_panel_layout, mDockPanelView, false);
        contents.setOrientation(LinearLayout.VERTICAL);

        mDockPanelView.addView(contents);

        addView(mDockPanelView);
    }

    public int getViewID(Point point) {
        View result= getConcernedView0(point, mDockPanelView);
        if (result == null) return View.NO_ID;

        return result.getId();
    }

    public static View getConcernedView0(Point p, View v) {
        if (v.getVisibility() != View.VISIBLE) return null;
        if (!isPointInsideView(p, v)) return null;
        if (!(v instanceof ViewGroup)) {
            if (v.getId() != View.NO_ID) return v;
            return null;
        }

        // is a ViewGroup, iterate children
        ViewGroup vg= (ViewGroup) v;

        int childCount= vg.getChildCount();

        for (int i= 0; i< childCount; i++) {
            View result = getConcernedView0(p, vg.getChildAt(i));
            if (result != null) return result;
        }

        return null;
    }

    public static boolean isPointInsideView(Point p, View view) {
        if (view == null) return false;

//        int[] location = new int[2];
        int[] location = {0, 0};

        view.getLocationOnScreen(location);

        return !(p.x < location[0] || p.y < location[1]) &&
                !(location[0] + view.getWidth() < p.x ||
                        location[1] + view.getHeight() < p.y);
    }

}
