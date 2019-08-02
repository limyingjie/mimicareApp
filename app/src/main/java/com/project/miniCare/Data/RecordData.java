package com.project.miniCare.Data;

import android.util.Log;

import java.util.ArrayList;

public class RecordData {
    private static final String TAG = "RecordData";
    private ArrayList<int[]> pressures;

    public RecordData() {
        pressures = new ArrayList<>();
    }

    public void add(int[] pressure){
        if (pressure.length!=6){
            return;
        }
        pressures.add(pressure);
    }

    public int[] getAverage(){
        if (pressures.isEmpty()){
            return new int[] {0,0,0,0,0,0};
        }
        int[] total = {0,0,0,0,0,0};

        // adding
        for (int i = 0; i < pressures.size(); i++){
            for (int j = 0; j < total.length ;j++){
                int [] pressure = pressures.get(i);
                total[j] += pressure[j];
            }
        }
        Log.d(TAG, "getAverage: " + total);

        int[] average = {0,0,0,0,0,0};
        // dividing
        for (int i = 0; i < total.length; i++){
            average[i] = total[i]/pressures.size();
        }
        return average;
    }

    public int[] getLatest(){
        if (pressures.isEmpty()){
            return null;
        }
        else{
            return pressures.get(pressures.size()-1);
        }
    }

    public ArrayList<int[]> getAll(){
        if (pressures.isEmpty()){
            return null;
        }
        else{
            return pressures;
        }
    }
}
