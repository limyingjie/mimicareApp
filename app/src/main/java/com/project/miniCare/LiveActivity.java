package com.project.miniCare;

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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.project.miniCare.Data.Assignment;
import com.project.miniCare.Utils.BluetoothDataHandler;
import com.project.miniCare.Utils.MockStepGenerator;
import com.project.miniCare.Services.SerialListener;
import com.project.miniCare.Services.SerialService;
import com.project.miniCare.Services.SerialSocket;
import com.project.miniCare.Utils.SharedPreferenceHelper;
import com.project.miniCare.Utils.StepChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class LiveActivity extends AppCompatActivity implements ServiceConnection, SerialListener {
    private static final String TAG = "LiveActivity";
    private enum Connected {False, Pending, True}

    private String deviceAddress;

    private TextView[] pressureTexts = new TextView[6];
    private TextView grade;
    private TextView tv;
    private ProgressBar pb;
    private Button button;

    private SerialSocket socket;
    private SerialService service;
    private boolean initialStart = true;
    private Connected connected = Connected.False;

    private boolean inExercise = false;
    private boolean isDone = false;
    private int currentStep,targetStep,position,poor,good,perfect;
    private boolean inLowState = false;
    private ArrayList<Assignment> mAssignment;

    // the correct step
    StepChecker stepChecker = new StepChecker(new int[]{0, 250, 500, 1000, 2000, 4000});

    BluetoothDataHandler bluetoothDataHandler = new BluetoothDataHandler();

    Thread mockDataThread;
    MockDataRunnable mockDataRunnable;
    boolean isMocking = true;

    public LiveActivity() {
    }

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_live);
        // default value
        deviceAddress = null;
        good = poor = perfect = 0;
        currentStep = 0;
        targetStep = 20;

        Intent intent = getIntent();
        if (intent!=null) {
            deviceAddress = intent.getStringExtra("device");
            Assignment assignment = intent.getParcelableExtra("assignment");
            if (assignment!=null){
                currentStep = assignment.getCurrent();
                targetStep = assignment.getTarget();
                perfect = assignment.getPerfect();
                good = assignment.getGood();
                poor = assignment.getPoor();
                position = intent.getIntExtra("position",-1);
            }
            Log.d(TAG, "onCreate: " + deviceAddress);
            Log.d(TAG, "onCreate: " + assignment);
        }

        // load Data
        loadPreferenceData();

        // bind service
        bindService(new Intent(this, SerialService.class), this, Context.BIND_AUTO_CREATE);

        /*
         * UI
         */
        pressureTexts[0] = findViewById(R.id.p0);
        pressureTexts[1] = findViewById(R.id.p1);
        pressureTexts[2] = findViewById(R.id.p2);
        pressureTexts[3] = findViewById(R.id.p3);
        pressureTexts[4] = findViewById(R.id.p4);
        pressureTexts[5] = findViewById(R.id.p5);

        // set the initial progress text and bar
        pb = findViewById(R.id.record_progressBar);
        tv = findViewById(R.id.progressText);
        grade = findViewById(R.id.grade);
        pb.setMax(targetStep);
        pb.setProgress(currentStep);
        tv.setText(String.format("%d steps", currentStep));
        button = findViewById(R.id.start_stopButton);
        button.setOnClickListener(v -> exercise());
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
        if (inExercise) mockDataRunnable.isActive = false;
        // update and save the data
        savePreferenceData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;
            runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        if (initialStart) {
            initialStart = false;
            runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    private void loadPreferenceData(){
        mAssignment = (ArrayList<Assignment>) SharedPreferenceHelper.loadPreferenceData(this,"assignment",
                new TypeToken<ArrayList<Assignment>>(){}.getType());
    }

    private void savePreferenceData(){
        if (isDone){
            mAssignment.remove(position);
        }
        else {
            Assignment assignment = mAssignment.get(position);
            assignment.setCurrent(currentStep);
            assignment.setGood(good);
            assignment.setPerfect(perfect);
            assignment.setPoor(poor);
            mAssignment.set(position,assignment);
        }
        SharedPreferenceHelper.savePreferenceData(this,"assignment",mAssignment);

    }
    private void startExercise() {
        if (isMocking) {
            mockDataRunnable = new MockDataRunnable();
            // create new thread
            mockDataThread = new Thread(mockDataRunnable);
            Log.d("MOCK", "Thread Id: " + mockDataThread.currentThread().getId());
            mockDataThread.start();
        }

        button.setText(R.string.stop_exercise);
        inExercise = true;
    }

    private void stopExercise(){
        if (isMocking) mockDataRunnable.isActive = false;
        button.setText(R.string.start_exercise);
        inExercise = false;
    }

    private void exercise(){
        if (isDone)return;
        if (inExercise){
            // we are exercising and we want to stop exercising
            stopExercise();
        }
        else{
            // we are not exercising and want to start exercising
            startExercise();
        }

    }
    private void updateProgress(String result) {
        // this is not in UI thread so need to use the runOnUIThread method
        runOnUiThread(() -> {
            tv.setText(String.format("%d steps", currentStep));
            pb.setProgress(currentStep);
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
        });
        if (currentStep >= pb.getMax()) {
            runOnUiThread(() -> {
                tv.setText("Congratulations! You've completed an exercise!");
                // empty the grade
                grade.setText("");
            });
            // stop the thread
            stopExercise();
            isDone = true;
        }
    }

    private void process_data(int[] pressureData) {
        Log.i("LiveActivity", Arrays.toString(pressureData));

        if (pressureData == null) {
            return;
        }

        // on the ground
        boolean nowInLowState = !isAllZero(pressureData);
        if (nowInLowState) {
            String result = stepChecker.checkStep(pressureData);
            currentStep += 1;
            Log.d(TAG, "process_data: " + currentStep);
            updateProgress(result);
        }
        updatePressureText(pressureData);
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
    /*
     * Serial + UI
     */
    private void connect() {
        Log.d("L SC", "connect");
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            String deviceName = device.getName() != null ? device.getName() : device.getAddress();
            status("connecting...");
            connected = Connected.Pending;
            socket = new SerialSocket();
            service.connect(this, "Connected to " + deviceName);
            socket.connect(this, service, device);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        Log.d("L SC", "disconnect");
        connected = Connected.False;
        service.disconnect();
        if (socket!=null){
            socket.disconnect();
        }
        socket = null;
    }

    private void status(String str) {
        /*SpannableStringBuilder spn = new SpannableStringBuilder(str+'\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);*/
        Log.d("L", str);
    }

    private void updatePressureText(int[] pressureData) {
        for (int i = 0; i < 6; i++) {
            int ScaledPressure = pressureData[i] / 200;
            pressureTexts[i].setText(String.format(Locale.US, "%d", ScaledPressure));
        }
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        process_data(bluetoothDataHandler.receive(data));
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
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
}