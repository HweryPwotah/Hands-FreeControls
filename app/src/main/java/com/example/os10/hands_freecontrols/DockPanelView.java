package com.example.os10.hands_freecontrols;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
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
}
