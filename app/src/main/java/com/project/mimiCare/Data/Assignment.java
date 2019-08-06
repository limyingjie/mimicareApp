package com.project.mimiCare.Data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.Calendar;

public class Assignment implements Parcelable {
    private static final String TAG = "Assignment";
    private String name;
    private int target;
    private int current;
    private Calendar date;
    private int poor;
    private int good;
    private int perfect;

    // may consider use https://github.com/JakeWharton/ThreeTenABP instead of calendar
    public Assignment(String name, int target, int current, Calendar date) {
        this.name = name;
        this.target = target;
        this.current = current;
        this.date = date;
        this.poor = 0;
        // when it is randomly initialize
        this.good = current;
        this.perfect = 0;
    }

    protected Assignment(Parcel in) {
        name = in.readString();
        target = in.readInt();
        current = in.readInt();
        perfect = in.readInt();
        good = in.readInt();
        poor = in.readInt();
    }

    public static final Creator<Assignment> CREATOR = new Creator<Assignment>() {
        @Override
        public Assignment createFromParcel(Parcel in) {
            return new Assignment(in);
        }

        @Override
        public Assignment[] newArray(int size) {
            return new Assignment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(target);
        parcel.writeInt(current);
        parcel.writeInt(perfect);
        parcel.writeInt(good);
        parcel.writeInt(poor);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar data) {
        this.date = data;
    }

    public int getPoor() {
        return poor;
    }

    public void setPoor(int poor) {
        this.poor = poor;
    }

    public int getGood() {
        return good;
    }

    public void setGood(int good) {
        this.good = good;
    }

    public int getPerfect() {
        return perfect;
    }

    public void setPerfect(int perfect) {
        this.perfect = perfect;
    }

    public int getScore(){
        if (this.current==0){
            return 0;
        }
        return (this.perfect*100+this.good*50)/this.current;
    }

    public String getRemainingTime(){
        Calendar calendar = Calendar.getInstance();
        if (this.date.after(calendar)){
            long timeDiff = this.date.getTimeInMillis() - calendar.getTimeInMillis();
            int daysLeft = (int) (timeDiff / DateUtils.DAY_IN_MILLIS);
            if (daysLeft<1){
                int hour = (int) (timeDiff/DateUtils.HOUR_IN_MILLIS);
                if (hour == 0){
                    return "<1 hours";
                }
                else{
                    return hour + " hours";
                }
            }
            Log.d(TAG, "getRemainingTime: " + daysLeft);
            return daysLeft + " days";
        }
        // it means it is in the past
        return "the past";
    }

}
