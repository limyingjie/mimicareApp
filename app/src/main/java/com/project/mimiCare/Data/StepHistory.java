package com.project.mimiCare.Data;

import java.util.ArrayList;
import java.util.HashMap;

public class StepHistory {
    private static HashMap<Pair,ArrayList<StepData>> collection = new HashMap<>();
    private static Boolean isInitialized = false;

    public static void initialize(HashMap<Pair,ArrayList<StepData>> newCollection){
        if (collection.isEmpty()){
            collection.putAll(newCollection);
        }
        isInitialized = true;
    }

    public static void put(Pair pair, ArrayList<StepData> stepData){
        if (isInitialized){
            collection.put(pair,stepData);
        }
    }
    
    class Pair{
        int year;
        int month;

        public Pair(int year, int month) {
            this.year = year;
            this.month = month;
        }
    }
}
