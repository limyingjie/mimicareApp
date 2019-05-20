package de.kai_morich.simple_bluetooth_terminal;

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

import java.util.Locale;

public class LiveFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }

    private String deviceAddress;

    private StringBuffer buffer = new StringBuffer();
    private TextView[] pressureTexts = new TextView[6];

    private SerialSocket socket;
    private SerialService service;
    private boolean initialStart = true;
    private Connected connected = Connected.False;

    private boolean inExercise = false;
    private int numSteps = 0;
    private boolean inLowState = false;

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
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service !=null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        if(initialStart && isResumed()) {
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

        Button button = (Button) view.findViewById(R.id.startButton);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startExercise();
            }
        });
        return view;
    }

    private void startExercise(){
        inExercise = true;
        numSteps = 0;
        inLowState = false;
        updateProgress();
    }

    private void updateProgress(){
        TextView tv = getView().findViewById(R.id.progressText);
        tv.setText(String.format("%d steps", numSteps));
        ProgressBar pb = getView().findViewById(R.id.progressBar);
        pb.setProgress(numSteps);
        if (numSteps >= pb.getMax()){
            tv.setText("Congratulations! You've completed an exercise!");
        }
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

    private void process_data(int[] pressureData){
        if (inExercise){
            boolean nowInLowState = pressureData[4]>200;
            if (inLowState && !nowInLowState){
                numSteps += 1;
                updateProgress();
            }
            inLowState = nowInLowState;
        }

        for (int i = 0; i < 6; i++) {
            int ScaledPressure = pressureData[i]/200;
            pressureTexts[i].setText(String.format(Locale.US, "%d", ScaledPressure));
        }
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

    private void receive(byte[] data) {
        String strData = new String(data);
        buffer.append(strData.replaceAll("\\s+",""));

        String[] lines = buffer.toString().split("#");
        if (lines.length == 0){
            return;
        }

        int[] lineData;

        //Check if last line is complete. Print if is.
        lineData = get_line_data(lines[lines.length-1]);
        if (lineData != null){
            process_data(lineData);
            buffer.setLength(0);
            Log.d("pos61", (String.format(Locale.US, "%d < %s", lineData[5], lines[lines.length-1])));
            return;
        }

        //Check if 2nd last line exists is complete. Print if is. (if it exists and is not complete, it must also be first line. so ignore.)
        if (lines.length < 2) {
            return;
        }
        lineData = get_line_data(lines[lines.length-2]);
        if (lineData != null){
            process_data(lineData);
            buffer.setLength(0);
            Log.d("pos62", (String.format(Locale.US, "%d < %s", lineData[5], lines[lines.length-2])));
            return;
        }
    }

    private void status(String str) {
        /*SpannableStringBuilder spn = new SpannableStringBuilder(str+'\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);*/
        Log.d("L", str);
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
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

}