package com.example.os10.hands_freecontrols;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;

import java.util.List;

/**
 * Class that handles user dwell click and click functions
 */

class ClickEngine {
    private final AccessibilityService mAccessibilityService;
    private final DockPanelView mDockPanelView;
    private final PointerView mPointerView;

    /**
     * Enums and constants
     */
    private enum State {
        RESET, POINTER_MOVING, Timer_STARTED, CLICK_DONE
    }

    private final int DWELL_TIME_DEFAULT;
    private final int DWELL_AREA_DEFAULT;

    // delegate to measure elapsed time
    private Timer mTimer;

    // current dwell click state
    private State mState = State.RESET;

    // dwell area tolerance. stored squared to avoid sqrt
    // for each updatePointerLocation call
    private float mDwellAreaSquared;

    // to remember previous pointer location and measure traveled distance
    private PointF mPrevPointerLocation = new PointF();

    ClickEngine(@NonNull AccessibilityService c, DockPanelView dockPanelView, PointerView pointerView) {
        // get constants from resources
//        Resources r= c.getResources();
        mAccessibilityService = c;
        mDockPanelView = dockPanelView;
        mPointerView = pointerView;

        DWELL_TIME_DEFAULT = 10 * 100; //10 seconds
        DWELL_AREA_DEFAULT = 7;

        mTimer = new Timer(DWELL_TIME_DEFAULT);

        // register preference change listener
//        Preferences.get().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        ClickEngineInitialization();

        AccessibilityServiceInfo asi = mAccessibilityService.getServiceInfo();
            /* This call should not return null under normal circumstances,
               just to avoid spurious crashes */
        if (asi != null) {
            asi.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            mAccessibilityService.setServiceInfo(asi);
        }
    }

    private void ClickEngineInitialization() {
//        SharedPreferences sp=  Preferences.get().getSharedPreferences();
        // get values from shared resources
        int dwellTime = DWELL_TIME_DEFAULT;
        mTimer.setTimer(dwellTime);
        int dwellArea = DWELL_AREA_DEFAULT;
        mDwellAreaSquared = dwellArea * dwellArea;
    }

//    public void cleanup() {
//        Preferences.get().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
//    }

//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
//                                          String key) {
//        if (key.equals(Preferences.KEY_DWELL_TIME) || key.equals(Preferences.KEY_DWELL_AREA) ||
//                key.equals(Preferences.KEY_CONSECUTIVE_CLICKS)) {
//            updateSettings();
//        }
//    }

    private boolean movedAboveThreshold(PointF p1, PointF p2) {
        float dx = p1.x - p2.x;
        float dy = p1.y - p2.y;
        float dist = dx * dx + dy * dy;
        return (dist > mDwellAreaSquared);
    }

    /**
     * Reset dwell click internal state
     */
    public void reset() {
        mState = State.RESET;
    }

    /**
     * Given the current position of the pointer calculates if needs to generate a click
     *
     * @param pl - position of the pointer
     * @return true if click generated
     * <p>
     * this method is called from a secondary thread
     */
    boolean updatePointerLocation(@NonNull PointF pl) {
        boolean retval = false;

        // state machine
        if (mState == State.RESET) {
            /* Means previous pointer position is not valid. Change to
             * POINTER_MOVING state and allow pointer position update.
             */
            mState = State.POINTER_MOVING;
        } else if (mState == State.POINTER_MOVING) {
            if (!movedAboveThreshold(mPrevPointerLocation, pl)) {
                mState = State.Timer_STARTED;
                mTimer.start();
            }
        } else if (mState == State.Timer_STARTED) {
            if (movedAboveThreshold(mPrevPointerLocation, pl)) {
                mState = State.POINTER_MOVING;
            } else {
                if (mTimer.hasFinished()) {
                    retval = true;
                    mState = State.CLICK_DONE;
                }
            }
        } else if (mState == State.CLICK_DONE) {
            if (movedAboveThreshold(mPrevPointerLocation, pl)) {
                mState = State.POINTER_MOVING;
            }
        }

        mPrevPointerLocation.set(pl);  // deep copy
        return retval;
    }

    /**
     * Get click progress percent
     *
     * @return value in the range 0 to 100
     */
    int getClickProgressPercent() {
        if (mState != State.Timer_STARTED) return 0;

        return mTimer.getElapsedPercent();
    }

    boolean fromTo = false;
    Point prevPoint = new Point();
    AccessibilityNodeInfo root;

