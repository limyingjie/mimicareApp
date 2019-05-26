package de.kai_morich.simple_bluetooth_terminal;

import android.util.Log;

import java.util.Locale;

public class BluetoothDataHandler {
    private StringBuffer buffer = new StringBuffer();

    public int[] receive(byte[] data) {
        String strData = new String(data);
        buffer.append(strData.replaceAll("\\s+", ""));

        String[] lines = buffer.toString().split("#");
        if (lines.length == 0) {
            return null;
        }

        int[] lineData;

        //Check if last line is complete. Print if is.
        lineData = get_line_data(lines[lines.length - 1]);
        if (lineData != null) {
            buffer.setLength(0);
            Log.d("pos61", (String.format(Locale.US, "%d < %s", lineData[5], lines[lines.length - 1])));
            return lineData;
        }

        //Check if 2nd last line exists is complete. Print if is. (if it exists and is not complete, it must also be first line. so ignore.)
        if (lines.length < 2) {
            return null;
        }
        lineData = get_line_data(lines[lines.length - 2]);
        if (lineData != null) {
            buffer.setLength(0);
            Log.d("pos62", (String.format(Locale.US, "%d < %s", lineData[5], lines[lines.length - 2])));
            return lineData;
        }

        return null;
    }

    private int[] get_line_data(String line){
        String[] splitLine = line.split(",");
        if (splitLine.length == 6){
            int[] data = new int[6];
            for (int i = 0; i < 6; i++) {
                data[i] = Integer.parseInt(splitLine[i]);
            }
            return data;
        }
        return null;
    }


}
