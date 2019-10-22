package com.project.mimiCare.Utils;

import android.util.AndroidRuntimeException;
import android.util.Log;

public class BluetoothDataHandler {
    private final String TAG = "BluetoothDataHandler";

    public int[] receive(byte[] data) {
        String strData = new String(data);
//        Log.d(TAG, "message string received: " + strData);

        int[] lineData = new int[8];

        try {
            if (strData.length() != 16) {
                throw new AndroidRuntimeException("message invalid length");
            }
            for (int i = 0; i < 8; i++) {
                lineData[i] = Integer.parseInt(strData.substring(2 * i, 2 * i + 2));
            }
        } catch (AndroidRuntimeException | NumberFormatException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }

        // THESE TWO PINS ARE FAULTY AND HAVE GHOST READINGS
        lineData[0] = 0;
        lineData[2] = 0;
        return lineData;
    }
}
