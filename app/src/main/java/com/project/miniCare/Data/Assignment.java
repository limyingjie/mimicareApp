package com.project.miniCare.Data;

import android.os.Parcel;
import android.os.Parcelable;

public class Assignment implements Parcelable {
    private String name;
    private int target;
    private int current;
    private String data;

    public Assignment(String name, int target, int current, String data) {
        this.name = name;
        this.target = target;
        this.current = current;
        this.data = data;
    }

    protected Assignment(Parcel in) {
        name = in.readString();
        target = in.readInt();
        current = in.readInt();
        data = in.readString();
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
        parcel.writeString(data);
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

    public String getDate() {
        return data;
    }

    public void setDate(String data) {
        this.data = data;
    }

}
