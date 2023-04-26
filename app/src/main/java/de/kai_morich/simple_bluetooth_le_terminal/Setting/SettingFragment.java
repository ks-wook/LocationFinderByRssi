package de.kai_morich.simple_bluetooth_le_terminal.Setting;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.kai_morich.simple_bluetooth_le_terminal.MainStation.BroadCastThread;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ButtonManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ThreadManager;
import de.kai_morich.simple_bluetooth_le_terminal.R;

public class SettingFragment extends Fragment {



    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnInit();
    }

    public void btnInit() {

        ButtonManager.getInstance().setClickListener(getActivity().findViewById(R.id.settings_search_btn), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThreadManager.startThread(new BroadCastThread());
            }
        });
    }
}