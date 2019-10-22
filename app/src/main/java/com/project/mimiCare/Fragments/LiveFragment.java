package com.project.mimiCare.Fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.project.mimiCare.Constants;
import com.project.mimiCare.Data.AppState;
import com.project.mimiCare.MainActivity;
import com.project.mimiCare.R;
import com.project.mimiCare.Services.SerialListener;
import com.project.mimiCare.Services.SerialService;
import com.project.mimiCare.Services.SerialSocket;
import com.project.mimiCare.Utils.BluetoothDataHandler;
import com.project.mimiCare.Utils.MockStepGenerator;
import com.project.mimiCare.Utils.PressureColor;
import com.project.mimiCare.Utils.SharedPreferenceHelper;
import com.project.mimiCare.Utils.StepChecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class LiveFragment extends Fragment implements ServiceConnection, SerialListener {
    private static final String TAG = "LiveActivity";

    private enum Connected {False, Pending, True}

    private String deviceAddress;

    private ImageView[] pressureImageView = new ImageView[8];
    private TextView grade;
    private TextView tv,pr,g,p;

    private SerialSocket socket;
    private SerialService service;
    private boolean initialStart = true;
    private Connected connected = Connected.False;

    private boolean inExercise = false;
    private int currentStep,position,poor,good;
    private boolean inLowState = false;

    // the correct step
    private StepChecker stepChecker;

    BluetoothDataHandler bluetoothDataHandler = new BluetoothDataHandler();

    Thread mockDataThread;
    MockDataRunnable mockDataRunnable;

    public LiveFragment() {
    }

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // default value

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live,container,false);
        deviceAddress = null;
        good = poor = 0;
        currentStep = 0;

        // change to fragment argument
        try {
            deviceAddress = AppState.getBleDeviceAddress();
        }
        catch (Exception e){
            deviceAddress = null;
        }

        if (deviceAddress==null){
            Log.w(TAG, "no device found, maybe turn on mock?");
        }
        int[] stepCheckerData = (int[])SharedPreferenceHelper.loadPreferenceData(getActivity(),SharedPreferenceHelper.recordDataKey,new TypeToken<int[]>(){}.getType());

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
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);

        /*
         * UI
         */
        pressureImageView[0] = view.findViewById(R.id.p0);
        pressureImageView[1] = view.findViewById(R.id.p1);
        pressureImageView[2] = view.findViewById(R.id.p2);
        pressureImageView[3] = view.findViewById(R.id.p3);
        pressureImageView[4] = view.findViewById(R.id.p4);
        pressureImageView[5] = view.findViewById(R.id.p5);
        pressureImageView[6] = view.findViewById(R.id.p6);
        pressureImageView[7] = view.findViewById(R.id.p7);

        // set the initial progress text and bar
        g = view.findViewById(R.id.good_text);
        p = view.findViewById(R.id.poor_text);
        // set the initial progress text and bar
        tv = view.findViewById(R.id.progressText);
        grade = view.findViewById(R.id.grade);
        tv.setText(String.format("%d Steps", currentStep));
        g.setText(Integer.toString(good));
        p.setText(Integer.toString(poor));
        return view;
}

    @Override
    public void onDestroy() {
        // disconnect
        if (connected != Connected.False)
            disconnect();
        // unbind service
        try {
            getActivity().unbindService(this);
        } catch (Exception ignored) {}
        getActivity().stopService(new Intent(getActivity(), SerialService.class));

        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }



    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
        startExercise();
        ((MainActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onStop() {
        if (service != null && !getActivity().isChangingConfigurations())
            service.detach();
        // stop the thread if it has started (they are exercising)
        if (inExercise) mockDataRunnable.isActive = false;
        ((MainActivity)getActivity()).getSupportActionBar().show();
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        // stop the thread if it has started (they are exercising)
        stopExercise();
        // update and save the data if there is an intent from assignmentKey
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        if (initialStart) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
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
        getActivity().runOnUiThread(() -> {
            grade.setText(result);
            switch (result){
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
            tv.setText(String.format("%d Steps", currentStep));
            g.setText(Integer.toString(good));
            p.setText(Integer.toString(poor));
        });
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
            if (result != null){
                if (!Constants.IS_MOCKING){
                    write(result);
                    restart();
                }
                currentStep += 1;
                updateProgress(result);
            }
        }
        getActivity().runOnUiThread(()->{
            updatePressureImageView(pressureData,nowInLowState);
        });
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
            socket.connect(getActivity(), service, device);
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

    private void updatePressureImageView(int[] pressureData, Boolean in_low_state) {
        ArrayList<String> color_result = PressureColor.get_color(pressureData);
        for (int i=0; i < color_result.size(); i++){
            String color = color_result.get(i);
//            Log.d("hahahah", "updatePressureImageView: " + i);
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

        /***
        for (int i = 0; i < 6; i++) {
            //int ScaledPressure = pressureData[i] / 200;
            //pressureImageView[i].setText(String.format(Locale.US, "%d", ScaledPressure));
            if (in_low_state){
                switch (stepChecker.checkIndividual(i,pressureData[i])){
                    case "perfect":
                        pressureImageView[i].setImageResource(R.drawable.circle_green);
                        break;
                    case "good":
                        pressureImageView[i].setImageResource(R.drawable.circle_yellow);
                        break;
                    case "poor":
                        pressureImageView[i].setImageResource(R.drawable.circle_red);
                        break;
                }
            }
        }***/
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
        if (inExercise && !Constants.IS_MOCKING)
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

    public static Handler myHandler = new Handler();
    private static final int TIME_TO_WAIT = 2000;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try{
                Log.d(TAG, "run: stop the device");
                socket.write("0".getBytes());
            }
            catch (IOException e){
                Log.e(TAG, e.toString());
            }
        }
    };

    private void start(){
        myHandler.postDelayed(runnable,TIME_TO_WAIT);
    }

    private void stop(){
        myHandler.removeCallbacks(runnable);
    }

    private void restart(){
        stop();
        start();
    }
}