package com.project.mimiCare;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.project.mimiCare.Data.AppState;
import com.project.mimiCare.Data.Assignment;
import com.project.mimiCare.Utils.BluetoothDataHandler;
import com.project.mimiCare.Utils.MockStepGenerator;
import com.project.mimiCare.Services.SerialListener;
import com.project.mimiCare.Services.SerialService;
import com.project.mimiCare.Services.SerialSocket;
import com.project.mimiCare.Utils.PressureColor;
import com.project.mimiCare.Utils.SharedPreferenceHelper;
import com.project.mimiCare.Utils.StepChecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class AssignmentStepActivity extends WalkingActivity {
    private static final String TAG = "LiveActivity";
    private static final String subKey1 = "assignment";
    private static final String subKey2 = "recordData";

    private String deviceAddress;
    
    private TextView grade, assignment_tv;
    private TextView tv,pr,g,p;
    private ProgressBar progressBar;

    private boolean isDone = false;
    private int currentStep,targetStep,position,poor,good,perfect;
    private String assignmentTitle;
    private boolean inLowState = false;
    private ArrayList<Assignment> mAssignment;

    // the correct step
    private StepChecker stepChecker;

    BluetoothDataHandler bluetoothDataHandler = new BluetoothDataHandler();

    Thread mockDataThread;

    public AssignmentStepActivity() {
    }

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_assignment_step);
        // default value
        deviceAddress = null;
        good = poor = perfect = 0;
        currentStep = 0;
        targetStep = 20;

        Intent intent = getIntent();
        if (intent!=null) {
            deviceAddress = AppState.getBleDeviceAddress();
            Assignment assignment = intent.getParcelableExtra("assignment");
            if (assignment!=null){
                assignmentTitle = assignment.getName();
                currentStep = assignment.getCurrent();
                targetStep = assignment.getTarget();
                perfect = assignment.getPerfect();
                good = assignment.getGood();
                poor = assignment.getPoor();
                position = intent.getIntExtra("position",-1);
            }
            else{
                // if there is no assignment chosen, close the activity
                finish();
            }
            Log.d(TAG, "onCreate: " + deviceAddress);
            Log.d(TAG, "onCreate: " + assignment);
        }

        if (deviceAddress==null){
            Log.w(TAG, "no device found, maybe turn on mock?");
        }
        // load Data
        loadPreferenceData();

        int[] stepCheckerData = (int[])SharedPreferenceHelper.loadPreferenceData(this,subKey2,new TypeToken<int[]>(){}.getType());

        if (stepCheckerData != null){
            if (stepCheckerData.length != 8) stepCheckerData = null; //INVALID DATA
        }
        if (stepCheckerData != null){
            Log.i(TAG, "Existing saved stepCheckerData");
            stepChecker = new StepChecker(stepCheckerData);
        }
        else{
            stepChecker = new StepChecker(new int[]{50, 50, 50, 50, 50, 50, 50, 50});
        }
        // bind service
        bindService(new Intent(this, SerialService.class), this, Context.BIND_AUTO_CREATE);

        /*
         * UI
         */
        pressureImageView[0] = findViewById(R.id.p0);
        pressureImageView[1] = findViewById(R.id.p1);
        pressureImageView[2] = findViewById(R.id.p2);
        pressureImageView[3] = findViewById(R.id.p3);
        pressureImageView[4] = findViewById(R.id.p4);
        pressureImageView[5] = findViewById(R.id.p5);
        pressureImageView[6] = findViewById(R.id.p6);
        pressureImageView[7] = findViewById(R.id.p7);

        assignment_tv = findViewById(R.id.assignment_step_title);
        progressBar = findViewById(R.id.record_progressBar);
        pr = findViewById(R.id.perfect_text);
        g = findViewById(R.id.good_text);
        p = findViewById(R.id.poor_text);
        tv = findViewById(R.id.progressText);
        grade = findViewById(R.id.grade);

        // set the assignment title
        assignment_tv.setText(assignmentTitle);
        // set the initial progress text and bar
        progressBar.setMax(targetStep);
        progressBar.setProgress(currentStep);
        tv.setText(String.format("%d Steps", currentStep));
        pr.setText(Integer.toString(perfect));
        g.setText(Integer.toString(good));
        p.setText(Integer.toString(poor));
    }

    @Override
    public void onDestroy() {
        // disconnect
        if (connected != Connected.False)
            disconnect();
        // unbind service
        try {
            unbindService(this);
        } catch (Exception ignored) {}
        stopService(new Intent(this, SerialService.class));

        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (service != null)
            service.attach(this);
        else
            startService(new Intent(this, SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if (service != null && !this.isChangingConfigurations())
            service.detach();
        // stop the thread if it has started (they are exercising)
        if (inExercise) mockDataRunnable.isActive = false;
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop the thread if it has started (they are exercising)
        stopExercise();
        // update and save the data if there is an intent from assignment
        Log.d(TAG, "onPause: Save");
        savePreferenceData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;
            runOnUiThread(this::connect);
        }
        startExercise();
    }

    @Override
    public void onSerialRead(byte[] data) {
        if (inExercise && !Constants.IS_MOCKING)
            process_data(bluetoothDataHandler.receive(data));
    }

    private void loadPreferenceData(){
        Log.d(TAG, "loadPreferenceData: Load");
        mAssignment = (ArrayList<Assignment>) SharedPreferenceHelper.loadPreferenceData(this,"assignment",
                new TypeToken<ArrayList<Assignment>>(){}.getType());
    }

    private void savePreferenceData(){
        Assignment assignment = mAssignment.get(position);
        assignment.setCurrent(currentStep);
        assignment.setGood(good);
        assignment.setPerfect(perfect);
        assignment.setPoor(poor);
        mAssignment.set(position,assignment);
        Log.d(TAG, "savePreferenceData: "+mAssignment);
        SharedPreferenceHelper.savePreferenceData(this,"assignment",mAssignment);

    }
    private void startExercise() {
        if (Constants.IS_MOCKING) {
            mockDataRunnable = new MockDataRunnable();
            // create new thread
            mockDataThread = new Thread(mockDataRunnable);
            Log.d("MOCK", "Thread Id: " + mockDataThread.currentThread().getId());
            mockDataThread.start();
        }
        inExercise = true;
    }

    private void stopExercise(){
        if (Constants.IS_MOCKING) mockDataRunnable.isActive = false;
        inExercise = false;
    }

    private void updateProgress(String result) {
        // this is not in UI thread so need to use the runOnUIThread method
        runOnUiThread(() -> {
            grade.setText(result);
            switch (result){
                case "PERFECT":
                    grade.setTextColor(getResources().getColor(R.color.colorAlternateVariant));
                    perfect+=1;
                    Log.d(TAG, "updateProgress: p: " + perfect);
                    break;
                case "GOOD":
                    grade.setTextColor(getResources().getColor(R.color.colorSecondary));
                    good+=1;
                    Log.d(TAG, "updateProgress: g: " + good);
                    break;
                case "POOR":
                    grade.setTextColor(getResources().getColor(R.color.colorSecondaryVariant));
                    poor+=1;
                    Log.d(TAG, "updateProgress: pr: " + poor);
            }
            progressBar.setProgress(currentStep);
            tv.setText(String.format("%d Steps", currentStep));
            pr.setText(Integer.toString(perfect));
            g.setText(Integer.toString(good));
            p.setText(Integer.toString(poor));
        });
    }

    @Override
    protected void process_data(int[] pressureData) {
        Log.i("LiveActivity", Arrays.toString(pressureData));

        if (pressureData == null) {
            return;
        }

        // on the ground
        boolean nowInLowState = !isAllZero(pressureData);
        if (nowInLowState) {
            String result = stepChecker.checkStep(pressureData);
            if (!Constants.IS_MOCKING){
                write(result);
            }
            currentStep += 1;
            Log.d(TAG, "process_data: " + currentStep);
            updateProgress(result);
        }
        runOnUiThread(()->{
            updatePressureImageView(pressureData,nowInLowState);
        });
        // when it is done
        if (currentStep>=progressBar.getMax()){
            isDone = true;
            // save the data
            savePreferenceData();
            // change the UI
            tv.setText("Done!");
            // stop threading
            stopExercise();
            // call the summary page
            runOnUiThread(()->{
                Intent intent = new Intent(this,AssignmentSummaryActivity.class);
                intent.putExtra("assignment",mAssignment.get(position));
                startActivity(intent);
            });
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


    private void write(String result) {
        try{
            if (result =="PERFECT") {
                socket.write("g".getBytes());
            } else if (result == "GOOD") {
                socket.write("g".getBytes());
            } else if (result == "POOR") {
                socket.write("br".getBytes());
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }
}
