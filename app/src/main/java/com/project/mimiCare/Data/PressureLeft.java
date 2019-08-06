package com.project.mimiCare.Data;

import java.util.ArrayList;

public class PressureLeft {
    private int[] step;
    private ArrayList<int[]> pressureData;

    public PressureLeft(int[] step, ArrayList<int[]> pressureData) {
        this.step = step;
        this.pressureData = pressureData;
    }

    public void addPressure(int step,int[] data){
        this.pressureData.add(data);
    }

}
