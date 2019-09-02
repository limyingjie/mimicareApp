package com.project.mimiCare.Utils;

import android.util.Log;

public class StepChecker {

    private int[] correctStepPressure;

    public StepChecker(int[] correctStepPressure) {
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

        // check the total difference
        if (difference <= 8000) {
            result = "PERFECT";
        } else if (difference <= 10000) {
            result = "GOOD";
        } else {
            result = "POOR";
        }
        return result;
    }

    public String checkIndividual(int position, int step){
        int diff = Math.abs(step - correctStepPressure[position]);
        if (diff < 1000){
            return "perfect";
        }
        else if (diff < 2000){
            return "good";
        }
        else{
            return "poor";
        }
    }
}
