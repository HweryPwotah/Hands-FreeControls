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

import java.util.List;

/**
 * Class that handles user dwell click and click functions
 */

class ClickEngine {
    private final AccessibilityService mAccessibilityService;
    private final DockPanelView mDockPanelView;

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

    ClickEngine(@NonNull AccessibilityService c, DockPanelView dockPanelView) {
        // get constants from resources
//        Resources r= c.getResources();
        mAccessibilityService = c;
        mDockPanelView = dockPanelView;

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

    void onMouseEvent(Point pInt, boolean clickGenerated) {
        if (!clickGenerated) return;
//        Log.i("ClickEngine", "Pointer is on : (" + pInt.x + ", " + pInt.y + ")");

        // TODO: 1/29/2018
        //check if clicked item is one of dock panel buttons. if yes, handle button functions
        if (HandleDockPanel(pInt)) return;

        AccessibilityNodeInfo root;// = null;
//        List<AccessibilityWindowInfo> l = mAccessibilityService.getWindows();
//        Rect bounds = new Rect();
//        for (AccessibilityWindowInfo awi : l) {
//            awi.getBoundsInScreen(bounds);
//            if (bounds.contains(pInt.x, pInt.y)) {
//                AccessibilityNodeInfo rootCandidate = awi.getRoot();
//                if (rootCandidate == null) continue;
//                        /*
//                          Check bounds for the candidate root node. Sometimes windows bounds
//                           are larger than root bounds
//                         */
//                rootCandidate.getBoundsInScreen(bounds);
//                if (bounds.contains(pInt.x, pInt.y)) {
//                    root = rootCandidate;
//                    break;
//                }
//            }
//        }
//
//        if (root == null) {
//            Log.e("ClickEngine", "Error code: #01 root is null");
            root = mAccessibilityService.getRootInActiveWindow();
            if (root == null) {
                Log.e("ClickEngine", "Error code: #02 root in active window is null");
                return;
            }
//        }

        //find node under pointer location
        AccessibilityNodeInfo node = getConcernedNodes(pInt, root);

        if (node == null) {
            Log.e("ClickEngine", "Error code: #03 node found is null");
            return;
        }

        int mAction = AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK.getId();
        node.performAction(mAction);
    }

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
            /*
            case R.id.swipemode_button:
                mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
                change state from click mode to swipe mode
                break;
             */
        }
        return true;
    }

    private AccessibilityNodeInfo getConcernedNodes(Point point, AccessibilityNodeInfo root) {
        RecursionInfo ri = new RecursionInfo(point);
        return getConcernedNodes0(root, ri);
    }

    private AccessibilityNodeInfo getConcernedNodes0(AccessibilityNodeInfo node, RecursionInfo ri) {
        AccessibilityNodeInfo nodeResult = null;
        //check requirements
        if (node == null) return null;
        if (!node.isVisibleToUser()) return null;
        node.getBoundsInScreen(ri.bounds);
        if (!ri.bounds.contains(ri.p.x, ri.p.y)) return null;

        ri.actionList = node.getActionList();
        for (AccessibilityNodeInfo.AccessibilityAction x : ri.actionList){
            if (x == AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK) nodeResult = node;
        }

        //check if the node has anymore child. if yes, explore
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = getConcernedNodes0(node.getChild(i), ri);
            if (child != null) nodeResult = child;
        }
        return nodeResult;
    }

    /**
     * Class to store information across recursive calls
     */
    private static class RecursionInfo {
        final Point p;
        final Rect bounds = new Rect();
        List<AccessibilityNodeInfo.AccessibilityAction> actionList;

        RecursionInfo(Point p) {
            this.p = p;
        }
    }
}
