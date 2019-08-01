package com.project.miniCare.Utils;

import android.content.Context;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class IOHelper {
    public static void writeToFile(Context context, String fileName, String str){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(fileName,Context.MODE_PRIVATE);
            fos.write(str.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
