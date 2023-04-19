package de.kai_morich.simple_bluetooth_le_terminal.Beacon;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import de.kai_morich.simple_bluetooth_le_terminal.Beacon.BeaconUtil.BeaconRoom;
import de.kai_morich.simple_bluetooth_le_terminal.Beacon.BeaconUtil.BeaconRoomManager;
import de.kai_morich.simple_bluetooth_le_terminal.Beacon.BeaconUtil.BluetoothBeacon;

public class BluetoothConnector {

    public enum ScanState { NONE, LE_SCAN, DISCOVERY, DISCOVERY_FINISHED }
    private AllBeaconFragment.ScanState scanState = AllBeaconFragment.ScanState.NONE;
    private static final long LE_SCAN_PERIOD = 10000; // similar to bluetoothAdapter.startDiscovery
    private final Handler leScanStopHandler = new Handler();
    private final BluetoothAdapter.LeScanCallback leScanCallback;
    private final Runnable leScanStopCallback;
    private final BroadcastReceiver discoveryBroadcastReceiver;
    private final IntentFilter discoveryIntentFilter;

    private final ArrayList<BluetoothDevice> listItems = new ArrayList<>();


    Activity _activity = null;
    boolean BeaconRegisterMode = false;

    private static int roomId = 0;

    static ScanCallback _scanCallback;

    static BluetoothAdapter bluetoothAdapter;
    static BluetoothLeScanner bluetoothLeScanner;

    // 현재는 로컬에서만 관리 -> 메인스테이션 DB 에서 패킷을 받아오는 식으로 변경 계획
    public static BluetoothConnector InitBleConnector(Activity activity)
    {
        // ------------------------- TEMP --------------------------
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();





        // bluetoothLeScanner.startScan(Arrays.asList(scanFilter), scanSettings, scanCallback);

        // ------------------------- TEMP --------------------------

        if(activity == null)
            return null;

        return new BluetoothConnector(activity);
    }

    public static void setCallBack(ScanCallback scanCallback)
    {
        _scanCallback = scanCallback;

    }

    public static void startBeaconScan()
    {
        if(_scanCallback == null)
            return;

        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"))
                .build();
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        bluetoothLeScanner.startScan(Arrays.asList(scanFilter), scanSettings, _scanCallback);
    }


    public static void stopBeaconScan()
    {
        if(_scanCallback == null)
            return;

        bluetoothLeScanner.stopScan(_scanCallback);
    }













