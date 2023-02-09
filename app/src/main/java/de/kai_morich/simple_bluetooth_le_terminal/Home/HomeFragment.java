package de.kai_morich.simple_bluetooth_le_terminal.Home;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import de.kai_morich.simple_bluetooth_le_terminal.MainActivity;
import de.kai_morich.simple_bluetooth_le_terminal.MainStation.BroadCastThread;
import de.kai_morich.simple_bluetooth_le_terminal.MainStation.DisConnectThread;
import de.kai_morich.simple_bluetooth_le_terminal.MainStation.SendThread;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ButtonManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.MainStationManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ThreadManager;
import de.kai_morich.simple_bluetooth_le_terminal.R;
import de.kai_morich.simple_bluetooth_le_terminal.Service.MainStationService;
import de.kai_morich.simple_bluetooth_le_terminal.Util.TextUtility;


public class HomeFragment extends Fragment {

    private static final int BROADCAST_PORT = 5000;
    NotificationManager manager;
    Activity _mainActivity;
    Intent _serviceIntent;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_frag, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnInit();
        serviceInit();
    }

    public void btnInit() {

        _mainActivity = getActivity();

        ButtonManager.getInstance().setClickListener(getActivity().findViewById(R.id.ms_search),
                view -> ThreadManager.startThread(new BroadCastThread()));

        ButtonManager.getInstance().setClickListener(getActivity().findViewById(R.id.ms_send),
                view -> ThreadManager.startThread(new SendThread()));

        ButtonManager.getInstance().setClickListener(getActivity().findViewById(R.id.ms_disconnect),
                view -> ThreadManager.startThread(new DisConnectThread()));
    }

    public void serviceInit() {

        createNotificationChannel();
        _serviceIntent = new Intent(_mainActivity.getApplicationContext(), MainStationService.class);

        ButtonManager.getInstance().setClickListener(getActivity().findViewById(R.id.ms_service_start), view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Context context = getActivity().getApplicationContext();
                context.startService(_serviceIntent);
            }
        });

        // 메인스테이션 서비스가 현재 실행중인지를 확인
        ButtonManager.getInstance().setClickListener(getActivity().findViewById(R.id.ms_service_stop), view -> {

            SharedPreferences pref = _mainActivity.getSharedPreferences("isRunning", _mainActivity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isRunning", false);
            editor.commit();

            if(!pref.getBoolean("isRunning", false))
            {
                ThreadManager.startThread(new DisConnectThread()); // 연결 종료 패킷 전송
                getActivity().getApplicationContext().stopService(_serviceIntent);
                manager.cancel(1000);
            }
            else
                TextUtility.showToastMessage(_mainActivity, "실행중인 서비스가 없습니다.");

        });
    }

    void createNotificationChannel() {
        manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "1000",
                    "ServiceNotification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            manager.createNotificationChannel(channel);
        }
    }
}
