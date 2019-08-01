package com.project.miniCare.Data;

import java.util.ArrayList;

public class History {
    private static ArrayList<Assignment> mAssignments = new ArrayList<>();

    public static void add(Assignment assignment){
        mAssignments.add(assignment);
    }

    public static void extend(ArrayList<Assignment> assignments){
        mAssignments.addAll(assignments);
    }

    public static ArrayList<Assignment> getmAssignments(){
        return mAssignments;
    }
}
