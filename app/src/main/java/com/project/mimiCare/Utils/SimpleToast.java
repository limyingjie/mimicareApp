package com.project.mimiCare.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class SimpleToast {
    private static Toast mToast;
    public static void show(Context context, String text, int duration){
        if (mToast != null) {
            Log.d("myToast", "cancelled");
            mToast.cancel();
        }
        mToast = Toast.makeText(context,text,duration);
        mToast.show();
    }
}
