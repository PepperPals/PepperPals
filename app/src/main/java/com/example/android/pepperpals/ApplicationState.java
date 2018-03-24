package com.example.android.pepperpals;

import android.app.Application;

/*
 * Contains information about the global application state
 */
public class ApplicationState extends Application {
    private boolean initialised = false;

    private int currentRoutine = 0;

    public boolean isInitialised() {
        return initialised;
    }

    public void setInitialised(boolean initialised) {
        this.initialised = initialised;
    }

    public int getCurrentRoutine() {
        return currentRoutine;
    }

    public void setCurrentRoutine(int currentRoutine) {
        this.currentRoutine = currentRoutine;
    }
}
