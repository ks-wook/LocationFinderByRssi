package de.kai_morich.simple_bluetooth_le_terminal.Beacon;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.kai_morich.simple_bluetooth_le_terminal.Managers.ButtonManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.UtilityManager;
import de.kai_morich.simple_bluetooth_le_terminal.R;
import de.kai_morich.simple_bluetooth_le_terminal.TerminalFragment;

// 블루투스 관련 프래그먼트
public class AllBeaconFragment extends ListFragment {

    public enum ScanState { NONE, LE_SCAN, DISCOVERY, DISCOVERY_FINISHED }
    private ScanState scanState = ScanState.NONE;

    public static int itemCount = 0;

    public static ArrayAdapter<ScanResult> _listAdapter;
    public static HashMap<String, Integer> _itemPosition = new HashMap<String, Integer>();
    public static HashMap<String, ArrayList<Integer>> _rssiValues = new HashMap<String, ArrayList<Integer>>();
    public static ArrayList<ScanResult> _listItems = new ArrayList<ScanResult>();

    public AllBeaconFragment() {
        BluetoothConnector.setCallBack(new BeaconScanCallBack());
    }

    public static void Clear()
    {
        _itemPosition.clear();
        _rssiValues.clear();
        _listItems.clear();

        itemCount = 0;
        _listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _listAdapter = new ArrayAdapter<ScanResult>(getActivity(), 0, _listItems) {
            @NonNull
            @Override
            public View getView(int position, View view, @NonNull ViewGroup parent) {

                ScanResult result = _listItems.get(position);
                ScanRecord scanRecord = result.getScanRecord();

                byte[] rssiByte;
                byte[] namespaceBytes;
                byte[] instanceBytes;
                byte[] stateBytes;
                byte[] batteryBytes;

                int rssiAt1M = 0;
                String namespace = "";
                String instance = "";
                String state = "";
                String battery = "";
                String rssi = "";

                // 바이트 배열 파싱
                if (scanRecord != null)
                {
                    byte[] serviceData = scanRecord.getServiceData(ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"));
                    if (serviceData != null && serviceData.length > 2)
                    {
                        int frameType = serviceData[0];
                        if (frameType == 0x00)
                        {
                            // UID 프레임
                            rssiByte = Arrays.copyOfRange(serviceData, 1, 2);
                            namespaceBytes = Arrays.copyOfRange(serviceData, 2, 12);
                            instanceBytes = Arrays.copyOfRange(serviceData, 12, 18);
                            stateBytes = Arrays.copyOfRange(serviceData, 18, 19);
                            batteryBytes = Arrays.copyOfRange(serviceData, 19, 20);

                            // String conversion
                            rssiAt1M = (byte) Integer.parseInt(bytesToHex(rssiByte), 16);
                            namespace = bytesToHex(namespaceBytes);
                            instance = bytesToHex(instanceBytes);
                            state = bytesToHex(stateBytes);
                            battery = Integer.toString(Integer.parseInt(bytesToHex(batteryBytes), 16));
                            rssi = _rssiValues.get(instance).toString();

                            Log.d(TAG, "UID Frame: " + rssiAt1M + " " + namespace + " " + instance + " " + battery);
                        }
                    }
                }

                if (view == null)
                    view = getActivity().getLayoutInflater().inflate(R.layout.device_list_item, parent, false);

                ImageView stateImg = view.findViewById(R.id.img_state_wave);

                TextView uiId = view.findViewById(R.id.all_beacon_id);

                TextView uiState = view.findViewById(R.id.all_beacon_state);
                TextView uiBattery = view.findViewById(R.id.all_beacon_bat);

                TextView uiRssiAt1m = view.findViewById(R.id.all_beacon_rssi_at_1m);
                TextView uiRssi = view.findViewById(R.id.all_beacon_rssi);


                // TEST
                uiId.setText("ID: " + instance);
                if(state.equals("00")) // Normal
                {
                    uiState.setText("State : Normal");
                    stateImg.setBackground(getActivity().getDrawable(R.drawable.corner_round_green));
                }
                else if(state.equals("10"))
                {
                    uiState.setText("State : Triggered");
                    stateImg.setBackground(getActivity().getDrawable(R.drawable.corner_round_yellow));
                }
                else if(state.equals("20"))
                {
                    uiState.setText("State : Low Battery");
                    stateImg.setBackground(getActivity().getDrawable(R.drawable.corner_round_red));
                }
                else if(state.equals("30"))
                {
                    uiState.setText("State : Default");
                    stateImg.setBackground(getActivity().getDrawable(R.drawable.corner_round_black));
                }


                uiBattery.setText("Battery : " + battery + "%");
                uiRssiAt1m.setText("RSSI at 1m: " + rssiAt1M);
                uiRssi.setText("RSSI: " + rssi);

                return view;
            }
        };
    }

    public void modifyItem(int position)
    {

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(_listAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            // new Handler(Looper.getMainLooper()).postDelayed(this::startScan,1); // run after onResume to avoid wrong empty-text
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getText(R.string.location_denied_title));
            builder.setMessage(getText(R.string.location_denied_message));
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        BluetoothConnector.stopBeaconScan();
        UtilityManager.getInstance().showToastMessage("아이템 터치");
    }


    // Scan CallBack
    public static class BeaconScanCallBack extends ScanCallback
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {

            ScanRecord scanRecord = result.getScanRecord();

            byte[] rssiAt1MByte;
            byte[] namespaceBytes;
            byte[] instanceBytes;
            byte[] stateBytes;
            byte[] batteryBytes;

            int rssi = result.getRssi();
            int rssiAt1M = 0;
            String namespace = "";
            String instance = "";
            String state = "";
            String battery = "";

            if (scanRecord != null)
            {
                byte[] serviceData = scanRecord.getServiceData(ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"));
                if (serviceData != null && serviceData.length > 2)
                {
                    int frameType = serviceData[0];
                    if (frameType == 0x00)
                    {
                        // UID 프레임
                        rssiAt1MByte = Arrays.copyOfRange(serviceData, 1, 2);
                        namespaceBytes = Arrays.copyOfRange(serviceData, 2, 12);
                        instanceBytes = Arrays.copyOfRange(serviceData, 12, 18);
                        stateBytes = Arrays.copyOfRange(serviceData, 18, 19);
                        batteryBytes = Arrays.copyOfRange(serviceData, 19, 20);

                        // String conversion
                        rssiAt1M = (byte) Integer.parseInt(bytesToHex(rssiAt1MByte), 16);
                        namespace = bytesToHex(namespaceBytes);
                        instance = bytesToHex(instanceBytes);
                        state = bytesToHex(stateBytes);
                        battery = Integer.toString(Integer.parseInt(bytesToHex(batteryBytes), 16));

                        if(_rssiValues.get(instance) != null) // 이미 값이 있는 경우
                        {
                            ArrayList<Integer> rssiList = _rssiValues.get(instance);
                            if(rssiList.size() < 10)
                            {
                                rssiList.add(rssi);

                                _listAdapter.remove(_listAdapter.getItem(_itemPosition.get(instance)));
                                _listAdapter.insert(result, _itemPosition.get(instance));
                                _listAdapter.notifyDataSetChanged();
                            }


                            Log.v("test", rssiList.toString());
                            return;
                        }
                        else // 처음 스캔된 디바이스
                        {
                            _itemPosition.put(instance, itemCount++);
                            _rssiValues.put(instance, new ArrayList<>());
                            ArrayList<Integer> rssiList = _rssiValues.get(instance);
                            rssiList.add(rssi);
                            _listItems.add(result);
                            _listAdapter.notifyDataSetChanged();
                        }
                    }

                }

            }

        }

    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
