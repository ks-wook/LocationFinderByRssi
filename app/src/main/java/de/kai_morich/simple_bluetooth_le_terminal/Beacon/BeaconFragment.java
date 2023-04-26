package de.kai_morich.simple_bluetooth_le_terminal.Beacon;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;
import de.kai_morich.simple_bluetooth_le_terminal.MainActivity;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.ButtonManager;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.FragManager;
import de.kai_morich.simple_bluetooth_le_terminal.R;

@RequiresApi(api = Build.VERSION_CODES.O)
public class BeaconFragment extends Fragment {


    AllBeaconFragment allBeacon = new AllBeaconFragment();


    public BeaconFragment()
    {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnInit();
        ScreenInit();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_beacon, container, false);
    }

    public void btnInit()
    {

        ButtonManager.getInstance().setTxtClickListener(getActivity().findViewById(R.id.allBeacon), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragManager.getInstance().showFragment(getActivity(), allBeacon, Define.ScreenState.AllBeacon);
            }
        });

        ButtonManager.getInstance().setTxtClickListener(getActivity().findViewById(R.id.specificBeacon), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("test", "specific beacon");
            }
        });

        ButtonManager.getInstance().setTxtClickListener(getActivity().findViewById(R.id.primaryBeacon), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("test", "primary beacon");
            }
        });



    }

    public void ScreenInit()
    {
        // TODO 기타 2개의 다른 프래그먼트 추가 및 초기화
        FragManager.getInstance().addAndHideFragment(getActivity(), R.id.MainContainer, allBeacon, "AllBeacon", Define.ScreenState.AllBeacon, true);

    }


}