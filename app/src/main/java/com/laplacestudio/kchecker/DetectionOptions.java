package com.laplacestudio.kchecker;

import androidx.annotation.NonNull;

public class DetectionOptions implements Cloneable {
    /**
     * Milliseconds between two adjacent detection. Default is 1000 ms.
     */
    public int frequency = 24;
    public static int MAX_FREQUENCY=100;
    public static int MIN_FREQUENCY=1;

    /**
     * Maximum detection time. Default is 15 seconds.
     */
    public int maxDetectionTime = 10*1000;
    public static int MAX_DETECTION_TIME_MAX =30;
    public static int MAX_DETECTION_TIME_MIN =1;

    /**
     * If it's true, the result will only show incorrect controls.
     */
    public boolean onlyShowIncorrect = false;

    /**
     * Indicate the detection mode.
     */
    public int detectionMode=DETECT_MODE_FLOW;

    public static int DETECT_MODE_IDLE=-1;
    public static int DETECT_MODE_CAPTURE=0;
    public static int DETECT_MODE_SINGLE_SHOT=1;
    public static int DETECT_MODE_FLOW=2;

    public DetectionOptions(int detectionMode){
        this.detectionMode=detectionMode;
    }

    public DetectionOptions() {
    }

    public int getDetectionInterval(){
        return 1000/frequency;
    }

    @Override
    protected DetectionOptions clone(){
        try {
            return (DetectionOptions)super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