    private BluetoothConnector(Activity activity)
    {
        this._activity = activity;

        if(activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        leScanCallback = (device, rssi, scanRecord) -> {
            if(device != null && _activity != null) {
                _activity.runOnUiThread(() -> { updateScan(device, rssi); });
            }
        };

        // 수신 리시버
        discoveryBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction()))
                {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    ScanResult result = intent.getParcelableExtra(BluetoothDevice.EXTRA_RSSI);
                    int rssi = result.getRssi();

                    if(device.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC && _activity != null) {
                        activity.runOnUiThread(() -> updateScan(device, rssi));
                    }
                }
                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                    scanState = AllBeaconFragment.ScanState.DISCOVERY_FINISHED; // don't cancel again
                    stopScan();
                }
            }
        };

        discoveryIntentFilter = new IntentFilter();
        discoveryIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        discoveryIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        leScanStopCallback = this::stopScan;

    }

    int _scanCount = 0;
    @SuppressLint("StaticFieldLeak") // AsyncTask needs reference to this fragment
    public void startScan() {

        if(scanState != AllBeaconFragment.ScanState.NONE)
            return;
        scanState = AllBeaconFragment.ScanState.LE_SCAN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            /*

            if (_mainActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                scanState = BluetoothFragment.ScanState.NONE;
                AlertDialog.Builder builder = new AlertDialog.Builder(_mainActivity);
                builder.setTitle(R.string.location_permission_title);
                builder.setMessage(R.string.location_permission_message);
                builder.setPositiveButton(android.R.string.ok,
                        (dialog, which) -> requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0));
                builder.show();
                return;
            }

            */


            LocationManager locationManager = (LocationManager) _activity.getSystemService(Context.LOCATION_SERVICE);
            boolean         locationEnabled = false;
            try {
                locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch(Exception ignored) {}
            try {
                locationEnabled |= locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ignored) {}
            if(!locationEnabled)
                scanState = AllBeaconFragment.ScanState.DISCOVERY;
            // Starting with Android 6.0 a bluetooth scan requires ACCESS_COARSE_LOCATION permission, but that's not all!
            // LESCAN also needs enabled 'location services', whereas DISCOVERY works without.
            // Most users think of GPS as 'location service', but it includes more, as we see here.
            // Instead of asking the user to enable something they consider unrelated,
            // we fall back to the older API that scans for bluetooth classic _and_ LE
            // sometimes the older API returns less results or slower
        }

        listItems.clear();

        if(scanState == AllBeaconFragment.ScanState.LE_SCAN) {
            leScanStopHandler.postDelayed(leScanStopCallback, LE_SCAN_PERIOD);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void[] params) {
                    bluetoothAdapter.startLeScan(null, leScanCallback);
                    return null;
                }
            }.execute(); // start async to prevent blocking UI, because startLeScan sometimes take some seconds
        }
        else
        {
            bluetoothAdapter.startDiscovery();
        }

    }

    int addRoomNum = 0;
    int _beaconScan = 0;
    private void updateScan(BluetoothDevice device, int rssi) {
        if(scanState == AllBeaconFragment.ScanState.NONE)
            return;
        if(!listItems.contains(device)) {
            listItems.add(device);
            // Collections.sort(listItems, DevicesFragment::compareTo);

            String name = device.getName();
            String adr = device.getAddress();
            
            // 비콘 정보
            if(Objects.equals(name, "BLE RSSI TEST"))
            {
                // 등록 모드인 경우
                if(BeaconRegisterMode)
                {
                    BeaconRoom room = BeaconRoomManager.getInstance().Find(addRoomNum);
                    room.AddBeacon(new BluetoothBeacon(adr));

                    Log.v("beacon_register", "Device name : " + device.getName());
                    Log.v("beacon_register", "Device address : " + device.getAddress());
                    Log.v("beacon_register", "Device rssi : " + Integer.toString(rssi));

                    if(room._beacons.size() == 3) // 비콘 3개 등록 완료 및 스캔 종료
                    {
                        BeaconRegisterMode = false;
                        stopScan();
                    }

                }
                else // rssi 측정일 때
                {
                    // 1. 서버로부터 위치 요청 패킷 수신
                    // 2. 패킷 수신 후 AP를 통해 어떤 방인지 구분
                    // 3. 해당 방에 등록된 비콘 정보를 이용해 해당 비콘의 rssi 값만을 저장

                    // 1, 2를 생략 -> TODO
                    BeaconRoom searchedRoom = BeaconRoomManager.getInstance().Find(addRoomNum);
                    BluetoothBeacon beacon = searchedRoom.FindBeacon(adr);
                    if(beacon == null)
                        Log.v("error", "beacon null");
                    assert beacon != null;
                    beacon.saveRssi(rssi);
                    _beaconScan++;

                    if(_beaconScan == 3)
                    {
                        stopScan();
                        startScan();
                    }

                    /*
                    if(!beacon.saveRssi(rssi))
                    {
                        // 10개의 값 측정 완료
                        stopScan();
                    }
                    else
                    {
                        stopScan();
                        startScan();
                    }
                     */
                }

            }

        }
    }

    public boolean RegisterBeacon()
    {
        // 임시 : 0번방이 존재 하지 않는다면
        BeaconRoom room = BeaconRoomManager.getInstance().Find(0);
        if(room == null)
            return false;

        // 원하는 방이 존재 할때
        // 비콘 등록 -> 비콘 등록이 완료된 후 메인 스테이션으로 정보 전송

        addRoomNum = 0;
        BeaconRegisterMode = true;
        startScan();

        /*
        try {


            Thread.sleep(3000);

            // BeaconRegisterMode = false;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

         */

        return true;
    }

    public void RegisterRoom()
    {
        BeaconRoomManager.getInstance().Add(new BeaconRoom(roomId++));
    }


    public void stopScan() {
        if(scanState == AllBeaconFragment.ScanState.NONE)
            return;

        switch(scanState)
        {
            case LE_SCAN:
                leScanStopHandler.removeCallbacks(leScanStopCallback);
                bluetoothAdapter.stopLeScan(leScanCallback);
                break;
            case DISCOVERY:
                bluetoothAdapter.cancelDiscovery();
                break;
            default:
                // already canceled
        }

        scanState = AllBeaconFragment.ScanState.NONE;

    }



}
