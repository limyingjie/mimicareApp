package com.project.miniCare.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

public class Assignment implements Parcelable {
    private String name;
    private int target;
    private int current;
    private Calendar date;
    private int poor;
    private int good;
    private int perfect;

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

    public Assignment(Assignment assignment){
        this.name = assignment.getName();
        this.target = assignment.getTarget();
        this.current = assignment.getCurrent();
        this.date = assignment.getDate();
        this.poor = assignment.getPoor();
        this.good = assignment.getGood();
        this.perfect = assignment.getPerfect();
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
    public boolean equal(Assignment assignment){
        if (this.name != assignment.name){
            return false;
        }
        else if (this.target != assignment.target){
            return false;
        }
        else if (this.current != assignment.current){
            return false;
        }
        else if (this.date.compareTo(assignment.date)!=0){
            return false;
        }
        return true;
    }

}
