package com.project.mimiCare;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.project.mimiCare.Data.AppState;
import com.project.mimiCare.Fragments.LiveFragment;
import com.project.mimiCare.Services.SerialListener;
import com.project.mimiCare.Services.SerialService;
import com.project.mimiCare.Services.SerialSocket;
import com.project.mimiCare.Utils.BluetoothDataHandler;
import com.project.mimiCare.Utils.MockStepGenerator;
import com.project.mimiCare.Utils.PressureColor;

import java.util.ArrayList;

public abstract class WalkingActivity extends AppCompatActivity implements ServiceConnection, SerialListener {
    private static final String TAG = "";

    protected enum Connected {False, Pending, True}

    protected ImageView[] pressureImageView = new ImageView[8];

    protected MockDataRunnable mockDataRunnable;

    protected abstract void process_data(int[] data);

    protected SerialSocket socket;
    protected SerialService service;
    protected boolean initialStart = true;
    protected Connected connected = Connected.False;

    protected BluetoothDataHandler bluetoothDataHandler = new BluetoothDataHandler();

    protected boolean inExercise = false;

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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

    protected void connect() {
        Log.d("L SC", "connect");
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(AppState.getBleDeviceAddress());
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

    protected void status(String str) {
        /*SpannableStringBuilder spn = new SpannableStringBuilder(str+'\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);*/
        Log.d("L", str);
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

    protected void disconnect() {
        Log.d("L SC", "disconnect");
        connected = Connected.False;
        service.disconnect();
        if (socket!=null){
            socket.disconnect();
        }
        socket = null;
    }
}