    void onMouseEvent(Point pInt, boolean clickGenerated) {
        if (!clickGenerated) return;

        AccessibilityNodeInfo node;

        int mAction;

        // TODO: 1/29/2018
        //check if clicked item is one of dock panel buttons. if yes, handle button functions
        if (HandleDockPanel(pInt)) return;

        switch (mSwipeMode) {
            case 0: //swipe mode is in OFF condition
                root = null;
                root = mAccessibilityService.getRootInActiveWindow();
                if (root == null) return;

                //find node under pointer location
                node = getConcernedNodes(pInt, root, AccessibilityAction.ACTION_CLICK);
                if (node == null) return;

                mAction = AccessibilityAction.ACTION_CLICK.getId();
                node.performAction(mAction);
                break;
            case 1:
            case 2: //swipe mode is in ON condition
                if (!fromTo) { //FROM : capture the "from" point
                    Log.i("ClickEngine", "onMouseEvent: FROM");
                    root = null;
                    prevPoint.x = pInt.x;
                    prevPoint.y = pInt.y;
                    fromTo = true;
                    mPointerView.saveSwipeLocation();
                } else { //TO : capture the two point, and do swipe
                    Log.i("ClickEngine", "onMouseEvent: TO");
                    root = mAccessibilityService.getRootInActiveWindow();
                    if (root == null) return;
                    AccessibilityAction AccAction = CheckDirection(prevPoint, pInt);
                    //find node under pointer location
                    node = getConcernedNodes(prevPoint, root, AccAction);
                    if (node == null) {
                        Log.i("ClickEngine", "onMouseEvent: error report 209 node null");
                    } else {
                        mAction = AccAction.getId();
                        node.performAction(mAction);
                    }
                    mPointerView.disableSwipeMode();
                    prevPoint.x = 0;
                    prevPoint.y = 0;
                    fromTo = false;
                    if (mSwipeMode == 1) { //if swipe mode is in ON - ONCE condition
                        mSwipeMode = 0;
                        mDockPanelView.post(new Runnable() {
                            @Override
                            public void run() {
                                mDockPanelView.updateSwipeButton(mSwipeMode);
                            }
                        });
                    }
                }
                break;
//            case 2: //swipe mode is in ON - ALWAYS condition
//                if (!fromTo){ //FROM : capture the "from" point
//
//                }else{ //TO : capture the two point, and do swipe
//
//                }
//
//                node.performAction(mAction);
//                break;
        }


//        node.performAction(mAction);
    }

    private AccessibilityAction CheckDirection(Point prevPoint, Point currPoint) {
        int x, y;

        x = currPoint.x - prevPoint.x;
        y = currPoint.y - prevPoint.y;

        if (Math.abs(x) >= Math.abs(y)) {
            if (x > 0) return AccessibilityAction.ACTION_SCROLL_FORWARD;
            else return AccessibilityAction.ACTION_SCROLL_BACKWARD;
        } else {
            if (y > 0) return AccessibilityAction.ACTION_SCROLL_BACKWARD;
            else return AccessibilityAction.ACTION_SCROLL_FORWARD;
        }
    }

    private int mSwipeMode = 0;

    private boolean HandleDockPanel(Point point) {
        //check whether the click is on dock panel or not
        int dockPanelButtonID = mDockPanelView.getViewID(point);
        if (dockPanelButtonID == View.NO_ID) return false;

        // TODO: 1/31/2018 any buttons added is implemented here
        switch (dockPanelButtonID) {
            case R.id.back_button:
                mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                break;
            case R.id.home_button:
                mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                break;
            case R.id.recents_button:
                mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                break;
            case R.id.notifications_button:
                mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
                break;
            case R.id.toggle_swipe_mode:
                //change state from click mode to swipe mode
                mSwipeMode++;
                if (mSwipeMode > 2) mSwipeMode = 0;
//                mDockPanelView.setSwipeMode(mSwipeMode);
                mDockPanelView.post(new Runnable() {
                    @Override
                    public void run() {
                        mDockPanelView.updateSwipeButton(mSwipeMode);
                    }
                });
                break;
        }

        return true;
    }

    /**
     * Class to store information across recursive calls
     */
    private static class RecursionInfo {
        final Point p;
        final Rect bounds = new Rect();
        AccessibilityAction action;

        RecursionInfo(Point p, AccessibilityAction action) {
            this.p = p;
            this.action = action;
        }
    }

    private AccessibilityNodeInfo getConcernedNodes(Point point, AccessibilityNodeInfo root, AccessibilityAction action) {
        RecursionInfo ri = new RecursionInfo(point, action);
        return getConcernedNodes0(root, ri);
    }

    private AccessibilityNodeInfo getConcernedNodes0(AccessibilityNodeInfo node, RecursionInfo ri) {
        AccessibilityNodeInfo nodeResult = null;
        //check requirements
        if (node == null) return null;
        if (!node.isVisibleToUser()) return null;
        node.getBoundsInScreen(ri.bounds);
        if (!ri.bounds.contains(ri.p.x, ri.p.y)) return null;

        List<AccessibilityAction> actionList = node.getActionList();
        for (AccessibilityAction x : actionList) {
            if (x == ri.action) nodeResult = node;
        }

        //check if the node has anymore child. if yes, explore
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = getConcernedNodes0(node.getChild(i), ri);
            if (child != null) nodeResult = child;
        }
        return nodeResult;
    }

}
