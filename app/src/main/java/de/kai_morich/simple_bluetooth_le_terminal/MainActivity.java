package de.kai_morich.simple_bluetooth_le_terminal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import de.kai_morich.simple_bluetooth_le_terminal.Beacon.AllBeaconFragment;
import de.kai_morich.simple_bluetooth_le_terminal.Beacon.BeaconFragment;
import de.kai_morich.simple_bluetooth_le_terminal.Beacon.BluetoothConnector;
import de.kai_morich.simple_bluetooth_le_terminal.Connection.ConnectionFragment;
import de.kai_morich.simple_bluetooth_le_terminal.Connection.TestConnectFragment;
import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;
import de.kai_morich.simple_bluetooth_le_terminal.Home.HomeFragment;
import de.kai_morich.simple_bluetooth_le_terminal.MainStation.BroadCastThread;
import de.kai_morich.simple_bluetooth_le_terminal.MainStation.MainStationConnector;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ButtonManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.FragManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ThreadManager;
import de.kai_morich.simple_bluetooth_le_terminal.Setting.SettingFragment;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.UtilityManager;


@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    private final HomeFragment home = new HomeFragment();
    private final BeaconFragment beacon = new BeaconFragment();
    private final ConnectionFragment connection = new ConnectionFragment();
    private final SettingFragment setting = new SettingFragment();

    public static Define.ScreenState state = Define.ScreenState.Home;

    private long backKeyPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenInit();
        btnInit();

        MainStationConnector.InitConnector(this);
        BluetoothConnector.InitBleConnector(this);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 세로 고정



    }

    public void ScreenInit() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck == PackageManager.PERMISSION_DENIED){ // 포그라운드 위치 권한 확인
            //위치 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        try{
            Thread.sleep(3000);

        } catch (Exception e) {
            Log.v("error", e.toString());
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Default
        FragManager.getInstance().addAndHideFragment(this, R.id.MainContainer, home, "Home", Define.ScreenState.Home, true);
        FragManager.getInstance().addAndHideFragment(this, R.id.MainContainer, beacon, "Beacon", Define.ScreenState.Beacon, true);
        FragManager.getInstance().addAndHideFragment(this, R.id.MainContainer, connection, "Connection", Define.ScreenState.Connection, true);
        FragManager.getInstance().addAndHideFragment(this, R.id.MainContainer, setting, "Settings", Define.ScreenState.Settings, true);

        FragManager.getInstance().showFragment(MainActivity.this, home, Define.ScreenState.Home);
        UtilityManager.getInstance().Init(this);

    }

    public void btnInit() {
        // Home
        ButtonManager.getInstance().setImgClickListener(findViewById(R.id.barHome), view -> {
            FragManager.getInstance().showFragment(MainActivity.this, home, Define.ScreenState.Home);
            UtilityManager.getInstance().showToastMessage(this.state.name());
        });

        // Beacon
        ButtonManager.getInstance().setImgClickListener(findViewById(R.id.barBeacon), view -> {
            FragManager.getInstance().showFragment(MainActivity.this, beacon, Define.ScreenState.Beacon);
            UtilityManager.getInstance().showToastMessage(this.state.name());
        });

        // connection
        ButtonManager.getInstance().setImgClickListener(findViewById(R.id.barConnection), view -> {
            FragManager.getInstance().showFragment(MainActivity.this, connection, Define.ScreenState.Connection);
            UtilityManager.getInstance().showToastMessage(this.state.name());
        });

        // Setting
        ButtonManager.getInstance().setImgClickListener(findViewById(R.id.barSettings), view -> {
            FragManager.getInstance().showFragment(MainActivity.this, setting, Define.ScreenState.Settings);
            UtilityManager.getInstance().showToastMessage(this.state.name());
        });

    }

    @Override
    public void onBackPressed() {

        if(state == Define.ScreenState.AllBeacon
                || state == Define.ScreenState.SpecificBeacon || state == Define.ScreenState.PrimaryBeacon)
        {
            FragManager.getInstance().showFragment(MainActivity.this, beacon, Define.ScreenState.Beacon);
            return;
        }
        else if(state == Define.ScreenState.TestConnect
                || state == Define.ScreenState.DataTransfer || state == Define.ScreenState.PositionCalculate)
        {
            FragManager.getInstance().showFragment(MainActivity.this, connection, Define.ScreenState.Connection);
            return;
        }


        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            UtilityManager.getInstance().showToastMessage("\'뒤로\' 버튼을 한번 더 누르면 종료됩니다.");
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
        }
    }

    public void setState(Define.ScreenState s)
    {
        this.state = s;
        HandleState();
    }

    @SuppressLint({"ResourceAsColor", "UseCompatLoadingForDrawables"})
    private void HandleState()
    {

        if(this.state == Define.ScreenState.Home)
        {
            ((ImageButton)findViewById(R.id.barHome)).setImageResource(R.drawable.ic_house_on);
            ((ImageButton)findViewById(R.id.barBeacon)).setImageResource(R.drawable.ic_wave_off);
            ((ImageButton)findViewById(R.id.barConnection)).setImageResource(R.drawable.ic_network_off);
            ((ImageButton)findViewById(R.id.barSettings)).setImageResource(R.drawable.ic_gear_off);

            ((TextView)findViewById(R.id.txt_house)).setTextColor(R.color.colorUiOn);
            ((TextView)findViewById(R.id.txt_beacon)).setTextColor(R.color.colorUiOff);
            ((TextView)findViewById(R.id.txt_connection)).setTextColor(R.color.colorUiOff);
            ((TextView)findViewById(R.id.txt_gear)).setTextColor(R.color.colorUiOff);

            ((TextView)findViewById(R.id.title_action_bar)).setText("Home");

            Button btn = findViewById(R.id.btn_action_bar);
            btn.setVisibility(View.INVISIBLE);
        }
        else if(this.state == Define.ScreenState.Beacon)
        {
            ((ImageButton)findViewById(R.id.barHome)).setImageResource(R.drawable.ic_house_off);
            ((ImageButton)findViewById(R.id.barBeacon)).setImageResource(R.drawable.ic_wave_on);
            ((ImageButton)findViewById(R.id.barConnection)).setImageResource(R.drawable.ic_network_off);
            ((ImageButton)findViewById(R.id.barSettings)).setImageResource(R.drawable.ic_gear_off);

            ((TextView)findViewById(R.id.txt_house)).setTextColor(R.color.colorUiOff);
            ((TextView)findViewById(R.id.txt_beacon)).setTextColor(R.color.colorUiOn);
            ((TextView)findViewById(R.id.txt_connection)).setTextColor(R.color.colorUiOff);
            ((TextView)findViewById(R.id.txt_gear)).setTextColor(R.color.colorUiOff);

            ((TextView)findViewById(R.id.title_action_bar)).setText("Beacon");

            Button btn = findViewById(R.id.btn_action_bar);
            btn.setVisibility(View.INVISIBLE);
        }
        else if(this.state == Define.ScreenState.Connection)
        {
            ((ImageButton)findViewById(R.id.barHome)).setImageResource(R.drawable.ic_house_off);
            ((ImageButton)findViewById(R.id.barBeacon)).setImageResource(R.drawable.ic_wave_off);
            ((ImageButton)findViewById(R.id.barConnection)).setImageResource(R.drawable.ic_network_on);
            ((ImageButton)findViewById(R.id.barSettings)).setImageResource(R.drawable.ic_gear_off);

            ((TextView)findViewById(R.id.txt_house)).setTextColor(R.color.colorUiOff);
            ((TextView)findViewById(R.id.txt_beacon)).setTextColor(R.color.colorUiOff);
            ((TextView)findViewById(R.id.txt_connection)).setTextColor(R.color.colorUiOn);
            ((TextView)findViewById(R.id.txt_gear)).setTextColor(R.color.colorUiOff);

            ((TextView)findViewById(R.id.title_action_bar)).setText("Connection");

            Button btn = findViewById(R.id.btn_action_bar);
            btn.setVisibility(View.INVISIBLE);
        }
        else if(this.state == Define.ScreenState.Settings)
        {
            ((ImageButton)findViewById(R.id.barHome)).setImageResource(R.drawable.ic_house_off);
            ((ImageButton)findViewById(R.id.barBeacon)).setImageResource(R.drawable.ic_wave_off);
            ((ImageButton)findViewById(R.id.barConnection)).setImageResource(R.drawable.ic_network_off);
            ((ImageButton)findViewById(R.id.barSettings)).setImageResource(R.drawable.ic_gear_on);

            ((TextView)findViewById(R.id.txt_house)).setTextColor(R.color.colorUiOff);
            ((TextView)findViewById(R.id.txt_beacon)).setTextColor(R.color.colorUiOff);
            ((TextView)findViewById(R.id.txt_connection)).setTextColor(R.color.colorUiOff);
            ((TextView)findViewById(R.id.txt_gear)).setTextColor(R.color.colorUiOn);

            ((TextView)findViewById(R.id.title_action_bar)).setText("Settings");

            Button btn = findViewById(R.id.btn_action_bar);
            btn.setText("Save");
            btn.setVisibility(View.VISIBLE);
            btn.setBackground(getDrawable(R.drawable.corner_round_green));

            // 리스너 등록
            ButtonManager.getInstance().setClickListener(btn, null);
            ButtonManager.getInstance().setClickListener(btn, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO : 화면의 값 저장
                }
            });
        }
        else if(this.state == Define.ScreenState.AllBeacon)
        {
            // ((TextView)findViewById(R.id.title_action_bar)).setText("All Beacon");

            Button btn = findViewById(R.id.btn_action_bar);
            btn.setText("Scan");
            btn.setVisibility(View.VISIBLE);
            btn.setBackground(getDrawable(R.drawable.corner_round_blue));
            
            // 리스너 등록
            ButtonManager.getInstance().setClickListener(btn, null);
            ButtonManager.getInstance().setClickListener(btn, view -> {

                if(btn.getText().equals("Scan"))
                {
                    AllBeaconFragment.Clear();
                    BluetoothConnector.startBeaconScan();

                    btn.setText("Stop");
                    btn.setBackground(getDrawable(R.drawable.corner_round_red));

                }
                else // stop
                {
                    BluetoothConnector.stopBeaconScan();

                    btn.setText("Scan");
                    btn.setBackground(getDrawable(R.drawable.corner_round_blue));


                }
            });

        }
        else if(this.state == Define.ScreenState.TestConnect)
        {
            ((TextView)findViewById(R.id.title_action_bar)).setText("Test Connect");

            Button btn = findViewById(R.id.btn_action_bar);
            btn.setText("CONNECTION");
            btn.setVisibility(View.VISIBLE);
            btn.setBackground(getDrawable(R.drawable.corner_round_blue));
            
            ButtonManager.getInstance().setClickListener(btn, null);
            ButtonManager.getInstance().setClickListener(btn, view -> {

                if(btn.getText().equals("CONNECTION")) // 연결 시도
                {
                    if(MainStationConnector.GetConnector() == null) {
                        Log.v("error", "null exception");
                        UtilityManager.getInstance().showToastMessage("메인스테이션 연결이 초기화되지 않았습니다.");
                        return;
                    }

                    TestConnectFragment testConnectFragment = FragManager.getInstance().getFragment("TestConnectFragment");

                    ((TextView)testConnectFragment.getActivity().findViewById(R.id.test_connect_receive)).setText("");
                    ((TextView)testConnectFragment.getActivity().findViewById(R.id.test_connect_message)).setText("");
                    ((TextView)testConnectFragment.getActivity().findViewById(R.id.test_connect_hex)).setText("");
                    ((TextView)testConnectFragment.getActivity().findViewById(R.id.test_connect_int)).setText("");
                    ((TextView)testConnectFragment.getActivity().findViewById(R.id.test_connect_float)).setText("");
                    testConnectFragment.getActivity().findViewById(R.id.test_connect_disconnected_container).setVisibility(View.INVISIBLE);


                    ThreadManager.startThread(new Thread(() -> MainStationConnector.GetConnector().Connect()));

                    btn.setText("Disconnection");
                    btn.setBackground(getDrawable(R.drawable.corner_round_red));
                }
                else // 연결 종료
                {
                    TestConnectFragment testConnectFragment = FragManager.getInstance().getFragment("TestConnectFragment");
                    testConnectFragment.getActivity().findViewById(R.id.test_connect_disconnected_container).setVisibility(View.VISIBLE);

                    // TODO : 연결 종료 처리
                    ThreadManager.startThread(new Thread(() -> MainStationConnector.GetConnector().Disconnect()));

                    btn.setText("CONNECTION");
                    btn.setBackground(getDrawable(R.drawable.corner_round_blue));
                }
            });

        }

    }


}
