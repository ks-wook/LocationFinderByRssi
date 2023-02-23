package de.kai_morich.simple_bluetooth_le_terminal;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;
import de.kai_morich.simple_bluetooth_le_terminal.Home.HomeFragment;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ButtonManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.FragManager;
import de.kai_morich.simple_bluetooth_le_terminal.Setting.SettingFragment;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.UtilityManager;
import de.kai_morich.simple_bluetooth_le_terminal.Wifi.WiFiFragment;

public class MainActivity extends AppCompatActivity {

    private final HomeFragment home = new HomeFragment();
    private final WiFiFragment wifi = new WiFiFragment();
    private final SettingFragment setting = new SettingFragment();

    public Define.ScreenState state = Define.ScreenState.Home;

    private long backKeyPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenInit();
        btnInit();
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
        FragManager.getInstance().addAndHideFragment(this, R.id.MainContainer, home, "Home", Define.ScreenState.Home, false);
        FragManager.getInstance().addAndHideFragment(this, R.id.MainContainer, wifi, "Wifi", Define.ScreenState.Wifi, true);
        FragManager.getInstance().addAndHideFragment(this, R.id.MainContainer, setting, "Setting", Define.ScreenState.Setting, true);
        // bluetooth
        // TODO

        UtilityManager.getInstance().Init(this);
    }

    public void btnInit() {
        ButtonManager.getInstance().setClickListener(findViewById(R.id.barHome), view -> {
            FragManager.getInstance().showFragment(MainActivity.this, home, Define.ScreenState.Home);
            UtilityManager.getInstance().showToastMessage(this.state.name());
        });

        ButtonManager.getInstance().setClickListener(findViewById(R.id.barWifi), view -> {
            if(FragManager.getInstance().contains(wifi.wiFiTestFragment)) {
                FragManager.getInstance().showFragment(MainActivity.this, wifi.wiFiTestFragment, Define.ScreenState.WifiTest);
                return;
            }
            FragManager.getInstance().showFragment(MainActivity.this, wifi, Define.ScreenState.Wifi);
            UtilityManager.getInstance().showToastMessage(this.state.name());
        });

        ButtonManager.getInstance().setClickListener(findViewById(R.id.barSetting), view -> {
            FragManager.getInstance().showFragment(MainActivity.this, setting, Define.ScreenState.Setting);
            UtilityManager.getInstance().showToastMessage(this.state.name());
        });

        // ----------------Debug---------------------
        // Bluetooth
        // TODO
        ButtonManager.getInstance().setClickListener(findViewById(R.id.barBluetooth), view -> {
            FragManager.getInstance().showFragment(MainActivity.this, setting, Define.ScreenState.Setting);
            UtilityManager.getInstance().showToastMessage("개발중...");
        });
        // ------------------------------------------

    }

    @Override
    public void onBackPressed() {

        if(this.state == Define.ScreenState.WifiTest) {
            FragManager.getInstance().showFragment(this, wifi, Define.ScreenState.Wifi);
            wifi.clearRegisteredList();
            FragManager.getInstance().removeFragment(this, wifi.wiFiTestFragment);
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

    public void setState(Define.ScreenState s) {
        this.state = s;
    }

}
