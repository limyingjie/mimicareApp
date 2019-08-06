package com.project.mimiCare.Data;

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

    public int getDay(){return Date.get(Calendar.DAY_OF_MONTH);}

    public int getMonth() {return Date.get(Calendar.MONTH);}

    public int getYear() {return Date.get(Calendar.YEAR);}

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

// use hashMap<Pair,ArrayList<Step>>>
// use ArrayList<StepData>
