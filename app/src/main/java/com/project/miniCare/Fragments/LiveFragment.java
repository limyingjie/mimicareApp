package com.project.miniCare.Fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.project.miniCare.Utils.BluetoothDataHandler;
import com.project.miniCare.Utils.MockStepGenerator;
import com.project.miniCare.R;
import com.project.miniCare.Services.SerialListener;
import com.project.miniCare.Services.SerialService;
import com.project.miniCare.Services.SerialSocket;
import com.project.miniCare.Utils.StepChecker;

import java.util.Arrays;
import java.util.Locale;

public class LiveFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected {False, Pending, True}

    private String deviceAddress;

    private TextView[] pressureTexts = new TextView[6];

    private SerialSocket socket;
    private SerialService service;
    private boolean initialStart = true;
    private Connected connected = Connected.False;

    private boolean inExercise = false;
    private int numSteps = 0;
    private boolean inLowState = false;

    StepChecker stepChecker = new StepChecker(new int[]{0, 200, 400, 600, 800, 1000});

    BluetoothDataHandler bluetoothDataHandler = new BluetoothDataHandler();

    Thread mockDataThread;
    MockDataRunnable mockDataRunnable;
    boolean isMocking = true;

    public LiveFragment() {
    }

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
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
    public void onStop() {
        if (service != null && !getActivity().isChangingConfigurations())
            service.detach();
        if (isMocking) mockDataRunnable.isActive = false;
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try {
            getActivity().unbindService(this);
        } catch (Exception ignored) {
        }
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        if (initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live, container, false);
        pressureTexts[0] = view.findViewById(R.id.p0);
        pressureTexts[1] = view.findViewById(R.id.p1);
        pressureTexts[2] = view.findViewById(R.id.p2);
        pressureTexts[3] = view.findViewById(R.id.p3);
        pressureTexts[4] = view.findViewById(R.id.p4);
        pressureTexts[5] = view.findViewById(R.id.p5);

        if (isMocking) {
            mockDataRunnable = new MockDataRunnable();
            mockDataThread = new Thread(mockDataRunnable);
            mockDataThread.start();
        }

        Button button = view.findViewById(R.id.startButton);
        button.setOnClickListener(v -> startExercise());
        return view;
    }

    private void startExercise() {
        inExercise = true;
        numSteps = 0;
        inLowState = false;
        updateProgress();
    }

    private void updateProgress() {
        TextView tv = getView().findViewById(R.id.progressText);
        getActivity().runOnUiThread(() -> tv.setText(String.format("%d steps", numSteps)));
        ProgressBar pb = getView().findViewById(R.id.progressBar);
        pb.setProgress(numSteps);
        if (numSteps >= pb.getMax()) {
            getActivity().runOnUiThread(() -> tv.setText("Congratulations! You've completed an exercise!"));

        }
    }

    private void process_data(int[] pressureData) {
        Log.i("LiveFragment", Arrays.toString(pressureData));

        if (pressureData == null) {
            return;
        }

        boolean nowInLowState = pressureData[4] > 200;
        if (nowInLowState) {
            String result = stepChecker.checkStep(pressureData);
            Log.i("LiveFragment", result);
        }

        if (inExercise) {
            if (inLowState && !nowInLowState) {
                numSteps += 1;
                updateProgress();
            }
            inLowState = nowInLowState;
        }

        updatePressureText(pressureData);
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
            socket.connect(getContext(), service, device);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        Log.d("L SC", "disconnect");
        connected = Connected.False;
        service.disconnect();
        socket.disconnect();
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