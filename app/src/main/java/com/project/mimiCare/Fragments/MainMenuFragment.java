package com.project.mimiCare.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.project.mimiCare.AssignmentSummaryActivity;
import com.project.mimiCare.MainActivity;
import com.project.mimiCare.R;
import com.project.mimiCare.RecordActivity;
import com.project.mimiCare.Constants;
import com.project.mimiCare.Utils.SharedPreferenceHelper;
import com.project.mimiCare.Utils.changeHandler;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainMenuFragment extends Fragment implements changeHandler {
    private static final String TAG = "MainMenuFragment";
    private static String name = "ES32Testbed";
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver bleDiscoveryBroadcastReceiver;
    private IntentFilter bleDiscoveryIntentFilter;
    private BluetoothDevice myDevice;

    private String[][] messages = {
            {"The weather\nlooks good~", "Let's go for\na walk shall we?"},
            {"Ahhh...\nI am starving...", "Shall we go\nget some food?"},
            {"Cheese Burger!!!","Control yourself\nplease!"}
    };

    private int minInterval = 10000;
    private int maxInterval = 15000;
    public Handler handler;
    private Random rng = new Random();
    private TextView speechBubbleLeft;
    private TextView speechBubbleRight;
    private TextView BLEstatus;
    private Button scan;
    private Button record_button;
    private Boolean inAnimation;
    private Boolean inScanning = false;

    private List<ScanFilter> scanFilter = Collections.singletonList(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(Constants.FOOT_BLE_SERVICE)).build());
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BLEstatus.setText("Device found");
            Log.i(TAG, "Device found: " + result.toString());
            myDevice = result.getDevice();
            super.onScanResult(callbackType, result);
        }
    };


    @Override
    public void onStartHandler() {
        handler = new Handler();
        handler.postDelayed(Speak, (long) rng.nextFloat()*(maxInterval-minInterval)+5000);
    }

    @Override
    public void onStopHandler() {
        handler.removeCallbacksAndMessages(null);
    }

    class ShowSpeech implements Runnable {
        private TextView speechBubble;
        private String message;
        public ShowSpeech(TextView speechBubble, String message){
            this.speechBubble = speechBubble;
            this.message = message;
        }
        public void run() {
            Log.d(TAG, "run: Show: "+Thread.currentThread().getId());
            speechBubble.setText(message);
            speechBubble.setVisibility(View.VISIBLE);
        }
    };
    class HideSpeech implements Runnable {
        private TextView speechBubble;
        public HideSpeech(TextView speechBubble){
            this.speechBubble = speechBubble;
        }
        public void run() {
            Log.d(TAG, "run: Hide: "+Thread.currentThread().getId());
            speechBubble.setVisibility(View.INVISIBLE);
        }
    };

    private Runnable Speak = new Runnable() {
        @Override
        public void run() {
            try {
                int i = rng.nextInt(messages.length);
                Handler mHandler = new Handler(Looper.getMainLooper());
                Log.d(TAG, "run: "+Thread.currentThread().getId());
                mHandler.postDelayed(new ShowSpeech(speechBubbleLeft, messages[i][0]), 0);
                mHandler.postDelayed(new ShowSpeech(speechBubbleRight, messages[i][1]), 1000);
                mHandler.postDelayed(new HideSpeech(speechBubbleLeft), 6000);
                mHandler.postDelayed(new HideSpeech(speechBubbleRight), 6000);
            } finally {
                handler.postDelayed(Speak, (long) rng.nextFloat()*(maxInterval-minInterval)+minInterval);
            }
        }
    };

    public MainMenuFragment() {

    }

    private void stopScan() {
        if (myDevice==null){
            BLEstatus.setText("No device is found");
        }
        scan.setVisibility(View.VISIBLE);
//        bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        inScanning = false;
    }

    private void startScan() {
        // check the build version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getText(R.string.location_permission_title));
                builder.setMessage(getText(R.string.location_permission_message));
                // requestPermission
                Log.d(TAG, "startScan: check");
                builder.setPositiveButton(android.R.string.ok,
                        (dialog, which) -> requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0));
                builder.show();
                return;
            }
            if (!bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "startScan: check");
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 0);
                return;
            }
        }
        Log.d(TAG, "startScan: done checking");
        BLEstatus.setText("Scanning");
