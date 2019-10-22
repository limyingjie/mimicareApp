package com.project.mimiCare.Utils;

import android.util.Log;

import java.util.Arrays;

public class StepChecker {
    private static int GOOD_THRESHOLD = 105;
    private static int TOTAL_PRESSURE_DIFF_THRESHOLD = 30;
    private static int marginPercentage = 25;

    private int[] correctStepPressure;
    private int upperMargin;
    private int bottomMargin;
    private int peakTotalPressure = 0;
    private int[] peakStepReading;

    public StepChecker(int[] correctStepPressure) {
        this.correctStepPressure = correctStepPressure;
        int totalCorrectStepPressure = calcTotalPressure(correctStepPressure);
        double margin = totalCorrectStepPressure * ((double)marginPercentage / 100);
        upperMargin = (int) Math.ceil(totalCorrectStepPressure + margin);
        bottomMargin = (int) Math.floor(totalCorrectStepPressure - margin);
    }

    public StepChecker(){}
    public String checkStep(int[] step) {
        if (correctStepPressure==null || step.length != correctStepPressure.length) {
            throw new RuntimeException("Number of readings don't match!");
        }

        int totalPressure = calcTotalPressure(step);

        // we only grade the reading when the step is at its peak pressure (ie foot fully on ground)
        if (totalPressure < peakTotalPressure - TOTAL_PRESSURE_DIFF_THRESHOLD) {
            // feet is already lifting off after its peak, so we grade that step;
            Log.d("step reading", Integer.toString(peakTotalPressure-totalPressure));
            String result = grade(peakTotalPressure);
            peakTotalPressure = 0; // reset this value


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

    public int[] validateStep(int[] step){

        int totalPressure = calcTotalPressure(step);

        // we only grade the reading when the step is at its peak pressure (ie foot fully on ground)
        if (totalPressure < peakTotalPressure - TOTAL_PRESSURE_DIFF_THRESHOLD) {
            // feet is already lifting off after its peak, so we grade that step;
            Log.d("step reading", Integer.toString(peakTotalPressure-totalPressure));
            peakTotalPressure = 0; // reset this value
            return peakStepReading;
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

    private String grade(int peak) {
        /*int difference = 0;
        for (int i = 0; i < step.length; i++) {
            difference += step[i];
        }
        Log.d("StepChecker", "Difference: " + difference);
        */
        String result;

        // check the total difference
/*        if (difference <= GOOD_THRESHOLD) {
            result = "GOOD";
        } else {
            result = "POOR";
        }*/

        if (peak <= upperMargin && peak >= bottomMargin){
            result = "GOOD";
        }
        else {
            result = "POOR";
        }
        Log.d("StepChecker", result);
        return result;
    }
}
