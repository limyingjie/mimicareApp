package com.project.miniCare.Data;

import java.util.Calendar;

public class StepData {
    private Calendar Date;
    private int step;

    public StepData(Calendar date, int step) {
        Date = date;
        this.step = step;
    }

    public Calendar getDate() {
        return Date;
    }

    public void setDate(Calendar date) {
        Date = date;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
}
