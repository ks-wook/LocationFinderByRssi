package de.kai_morich.simple_bluetooth_le_terminal.Wifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Objects;

import de.kai_morich.simple_bluetooth_le_terminal.Managers.WiFiTestResultManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ButtonManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ViewSetManager;
import de.kai_morich.simple_bluetooth_le_terminal.R;
import de.kai_morich.simple_bluetooth_le_terminal.Util.TextUtility;

public class WiFiTestFragment extends Fragment {

    public ListView MyRegisteredAP;
    public ListView MyRegisteredRoom;
    public ListView TestResultRoomList;

    private RegisteredAPAdapter registeredAPAdapter;
    private RegisteredRoomAdapter registeredRoomAdapter;
    public RankRoomAdapter rankRoomAdapter;

    private ArrayList<WiFiAccessPoint> MyRegisteredAPList;
    private ArrayList<Room> MyRegisteredRoomList = new ArrayList<>();
    public ArrayList<Room> RankRoomList = new ArrayList<>();

    private WifiManager wifiManager;
    private ArrayList<ScanResult> wifiScanResults;

    public Activity activity;

    public ConstraintLayout RoomListContainer;
    public ConstraintLayout APListContainer;
    public ConstraintLayout ValueSettingContainer;
    public ConstraintLayout TestResultContainer;
    public ConstraintLayout testResultContainer;

    boolean isScanEnable = false;
    boolean isRoomAdd = true;
    int fixIndex;

    boolean isResultScan = false;
    boolean isValueScan = false;

    static final int minEValue = 2;
    static final int maxEValue = 6;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(mWifiScanReceiver, filter);
        activity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MyRegisteredAPList = (ArrayList<WiFiAccessPoint>) getArguments().getSerializable("RegisteredAPList");
        registeredAPAdapter = new RegisteredAPAdapter(MyRegisteredAPList);
        registeredRoomAdapter = new RegisteredRoomAdapter(MyRegisteredRoomList);
        rankRoomAdapter = new RankRoomAdapter(RankRoomList);

        return inflater.inflate(R.layout.wifi_test_frag, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        MyRegisteredAP = (ListView) getActivity().findViewById(R.id.myRegisteredAP);
        MyRegisteredAP.setAdapter(registeredAPAdapter);
        MyRegisteredRoom = (ListView) getActivity().findViewById(R.id.MyRegisteredRoom);
        MyRegisteredRoom.setAdapter(registeredRoomAdapter);
        TestResultRoomList = (ListView) getActivity().findViewById(R.id.TestResultRoomList);
        TestResultRoomList.setAdapter(rankRoomAdapter);

        MyRegisteredAP.setVisibility(View.VISIBLE);
        MyRegisteredRoom.setVisibility(View.VISIBLE);

        this.ViewInit();
    }

