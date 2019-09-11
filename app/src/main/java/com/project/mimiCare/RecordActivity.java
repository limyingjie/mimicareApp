package com.project.mimiCare;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.project.mimiCare.Data.RecordData;
import com.project.mimiCare.Utils.MockStepGenerator;
import com.project.mimiCare.Utils.PressureColor;
import com.project.mimiCare.Utils.SharedPreferenceHelper;

import java.util.ArrayList;
import java.util.Arrays;

public class RecordActivity extends WalkingActivity {
    private static final String TAG = "RecordActivityMimicare";
    private static final String subKey = "recordData";

    private boolean inRecord;
    private int currentStep;
    private RecordData recordData;

    private Boolean isDone;

    private ProgressBar progressBar;
    private TextView record_step_text;
    private TextView record_data_text;
    private Thread mockDataThread;

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
        progressBar = findViewById(R.id.record_progressBar);
        record_step_text = findViewById(R.id.record_step);
        //record_data_text = findViewById(R.id.record_data);

        pressureImageView[0] = findViewById(R.id.p0);
        pressureImageView[1] = findViewById(R.id.p1);
        pressureImageView[2] = findViewById(R.id.p2);
        pressureImageView[3] = findViewById(R.id.p3);
        pressureImageView[4] = findViewById(R.id.p4);
        pressureImageView[5] = findViewById(R.id.p5);
        pressureImageView[6] = findViewById(R.id.p6);
        pressureImageView[7] = findViewById(R.id.p7);

        Log.d(TAG, "onCreate: Called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isDone && !inRecord){
            startRecord();
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // save and append as a text file
    /**
    private void saveData() {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("pressureRight.txt", Context.MODE_PRIVATE);
            StringBuilder sb = new StringBuilder();
            for (int[] data : recordData.getAll()){
                for (int i=0; i<data.length;i++){
                    sb.append(data[i]);
                    if (i < data.length-1){
                        sb.append(",");
                    }
                    else{
                        sb.append("\n");
                    }
                }
            }
            fos.write(sb.toString().getBytes());
            Log.d(TAG, "saveData: Called");

            SimpleToast.show(this,"Saved to: " + getFilesDir()+ "/" + "pressureRight.txt",Toast.LENGTH_SHORT);

            // check every file inside
            File file = new File(getFilesDir().toURI());
            File[] listFile = file.listFiles();
            for (int i=0; i<listFile.length;i++){
                Log.d(TAG, "saveData: " + listFile[i].getAbsolutePath());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadData(){
        FileInputStream fis = null;
        String output = null;
        try{
            fis = openFileInput("pressureRight.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            while((line = br.readLine())!=null){
                sb.append(line).append("\n");
            }
            output = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(TAG, "loadData: \n" + output);
    }
**/
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
        if (isMocking){
            mockDataRunnable.isActive = false;
        }
        inRecord = false;
    }

    private void startRecord(){
        if (isMocking){
            // start runnable
            mockDataRunnable = new MockDataRunnable();

            //create new Thread
            mockDataThread = new Thread(mockDataRunnable);
            mockDataThread.start();
        }
        inRecord = true;
    }


    @Override
    protected void process_data(int[] record) {
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
                updateUI(currentStep,record);
            });
            // when it is done
            if (currentStep >= progressBar.getMax()){
                int[] average = recordData.getAverage();
                runOnUiThread(()->{
                    record_step_text.setText("Done!");
                    Log.d(TAG, "Average: " + Arrays.toString(average));
                    //record_data_text.setText("Average: " + Arrays.toString(average));
                });
                // save Average Data
                SharedPreferenceHelper.savePreferenceData(this, subKey,average);

                // stop the thread
                stopRecord();
                isDone = true;

                runOnUiThread(()->{
                    alertSuccess();
                    final Handler handler = new Handler();
                    // close after 3 seconds
                    handler.postDelayed(()->{finish();}, 3000);
                });


            }
        }
    }

    private void updateUI(int currentStep, int[] record){
        record_step_text.setText(String.format("%d",currentStep));
        //record_data_text.setText("Data: " + Arrays.toString(record));
        Log.d(TAG, "Data recorded: " + Arrays.toString(record));
        progressBar.setProgress(currentStep);

        updatePressureImageView(record, null);
    }

    private void alertSuccess(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.snippet_record_success,null);
        Button done = mView.findViewById(R.id.record_success_done);

        done.setOnClickListener((View v)->{
            finish();
        });

        alertDialog.setView(mView);
        Dialog alert = alertDialog.create();
        // transparent parent layout
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.setCanceledOnTouchOutside(false);
        alert.show();
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
