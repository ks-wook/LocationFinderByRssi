package de.kai_morich.simple_bluetooth_le_terminal.Managers;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;
import de.kai_morich.simple_bluetooth_le_terminal.MainActivity;

public class FragManager {

    private static final FragManager _instance = new FragManager();
    private FragManager() { }

    ArrayList<Fragment> _fragments = new ArrayList<>();

    public static FragManager getInstance() { return _instance; }

    public boolean contains (Fragment frag) {
        return _fragments.contains(frag);
    }

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