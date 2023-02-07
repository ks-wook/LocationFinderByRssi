package de.kai_morich.simple_bluetooth_le_terminal.Wifi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;
import java.util.Objects;

import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;
import de.kai_morich.simple_bluetooth_le_terminal.MainStation.SendThread;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ButtonManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.FragManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ViewSetManager;
import de.kai_morich.simple_bluetooth_le_terminal.R;
import de.kai_morich.simple_bluetooth_le_terminal.Util.TextUtility;

public class WiFiFragment extends ListFragment {

    boolean _isScanEnable = false;

    private WifiManager wifiManager;

    // View
    private View _header;

    // Fragment
    public WiFiTestFragment wiFiTestFragment = new WiFiTestFragment();

    // List
    ArrayList<ScanResult> _wifiScanResults;
    ArrayAdapter<ScanResult> _wifiListAdapter;
    ArrayList<WiFiAccessPoint> _registeredAPList;

    public SendThread mainStationThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        } catch (NullPointerException e) {
            Log.v("error", e.toString());
        }

        final IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(wifiScanReceiver, filter);

        wifiManager.startScan();
        _wifiScanResults = (ArrayList<ScanResult>) wifiManager.getScanResults();

        _wifiListAdapter = new ArrayAdapter<ScanResult>(getActivity(), 0, _wifiScanResults) {
            @NonNull
            @Override
            public View getView(int position, View view, @NonNull ViewGroup parent) {

                if (view == null)
                    view = getActivity().getLayoutInflater().inflate(R.layout.wifi_ap_item, parent, false);

                if(position >= _wifiScanResults.size())
                    return view;

                ScanResult result = _wifiScanResults.get(position);

                ViewSetManager.getInstance().setText((TextView) view.findViewById(R.id.itemSSID), result.SSID);
                ViewSetManager.getInstance().setText((TextView) view.findViewById(R.id.itemBSSID), result.BSSID);
                ViewSetManager.getInstance().setText((TextView) view.findViewById(R.id.itemRSSI), "rssi: " + result.level);

                return view;
            }
        };

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(null);
        _header = getActivity().getLayoutInflater().inflate(R.layout.wifi_ap_header, null, false);
        getListView().addHeaderView(_header, null, false);
        ButtonInit();
        setListAdapter(_wifiListAdapter);
        _registeredAPList = new ArrayList<>();
    }

    @SuppressLint("StaticFieldLeak") // AsyncTask needs reference to this fragment
    public void ButtonInit() {

        ButtonManager.getInstance().setClickListener(_header.findViewById(R.id.wifi_ScanBtn), view -> {
            ViewSetManager.getInstance().setText((TextView) _header.findViewById(R.id.ScanState), "scanning...");
            _isScanEnable = true;
            wifiManager.startScan();
        });

        ButtonManager.getInstance().setClickListener(_header.findViewById(R.id.mainStation_sendingToMS), view -> {
            mainStationThread = new SendThread();
            mainStationThread.start();
        });

        ButtonManager.getInstance().setClickListener(_header.findViewById(R.id.wifi_RegisteredAPList), view -> {

            if(_registeredAPList.size() != 3) {
                TextUtility.showToastMessage(Objects.requireNonNull(getActivity()), "ap는 3개가 선택되어야 합니다.");
                return;
            }

            // 등록된 AP list 전달 -> wifiTestFragment 이동
            Bundle bundle = new Bundle();
            bundle.putSerializable("RegisteredAPList", _registeredAPList);

            wiFiTestFragment.setArguments(bundle);

            FragManager.getInstance().addAndHideFragment(getActivity(), R.id.MainContainer, wiFiTestFragment, "wifiTest", Define.ScreenState.WifiTest, false);

        });

    }

    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(!_isScanEnable) {
                ViewSetManager.getInstance().setText((TextView) _header.findViewById(R.id.ScanState), "Done!!");
                return;
            }
            _isScanEnable = false;
            boolean success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                getWIFIScanResult();
                Log.e("wifi","scanSuccess !!!!!!!!!!!!!!!");
            } else {
                // scan failure handling
                Log.e("wifi","scanFailure ..............");
            }
            ViewSetManager.getInstance().setText((TextView) _header.findViewById(R.id.ScanState), "Done!!");
        }
    };

    public void getWIFIScanResult() {
        _wifiScanResults = (ArrayList<ScanResult>) wifiManager.getScanResults();
        _wifiListAdapter.notifyDataSetChanged();

        // -------------Debug-----------------
        for(int i = 0; i < _wifiScanResults.size(); i++) {
            ScanResult result = _wifiScanResults.get(i);
            if(result.frequency < 3000) {
                Log.d(". SSID : " + result.SSID,
                        result.level + ", " + result.BSSID);
            }
        }
        // -----------------Debug----------------

    }

    public void clearRegisteredList() {
        _registeredAPList.clear();
    }

    // 리스트 아이템을 클릭하여 registered ap list 에 추가
    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {

        if(_registeredAPList.size() >= 3) {
            TextUtility.showToastMessage(Objects.requireNonNull(getActivity()), "리스트에는 3개까지만 등록 가능합니다.");
            return;
        }

        ScanResult result = _wifiScanResults.get(position - 1);
        TextUtility.showToastMessage(Objects.requireNonNull(getActivity()), "SSID: " + result.SSID + "를 리스트에 추가 하였습니다.");

        _registeredAPList.add(new WiFiAccessPoint(result.SSID, result.BSSID));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(wifiScanReceiver);
    }
}