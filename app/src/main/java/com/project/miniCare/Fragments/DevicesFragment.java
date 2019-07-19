package com.project.miniCare.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.project.miniCare.MainActivity;
import com.project.miniCare.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * show list of BLE devices
 */
public class DevicesFragment extends ListFragment {

    private Menu menu;
    private final BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> listItems = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> listAdapter;

    private BroadcastReceiver bleDiscoveryBroadcastReceiver;
    private IntentFilter bleDiscoveryIntentFilter;

    // Need to understand this fragment in detail in order to be able to manage its lifecycle
    public DevicesFragment() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // handle the event (setup bluetooth receiver)
        bleDiscoveryBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // remove all that are bluetooth classic
                    // probably still need BluetoothLEscanner to scan specific UUID
                    if(device.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                        getActivity().runOnUiThread(() -> updateScan(device));
                    }
                }
                // when it is done (found something), stop the scan
                if(intent.getAction().equals((BluetoothAdapter.ACTION_DISCOVERY_FINISHED))) {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // setting the arrayAdapter with listItems as the content
        listAdapter = new ArrayAdapter<BluetoothDevice>(getActivity(), 0, listItems) {
            @Override
            public View getView(int position, View view, ViewGroup parent) {
                BluetoothDevice device = listItems.get(position);
                if (view == null)
                    view = getActivity().getLayoutInflater().inflate(R.layout.device_list_item, parent, false);
                TextView text1 = view.findViewById(R.id.text1);
                TextView text2 = view.findViewById(R.id.text2);
                if(device.getName() == null || device.getName().isEmpty())
                    text1.setText("<unnamed>");
                else
                    text1.setText(device.getName());

                // this is not addressed?
                text2.setText(device.getAddress());
                return view;
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // change the toolbar title
        ((MainActivity)getActivity()).changeTitle(R.string.app_name);
        // empty the listAdapter
        setListAdapter(null);
        View header = getActivity().getLayoutInflater().inflate(R.layout.device_list_header, null, false);
        getListView().addHeaderView(header, null, false);
        setEmptyText("initializing...");
        ((TextView) getListView().getEmptyView()).setTextSize(18);
        // set the listAdapter
        setListAdapter(listAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_devices, menu);
        this.menu = menu;

        // check if the system supports bluetooth
        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
            menu.findItem(R.id.bt_settings).setEnabled(false);

        // check if the system supports bluetoothLE and set the menu
        if(bluetoothAdapter==null || !getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            menu.findItem(R.id.ble_scan).setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // register the receiver and set the emptyText
        getActivity().registerReceiver(bleDiscoveryBroadcastReceiver, bleDiscoveryIntentFilter);
        if(bluetoothAdapter == null || !getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            setEmptyText("<bluetooth LE not supported>");
        else if(!bluetoothAdapter.isEnabled())
            setEmptyText("<bluetooth is disabled>");
        else
            setEmptyText("<use SCAN to refresh devices>");
    }

    @Override
    public void onPause() {
        super.onPause();

        // stop the scan and stop the bluetooth broadcast
        stopScan();
        getActivity().unregisterReceiver(bleDiscoveryBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.ble_scan) {
            startScan();
            return true;
        } else if (id == R.id.ble_scan_stop) {
            stopScan();
            return true;
        } else if (id == R.id.bt_settings) {
            Intent intent = new Intent();
            // access android bluetooth setting
            intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intent);
            return true;
        } else {
            // what is this for?
            return super.onOptionsItemSelected(item);
        }
    }

    private void startScan() {
        // check the build version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getText(R.string.location_permission_title));
                builder.setMessage(getText(R.string.location_permission_message));
                // requestPermission
                builder.setPositiveButton(android.R.string.ok,
                        (dialog, which) -> requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0));
                builder.show();
                return;
            }
            if (!bluetoothAdapter.isEnabled()){
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);
                return;
            }
        }

        // empty the list
        listItems.clear();
        listAdapter.notifyDataSetChanged();
        setEmptyText("<scanning...>");
        // change the menu visibility
        menu.findItem(R.id.ble_scan).setVisible(false);
        menu.findItem(R.id.ble_scan_stop).setVisible(true);
        bluetoothAdapter.startDiscovery();
        //  BluetoothLeScanner.startScan(...) would return more details, but that's not needed here
        //  why?
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

    private void updateScan(BluetoothDevice device) {
        if(listItems.indexOf(device) < 0) {
            listItems.add(device);
            Collections.sort(listItems, DevicesFragment::compareTo);
            listAdapter.notifyDataSetChanged();
        }
    }

    private void stopScan() {
        setEmptyText("<no bluetooth devices found>");
        if(menu != null) {
            menu.findItem(R.id.ble_scan).setVisible(true);
            menu.findItem(R.id.ble_scan_stop).setVisible(false);
        }
        bluetoothAdapter.cancelDiscovery();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        stopScan();
        BluetoothDevice device = listItems.get(position-1);
        // hack
        ((MainActivity) getActivity()).device = device.getAddress();
        Bundle args = new Bundle();
        args.putString("device", device.getAddress());
        Fragment fragment = new LiveFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "menu").addToBackStack(null).commit();
    }

    /**
     * sort by name, then address. sort named devices first
     */
    static int compareTo(BluetoothDevice a, BluetoothDevice b) {
        boolean aValid = a.getName()!=null && !a.getName().isEmpty();
        boolean bValid = b.getName()!=null && !b.getName().isEmpty();
        if(aValid && bValid) {
            int ret = a.getName().compareTo(b.getName());
            if (ret != 0) return ret;
            return a.getAddress().compareTo(b.getAddress());
        }
        if(aValid) return -1;
        if(bValid) return +1;
        return a.getAddress().compareTo(b.getAddress());
    }
}
