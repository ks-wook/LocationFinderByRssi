package de.kai_morich.simple_bluetooth_le_terminal.Home;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;
import de.kai_morich.simple_bluetooth_le_terminal.MainStation.BroadCastThread;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ButtonManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.SessionManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ThreadManager;
import de.kai_morich.simple_bluetooth_le_terminal.R;
import de.kai_morich.simple_bluetooth_le_terminal.Service.MainStationService;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.UtilityManager;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HomeFragment extends Fragment {

    NotificationManager manager;
    Activity _mainActivity;
    Intent _serviceIntent;

    // test packet
    byte[] sync = {(byte) Define.PacketSize.SyncSize.getValue(),
            (byte)Define.PacketId.Sync.getValue(),
            (byte) 10, (byte) 20, (byte) 30}; // dummy message


    byte[] disc = {(byte) Define.PacketSize.DiscSize.getValue(),
            (byte) Define.PacketId.Disc.getValue() };

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
                view -> {
            if(SessionManager.CurrentSession() != null)
                SessionManager.CurrentSession().Send(sync);
            else
                UtilityManager.getInstance().showToastMessage("?????????????????? ????????? ???????????? ???????????????.");
        });

        ButtonManager.getInstance().setClickListener(getActivity().findViewById(R.id.ms_disconnect),
                view -> {
            if(SessionManager.CurrentSession() != null)
                SessionManager.CurrentSession().Send(disc);
            else
                UtilityManager.getInstance().showToastMessage("?????????????????? ????????? ???????????? ???????????????.");
        });
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

        // ?????????????????? ???????????? ?????? ?????????????????? ??????
        ButtonManager.getInstance().setClickListener(getActivity().findViewById(R.id.ms_service_stop), view -> {

            if(MainStationService.isOnService())
            {
                if(SessionManager.CurrentSession() != null) // ?????? ?????? ?????? ??????
                    SessionManager.CurrentSession().Send(disc);
                else
                    UtilityManager.getInstance().showToastMessage("?????????????????? ????????? ???????????? ???????????????.");

                getActivity().getApplicationContext().stopService(_serviceIntent);
                manager.cancel(1000);
            }
            else
                UtilityManager.getInstance().showToastMessage("???????????? ???????????? ????????????.");

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
