package com.project.mimiCare.Utils;

import android.util.Log;

import java.util.Arrays;

public class StepChecker {
    private int GOOD_THRESHOLD = 105;
    private int[] correctStepPressure;

    private int peakTotalPressure = 0;
    private int TOTAL_PRESSURE_DIFF_THRESHOLD = 45;
    private int[] peakStepReading;

    public StepChecker(int[] correctStepPressure) {
        this.correctStepPressure = correctStepPressure;
    }

    public String checkStep(int[] step) {
        if (step.length != correctStepPressure.length) {
            throw new RuntimeException("Number of readings don't match!");
        }

        int totalPressure = calcTotalPressure(step);

        // we only grade the reading when the step is at its peak pressure (ie foot fully on ground)
        if (totalPressure < peakTotalPressure - TOTAL_PRESSURE_DIFF_THRESHOLD) {
            // feet is already lifting off after its peak, so we grade that step;
            Log.d("step reading", Integer.toString(peakTotalPressure-totalPressure));
            peakTotalPressure = 0; // reset this value
            String result = grade(peakStepReading);

            return result;
        }
        else {
             if (peakTotalPressure < totalPressure) {
                 peakTotalPressure = totalPressure;
                 peakStepReading = step;
             }
            Log.d("non reading", Integer.toString(totalPressure-peakTotalPressure));
            return null;
        }
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

    private int calcTotalPressure(int[] step) {
        int sum = 0;
        for (int i = 0; i < step.length; i++) {
            sum += step[i];
        }
        return sum;
    }

    private String grade(int[] step) {
        int difference = 0;
        for (int i = 0; i < correctStepPressure.length; i++) {
            difference += Math.abs(step[i] - correctStepPressure[i]);
        }
        Log.d("StepChecker", "Difference: " + difference);

        String result;

        // check the total difference
        if (difference <= GOOD_THRESHOLD) {
            result = "GOOD";
        } else {
            result = "POOR";
        }
        Log.d("StepChecker", result);
        return result;
    }
}
