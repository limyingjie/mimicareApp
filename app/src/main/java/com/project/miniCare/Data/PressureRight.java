package com.project.miniCare.Data;

import java.util.ArrayList;

public class PressureRight {
    private int step;
    private ArrayList<int[]> pressureData;

    public PressureRight(int step, ArrayList<int[]> pressureData) {
        this.step = step;
        this.pressureData = pressureData;
    }

    public void addPressure(int[] data){
        this.pressureData.add(data);
    }
}