//        bluetoothAdapter.startDiscovery();
        inScanning = true;
        bluetoothAdapter.getBluetoothLeScanner().startScan(scanFilter, new ScanSettings.Builder().build(), scanCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==0){
            startScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // ignore requestCode as there is only one in this fragment
        // run the start scan again after the permission has been given
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new Handler(Looper.getMainLooper()).postDelayed(this::startScan,1); // run after onResume to avoid wrong empty-text
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getText(R.string.location_denied_title));
            builder.setMessage(getText(R.string.location_denied_message));
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }
    }
    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        myDevice = null;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bleDiscoveryBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // remove all that are bluetooth classic
                    // probably still need BluetoothLEscanner to scan specific UUID
                    if(device.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                        Log.d(TAG, "onReceive: " + device);
                        // check the device name
                        if (device.getName() == name){
                            // do something here
                            // set Text as connecting then connected and saved the device address
                            BLEstatus.setText("Device found");
                            // start connecting
                            myDevice = device;
                        }
                    }
                }
                // when it is done (found something), stop the scan
                if(intent.getAction().equals((BluetoothAdapter.ACTION_DISCOVERY_FINISHED))) {
                    // check if the device is there
                    stopScan();
                }
            }
        };
        bleDiscoveryIntentFilter = new IntentFilter();
        // add the intent action
        bleDiscoveryIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bleDiscoveryIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: Called");
        if (!inAnimation){
            onStartHandler();
            inAnimation = true;
        }
        // register the receiver and set the emptyText
        getActivity().registerReceiver(bleDiscoveryBroadcastReceiver, bleDiscoveryIntentFilter);
        if(bluetoothAdapter == null || !getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Log.d(TAG, "onResume: <bluetooth LE not supported>");
            BLEstatus.setText("Bluetooth LE is not supported");}
        else if(!bluetoothAdapter.isEnabled()){
            Log.d(TAG, "onResume: <bluetooth is disabled>");
            BLEstatus.setText("Bluetoorh is disabled");}
        else
            Log.d(TAG, "onResume: <use SCAN to refresh devices>");
        super.onResume();

        // hide actionbar
        ((MainActivity)getActivity()).getSupportActionBar().hide();

        // check if the user has calibrated or not
        checkCalibration();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((MainActivity)getActivity()).getSupportActionBar().show();

        // check if the user has calibrated or not
        checkCalibration();
    }
    @Override
    public void onPause() {
        Log.d(TAG, "onPause: Called");
        // stop the handler
        if (inAnimation){
            onStopHandler();
            inAnimation = false;
        }
        // stop the scan and stop the bluetooth broadcast
        if (inScanning)
            stopScan();
        getActivity().unregisterReceiver(bleDiscoveryBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: Called");
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Called");
        super.onDestroy();
    }
    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mainmenu, container, false);
        speechBubbleLeft = view.findViewById(R.id.speechleft);
        speechBubbleRight = view.findViewById(R.id.speechright);
        record_button = view.findViewById(R.id.calibrate_button);
        BLEstatus = view.findViewById(R.id.BLEstatus_text);
        scan = view.findViewById(R.id.BLEscan_button);
        scan.setVisibility(View.GONE);

        startScan();
        scan.setOnClickListener((View v)->{
            startScan();
            scan.setVisibility(View.GONE);
        });
        record_button.setOnClickListener((View v)-> startActivity(new Intent(getActivity(), RecordActivity.class)));

        // send data to live fragment
        Intent intent = new Intent(getActivity(), AssignmentSummaryActivity.class);
        if (myDevice==null){
            Toast.makeText(getActivity(),"No device is found",Toast.LENGTH_SHORT).show();
        }
        else{
            intent.putExtra("device",myDevice.getAddress());
        }

        inAnimation = true;
        onStartHandler();
        return view;
    }

    private void checkCalibration(){
        // load data of the calibration
        int[] recordData = (int[]) SharedPreferenceHelper.loadPreferenceData(getActivity(),
                SharedPreferenceHelper.recordData,
                new TypeToken<int[]>() {}.getType());
        if (recordData==null){
            Log.d(TAG, "checkCalibration: " + "null");
            ((MainActivity)getActivity()).bottomNav.setVisibility(View.GONE);
        }
        else{
            Log.d(TAG, "checkCalibration: " + recordData.toString());
            ((MainActivity)getActivity()).bottomNav.setVisibility(View.VISIBLE);
        }
    }
}