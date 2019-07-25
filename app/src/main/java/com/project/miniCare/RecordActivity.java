package com.project.miniCare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.project.miniCare.Data.RecordData;
import com.project.miniCare.Utils.MockStepGenerator;

import java.lang.reflect.Array;
import java.util.Arrays;

public class RecordActivity extends AppCompatActivity {
    private static final String TAG = "RecordActivityMimicare";

    private boolean inRecord;
    private int currentStep;
    private RecordData recordData;
    private MockDataRunnable mockDataRunnable;
    private Boolean isDone;


    private ProgressBar progressBar;
    private Button record_button;
    private TextView record_step_text;
    private TextView record_data_text;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        Intent intent = getIntent();
        if (intent!=null){

        }

        // initialize
        currentStep = 0;
        recordData = new RecordData();
        isDone = false;
        inRecord = false;
        /*
        UI
         */
        record_button = findViewById(R.id.record_start_stop_Button);
        progressBar = findViewById(R.id.record_progressBar);
        record_step_text = findViewById(R.id.record_step);
        record_data_text = findViewById(R.id.record_data);

        record_button.setOnClickListener((View v)->onClick());
        Log.d(TAG, "onCreate: Called");
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (inRecord) stopRecord();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (inRecord) stopRecord();
    }

    private void onClick(){
        Log.d(TAG, "onClick: Called");
        if(isDone)return;

        if (inRecord){
            stopRecord();
        }
        else{
            startRecord();
        }
    }

    private void stopRecord(){
        mockDataRunnable.isActive = false;

        runOnUiThread(()->record_button.setText(R.string.start_recording));
        inRecord = false;
    }

    private void startRecord(){
        // start runnable
        mockDataRunnable = new MockDataRunnable();

        //create new Thread
        Thread mockDataThread = new Thread(mockDataRunnable);
        mockDataThread.start();

        record_button.setText(R.string.stop_recording);
        inRecord = true;
    }

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
            // short pause before starting the thread
            sleep(1000);
            MockStepGenerator mockStepGenerator = new MockStepGenerator();
            Log.i("MOCK", "Mock data thread is started");
            while (isActive) {
                int [] record_mock_data = mockStepGenerator.nextRandom();
                process_data(record_mock_data);
                // short pause
                sleep(500);
            }
            Log.i("MOCK", "Mock data thread is stopping");
        }
    }

    private void process_data(int[] record) {
        if (record==null){
            return;
        }
        boolean inLowState = !isAllZero(record);
        if (inLowState){
            Log.d(TAG, "process_data: " + Arrays.toString(record));
            // add Data
            currentStep++;
            recordData.add(record);

            // update UI
            runOnUiThread(()->{
                record_step_text.setText(String.format("%d Steps.",currentStep));
                record_data_text.setText("Data: " + Arrays.toString(record));
                progressBar.setProgress(currentStep);

            });
            // when it is done
            if (currentStep >= progressBar.getMax()){
                int[] average = recordData.getAverage();
                runOnUiThread(()->{
                    record_step_text.setText("Done!");
                    record_data_text.setText("Average: " + Arrays.toString(average));
                });

                // stop the thread
                stopRecord();
                isDone = true;
            }
        }
    }
    private boolean isAllZero(int[] pressure){
        for (int i = 0; i < pressure.length; i++){
            // once it is non-zero, not all is zero
            if (pressure[i]>0){
                return false;
            }
        }
        return true;
    }
}