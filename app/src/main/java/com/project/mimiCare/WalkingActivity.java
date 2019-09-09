package com.project.mimiCare;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.project.mimiCare.Utils.MockStepGenerator;
import com.project.mimiCare.Utils.PressureColor;

import java.util.ArrayList;

public abstract class WalkingActivity extends AppCompatActivity {
    protected static final String TAG = "";

    protected ImageView[] pressureImageView = new ImageView[8];

    protected MockDataRunnable mockDataRunnable;

    protected abstract void process_data(int[] data);

    class MockDataRunnable implements Runnable {
        boolean isActive = true;

        private void sleep(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Log.e("MOCK", e.getMessage());
            }
        }

        @Override
        public void run() {
            sleep(1000);
            MockStepGenerator mockStepGenerator = new MockStepGenerator();
            Log.i("MOCK", "Mock data thread is started");
            while (isActive) {
                int[] data = mockStepGenerator.nextRandom();
                process_data(data);
                sleep(500);
            }
            Log.i("MOCK", "Mock data thread is stopping");
        }
    }

    protected void updatePressureImageView(int[] pressureData, Boolean in_low_state) {
        ArrayList<String> color_result = PressureColor.get_color(pressureData);
        for (int i=0; i < color_result.size(); i++){
            String color = color_result.get(i);
            switch (color){
                case "g":
                    pressureImageView[i].setImageResource(R.drawable.circle_grey);
                    break;
                case "lb":
                    pressureImageView[i].setImageResource(R.drawable.circle_lightblue);
                    break;
                case "b":
                    pressureImageView[i].setImageResource(R.drawable.circle_blue);
                    break;
                case "db":
                    pressureImageView[i].setImageResource(R.drawable.circle_darkblue);
                    break;
            }
        }
    }
}
