package com.example.os10.hands_freecontrols;

/**
 * Class to imitate that of a timer
 */

class Timer {


    private long mTimerTick;
    private long mLastTimeStamp;

    Timer(long timerTick) {
        setTimer(timerTick);

    }

    public void setTimer(long timerTick) {
        if (timerTick < 0) throw new AssertionError();
        this.mTimerTick = timerTick;
    }

    /**
     * Start the timer
     */
    public void start() {
        mLastTimeStamp = System.currentTimeMillis();
    }

    public boolean hasFinished() {
        return (System.currentTimeMillis() - mLastTimeStamp > mTimerTick);
    }

    public int getElapsedPercent() {
        if (mTimerTick == 0) return 100;

        long elapsed = System.currentTimeMillis() - mLastTimeStamp;

        if (elapsed > mTimerTick) return 100;

        return (int) ((100 * elapsed) / mTimerTick);
    }
}
