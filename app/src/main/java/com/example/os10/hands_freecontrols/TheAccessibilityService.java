package com.example.os10.hands_freecontrols;

import android.accessibilityservice.AccessibilityService;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

/**
 * Handles Accessibility Service
 */

public class TheAccessibilityService extends AccessibilityService {
    private static TheAccessibilityService sTheAccessibilityService;
    private MainEngine mEngine;
    OverlayView mOverlayView;

    /**
     * (required) This method is called back by the system when it detects an
     * AccessibilityEvent that matches the event filtering parameters specified
     * by your accessibility service.
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        switch(event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:

                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:

                break;
        }
    }
    /**
     * (required) This method is called when the system wants to interrupt the
     * feedback your service is providing, usually in response to a user action
     * such as moving focus to a different control. This method may be called
     * many times over the life cycle of your service.
     */
    @Override
    public void onInterrupt() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onInterrupt");
    }

    /**
     * Called every time the service is switched ON
     */

    @Override
    public void onServiceConnected() {
        Log.i(TAG, "onServiceConnected");
        Toast.makeText(this, "Accessibility is granted uyeaayyy !", Toast.LENGTH_SHORT).show();

        sTheAccessibilityService = this;
        if (null == mEngine) mEngine = new MainEngine();
        mEngine.initialize(this);
    }

    /**
     * Get the current instance of the accessibility service
     *
     * @return reference to the accessibility service or null
     */
    public static @Nullable TheAccessibilityService get() {
        return sTheAccessibilityService;
    }

}

