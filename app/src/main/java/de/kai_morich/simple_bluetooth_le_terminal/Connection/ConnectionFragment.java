package de.kai_morich.simple_bluetooth_le_terminal.Connection;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ButtonManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.FragManager;
import de.kai_morich.simple_bluetooth_le_terminal.R;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ConnectionFragment extends Fragment {

    TestConnectFragment testConnect = new TestConnectFragment();

    public ConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_connection, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ScreenInit();
        btnInit();
    }

    public void btnInit() {

        ButtonManager.getInstance().setTxtClickListener(getActivity().findViewById(R.id.testConnect), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragManager.getInstance().showFragment(getActivity(), testConnect, Define.ScreenState.TestConnect);
            }
        });

        ButtonManager.getInstance().setTxtClickListener(getActivity().findViewById(R.id.dataTransfer), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("connection", "dataTransfer");
            }
        });

        ButtonManager.getInstance().setTxtClickListener(getActivity().findViewById(R.id.positionCalculate), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("connection", "positionCalculate");
            }
        });

    }

    public void ScreenInit()
    {
        // TODO 기타 2개의 다른 프래그먼트 추가 및 초기화
        FragManager.getInstance().addAndHideFragment(getActivity(), R.id.MainContainer, testConnect, "AllBeacon", Define.ScreenState.AllBeacon, true);

    }
}