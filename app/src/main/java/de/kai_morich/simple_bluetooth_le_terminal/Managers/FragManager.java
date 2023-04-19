package de.kai_morich.simple_bluetooth_le_terminal.Managers;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;
import de.kai_morich.simple_bluetooth_le_terminal.MainActivity;

// 프래그먼트들을 관리하기 위한 클래스
// 프래그먼트들은 리스트형태로 관리되며 Define 클래스에 정의된 상태를 이용해 현재 상태를 업데이트 해야함
@RequiresApi(api = Build.VERSION_CODES.O)
public class FragManager {

    private static final FragManager _instance = new FragManager();
    private FragManager() { }

    ArrayList<Fragment> _fragments = new ArrayList<>();

    public static FragManager getInstance() { return _instance; }

    public boolean contains (Fragment frag) {
        return _fragments.contains(frag);
    }

    // 플래그 선언에 따라 프래그먼트를 추가하고 숨길지를 결정
    public void addAndHideFragment(Activity activity, int containerId, Fragment frag, String tag, Define.ScreenState s, boolean flag) {

        if(!_fragments.contains(frag))
        {
            ((MainActivity)activity).getSupportFragmentManager().beginTransaction().add(containerId, frag, tag).commit();
            _fragments.add(frag);
        }

        if(flag)
            hideFragment(activity, frag);
        else
            showFragment(activity, frag, s);
    }


    public void hideFragment(Activity activity, Fragment frag) {
        ((MainActivity)activity).getSupportFragmentManager().beginTransaction().hide(frag).commit();
    }

    public void showFragment(Activity activity, Fragment frag, Define.ScreenState s) {
        for(Fragment f : _fragments)
            hideFragment(activity, f);

        ((MainActivity)activity).getSupportFragmentManager().beginTransaction().show(frag).commit();
        ((MainActivity)activity).setState(s);

    }

    public void removeFragment(Activity activity, Fragment frag)
    {
        ((MainActivity)activity).getSupportFragmentManager().beginTransaction().remove(frag).commit();

        _fragments.remove(frag);
    }

}