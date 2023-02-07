package de.kai_morich.simple_bluetooth_le_terminal.Home;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import de.kai_morich.simple_bluetooth_le_terminal.MainStation.BroadCastThread;
import de.kai_morich.simple_bluetooth_le_terminal.MainStation.DisConnectThread;
import de.kai_morich.simple_bluetooth_le_terminal.MainStation.SendThread;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ButtonManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.MainStationManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ThreadManager;
import de.kai_morich.simple_bluetooth_le_terminal.R;
import de.kai_morich.simple_bluetooth_le_terminal.Util.TextUtility;


public class HomeFragment extends Fragment {

    private static final int BROADCAST_PORT = 5000;

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
    }

    public void btnInit() {

        ButtonManager.getInstance().setClickListener(getActivity().findViewById(R.id.ms_search), view -> {
            BroadCastThread broadCastThread = new BroadCastThread(MainStationManager.getInstance());
            broadCastThread.start();
        });

        ButtonManager.getInstance().setClickListener(getActivity().findViewById(R.id.ms_send),
                view -> ThreadManager.startThread(new SendThread()));

        ButtonManager.getInstance().setClickListener(getActivity().findViewById(R.id.ms_disconnect),
                view -> ThreadManager.startThread(new DisConnectThread()));
    }
}