package com.project.mimiCare.Utils;

import java.util.ArrayList;

public class PressureColor {
    private static int lightBlueThreshold = 33;
    private static int blueThreshold = 66;
    //private static int darkBlueThreshold = 99;

    public static ArrayList<String> get_color(int[] pressure){
        if (pressure.length!=8) {
            return null;
        }
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < pressure.length; i++){
            int value = pressure[i];
            if (value == 0){
                result.add("g");
            }
            else if (value <= lightBlueThreshold){
                result.add("lb");
            }
            else if (value <= blueThreshold ){
                result.add("b");
            }
            else{
                result.add("db");
            }
        }
        return result;
    }
}