    // 위젯 리스너 등록
    private void ViewInit() {

        Activity activity = getActivity();

        assert activity != null;
        RoomListContainer = activity.findViewById(R.id.RoomListContainer);
        APListContainer = activity.findViewById(R.id.APListContainer);
        ValueSettingContainer = activity.findViewById(R.id.ValueSettingContainer);
        TestResultContainer = activity.findViewById(R.id.TestResultContainer);

        ButtonManager.getInstance().setClickListener(activity.findViewById(R.id.GoToAPTest), view -> {
            isResultScan = true;
            isValueScan = false;

            // 버튼 비활성화
            activity.findViewById(R.id.GoToAPTest).setVisibility(View.INVISIBLE);

            // 테스트 시작 시, 리스트 뷰 들을 비활성화 -> 화면전환 없이 바로 결과 화면 출력
            RoomListContainer.setVisibility(View.INVISIBLE);
            APListContainer.setVisibility(View.INVISIBLE);

            isScanEnable = true;

            TestResultContainer.setVisibility(View.VISIBLE);
            TestResultRoomList.setVisibility(View.INVISIBLE);
            ViewSetManager.getInstance().setVisibility(activity.findViewById(R.id.ScanningState), View.VISIBLE);

            wifiManager.startScan();
        });

        ViewSetManager.getInstance().setText((TextView) activity.findViewById(R.id.AP1Name), "AP1:" + MyRegisteredAPList.get(0).getSSID());
        ViewSetManager.getInstance().setText((TextView) activity.findViewById(R.id.AP2Name), "AP1:" + MyRegisteredAPList.get(1).getSSID());
        ViewSetManager.getInstance().setText((TextView) activity.findViewById(R.id.AP3Name), "AP1:" + MyRegisteredAPList.get(2).getSSID());

        /*
        ------------AP1 data--------------
         */
        ViewSetManager.getInstance().setListener(activity.findViewById(R.id.MinRssiSeekbar1), new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ViewSetManager.getInstance().setText(activity.findViewById(R.id.MinRssiEdit1), i + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        ViewSetManager.getInstance().setListener(activity.findViewById(R.id.MaxRssiSeekbar1), new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ViewSetManager.getInstance().setText(activity.findViewById(R.id.MaxRssiEdit1), i + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        ButtonManager.getInstance().setClickListener(activity.findViewById(R.id.MinEnter1), view -> {
            String Value = (ViewSetManager.getInstance().getText((EditText) activity.findViewById(R.id.MinRssiEdit1)));
            if(!TextUtility.isNumeric(Value))
                return;

            int tmp = Integer.parseInt(Value);

            if(tmp < 0 || tmp > 100)
                return;

            ((SeekBar)activity.findViewById(R.id.MinRssiSeekbar1)).setProgress(tmp);
        });

        ButtonManager.getInstance().setClickListener(activity.findViewById(R.id.MaxEnter1), view -> {
            String Value = (ViewSetManager.getInstance().getText((EditText) activity.findViewById(R.id.MaxRssiEdit1)));
            if(!TextUtility.isNumeric(Value))
                return;

            int tmp = Integer.parseInt(Value);

            if(tmp < 0 || tmp > 100)
                return;

            ((SeekBar)activity.findViewById(R.id.MaxRssiSeekbar1)).setProgress(tmp);
        });

        /*
        ------------AP2 data--------------
         */
        ViewSetManager.getInstance().setListener(activity.findViewById(R.id.MinRssiSeekbar2), new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ViewSetManager.getInstance().setText(activity.findViewById(R.id.MinRssiEdit2), i + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        ViewSetManager.getInstance().setListener(activity.findViewById(R.id.MaxRssiSeekbar2), new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ViewSetManager.getInstance().setText(activity.findViewById(R.id.MaxRssiEdit2), i + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        ButtonManager.getInstance().setClickListener(activity.findViewById(R.id.MinEnter2), view -> {
            String Value = (ViewSetManager.getInstance().getText((EditText) activity.findViewById(R.id.MinRssiEdit2)));
            if(!TextUtility.isNumeric(Value))
                return;

            int tmp = Integer.parseInt(Value);

            if(tmp < 0 || tmp > 100)
                return;

            ((SeekBar)activity.findViewById(R.id.MinRssiSeekbar2)).setProgress(tmp);
        });

        ButtonManager.getInstance().setClickListener(activity.findViewById(R.id.MaxEnter2), view -> {
            String Value = (ViewSetManager.getInstance().getText((EditText) activity.findViewById(R.id.MaxRssiEdit2)));
            if(!TextUtility.isNumeric(Value))
                return;

            int tmp = Integer.parseInt(Value);

            if(tmp < 0 || tmp > 100)
                return;

            ((SeekBar)activity.findViewById(R.id.MinRssiSeekbar2)).setProgress(tmp);
        });

        /*
        -------------AP3 data-------------------
         */

        ViewSetManager.getInstance().setListener(activity.findViewById(R.id.MinRssiSeekbar3), new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ViewSetManager.getInstance().setText(activity.findViewById(R.id.MinRssiEdit3), i + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        ViewSetManager.getInstance().setListener(activity.findViewById(R.id.MaxRssiSeekbar3), new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ViewSetManager.getInstance().setText(activity.findViewById(R.id.MaxRssiEdit3), i + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        ButtonManager.getInstance().setClickListener(activity.findViewById(R.id.MinEnter3), view -> {
            String Value = (ViewSetManager.getInstance().getText((EditText) activity.findViewById(R.id.MinRssiEdit3)));
            if(!TextUtility.isNumeric(Value))
                return;

            int tmp = Integer.parseInt(Value);

            if(tmp < 0 || tmp > 100)
                return;

            ((SeekBar)activity.findViewById(R.id.MinRssiSeekbar3)).setProgress(tmp);
        });

        ButtonManager.getInstance().setClickListener(activity.findViewById(R.id.MaxEnter3), view -> {
            String Value = (ViewSetManager.getInstance().getText((EditText) activity.findViewById(R.id.MaxRssiEdit3)));
            if(!TextUtility.isNumeric(Value))
                return;

            int tmp = Integer.parseInt(Value);

            if(tmp < 0 || tmp > 100)
                return;

            ((SeekBar)activity.findViewById(R.id.MaxRssiSeekbar3)).setProgress(tmp);
        });

        // -----------방 추가----------------
        ButtonManager.getInstance().setImgClickListener((ImageButton) activity.findViewById(R.id.AddRoom), view -> {
            // 방 추가..
            // Todo
            RoomListContainer.setVisibility(View.INVISIBLE);
            APListContainer.setVisibility(View.INVISIBLE);
            ValueSettingContainer.setVisibility(View.VISIBLE);

            ((SeekBar)activity.findViewById(R.id.MinRssiSeekbar1)).setProgress(0);
            ((SeekBar)activity.findViewById(R.id.MinRssiSeekbar2)).setProgress(0);
            ((SeekBar)activity.findViewById(R.id.MinRssiSeekbar3)).setProgress(0);
            ((SeekBar)activity.findViewById(R.id.MaxRssiSeekbar1)).setProgress(0);
            ((SeekBar)activity.findViewById(R.id.MaxRssiSeekbar2)).setProgress(0);
            ((SeekBar)activity.findViewById(R.id.MaxRssiSeekbar3)).setProgress(0);

            ViewSetManager.getInstance().setText(activity.findViewById(R.id.MinRssiEdit1), null);
            ViewSetManager.getInstance().setText(activity.findViewById(R.id.MinRssiEdit2), null);
            ViewSetManager.getInstance().setText(activity.findViewById(R.id.MinRssiEdit3), null);
            ViewSetManager.getInstance().setText(activity.findViewById(R.id.MaxRssiEdit1), null);
            ViewSetManager.getInstance().setText(activity.findViewById(R.id.MaxRssiEdit2), null);
            ViewSetManager.getInstance().setText(activity.findViewById(R.id.MaxRssiEdit3), null);

        });

        ButtonManager.getInstance().setClickListener(activity.findViewById(R.id.ValueConfirm), view -> {

            // 방 이름 및 각 ap 최대 최소 값들을 가져와서 셋팅...
            String name = ViewSetManager.getInstance().getText(activity.findViewById(R.id.RoomName));

            if(isRoomAdd) { // 방을 추가 하는 경우
                Room room = new Room(name,
                        Integer.parseInt(ViewSetManager.getInstance().getText((EditText) activity.findViewById(R.id.MinRssiEdit1))),
                        Integer.parseInt(ViewSetManager.getInstance().getText((EditText) activity.findViewById(R.id.MaxRssiEdit1))),
                        Integer.parseInt(ViewSetManager.getInstance().getText((EditText) activity.findViewById(R.id.MinRssiEdit2))),
                        Integer.parseInt(ViewSetManager.getInstance().getText((EditText) activity.findViewById(R.id.MaxRssiEdit2))),
                        Integer.parseInt(ViewSetManager.getInstance().getText((EditText) activity.findViewById(R.id.MinRssiEdit3))),
                        Integer.parseInt(ViewSetManager.getInstance().getText((EditText) activity.findViewById(R.id.MaxRssiEdit3))));

                registeredRoomAdapter.addItem(room);
                ViewSetManager.getInstance().setVisibility(activity.findViewById(R.id.IsRoomNull), View.INVISIBLE);
            }

            else { // 원래 있던 방의 값을 수정하는 경우
                isRoomAdd = true;
                Room temp = MyRegisteredRoomList.get(fixIndex);
                temp.setName(name);
                temp.setAp1Min(Integer.parseInt(ViewSetManager.getInstance().getText(activity.findViewById(R.id.MinRssiEdit1))));
                temp.setAp1Max(Integer.parseInt(ViewSetManager.getInstance().getText(activity.findViewById(R.id.MaxRssiEdit1))));
                temp.setAp2Min(Integer.parseInt(ViewSetManager.getInstance().getText(activity.findViewById(R.id.MinRssiEdit2))));
                temp.setAp2Max(Integer.parseInt(ViewSetManager.getInstance().getText(activity.findViewById(R.id.MaxRssiEdit2))));
                temp.setAp3Min(Integer.parseInt(ViewSetManager.getInstance().getText(activity.findViewById(R.id.MinRssiEdit3))));
                temp.setAp3Max(Integer.parseInt(ViewSetManager.getInstance().getText(activity.findViewById(R.id.MaxRssiEdit3))));
            }

            registeredRoomAdapter.notifyDataSetChanged();
            MyRegisteredRoom.setAdapter(registeredRoomAdapter);

            ValueSettingContainer.setVisibility(View.INVISIBLE);
            APListContainer.setVisibility(View.VISIBLE);
            RoomListContainer.setVisibility(View.VISIBLE);
        });

        if(registeredRoomAdapter.isEmpty())
            ViewSetManager.getInstance().setVisibility(activity.findViewById(R.id.IsRoomNull), View.VISIBLE);
        else
            ViewSetManager.getInstance().setVisibility(activity.findViewById(R.id.IsRoomNull), View.INVISIBLE);

        // 방 아이템을 터치 시 rssi 최소 최대 값 재설정
        MyRegisteredRoom.setOnItemClickListener((adapterView, view, i, l) -> {

            // 설정 창으로 넘어가서 최소 최대 rssi 값 설정
            fixIndex = i;

            int min1 = Integer.parseInt(ViewSetManager.getInstance().getText((TextView) activity.findViewById(R.id.AP1Min)));
            int max1 = Integer.parseInt(ViewSetManager.getInstance().getText((TextView) activity.findViewById(R.id.AP1Max)));

            int min2 = Integer.parseInt(ViewSetManager.getInstance().getText((TextView) activity.findViewById(R.id.AP2Min)));
            int max2 = Integer.parseInt(ViewSetManager.getInstance().getText((TextView) activity.findViewById(R.id.AP2Max)));

            int min3 = Integer.parseInt(ViewSetManager.getInstance().getText((TextView) activity.findViewById(R.id.AP3Min)));
            int max3 = Integer.parseInt(ViewSetManager.getInstance().getText((TextView) activity.findViewById(R.id.AP3Max)));

            ((SeekBar)activity.findViewById(R.id.MinRssiSeekbar1)).setProgress(min1);
            ViewSetManager.getInstance().setText(activity.findViewById(R.id.MinRssiEdit1), String.valueOf(min1));
            ((SeekBar)activity.findViewById(R.id.MaxRssiSeekbar1)).setProgress(max1);
            ViewSetManager.getInstance().setText(activity.findViewById(R.id.MaxRssiEdit1), String.valueOf(max1));

            ((SeekBar)activity.findViewById(R.id.MinRssiSeekbar2)).setProgress(min2);
            ViewSetManager.getInstance().setText(activity.findViewById(R.id.MinRssiEdit2), String.valueOf(min2));
            ((SeekBar)activity.findViewById(R.id.MaxRssiSeekbar2)).setProgress(max2);
            ViewSetManager.getInstance().setText(activity.findViewById(R.id.MaxRssiEdit1), String.valueOf(max2));

            ((SeekBar)activity.findViewById(R.id.MinRssiSeekbar3)).setProgress(min3);
            ViewSetManager.getInstance().setText(activity.findViewById(R.id.MinRssiEdit3), String.valueOf(min3));
            ((SeekBar)activity.findViewById(R.id.MaxRssiSeekbar3)).setProgress(max3);
            ViewSetManager.getInstance().setText(activity.findViewById(R.id.MaxRssiEdit1), String.valueOf(max3));

            RoomListContainer.setVisibility(View.INVISIBLE);
            APListContainer.setVisibility(View.INVISIBLE);
            ValueSettingContainer.setVisibility(View.VISIBLE);

            // 값 수정
            isRoomAdd = false;
        });

        // 결과 화면 btn
        ButtonManager.getInstance().setClickListener(activity.findViewById(R.id.GoToAPTestAgain), view -> {

            // 버튼 활성화
            ViewSetManager.getInstance().setVisibility(activity.findViewById(R.id.GoToAPTest), View.VISIBLE);

            RoomListContainer.setVisibility(View.VISIBLE);
            APListContainer.setVisibility(View.VISIBLE);

            isScanEnable = true;

            TestResultContainer.setVisibility(View.INVISIBLE);
            TestResultRoomList.setVisibility(View.VISIBLE);
            ViewSetManager.getInstance().setVisibility(activity.findViewById(R.id.ScanningState), View.INVISIBLE);
        });

        // 현재위치 스캔
        ButtonManager.getInstance().setClickListener(activity.findViewById(R.id.CurrentLocationScan), view -> {
            isValueScan = true;
            isResultScan = false;
            isScanEnable = true;
            wifiManager.startScan();
        });
    }

    // Registered AP List adapter
    public static class RegisteredAPAdapter extends BaseAdapter {
        ArrayList<WiFiAccessPoint> items;

        public RegisteredAPAdapter(ArrayList<WiFiAccessPoint> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(WiFiAccessPoint item) {
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // ListView 아이템에 대한 뷰 객체 생성
        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            final Context context = viewGroup.getContext();
            final WiFiAccessPoint apData = items.get(position);

            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.wifi_test_registerd_ap, viewGroup, false);
            } else {
                View view = new View(context);
                view = (View) convertView;
            }

            TextView SSID = (TextView) convertView.findViewById(R.id.SSID);
            TextView MacAddress = (TextView) convertView.findViewById(R.id.BSSID);

            SSID.setText(apData.getSSID());
            MacAddress.setText(apData.getMacAddress());

            return convertView;
        }

    }

    public static class RegisteredRoomAdapter extends BaseAdapter {
        ArrayList<Room> items;

        public RegisteredRoomAdapter(ArrayList<Room> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(Room item) {
            items.add(item);
        }

        public boolean isEmpty() {
            return items.isEmpty();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // RoomList 에 대한 아이템 생성
        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            final Context context = viewGroup.getContext();
            final Room room = items.get(position);

            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.wifi_test_room_item, viewGroup, false);
            } else {
                View view = new View(context);
                view = (View) convertView;
            }

            ViewSetManager.getInstance().setText((TextView) convertView.findViewById(R.id.Room), room.getName());

            ViewSetManager.getInstance().setText((TextView) convertView.findViewById(R.id.AP1Min), String.valueOf(room.getAp1Min()));
            ViewSetManager.getInstance().setText((TextView) convertView.findViewById(R.id.AP1Max), String.valueOf(room.getAp1Max()));

            ViewSetManager.getInstance().setText((TextView) convertView.findViewById(R.id.AP2Min), String.valueOf(room.getAp2Min()));
            ViewSetManager.getInstance().setText((TextView) convertView.findViewById(R.id.AP2Max), String.valueOf(room.getAp2Max()));

            ViewSetManager.getInstance().setText((TextView) convertView.findViewById(R.id.AP3Min), String.valueOf(room.getAp3Min()));
            ViewSetManager.getInstance().setText((TextView) convertView.findViewById(R.id.AP3Max), String.valueOf(room.getAp3Max()));

            return convertView;
        }
    }

    public static class RankRoomAdapter extends BaseAdapter {
        ArrayList<Room> items;

        public RankRoomAdapter(ArrayList<Room> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(Room item) {
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // RoomList 에 대한 아이템 생성
        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            final Context context = viewGroup.getContext();
            final Room temp = items.get(position);

            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.wifi_test_result_item, viewGroup, false);
            } else {
                View view = new View(context);
                view = (View) convertView;
            }

            TextView roomRank = (TextView) convertView.findViewById(R.id.RoomRank);
            roomRank.setText(temp.rank);

            TextView roomName = (TextView) convertView.findViewById(R.id.RankRoomName);
            roomName.setText(temp.getName());

            TextView rankRoomError = (TextView) convertView.findViewById(R.id.RankRoomError);
            rankRoomError.setText(String.valueOf(temp.errorSum));

            return convertView;
        }
    }

    private BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {

            if(MyRegisteredAPList.isEmpty()) {
                TextUtility.showToastMessage(activity, "방을 추가해주세요");
                return;
            }

            // 테스트 시작할 때만 스캔
            if(!isScanEnable) {
                return;
            }

            isScanEnable = false;

            // 테스트 결과 화면
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            // 스캔은 현재 위치 스캔인지, 최종 결과를 위한 스캔인지 구분
            if (success) {
                if(isResultScan) {
                    isResultScan = false;
                    getWIFIScanResultAndSetRank();
                    Log.e("wifi","scanSuccess !!!!!!!!!!!!!!!");
                    TextUtility.showToastMessage(Objects.requireNonNull(activity), "스캔이 완료되었습니다.");
                    ViewSetManager.getInstance().setVisibility(activity.findViewById(R.id.ScanningState), View.INVISIBLE);
                    ViewSetManager.getInstance().setVisibility(TestResultRoomList, View.VISIBLE);
                }
                else if(isValueScan) {
                    isValueScan = false;
                    getWIFIScanResultAndSetValue();
                    Log.e("wifi","scanSuccess !!!!!!!!!!!!!!!");
                    TextUtility.showToastMessage(activity, "값을 현재 위치 기준으로 설정하였습니다.");
                }
            }
            else {
                // scan failure handling
                TextUtility.showToastMessage(activity, "scan failed...");
                Log.e("wifi","scanFailure ..............");
            }
        }
    };

    public void getWIFIScanResultAndSetRank() {

        String Mac1 = MyRegisteredAPList.get(0).getMacAddress();
        String Mac2 = MyRegisteredAPList.get(1).getMacAddress();
        String Mac3 = MyRegisteredAPList.get(2).getMacAddress();

        int rssi1 = -100;
        int rssi2 = -100;
        int rssi3 = -100;

        wifiScanResults = (ArrayList<ScanResult>) wifiManager.getScanResults();

        if(!RankRoomList.isEmpty())
            RankRoomList.clear();

        RankRoomList.addAll(MyRegisteredRoomList);
        for(int i = 0; i < wifiScanResults.size(); i++) {
            ScanResult result = wifiScanResults.get(i);
            if(wifiScanResults.get(i).BSSID.equals(Mac1)) {
                rssi1 = result.level;
            }
            else if(wifiScanResults.get(i).BSSID.equals(Mac2)) {
                rssi2 = result.level;

            }
            else if(wifiScanResults.get(i).BSSID.equals(Mac3)) {
                rssi3 = result.level;
            }
        }

        WiFiTestResultManager.sortRoomList(RankRoomList, Math.abs(rssi1), Math.abs(rssi2), Math.abs(rssi3));
        WiFiTestResultManager.setRank(RankRoomList);
        rankRoomAdapter.notifyDataSetChanged();

        // Test
        Log.v("rssiTest", rssi1 + "/" + rssi2 + "/" + rssi3);
        for(int i = 0; i < RankRoomList.size(); i++){
            Log.v("rssiTest", i + ": " +
                    RankRoomList.get(i).error1 + ", " + RankRoomList.get(i).error2 + ", " + RankRoomList.get(i).error3 + "/" + RankRoomList.get(i).errorSum);
        }
    }

    public void getWIFIScanResultAndSetValue() {

        String Mac1 = MyRegisteredAPList.get(0).getMacAddress();
        String Mac2 = MyRegisteredAPList.get(1).getMacAddress();
        String Mac3 = MyRegisteredAPList.get(2).getMacAddress();

        int rssi1 = -100;
        int rssi2 = -100;
        int rssi3 = -100;

        wifiScanResults = (ArrayList<ScanResult>) wifiManager.getScanResults();

        for(int i = 0; i < wifiScanResults.size(); i++) {
            ScanResult result = wifiScanResults.get(i);
            if(wifiScanResults.get(i).BSSID.equals(Mac1)) {
                rssi1 = result.level;
            }
            else if(wifiScanResults.get(i).BSSID.equals(Mac2)) {
                rssi2 = result.level;
            }
            else if(wifiScanResults.get(i).BSSID.equals(Mac3)) {
                rssi3 = result.level;
            }
        }

        rssi1 = Math.abs(rssi1);
        ((SeekBar)activity.findViewById(R.id.MinRssiSeekbar1)).setProgress(rssi1 - minEValue);
        ((SeekBar)activity.findViewById(R.id.MaxRssiSeekbar1)).setProgress(rssi1 + maxEValue);

        rssi2 = Math.abs(rssi2);
        ((SeekBar)activity.findViewById(R.id.MinRssiSeekbar2)).setProgress(rssi2 - minEValue);
        ((SeekBar)activity.findViewById(R.id.MaxRssiSeekbar2)).setProgress(rssi2 + maxEValue);

        rssi3 = Math.abs(rssi3);
        ((SeekBar)activity.findViewById(R.id.MinRssiSeekbar3)).setProgress(rssi3 - minEValue);
        ((SeekBar)activity.findViewById(R.id.MaxRssiSeekbar3)).setProgress(rssi3 + maxEValue);
    }
}
