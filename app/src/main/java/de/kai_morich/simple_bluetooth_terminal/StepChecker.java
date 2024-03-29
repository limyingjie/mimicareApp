package de.kai_morich.simple_bluetooth_terminal;

import android.util.Log;

public class StepChecker {

    private int[] correctStepPressure;

    StepChecker(int[] correctStepPressure) {
        this.correctStepPressure = correctStepPressure;
    }

    public String checkStep(int[] step) {
        if (step.length != correctStepPressure.length) {
            throw new RuntimeException("Number of readings don't match!");
        }

        int difference = 0;
        for (int i = 0; i < correctStepPressure.length; i++) {
            difference += Math.abs(step[i] - correctStepPressure[i]);
        }
        Log.d("StepChecker", "Difference: " + difference);

        String result;
        if (difference <= 2000) {
            result = "PERFECT";
        } else if (difference <= 2500) {
            result = "GOOD";
        } else {
            result = "POOR";
        }
        return result;
    }
}
